#include <stdio.h>
#include "ourHeaders.h"

// grab arguments from command line
int main(int argc, char *argv[])
{
    // check for correct number of arguments
    if (argc != 2)
    {
        printf("Usage: ./cacheSim <tracefile>\n");
        return 1;
    }
    // open file
    FILE *file = fopen(argv[1], "r");
    // check if file opened
    if (file == NULL)
    {
        printf("Could not open file.\n");
        return 1;
    }
    
    int cacheSize = 0;
    int assoc = 0;
    int blockSize = 0;
    int replacementPolicy = 0;

    // TODO: add in safeguards for bad input, 
    //  blocksizes that are not powers of 2,
    //  setsizes that are not powers of 2

    // Gets the input from the user
    printf("CACHE SIZE?: \n");
    scanf("%d", &cacheSize);

    printf("ASSOC?: \n");
    scanf("%d", &assoc);

    printf("BLOCKSIZE?: \n");
    scanf("%d", &blockSize);
    
    printf("Replacement policy?: \nLRU = 1\nFIFO = 2\nOptimal = 3\n");
    scanf("%d", &replacementPolicy);

    // read file
    char line[256];
    while (fgets(line, sizeof(line), file))
    {
        printf("%s", line);
    }

    // print out the input
    printf("SIZE: %d\n", cacheSize);
    printf("ASSOC: %d\n", assoc);
    printf("BLOCKSIZE: %d\n", blockSize);
    if (replacementPolicy == 1) {
        printf("Replacement policy: LRU\n");
    }
    else if (replacementPolicy == 2) {
        printf("Replacement policy: FIFO\n");
    }
    else if (replacementPolicy == 3) {
        printf("Replacement policy: Optimal\n");
    }

    // close file
    fclose(file);
    
    // call hello_world function from the header file
    hello_world();
}