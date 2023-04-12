`timescale 1ns / 1ps
//`include "test_inputs.vh"
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 04/05/2023 03:57:04 PM
// Design Name: 
// Module Name: cache_tb
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module cache_tb();
    reg clk, reset;
    reg write_policy, replace_policy;
    reg[1:0] inclusion_policy;
    reg[47:0] cache_addr;
    reg[7:0] cache_op;
    reg cache_lvl;
    wire[11:0] L1_reads, L1_writes, L1_misses, L1_hits;
    wire[11:0] L2_reads, L2_writes, L2_misses, L2_hits;
    wire[31:0] curr_tag;
    reg[11:0] curr_set;
    parameter SIZE = 100;
    reg [47:0] test_addrs[0:SIZE-1] = 
    {   48'h7fff493822b8,
        48'h0000006324d8,
        48'h7fff493822b0,
        48'h7fff493822a8,
        48'h7fff493822a0,
        48'h7fff49382298,
        48'h7fff49382290,
        48'h7fff49382288,
        48'h7fff49382260,
        48'h7fff49382248,
        48'h7f3035f6f3b0,
        48'h7fff49382240,
        48'h7fff49382238,
        48'h000000634600,
        48'h7fff49382270,
        48'h0fff49382278,
        48'h7f3035f6a7c0,
        48'h0000006346e0,
        48'h7f3035f6a7c0,
        48'h000000634628,
        48'h7fff49382270,
        48'h7fff49382238,
        48'h7fff49382240,
        48'h7fff49382248,
        48'h7fff49382270,
        48'h000000634600,
        48'h7f3035f6a7c0,
        48'h000000634618,
        48'h7fff49382268,
        48'h0000006346f0,
        48'h7fff49382260,
        48'h7f3035f820c0,
        48'h7f3035f6a6b0,
        48'h7fff49382248,
        48'h7f3035f82100,
        48'h7f3035f6f160,
        48'h7fff49382240,
        48'h7fff49382238,
        48'h7fff49382230,
        48'h7fff49382228,
        48'h7f3035540400,
        48'h7f3035540488,
        48'h7f30365f1750,
        48'h7f3035541a18,
        48'h7f3035544ff4,
        48'h7f3035541a10,
        48'h7f3035541a12,
        48'h7f3035540488,
        48'h7f3035540487,
        48'h7f3035541a18,
        48'h7f3035541a14,
        48'h7f3035541a13,
        48'h7f30355404c0,
        48'h7f30355404d8,
        48'h7f303553e6b8,
        48'h7fff49382218,
        48'h7fff49382210,
        48'h7fff49382208,
        48'h7fff49382200,
        48'h7fff493821f8,
        48'h7fff493821f0,
        48'h7fff493821e8,
        48'h7f3035540400,
        48'h7f3035540430,
        48'h7f3035540428,
        48'h7fff493821d8,
        48'h00000042a5d6,
        48'h7f2fd059f000,
        48'h00000042a5d7,
        48'h7f2fd059f001,
        48'h00000042a5d9,
        48'h7f2fd059f003,
        48'h00000042a5dd,
        48'h00000042a5e5,
        48'h7f2fd059f007,
        48'h7f2fd059f00f,
        48'h7fff493821d8,
        48'h7f3035540428,
        48'h7fff493821e8,
        48'h7fff493821f0,
        48'h7fff493821f8,
        48'h7fff49382200,
        48'h7fff49382208,
        48'h7fff49382210,
        48'h7fff49382218,
        48'h7f3035540400,
        48'h7f3035540488,
        48'h7f3035541a14,
        48'h7f3035541a13,
        48'h7f3035541a18,
        48'h7f3035544ff4,
        48'h7f3035541a10,
        48'h7f3035541a12,
        48'h7fff49382228,
        48'h7fff49382230,
        48'h7fff49382238,
        48'h7fff49382240,
        48'h7fff49382248,
        48'h000000634600,
        48'h7f3035f6a7c0
    };

    reg [7:0] test_ops[0:SIZE-1] =
    {   8'h57,
        8'h52,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h52,
        8'h52,
        8'h57,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h52,
        8'h52,
        8'h57,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h52,
        8'h52,
        8'h57,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h52,
        8'h52,
        8'h57,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h52,
        8'h52,
        8'h57,
        8'h57,
        8'h57,
        8'h52,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h57,
        8'h52,
        8'h52
    };
    
    // TODO: Add test which level will be accessed
    //reg test_cache_lvl[0:SIZE-1]={
    //};
    
    real L1_miss_rate, L2_miss_rate;
    real L1misses, L1reads, L1hits, L1writes;
    real L2misses, L2reads, L2hits, L2writes;
    cache_top UUT(
        .clk(clk), 
        .reset(reset), 
        .write_policy(write_policy), 
        .replace_policy(replace_policy),
        .inclusion_policy(inclusion_policy),
        .cache_addr(cache_addr),
        .L1_reads(L1_reads),
        .L1_writes(L1_writes),
        .L1_misses(L1_misses),
        .L1_hits(L1_hits),
        .L2_reads(L2_reads),
        .L2_writes(L2_writes),
        .L2_misses(L2_misses),
        .L2_hits(L2_hits),
        .curr_tag(curr_tag),
        .cache_op(cache_op),
        .curr_set(curr_set),
        .cache_lvl(cache_lvl)
        );
    integer i;
    
    /*                                   SIMULATION INPUTS
    ************************************************************************************************
        replace_policy: 0 -> FIFO | 1 -> LRU
        
        write_policy: 0 -> write-through | 1 -> write-back
        
        inclusion_policy: 0 -> inclusive | 1 -> exclusive | 2 -> non-inclusive
        
        cache_lvl: 0 -> L2 | 1 -> L1
    ************************************************************************************************   
    */
    initial begin
        replace_policy = 0;
        write_policy = 0;
        cache_lvl = 1;
        inclusion_policy = 0;
        clk = 1;
        reset = 1;
        #10
        reset = 0;
                //send an addr and op from array to top module
        for(i = 0; i < SIZE; i=i+1)begin
            cache_addr = test_addrs[i];
            cache_op = test_ops[i];
            #10;
        end
        
        // L1 stats
        L1misses = L1_misses;
        L1reads = L1_reads;
        L1hits = L1_hits;
        L1writes = L1_writes;
        L1_miss_rate = L1misses / (L1hits+L1misses);
        
        //L2 stats
        L2misses = L2_misses;
        L2reads = L2_reads;
        L2hits = L2_hits;
        L2writes = L2_writes;
        L2_miss_rate = L2misses / (L2hits+L2misses);
    end
    
    always #1 clk = ~clk;

endmodule
