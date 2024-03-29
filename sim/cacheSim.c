#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>
#include "ourHeaders.h"
#include <math.h>

typedef struct Block
{
    int validBit;
    int dirtyBit;
    int tag;
    int offset;
    int index;
    int LRU;
} Block;

typedef struct Node{
    struct Block *data;
    struct Node *next;
    struct Node *previous;
}Node;

typedef struct Set
{
    struct Node *head;
    struct Node *tail;
} Set;

typedef struct CacheLevel
{
    int level;
    int cacheSize;
    int associativity;
    int numSets;
    Set *sets;
} CacheLevel;

bool isPowerOfTwo(int x);

int checkBlock(char *input);
int checkCacheSize(char *input, int chacheLevel);
int checkCacheAssoc(char *input, int assoc);
int checkReplacementPolicy(char *input);
int checkInclusionProperty(char *input);
int checkTraceFile(char *input);
Block createMemoryAddress(int operation, unsigned long long int address, int* numCacheSets);
CacheLevel *createCacheLevel(int level, int cacheSize, int associativity, int numSets);
void printInfo();
void printCache();

// global variables
int BLOCK_SIZE = -1;

int L1_CACHE_SIZE = -1;
int L1_ASSOCIATIVITY = -1;
int L2_CACHE_SIZE = -1;
int L2_ASSOCIATIVITY = -1;

int REPLACEMENT_POLICY = -1;
int INCLUSION_PROPERTY = -1;

char *TRACE_FILE_NAME = NULL;
FILE *INPUT_FILE = NULL;

CacheLevel **MAIN_CACHE = NULL;



int main(int argc, char *argv[])
{
    // if number of command line args is not 8 then exit
    if (argc != 9)
    {
        printf("Usage: ./cacheSim <BLOCKSIZE> <L1_SIZE> <L1_ASSOC> <L2_SIZE> <L2_ASSOC> <REPLACEMENT_POLICY> <INCLUSION_PROPERTY> <trace_file>\n");
        return 1;
    }

    // ./cacheSim 16 1024 1 8192 4 LRU inclusive ./traces/compress_trace.txt

    if (checkBlock(argv[1]) != 0 || 
        checkCacheSize(argv[2], 1) != 0 || 
        checkCacheAssoc(argv[3], 1) != 0 || 
        checkCacheSize(argv[4], 2) != 0 || 
        checkCacheAssoc(argv[5], 2) != 0 || 
        checkReplacementPolicy(argv[6]) != 0 || 
        checkInclusionProperty(argv[7]) != 0 || 
        checkTraceFile(argv[8]) != 0) 
    {
        return 1;
    }

    char operation;
    unsigned long long int address;
    int numCacheSets[2];

    // avoid divide by zero error if associativity is 0
    numCacheSets[0] = L1_ASSOCIATIVITY <= 0 ? 0 : L1_CACHE_SIZE / (L1_ASSOCIATIVITY * BLOCK_SIZE);

    if (!isPowerOfTwo(numCacheSets[0]))
    {
        printf("L1 sets is not a power of 2, please re check values\n");
        return 1;
    }

    // avoid divide by zero error if associativity is 0
    numCacheSets[1] = L2_ASSOCIATIVITY <= 0 ? 0 : L2_CACHE_SIZE / (L2_ASSOCIATIVITY * BLOCK_SIZE);

    if (!isPowerOfTwo(numCacheSets[1]))
    {
        printf("L2 sets is not a power of 2, please re check values\n");
        return 1;
    }

    // create MAIN_CACHE
    int num_of_cache_levels = 2;
    MAIN_CACHE = malloc(num_of_cache_levels * sizeof(CacheLevel *));

    MAIN_CACHE[0] = createCacheLevel(1, L1_CACHE_SIZE, L1_ASSOCIATIVITY, numCacheSets[0]);
    MAIN_CACHE[1] = createCacheLevel(2, L2_CACHE_SIZE, L2_ASSOCIATIVITY, numCacheSets[1]);

    printInfo();
    printCache();
    // read file for debugging
    while (!feof(INPUT_FILE)) {
        fscanf(INPUT_FILE, " %c %llx", &operation, &address);
        Block blockAddress = createMemoryAddress(operation, address, numCacheSets);
        if(operation == 'r'){
            // printf("L1 read: %llx (tag: %llx, index: %d)\n", address, tag, index);
            // check the hash map for the tag and is valid
            // current version checks each cache for tag
            checkTag(blockAddress);
            // if it is in the hash map
                // if its in the map for L1 cache
                    // update the LRU
                        // move to the front of the linked list
                    // update the hit counter
                // else  check L2 cache
                    // update the miss counter
                    // update the LRU
                        // move to the front of the linked list
                    // update the hash map
                    // update the hit counter
                // else not found
                    // update the miss counter for all previous caches
                    // create a new block
                    // depending on the inclusion policy
                        // if inclusive
                            // update the LRU
                                // move to the front of the linked list
                            // update the hash map
                            // update the hash map of all caches
                            // if L1 is full evict the LRU block
                        // if non-inclusive
                            // update the LRU
                                // move to the front of the linked list
                            // update the hash map of only L1
                            // if L1 is full evict the LRU block into next level
                                // update the hash map of all caches

        } else if (operation == 'w'){
            // printf("write address:%llx (tag: %llx)\n", address, address/BLOCK_SIZE);
        } else {
            printf("error in file\n");
            return 1;
        }
    //     // if write 
    //         // do all the same shit
    //         // update the dirty bit to 1

    //     // when evecting 
    //         // check the dirty bit
    //             // this would determin if we put it back to mem or discar, may not apply to us

    //     // we will need 3 evict policies lru, fifo, and optimal
    }
    
    for (int i = 0; i < 2; i++){
        for (int j = 0; j < numCacheSets[i]; j++){
            free(MAIN_CACHE[i]->sets[j].blocks);
        }
        free(MAIN_CACHE[i]->sets);
        free(MAIN_CACHE[i]);
    }
    free(MAIN_CACHE);
    fclose(INPUT_FILE);
    return 0;
}

