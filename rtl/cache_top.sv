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
  input[47:0] cache_addr,
  input[7:0] cache_op,
  output reg[11:0] cache_miss_rate,
  output reg[11:0] num_reads, num_misses, num_hits,
  output reg[11:0] num_writes,
  output reg[31:0] curr_tag
);

  // write_policy: 0 -> write through | 1 -> write back                         IN-PROGRESS
  // replace_policy: 0 -> FIFO | 1 -> LRU                                       DONE
  // inclusion_policy: 0 -> inclusive | 1 -> exclusive | 2 -> non-inclusive     IN-PROGRESS
  // cache_op: W or R                                                           IN-PROGRESS
  
  // Cache properties
  parameter BLOCKSIZE = 64;
  parameter CACHESIZE = 32768;
  parameter ASSOC = 8;
  parameter NUMSETS = CACHESIZE/(BLOCKSIZE * ASSOC);
  reg[31:0] index;         
  reg[31:0] tag;
  reg[31:0] cache [0:NUMSETS][0:ASSOC];
  
  
  // FSM Regs and Parameters
  parameter IDLE = 0, READ = 1, SEARCH = 2, SHIFTFULL = 3, SHIFTEMPTY = 4, LRUHIT = 5, DONE = 6;
  reg[3:0] state, next_state;
  reg[47:0] prev_addr;
  
  // Counter variables
  reg      found;
  integer i, j, lru_index;
  
  // Replacement policy FSM | Combinational logic
  always@(*)begin
   
    case(state)
        
        // Idle state for cache, keep cache state value the same
        IDLE:begin
            next_state <= (cache_addr != prev_addr) ? READ:IDLE;
        end
        
        // Read in address to find tag, index, and block off-set
        READ:begin
            tag <= cache_addr / BLOCKSIZE;               // Current Tag address derived from the input cache address
            index <= (cache_addr / BLOCKSIZE) % NUMSETS; // Current Set that the tag will go into
            curr_tag <= tag;
            next_state <= SEARCH;
        end
        
        // Search for tag state
        SEARCH:begin 
            
            // FIFO
            if(replace_policy == 0)begin
                next_state <= (cache[index][ASSOC-1] != 0) ? SHIFTEMPTY:SHIFTFULL;
            end

            // LRU | If cache hit, go to LRUHIT logic, if cache miss  proceed with FIFO-like shifitng
            else
                next_state <= (found) ? LRUHIT:(cache[index][ASSOC-1] != 0) ? SHIFTEMPTY:SHIFTFULL;
        end
        
        // Shift if current cache is Full FIFO or Full LRU Miss
        SHIFTFULL: next_state <= DONE;

        // Shfit if current cache is not Full in FIFO or LRU
        SHIFTEMPTY: next_state <= DONE;
        
        // Shift logic in case of LRU hit
        LRUHIT: next_state <= DONE; 
        
        // Finish Shift operations, calculate miss rate, and jump into ideal
        DONE: next_state <= IDLE;
        
    endcase
  end
  
  // Sequential Logic for setting up FSM and initializing values
  always@(posedge clk)begin

    // Initialize values for testing
    if(reset)begin
        state <= IDLE;
        num_misses <= 8'b0;
        num_hits <= 8'b0;
        num_reads <= 8'b0;
        num_writes <= 8'b0;
        cache_miss_rate <= 12'b0;
        found <= 1'b0;
        prev_addr <= 48'b0;

        for(i = 0; i < NUMSETS; i = i + 1)begin
            for(j = 0; j < ASSOC; j = j + 1)begin
                cache[i][j] = 32'b0;
            end
        end
    end
    
    // Flexible Cache Logic
    else begin
        state <= next_state;
        
        // If an address has been read, increment reads, get tag & index for FSM
        if(next_state == READ)begin
            num_reads = num_reads + 1;
        end
        
        // Search for the tag within the current replacement policy, write policy, and inclusion policy
        else if(next_state == SEARCH)begin

                // FIFO 
                if(replace_policy == 0)begin
                
                // If tag is in cache, mark as found
                for(i = 0; i < ASSOC; i = i + 1)begin
                        
                    if(tag == cache[index][i])begin
                        found <= 1'b1;
                    end
                end

                // If found, increment hits, and reset found flag
                if(found)begin
                    num_hits = num_hits + 1;
                    found <= 1'b0;
                end

                // If tag is not found, increment misses
                else begin
                    num_misses <= num_misses + 1;
                end
            end
            
            // LRU
            else begin
                
                // If tag found in cache, mark as found and mark LRU tag index
                for(i = 0; i < ASSOC; i = i + 1)begin        
                if(tag == cache[index][ASSOC])begin
                        found <= 1'b1;
                        lru_index <= i;
                    end
                end

                // If found, increment hits and clear found flag
                if(found)begin
                    num_hits = num_hits + 1;
                    found <= 1'b0;
                end

                // If not found, increment misses
                else begin
                    num_misses = num_misses + 1;
                end
            end
                       
        end
        
        // Shift logic if the cache for LRU or FIFO is full
        else if(next_state == SHIFTFULL)begin
        
                // Pop out last address out of cache before shifting
                cache[index][ASSOC-1] <= 32'b0;
                    
                // Shifts through the current set with the size of the cache line to shift in FIFO order
                for(i = ASSOC; i > 0; i = i - 1)begin
                    cache[index][i] <= cache[index][i-1];
                end
                    
                // Insert new address at beginning of cache line
                cache[index][0] <= tag;
        
        end
        
        // Shift logic if the cache for LRU or FIFO isn't full
        else if(next_state == SHIFTEMPTY)begin

                // Shifts through the current set with the size of the cache line to shift in FIFO order
                for(i = ASSOC; i > 0; i = i - 1)begin
                    cache[index][i] <= cache[index][i-1];
                end
                    
                // Insert new address at beginning of cache line
                cache[index][0] <= tag;
        end

        // Shfit logic for LRU hit
        else if(next_state == LRUHIT)begin

                // Pop out LRU hit tag out of cache before shifting
                cache[index][lru_index] <= 32'b0;
                    
                // Shifts through the current set with the size of the cache line to shift in LRU order
                for(i = lru_index; i > 0; i = i - 1)begin
                    cache[index][i] <= cache[index][i-1];
                end
                    
                // Insert new address at beginning of cache line
                cache[index][0] <= tag;
        end
        
        // Finish LRU or FIFO address insertion and calculate cache miss rate
        else if(next_state == DONE)begin
            prev_addr <= cache_addr;
            cache_miss_rate <= num_misses / (num_reads);
        end
    end
  end   

endmodule