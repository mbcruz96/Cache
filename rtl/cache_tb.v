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
    wire[11:0] cache_miss_rate, num_reads, num_writes, num_misses, num_hits;
    wire[31:0] curr_tag;

     cache_top UUT(
        .clk(clk), 
        .reset(reset), 
        .write_policy(write_policy), 
        .replace_policy(replace_policy),
        .inclusion_policy(inclusion_policy),
        .cache_addr(cache_addr),
        .cache_miss_rate(cache_miss_rate),
        .num_reads(num_reads),
        .num_writes(num_writes),
        .num_misses(num_misses),
        .num_hits(num_hits),
        .curr_tag(curr_tag)
        );
        
    initial begin
        replace_policy = 0;
        clk = 1;
        reset = 1;
        #10
        reset = 0;
        cache_addr = 48'h7fff493822b0;
        #10
        cache_addr = 48'h7fff493822a;
        #10
        cache_addr = 48'h7f3035f6a7c0;
        #10
        cache_addr = 48'b0;
        
    end
    
    always #1 clk = ~clk;

endmodule