void executeReplacementPolicy(Set* currSet, Block* newBlock)
{
    if (REPLACEMENT_POLICY == 0 || REPLACEMENT_POLICY == 1)
    {
        updateFIFOAndLRU(currSet, newBlock);
    }
    else if (REPLACEMENT_POLICY == 2)
    {
        updateOptimal(currSet, newBlock);
    }
}

Block* updateFIFOAndLRU(Set* currSet, Block* newBlock)
{
    //create a new node to insert
    Node* newNode = malloc(sizeof(Node));

    //update the parameters of the new node
    newNode->data = newBlock;
    newNode->previous = NULL;
    newNode->next = currSet->head;

    //grab pointers to the hold head and tail so they arent lost
    Node* oldHead = currSet->head;
    Node* oldTail = currSet->tail;

    //update previous of old head and next for new tail
    oldHead->previous = newNode;
    oldTail->previous->next = NULL;

    //update head and tail of the set
    currSet->head = newNode;
    currSet->tail = oldTail->previous;

    //return the evicted block
    return(oldTail->data);
}

Block* updateOptimal(Set* currSet, Block* newBlock)
{

}



// function to check if a number is a power of 2
bool isPowerOfTwo(int x) {
    // 8 is 1000
    // 7 is 0111,
    // so 8 & 7 = 0

    // 7 is 0111
    // 6 is 0110
    // so 7 & 6 = 6 or essentiall not 0

    return !(x & (x - 1));
}

int checkBlock(char *input) {
    if (input == NULL)
    {
        printf(">>> Failed to read Block Size\n");
        return -1;
    }

    // convert input string to int
    BLOCK_SIZE = atoi(input);

    // safeguard for bad input
    if (BLOCK_SIZE <= 0)
    {
        printf(">>> Block Size must be a positive integer greater than 1\n");
        return -1;
    }
    
    if (!isPowerOfTwo(BLOCK_SIZE))
    {
        printf(">>> Block Size must be a power of 2\n");
        return -1;
    }
    return 0;
}

int checkCacheSize(char *input, int chacheLevel) {
    if (input == NULL)
    {
        printf(">>> Failed to read cache size\n");
        return -1;
    }

    // convert input string to int
    if (chacheLevel == 1) {
        L1_CACHE_SIZE = atoi(input);
        if (L1_CACHE_SIZE < 0)
        {
            printf(">>> L1 Cache size must be a non negative integer\n");
            return -1;
        }
    }
    else if (chacheLevel == 2) {
        L2_CACHE_SIZE = atoi(input);
        if (L1_CACHE_SIZE < 0)
        {
            printf(">>> L2 Cache size must be a non negative integer\n");
            return -1;
        }
    }
    return 0;
}

int checkCacheAssoc(char *input, int assoc) {
    if (input == NULL)
    {
        printf(">>> Failed to read assoc\n");
        return -1;
    }

    // convert input string to int
    if (assoc == 1) {
        L1_ASSOCIATIVITY = atoi(input);
        if (L1_ASSOCIATIVITY < 0)
        {
            printf(">>> L1 assoc must be a non negative integer\n");
            return -1;
        }
    }
    else if (assoc == 2) {
        L2_ASSOCIATIVITY = atoi(input);
        if (L1_ASSOCIATIVITY < 0)
        {
            printf(">>> L2 assoc must be a non negative integer\n");
            return -1;
        }
    }
    return 0;
}

