"# Cache" 

once in the sim file to use the Make file run
...$ make

then run the main sim with
...$ ./cacheSim <BLOCKSIZE> <L1_SIZE> <L1_ASSOC> <L2_SIZE> <L2_ASSOC> <REPLACEMENT_POLICY> <INCLUSION_PROPERTY> <trace_file>
i.e.
...$ ./cacheSim 16 1024 1 8192 4 LRU inclusive ./traces/compress_trace.txt

to clean up the .o files plus the sim_cache binary file run
...$ make clean

for more look in the Makefile within the sim dir
