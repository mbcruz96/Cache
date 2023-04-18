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
} Block;

typedef struct Node{
    struct Block data;
    struct Node *next;
    struct Node *previous;
}Node;

typedef struct Set
{
    struct Node *head;
    struct Node *tail;
    int size;
    int capacity;
    int lru;
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
void LRUPolicy(Node *temp, int currentLevel, int currentSet);
Block *createMemoryAddress(int operation, unsigned long long int address, int* numCacheSets);
CacheLevel *createCacheLevel(int level, int cacheSize, int associativity, int numSets);
void printSet(int setIndex, int cacheLevel);
void checkTag(int operation, Block *blockAddress);
void printInfo();
void printCache();

// global variables
int BLOCK_SIZE = -1;

int L1_CACHE_SIZE = -1;
int L1_ASSOCIATIVITY = -1;
int L2_CACHE_SIZE = -1;
int L2_ASSOCIATIVITY = -1;

int REPLACEMENT_POLICY = -1;
char *INCLUSION_PROPERTY = NULL;

int TOTAL_LEVELS = 0;

char *TRACE_FILE_NAME = NULL;
FILE *INPUT_FILE = NULL;

CacheLevel **MAIN_CACHE = NULL;

int reads[2];
int readMisses[2];
int writes[2];
int writeMisses[2];
int writeBacks[2];
int writeThroughs[2];
int cacheToCacheTransfers[2];
//evictions
int memoryTraffic;

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

    // hold read or write operation
    char operation;
    // hold operation as int, 0 for read, 1 for write
    int opIntRep = 0;
    unsigned long long int address = 0;
    // hold number of cache sets for L1 and L2 just at index 0 and 1
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

    if(numCacheSets[1] > 0 && numCacheSets[0] > 0){
        TOTAL_LEVELS = 2;
    }else{
        TOTAL_LEVELS = 1;
    }

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
    // read file for debugging
    while (!feof(INPUT_FILE)) {
        fscanf(INPUT_FILE, " %c %llx", &operation, &address);
        //printf("read: %s %llx\n", operation, address);

        // if(strcmp(operation, "r")){
        //     printf("its read\n");
        //     opIntRep = 0;
        // }
        // else if(strcmp(operation, "w")){
        //     opIntRep = 1;
        // }
        // else if(strcmp(operation, "wb")){
        //     opIntRep = 2;
        // }
        // else if(strcmp(operation, "wt")){
        //     opIntRep = 3;
        // }
        if(operation =='r'){
            opIntRep = 0;
        }
        else if(operation =='w'){
            opIntRep = 1;
        }
        Block *blockAddress = createMemoryAddress(opIntRep, address, numCacheSets);
        checkTag(opIntRep, blockAddress);
        // free blockAddress after use, relevent data has been copied to cache
        free(blockAddress);

    }
    
    printCache();
    
    // free all malloced memory
    for (int i = 0; i < 2; i++){
        //printf("fgdfgsfgsdfg %i\n", numCacheSets[i]);
        for (int j = 0; j < numCacheSets[i]; j++){
            //printf("freeing nodes now\n");
            Node *current = MAIN_CACHE[i]->sets[j].head;
            // as long as the set at the index was instantiated
            while (current != NULL){
                Node *freeNode = current;
                current = current->next;
                free(freeNode);
            }
            free(current);
        }
        free(MAIN_CACHE[i]->sets);        
        free(MAIN_CACHE[i]);
    }
    free(MAIN_CACHE);
    fclose(INPUT_FILE);
    return 0;
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

    // convert input string to int
    if (strcmp(input, "LRU"))
    {
        REPLACEMENT_POLICY = 1;
        return 0;
    }
    if (strcmp(input, "FIFO"))
    {
        REPLACEMENT_POLICY = 2;
        return 0;
    }
    if (strcmp(input, "OPTIMAL"))
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

    // convert input string to int
    INCLUSION_PROPERTY = input;
    if (strcmp(INCLUSION_PROPERTY, "inclusive") || strcmp(INCLUSION_PROPERTY, "non-inclusive"))
    {
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

void placeAtFront(Node *temp, int currentLevel, int currentSet){
    // if list is empty
    if(MAIN_CACHE[currentLevel]->sets[currentSet].head == NULL){
        MAIN_CACHE[currentLevel]->sets[currentSet].head = temp;
        MAIN_CACHE[currentLevel]->sets[currentSet].head->next = NULL;
        MAIN_CACHE[currentLevel]->sets[currentSet].head->previous = NULL;
        MAIN_CACHE[currentLevel]->sets[currentSet].tail = temp;   
        MAIN_CACHE[currentLevel]->sets[currentSet].tail->next = NULL;   
        MAIN_CACHE[currentLevel]->sets[currentSet].tail->next = NULL;   
        return ;
    }
    // if list is not empty
    else{
        temp->next = MAIN_CACHE[currentLevel]->sets[currentSet].head;
        MAIN_CACHE[currentLevel]->sets[currentSet].head->previous = temp;
        MAIN_CACHE[currentLevel]->sets[currentSet].head = temp;
        MAIN_CACHE[currentLevel]->sets[currentSet].head->previous = NULL;
        return ;
    }

}

void LRUPolicy(Node *temp, int currentLevel, int currentSet){

    Node *current = MAIN_CACHE[currentLevel]->sets[currentSet].head;
    while (current != NULL)
    {
        if(current->data.tag == temp->data.tag){
            //if the entry is already in the list
            if(current->previous == NULL){
                return;
            }
            //if the entry is at the end of the list
            if(current->next == NULL){
                MAIN_CACHE[currentLevel]->sets[currentSet].tail = current->previous;
                MAIN_CACHE[currentLevel]->sets[currentSet].tail->next = NULL;
                current->previous = NULL;
                current->next = MAIN_CACHE[currentLevel]->sets[currentSet].head;
                MAIN_CACHE[currentLevel]->sets[currentSet].head->previous = current;
                MAIN_CACHE[currentLevel]->sets[currentSet].head = current;
                return;
            }
            //if the entry is in the middle of the list
            if(current->previous != NULL && current->next != NULL){
                current->previous->next = current->next;
                current->next->previous = current->previous;
                current->previous = NULL;
                current->next = MAIN_CACHE[currentLevel]->sets[currentSet].head;
                MAIN_CACHE[currentLevel]->sets[currentSet].head->previous = current;
                MAIN_CACHE[currentLevel]->sets[currentSet].head = current;
                return;
            }
        }
        current = current->next;
    }    

    return;
}

void evictBlock(int currentLevel, int index){

    Node *deleteNode = MAIN_CACHE[currentLevel]->sets[index].tail;
    deleteNode->previous->next = NULL;
    MAIN_CACHE[currentLevel]->sets[index].tail = deleteNode->previous;
    MAIN_CACHE[currentLevel]->sets[index].size -= 1;
    // update access traffic to memory
    if (deleteNode->data.dirtyBit == 1) {
        memoryTraffic += 1;
    }
    free(deleteNode);
}

void checkTag(int operation, Block *blockAddress){
    int found = 0;
    
    // check each cache level
    for (int currentLevel = 0; currentLevel < TOTAL_LEVELS; currentLevel++){

        // ptr being the head of the set in the cache
        Node *ptr = MAIN_CACHE[currentLevel]->sets[blockAddress[currentLevel].index].head;
        while(ptr != NULL){
            //if found in cache
            
            if(ptr->data.tag == blockAddress[currentLevel].tag){
                found = 1;
                //update dirty bit
                // if write op
                if(operation == 1){
                    ptr->data.dirtyBit = 1;
                }
                //if read op and was already dirty
                // else if(ptr->data.dirtyBit == 1){
                //     ptr->data.dirtyBit = 1;
                // }
                // else{
                //     ptr->data.dirtyBit = 0;
                // }
                //if writethrough or writeback

                //if not head
                if(ptr->previous != NULL){
                    //move to head
                    LRUPolicy(ptr, currentLevel, blockAddress[currentLevel].index);
                }
            }
            ptr = ptr->next;
        }
        //if not in cache
        if (found == 0){
            // insert into index needed
            // update access traffic to memory
            memoryTraffic += 1;

            Node *newNode = (Node*)malloc(sizeof(Node));
            newNode->data = blockAddress[currentLevel];
            // newNode->data.dirtyBit = blockAddress[currentLevel].dirtyBit;
            
            newNode->next = NULL;
            newNode->previous = NULL;
            if(operation == 1){
                newNode->data.dirtyBit = 1;
            }
            // move to front
            placeAtFront(newNode, currentLevel, blockAddress[currentLevel].index);
            //increase size
            MAIN_CACHE[currentLevel]->sets[blockAddress[currentLevel].index].size += 1;  

            // if(MAIN_CACHE[currentLevel]->sets[blockAddress[currentLevel].index].head != NULL){
            //printCache();
            // if our set is to large we know we need to evict
            if(MAIN_CACHE[currentLevel]->sets[blockAddress[currentLevel].index].size > MAIN_CACHE[currentLevel]->sets[blockAddress[currentLevel].index].capacity){
                // evict LRU node
                evictBlock(currentLevel, blockAddress[currentLevel].index);
            }
            // }

            // update access traffic to memory
            // memoryTraffic += 1;
        }
    }
}

Block *createMemoryAddress(int operation, unsigned long long int address, int* numCacheSets){
    
    Block *block = (Block*)malloc(sizeof(Block)); 
    for (int i = 0; i < TOTAL_LEVELS; i++){
        int tagBits = log2(BLOCK_SIZE) + log2(MAIN_CACHE[0]->numSets);
        int blockOffset = log2(BLOCK_SIZE);
        int indexBits = log2(MAIN_CACHE[0]->numSets);
        //produce mask to extract the offset bits
        int offset = address & ((1 << blockOffset)-1);
        //remove offset bits value from address but keep length
        address = address ^ offset;
        // shift address by offset and & with mask to get index
        int index = (address >> (blockOffset)) & ((1 << indexBits)-1);

        //extract tag bits by shifting
        unsigned long long int tag = address >> tagBits;
        // initialize 
        // if not read
        block[i].index = index;
        block[i].validBit = 1;
        block[i].offset = offset;
        block[i].tag = tag;
        if (operation == 1){
            block[i].dirtyBit = 1;
        }else {
            block[i].dirtyBit = 0;
        }
    }
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
    
    for (int i = 0; i < numSets; i++){
        cache->sets[i].size = 0;
        cache->sets[i].capacity = associativity;
        cache->sets[i].head = NULL;
        cache->sets[i].tail = NULL;        
    }

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
    if (REPLACEMENT_POLICY == 1)
    {
        printf("REPLACEMENT POLICY:\tLRU\n");
    }
    else if (REPLACEMENT_POLICY == 2)
    {
        printf("REPLACEMENT POLICY:\tFIFO\n");
    }
    else if (REPLACEMENT_POLICY == 3)
    {
        printf("REPLACEMENT POLICY:\tOptimal\n");
    }

    // Inclusion Policy
    printf("INCLUSION PROPERTY:\t%s\n", INCLUSION_PROPERTY);
    
    // Trace File
    printf("trace_file:\t\t%s\n", TRACE_FILE_NAME);

    printf("----------------------------------------\n");

}

void printSet(int setNum, int level) {
    Node *current = MAIN_CACHE[level]->sets[setNum].head;
    while (current != NULL)
    {
        printf("Tag: %x, Index: %i ->", current->data.tag, current->data.index);
        current = current->next;
    }
    printf("\n");
}

void printCache() {
    // print the main cache to check if it works
    // print out blocks in each set and cache

    for (int i = 0; i < TOTAL_LEVELS; i++)
    {
        printf("===== L%d contents =====\n", (i + 1));
        for (int j = 0; j < MAIN_CACHE[i]->numSets; j++){
            if(MAIN_CACHE[i]->sets[j].head != NULL){
                printf("Set %4i: ", j);
                Node *ptr = MAIN_CACHE[i]->sets[j].head;
                while(ptr != NULL){
                    printf("%4x %2c ", ptr->data.tag, ptr->data.dirtyBit == 1 ? 'D' : ' ');
                    ptr = ptr->next;
                }
                printf("\n");
            }
        }        
    }
}