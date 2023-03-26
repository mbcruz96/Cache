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
    // read file
    char line[256];
    while (fgets(line, sizeof(line), file))
    {
        printf("%s", line);
    }
    // close file
    fclose(file);
    
    // call hello_world function from the header file
    hello_world();
}