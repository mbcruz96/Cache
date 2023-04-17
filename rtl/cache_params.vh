`ifndef _cache_params_vh_
`define _cache_params_vh_
parameter BLOCKSIZE = 64;

//L1 Cache properties
parameter L1_CACHESIZE = 2048;
parameter L1_ASSOC = 4;
parameter L1_NUMSETS = L1_CACHESIZE/(BLOCKSIZE * L1_ASSOC);

//L2 Cache properties
parameter L2_CACHESIZE = 8192;
parameter L2_ASSOC = 8;
parameter L2_NUMSETS = L2_CACHESIZE/(BLOCKSIZE * L2_ASSOC);
`endif