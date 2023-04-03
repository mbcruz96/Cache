`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 03/16/2023 12:43:31 AM
// Design Name: 
// Module Name: cache_top
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


module cache_top(   
  input clk,
  input reset,
  input write_policy,
  input replace_policy,
  input[1:0] inclusion_policy,
  input[31:0] cache_addr,
  output reg[11:0] cache_miss_rate,
  output reg[11:0] num_reads,
  output reg[11:0] num_writes
);
  
  // Cache properties
  parameter BLOCKSIZE = 64;
  parameter CACHESIZE = 32;
  parameter ASSOC = 8;
  parameter  NUMSETS = CACHESIZE/(BLOCKSIZE * ASSOC);
  reg[31:0] index;         
  reg[31:0] tag;
  reg[31:0] cache [0:NUMSETS];

  
  // FSM Regs and Parameters
  parameter IDLE = 0, READ = 1, SEARCH = 2, SHIFT = 3, LRUHIT = 4, DONE = 5;
  reg [3:0] state, next_state;
  
  // Counter variables
  reg [7:0] num_misses, num_hits;
  integer i;
  // Replacement policy FSM | Combinational logic
  always@(*)begin
   
    case(state)
        
        // Idle state for cache, keep cache state value the same
        IDLE:begin
            if(cache_addr != 0)begin
                next_state <= READ;
            end
            
            else begin
                next_state <= IDLE;
            end
        end
        
        // Read in address to find tag, index, and block off-set
        READ:begin
            tag <= cache_addr / BLOCKSIZE;
            index <= cache_addr / (BLOCKSIZE*ASSOC);
            
        end
        
        // Search for tag state | Increment misses or hits
        SEARCH:begin
            
            // FIFO
            if(replace_policy == 0)begin
                
                // If The current FIFO 
                if(cache[NUMSETS] != 0)begin
                
                    for(i = NUMSETS; i > 0; i = i - 1)begin
                        cache[NUMSETS] <= cache[i-1];
                    end
                    
                    cache[0] <= tag;
                end
                
            end
            
            // LRU
            else if(replace_policy == 1)begin
                
                /*if(hit)begin
                    next_state <= LRUHIT;
                end */
                
            end
            
        end
        
        // Write & Shift state  | Increment Reaads or Writes
        SHIFT:begin
        end
        
        LRUHIT:begin
        end
        
        // Finish Shift operations, calculate miss rate, and jump into ideal
        DONE:begin
            cache_miss_rate <= num_misses / (num_reads);
            next_state <= state;
        end
        
    endcase
  end
  
  // Sequential Logic for setting up FSM and initializing values
  always@(posedge clk)begin
    if(reset)begin
        state <= IDLE;
        num_misses <= 8'b0;
        num_hits <= 8'b0;
        num_reads <= 8'b0;
        num_writes <= 8'b0;
    end
    
    else begin
        state <= next_state;
    end
  end
    
endmodule