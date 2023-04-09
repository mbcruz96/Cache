#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include "ourHeaders.h"

// prototype for is power of 2 function
bool isPowerOfTwo(int x);
void getInputs();
void getSets();
void printInputs();

// global variables for cache size, associativity, block size, and replacement policy
int CAHCE_SIZE = -1;
int ASSOCIATIVITY = -1;
int BLOCK_SIZE = -1;
int REPLACEMENT_POLICY = -1;
int INCLUSION_POLICY = -1;
int NUM_SETS = -1;

// grab arguments from command line
int main(int argc, char *argv[])
{
    // check for correct number of arguments
    // this is left as a command line argument for 
    // ease of use
    if (argc != 2) {
        printf("Usage: ./cacheSim <tracefile path>\n");
        return 1;
    }
    // open file
    FILE *file = fopen(argv[1], "r");
    // check if file opened
    if (file == NULL) {
        printf("Could not open file.\n");
        return 1;
    }
    
    // get inputs from user
    getInputs();
    getSets();

    // read file
    // char line[256];
    // while (fgets(line, sizeof(line), file)) {
    //     printf("%s", line);
    // }

    printInputs();

    // close file
    fclose(file);
    
    // call hello_world function from the header file
    hello_world();
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

// function to get the inputs from the user
void getInputs() {
    int inputSize = 256;
    char input [inputSize];

    // CACHE SIZE
    while (true) {
        printf("Cache Size?: \n");
        if (fgets (input, inputSize, stdin) == NULL) {
            printf(">>> Failed to read Cache Size\n");
        }
        // convert input string to int
        CAHCE_SIZE = atoi (input);
        // safeguard for bad input
        if (CAHCE_SIZE <= 0) {
            printf(">>> Must be a positive integer greater than 1\n");
        }
        else if (!isPowerOfTwo(CAHCE_SIZE)) {
            printf(">>> Must be a power of 2\n");
        }
        else {
            break;
        }
    }
    // Associativity
    while (true) {
        printf("Associativity: \n");
        if (fgets (input, inputSize, stdin) == NULL) {
            printf(">>> Failed to read Associativity\n");
        }
        // convert input string to int
        ASSOCIATIVITY = atoi (input);
        // safeguard for bad input
        if (ASSOCIATIVITY <= 0) {
            printf(">>> Must be a positive integer greater than 1\n");
        }
        else if (!isPowerOfTwo(ASSOCIATIVITY)) {
            printf(">>> Must be a power of 2\n");
        }
        else {
            break;
        }
    }
    // Block Size
    while (true) {
        printf("Block Size: \n");
        if (fgets (input, inputSize, stdin) == NULL) {
            printf(">>> Failed to read Block Size\n");
        }
        // convert input string to int
        BLOCK_SIZE = atoi (input);
        // safeguard for bad input
        if (BLOCK_SIZE <= 0) {
            printf(">>> Must be a positive integer greater than 1\n");
        }
        else if (!isPowerOfTwo(BLOCK_SIZE)) {
            printf(">>> Must be a power of 2\n");
        }
        else {
            break;
        }
    }
    // Replacement Policy
    while (true) {
        printf("Replacement policy?: \n    LRU = 1\n    FIFO = 2\n    Optimal = 3\n");
        if (fgets (input, inputSize, stdin) == NULL) {
            printf(">>> Failed to read Replacement Policy\n");
        }
        // convert input string to int
        REPLACEMENT_POLICY = atoi (input);
        // safeguard for bad input
        if (REPLACEMENT_POLICY == 1 || REPLACEMENT_POLICY == 2 || REPLACEMENT_POLICY == 3) {
            break;
        }
        else {
            printf(">>> Invalid selection, must be 1, 2, or 3\n");
        }
    }
    // Inclusion policy
    while (true) {
        printf("Inclusion policy?: \n    Inclusive = 1\n    Non-inclusive = 2\n");
        if (fgets (input, inputSize, stdin) == NULL) {
            printf(">>> Failed to read Inclusion Policy\n");
        }
        // convert input string to int
        INCLUSION_POLICY = atoi (input);
        // safeguard for bad input
        if (INCLUSION_POLICY == 1 || INCLUSION_POLICY == 2) {
            break;
        }
        else {
            printf(">>> Invalid selection, must be 1 or 2\n");
        }
    }
    
}

// function to get the number of sets
void getSets() {
    while (true) {
        NUM_SETS = CAHCE_SIZE / (ASSOCIATIVITY * BLOCK_SIZE);
        if (NUM_SETS > 0) {
            break;
        }
        else {
            printf(">>> Invalid input for Set calculations with: \nCache Size and/or\nBlock Size and/or\nAssociativity\n");
            printf(">>> Cache Size must be greater than or equal \n    to the product of Associativity and Block Size\n");
            printf(">>> Please re-enter the following values:\n");

            getInputs();
        }
    }
}

void printInputs() {
    printf("Cache Size: %d\n", CAHCE_SIZE);
    printf("Associativity: %d\n", ASSOCIATIVITY);
    printf("Block Size: %d\n", BLOCK_SIZE);

    if (REPLACEMENT_POLICY == 1) {
        printf("Replacement policy: LRU\n");
    }
    else if (REPLACEMENT_POLICY == 2) {
        printf("Replacement policy: FIFO\n");
    }
    else if (REPLACEMENT_POLICY == 3) {
        printf("Replacement policy: Optimal\n");
    }

    if (INCLUSION_POLICY == 1) {
        printf("Inclusion policy: Inclusive\n");
    }
    else if (INCLUSION_POLICY == 2) {
        printf("Inclusion policy: Non-inclusive\n");
    }
    printf("Number of Sets: %d\n", NUM_SETS);
}