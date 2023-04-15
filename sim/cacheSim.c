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
int checkTagLRU(int operation, Block blockAddress);
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
char *INCLUSION_PROPERTY = NULL;

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

    char *operation = malloc(3 * sizeof(char));
    int opIntRep = 0;
    unsigned long long int address = 0;
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
    // read file for debugging
    while (!feof(INPUT_FILE)) {
        fscanf(INPUT_FILE, " %s %llx", operation, &address);
        // printf("%s %llx", operation, address);

        if(operation != NULL && strcmp(operation, "r")){
            opIntRep = 0;
        }
        else if(operation != NULL && strcmp(operation, "w")){
            opIntRep = 1;
        }
        else if(operation != NULL && strcmp(operation, "wb")){
            opIntRep = 2;
        }
        else if(operation != NULL && strcmp(operation, "wt")){
            opIntRep = 3;
        }

        Block blockAddress = createMemoryAddress(opIntRep, address, numCacheSets);
        int found = checkTagLRU(opIntRep, blockAddress);
    }
    
    printCache();

    for (int i = 0; i < 2; i++){
        for (int j = 0; j < numCacheSets[i]; j++){
            //free each node from all sets
            Node *current = MAIN_CACHE[i]->sets[j].head;
            while (current != NULL){
                Node *temp = current;
                current = current->next;
                free(temp);
            }

            free(MAIN_CACHE[i]->sets);
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

void moveToFront(Node *temp, Set set){

    // if list is empty
    if(set.head == NULL){
        // printf("inserted %x into index %i\n", temp->data.tag, temp->data.index);
        set.head = temp;
        set.tail = temp;   
        return;
    }
    Node *ptr = temp;
    //if the entry is new
    if(ptr->next == NULL && ptr->previous == NULL){
        if(set.head != NULL){
            set.head->previous = ptr;
        }
        ptr->next = set.head;
        set.head = ptr;
        // printf("inserted %x into index %i\n", temp->data.tag, temp->data.index);

        return;
    }

    //cut out of list
    ptr->previous->next = ptr->next;
    if(ptr->next != NULL){
        ptr->next->previous = ptr->previous;
    }
    // move to head
    ptr->next = set.head;
    ptr->previous = NULL;
    set.head->previous = ptr;
    set.head = ptr;
    // printf("inserted %x into index %i\n", temp->data.tag, temp->data.index);

    return;
}

int checkTagLRU(int operation, Block blockAddress){
    
    // check eaach cache level
    for (int currentLevel = 0; currentLevel < 2; currentLevel++){
        //check each set

        for (int currentSet = 0; currentSet < MAIN_CACHE[currentLevel]->numSets; currentSet++){
            Node *ptr = MAIN_CACHE[currentLevel]->sets[currentSet].head;

            // checking each node in set
            while(ptr != NULL){
                if(ptr->data.tag == blockAddress.tag){
                    //if writethrough or writeback
                    if(operation != 0){
                        ptr->data.dirtyBit = 1;
                    }
                    //if not head
                    if(ptr->previous != NULL){
                        //move to head
                        moveToFront(ptr, MAIN_CACHE[currentLevel]->sets[currentSet]);
                    }
                    return 1;
                }
                ptr = ptr->next;
                
            }
        }
    }

    //insert into index needed
    Node *newNode = (Node*)malloc(sizeof(Node));
    newNode->data = blockAddress;
    newNode->next = NULL;
    newNode->previous = NULL;
    // move to front
    moveToFront(newNode, MAIN_CACHE[0]->sets[blockAddress.index]);

    //increase size
    MAIN_CACHE[0]->sets[blockAddress.index].size += 1;
    //print current set
    Node *ptr = MAIN_CACHE[0]->sets[blockAddress.index].head;
    while(ptr != NULL){
        printf("tag: %x, index: %d\n", ptr->data.tag, ptr->data.index);
        ptr = ptr->next;
    }
    // printf("\n");
    

    // evict LRU node
    if(MAIN_CACHE[0]->sets[blockAddress.index].head != NULL){
        if(MAIN_CACHE[0]->sets[blockAddress.index].size > MAIN_CACHE[0]->sets[blockAddress.index].capacity){
            printf("in if tag\n");

            Node *deleteNode = MAIN_CACHE[0]->sets[blockAddress.index].tail;
            // printf("1\n");
            deleteNode->previous->next = NULL;
            // printf("2\n");
            MAIN_CACHE[0]->sets[blockAddress.index].tail = deleteNode->previous;
            // printf("3\n");
            free(deleteNode);
            // printf("4\n");

            MAIN_CACHE[0]->sets[blockAddress.index].size -= 1;
            // printf("5\n");
        }   
    }
    // printf("end of tag\n");

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
    // initialize 
    Block block = {0, 0, 0, 0, 0};

    // if not read
    if(operation != 0){
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

void printCache() {
    // print the main cache to check if it works
    int num_of_cache_levels = 2;
    // Node *ptr;
    // for (int i = 0; i < num_of_cache_levels; i++)
    // {
    //     printf("===== L%d contents =====\n", i + 1);
    //     for(int j = 0; j < MAIN_CACHE[i]->numSets; j++){
    //         printf("Set %d: ", j);
    //         while(ptr->next != NULL){
    //             printf("%x %c", ptr->data.tag, ptr->data.dirtyBit == 1 ? 'D' : ' ');
    //         }
    //     }
    // }
    // print out blocks in each set and cache
    for (int i = 0; i < num_of_cache_levels; i++)
    {
        printf("===== L%d contents =====\n", (i + 1));
        printf("wow %i\n", i);
        if(MAIN_CACHE[i]->numSets){
            printf("%i \n", MAIN_CACHE[i]->numSets);
        }
                printf("wow %i\n", i);

        printf("number of sets here %i: ", MAIN_CACHE[i]->numSets);

        for (int j = 0; j < MAIN_CACHE[i]->numSets; j++)
        {
            printf("Set %d: ", j);
            Node *ptr = MAIN_CACHE[i]->sets[j].head;
            printf("data here is %x\n", ptr->data.tag);


            while (ptr != NULL)
            {
                printf("%x %c ", ptr->data.tag, ptr->data.dirtyBit == 1 ? 'D' : ' ');
                ptr = ptr->next;
            }
            printf("\n");
        }
    }
}