`timescale 1ns / 1ps
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
    wire[11:0] num_reads, num_writes, num_misses, num_hits;
    wire[31:0] curr_tag;
    reg[47:0] test_addrs[0:19] = 
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
        48'h000000634628
    };
    real miss_rate;
    real misses, reads;
     cache_top UUT(
        .clk(clk), 
        .reset(reset), 
        .write_policy(write_policy), 
        .replace_policy(replace_policy),
        .inclusion_policy(inclusion_policy),
        .cache_addr(cache_addr),
        .num_reads(num_reads),
        .num_writes(num_writes),
        .num_misses(num_misses),
        .num_hits(num_hits),
        .curr_tag(curr_tag)
        );
    integer i;
    initial begin
        replace_policy = 0;
        clk = 1;
        reset = 1;
        #10
        reset = 0;
        for(i = 0; i < 20; i=i+1)begin
            cache_addr = test_addrs[i];
            #10;
        end
        misses = num_misses;
        reads = num_reads;
        miss_rate = misses / reads;
    end
    always #1 clk = ~clk;

endmodule