int checkReplacementPolicy(char *input) {
    if (input == NULL)
    {
        printf(">>> Failed to read replacement policy\n");
        return -1;
    }

    if(strcmp(input, "LRU") == 0)
    {
        REPLACEMENT_POLICY = 0;
        return 0;
    }
    else if(strcmp(input, "FIFO") == 0)
    {
        REPLACEMENT_POLICY = 1;
        return 0;
    }
    else if(strcmp(input, "OPTIMAL") == 0)
    {
        REPLACEMENT_POLICY = 2;
        return 0;
    }

    printf(">>> Replacement policy must be LRU, FIFO, or OPTIMAL\n");
    return -1;
}

int checkInclusionProperty(char *input) {
    if (input == NULL)
    {
        printf(">>> Failed to read inclusion property\n");
        return -1;
    }

    if(strcmp(input, "inclusive") == 0)
    {
        INCLUSION_PROPERTY = 0;
        return 0;
    }
    else if(strcmp(input, "non-inclusive") == 0)
    {
        INCLUSION_PROPERTY = 1;
        return 0;
    }

    printf(">>> Inclusion property must be inclusive or non-inclusive\n");
    return -1;
}

int checkTraceFile(char *input) {
    if (input == NULL)
    {
        printf(">>> Failed to read trace file\n");
        return -1;
    }

    INPUT_FILE = fopen(input, "r");
    TRACE_FILE_NAME = input;

    // check if file opened
    if (INPUT_FILE == NULL)
    {
        printf("Could not open file.\n");
        return -1;
    }
    return 0;
}

Block createMemoryAddress(int operation, unsigned long long int address, int* numCacheSets){
        // check if read or write
    int tagBits = log2(BLOCK_SIZE) + log2(MAIN_CACHE[0]->numSets);
    int blockOffset = log2(BLOCK_SIZE);
    int indexBits = log2(MAIN_CACHE[0]->numSets);

    //produce mask to extract the offset bits
    int offset = address & ((1 << blockOffset)-1);
    //remove offset bits value from address but keep length
    address = address ^ offset;
    // printf("Read:  %llx\n", address);

    // shift address by offset and & with mask to get index
    int index = (address >> (blockOffset)) & ((1 << indexBits)-1);
    // printf("index: %x\n", index);

    //extract tag bits by shifting
    unsigned long long int tag = address >> tagBits;
    // printf("tag: %llx\n", tag);
    Block block;
    if(operation != 'r'){
        block.dirtyBit = 1;
    }else{
        block.dirtyBit = 0;
    }
    block.index = index;
    block.validBit = 1;
    block.offset = offset;
    block.tag = tag;
    
    return block;
}

// A utility function to create a cache level
CacheLevel *createCacheLevel(int level, int cacheSize, int associativity, int numSets) {
    CacheLevel *cache = (CacheLevel *)malloc(sizeof(CacheLevel));
    cache->level = level;
    cache->cacheSize = cacheSize;
    cache->associativity = associativity;
    cache->numSets = numSets;
    
    cache->sets = (Set *)malloc(sizeof(Set) * numSets);

    return cache;
}

void printInfo() {
    printf("===== Simulator configuration =====\n");
    // Block size
    printf("BLOCKSIZE:\t\t%d\n", BLOCK_SIZE);

    // Cache specific prints
    int num_of_cache_levels = 2;
    for (int i = 0; i < num_of_cache_levels; i++)
    {
        printf("L%d_SIZE:\t\t%d\n", MAIN_CACHE[i]->level, MAIN_CACHE[i]->cacheSize);
        printf("L%d_ASSOC:\t\t%d\n", MAIN_CACHE[i]->level, MAIN_CACHE[i]->associativity);
    }

    // Replacement Policy
    printf("REPLACEMENT POLICY:\t%s\n", REPLACEMENT_POLICY);

    // Inclusion Policy
    printf("INCLUSION PROPERTY:\t%s\n", INCLUSION_PROPERTY);
    
    // Trace File
    printf("trace_file:\t\t%s\n", TRACE_FILE_NAME);

    printf("----------------------------------------\n");
}

void printCache() {
    // print the main cache to check if it works
    int num_of_cache_levels = 2;
    for (int i = 0; i < num_of_cache_levels; i++)
    {
        printf("Level: %d, Cache Size: %d, Associativity: %d, Sets: %d\n", 
            MAIN_CACHE[i]->level, 
            MAIN_CACHE[i]->cacheSize, 
            MAIN_CACHE[i]->associativity, 
            MAIN_CACHE[i]->numSets);
    }
}