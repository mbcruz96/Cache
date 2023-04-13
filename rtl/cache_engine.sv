`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: UCF
// Engineers: John Gierlach & Harrison Lipton
// 
// Create Date: 03/16/2023 12:43:31 AM
// Design Name: 
// Module Name: cache_top
// Project Name: Multi-level Cache project
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

parameter BLOCKSIZE = 64;

//L1 Cache properties
parameter L1_CACHESIZE = 2048;
parameter L1_ASSOC = 4;
parameter L1_NUMSETS = L1_CACHESIZE/(BLOCKSIZE * L1_ASSOC);

//L2 Cache properties
parameter L2_CACHESIZE = 8192;
parameter L2_ASSOC = 8;
parameter L2_NUMSETS = L2_CACHESIZE/(BLOCKSIZE * L2_ASSOC);
module cache_engine(
      input clk,
      input reset,
      input write_policy,
      input replace_policy,
      input[1:0] inclusion_policy,
      input[47:0] cache_addr,
      input[7:0] cache_op,
      input cache_lvl,
      output reg[11:0] L1_reads, L1_misses, L1_hits, L1_writes,
      output reg[11:0] L2_reads, L2_misses, L2_hits, L2_writes,
      output reg[31:0] L1_index, L2_index,
      output reg[31:0] L1_tag, L2_tag,
      output reg[31:0] curr_tag,
      output reg[11:0] curr_set,
      output reg[31:0] L1_cache [0:L1_NUMSETS-1][0:L1_ASSOC-1],
      output reg[31:0] L2_cache [0:L2_NUMSETS-1][0:L2_ASSOC-1]
    );
    
    
  // FSM Regs and Parameters
  parameter IDLE = 0, READ = 1, SEARCH = 2, SHIFTFULL = 3, SHIFTEMPTY = 4, LRUHIT = 5, DONE = 6;
  reg[3:0] state, next_state;
  reg[47:0] prev_addr;
  
  // Counter variables
  reg      L1_found, L2_found;
  integer i, j, L1_lru_index, L2_lru_index;
  
  // Replacement policy FSM | Combinational logic
  always@(*)begin
   
    case(state)
        
        // Idle state for cache, keep cache state value the same
        IDLE: next_state <= (cache_addr != prev_addr) ? READ:IDLE;
        
        // Read in address to find tag, index, and block off-set
        READ: next_state <= SEARCH;
        
        // Search for tag state
        SEARCH:begin 
            
            // L1 Cache search
            if(cache_lvl)begin 
                
                // FIFO
                if(replace_policy == 0)begin
                    next_state <= (L1_cache[L1_index][L1_ASSOC-1] != 0) ? SHIFTEMPTY:SHIFTFULL;
                end

                // LRU | If cache hit, go to LRUHIT logic, if cache miss proceed with FIFO-like shifitng
                else
                    next_state <= (L1_found | L2_found) ? LRUHIT:(L1_cache[L1_index][L1_ASSOC-1] != 0) ? SHIFTEMPTY:SHIFTFULL;
            end

            // L2 Cache search
            else begin

                // FIFO
                if(replace_policy == 0)begin
                    next_state <= (L2_cache[L2_index][L2_ASSOC-1] != 0) ? SHIFTEMPTY:SHIFTFULL;
                end

                // LRU | If cache hit, go to LRUHIT logic, if cache miss proceed with FIFO-like shifitng
                else
                    next_state <= (L1_found | L2_found) ? LRUHIT:(L2_cache[L2_index][L2_ASSOC-1] != 0) ? SHIFTEMPTY:SHIFTFULL;
            end
            
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
        L1_misses <= 12'b0;
        L1_hits <= 12'b0;
        L1_reads <= 12'b0;
        L1_writes <= 12'b0;
        L2_misses <= 12'b0;
        L2_hits <= 12'b0;
        L2_reads <= 12'b0;
        L2_writes <= 12'b0;
        curr_set <= 12'b0;
        curr_tag <= 32'b0;
        L1_found <= 1'b0;
        L2_found <= 1'b0;
        prev_addr <= 48'b0;

        // Clear for L1
        for(i = 0; i < L1_NUMSETS; i = i + 1)begin
            for(j = 0; j < L1_ASSOC; j = j + 1)begin
                L1_cache[i][j] = 32'b0;
            end
        end

        // Clear for L2
        for(i = 0; i < L2_NUMSETS; i = i + 1)begin
            for(j = 0; j < L2_ASSOC; j = j + 1)begin
                L2_cache[i][j] = 32'b0;
            end
        end
    end
    
    // Flexible Cache Logic
    else begin
        state <= next_state;
        
        // If an address has been read, increment reads, get tag & index for FSM
        if(next_state == READ)begin
            
            // L1 Cache input
            if(cache_lvl)begin
                L1_tag <= cache_addr / BLOCKSIZE;                   // Current Tag address derived from the input cache address
                L1_index <= (cache_addr / BLOCKSIZE) % L1_NUMSETS;  // Current Set that the tag will go into
                curr_tag <= L1_tag;
                curr_set <= L1_index;
            end

            // L2 Cache input
            else begin
                L2_tag <= cache_addr / BLOCKSIZE;                   // Current Tag address derived from the input cache address
                L2_index <= (cache_addr / BLOCKSIZE) % L2_NUMSETS;  // Current Set that the tag will go into
                curr_tag <= L2_tag;
                curr_set <= L2_index;
            end
            
            // If write through & W operation increment writes
            if(write_policy == 0 && cache_op == 8'h57)begin
                
                // L1 Writes
                if(cache_lvl)
                    L1_writes <= L1_writes + 1;
                    
                // L2 Writes
                else
                    L2_writes <= L2_writes + 1;
            end
        end
        
        // Search for the tag within the current replacement policy, write policy, and inclusion policy
        else if(next_state == SEARCH)begin

            // FIFO 
            if(replace_policy == 0)begin
                
                // If tag is in L1 or L2 cache, mark as found in respective cache
                for(i = 0; i < L1_ASSOC; i = i + 1)begin
                    if(L1_tag == L1_cache[L1_index][i])begin
                        L1_found <= 1'b1;
                        break;
                    end
                end  
                
                
                for(i = 0; i < L2_ASSOC; i = i + 1)begin
                    if(L2_tag == L2_cache[L2_index][i])begin
                        L2_found <= 1'b1;
                        break;
                    end
                end  

                // If found in both caches, increment hits, and reset found flag
                if(L1_found && L2_found)begin
                    
                    L1_hits <= L1_hits + 1;                   
                    L2_hits <= L2_hits + 1;
                        
                end
                
                // If Hit on L2 & not on L1
                else if(!L1_found && L2_found)begin
                
                     // Miss in L1
                    L1_misses <= L1_misses + 1;
                        
                    if(write_policy == 1 && cache_op == 7'h57)begin
                        L1_writes <= L1_writes + 1;
                    end

                    // Hit in L2
                    L2_hits <= L2_hits + 1;
                        
                end
                
                // If Hit on L1 & not on L2
                else if(L1_found && !L2_found)begin
                
                    // Miss in L2
                    L2_misses <= L2_misses + 1;
                    if(write_policy == 1 && cache_op == 7'h57)
                        L2_writes <= L2_writes + 1;

                    // Hit in L1
                    L1_hits <= L1_hits + 1;
                    
                end
                
                // If tag is not found in either cache, increment misses and reads for each cache
                else begin
                    
                    //L1_misses = L1_misses + 1;
                    L2_misses = L2_misses + 1;
                    //L1_reads = L1_reads + 1;
                    L2_reads = L1_reads + 1;
                    
                    // If write back & W operation increment writes for L2 and L1 cache
                    if(write_policy == 1 && cache_op == 7'h57)begin                        
                        L1_writes <= L1_writes + 1;
                        L2_writes <= L2_writes + 1;
                    end
                    
                end
            end
            
            // LRU
            else begin
                
                // L1 Cache | If tag found in cache, mark as found and mark LRU tag index
                for(i = 0; i < L1_ASSOC; i = i + 1)begin        
                    if(curr_tag == L1_cache[L1_index][i])begin
                            L1_found <= 1'b1;
                            L1_hits <= L1_hits + 1;
                            L1_lru_index = i;
                            break;
                    end
                end               

                // L2 Cache | If tag found in cache, mark as found and mark LRU tag index
                for(i = 0; i < L2_ASSOC; i = i + 1)begin        
                    if(curr_tag == L2_cache[L2_index][i])begin
                            L2_found <= 1'b1;
                            L2_hits <= L2_hits + 1;
                            L2_lru_index = i;
                            break;
                    end
                end               
            end                   
        end
        
        // Shift logic if the cache for LRU or FIFO is full
        else if(next_state == SHIFTFULL)begin
                
                // I
                if(cache_lvl)begin
                
                    if(replace_policy == 1)begin
                        L1_misses <= L1_misses + 1;
                        L1_reads <= L1_reads + 1;
                        if(write_policy == 1 && cache_op == 7'h57)begin
                            L1_writes <= L1_writes + 1;
                        end     
                    end
                    // Pop out last address out of cache before shifting
                    L1_cache[L1_index][L1_ASSOC-1] <= 32'b0;
                        
                    // Shifts through the current set with the size of the cache line to shift in FIFO order
                    for(i = L1_ASSOC; i > 0; i = i - 1)begin
                        L1_cache[L1_index][i] <= L1_cache[L1_index][i-1];
                    end
                    
                    // If the current inclusion policy is Non-inclusive and L1 & L2 miss, then insert tag in both caches
                    if(inclusion_policy == 2 && !L2_found)begin
                    
                        // Pop out last address out of cache before shifting
                        L2_cache[L1_index][L2_ASSOC-1] <= 32'b0;
                                
                        // Shifts through the current set with the size of the cache line to shift in FIFO order
                        for(i = L2_ASSOC; i > 0; i = i - 1)begin
                            L2_cache[L1_index][i] <= L2_cache[L1_index][i-1];
                        end
                        
                        // Insert tag entry from L1 into L2 Set from the L1 Set  
                        L2_cache[L1_index][0] <= L1_tag;
                        
                    end
                         
                    // Insert new address at beginning of cache line
                    L1_cache[L1_index][0] <= L1_tag;
                end

                else begin
                    if(replace_policy == 1)begin
                        L2_misses <= L2_misses + 1;
                        L2_reads <= L2_reads + 1;
                        if(write_policy == 1 && cache_op == 7'h57)
                            L2_writes <= L2_writes + 1;
                    end
                    // Pop out last address out of cache before shifting
                    L2_cache[L2_index][L2_ASSOC-1] <= 32'b0;
                        
                    // Shifts through the current set with the size of the cache line to shift in FIFO order
                    for(i = L2_ASSOC; i > 0; i = i - 1)begin
                        L2_cache[L2_index][i] <= L2_cache[L2_index][i-1];
                    end
                    
                    // If the current inclusion policy is Non-inclusive and L1 & L2 miss, then insert tag in both caches
                    if(inclusion_policy == 2 && !L1_found)begin
                            
                            // If the current L2 set is outside the range of the L1 sets, then write tag to 1st L1 set
                            if(L2_index > L1_ASSOC)begin
                                // Pop out last address out of cache before shifting
                                L1_cache[0][L1_ASSOC-1] <= 32'b0;
                                    
                                // Shifts through the current set with the size of the cache line to shift in FIFO order
                                for(i = L1_ASSOC; i > 0; i = i - 1)begin
                                    L1_cache[0][i] <= L1_cache[0][i-1];
                                end
                                
                                // Insert tag entry from L2 into L1 Set from the 1st L1 Set 
                                L1_cache[0][0] <= L2_tag;
                            end
                            
                            // If the current L2 set is outside the range of the L1 sets, then write tag to 1st L1 set
                            else begin
                                // Pop out last address out of cache before shifting
                                L1_cache[L2_index][L1_ASSOC-1] <= 32'b0;
                                    
                                // Shifts through the current set with the size of the cache line to shift in FIFO order
                                for(i = L1_ASSOC; i > 0; i = i - 1)begin
                                    L1_cache[L2_index][i] <= L1_cache[L2_index][i-1];
                                end
                                
                                // Insert tag entry from L2 into L1 Set from the L2 Set 
                                L1_cache[L2_index][0] <= L2_tag;
                            end     
                    end
                        
                    // Insert new address at beginning of cache line
                    L2_cache[L2_index][0] <= L2_tag;
                end
        end
        
        // Shift logic if the cache for LRU or FIFO isn't full
        else if(next_state == SHIFTEMPTY)begin

                // L1 Cache
                if(cache_lvl)begin
                
                    if(replace_policy == 1)begin
                        L1_misses <= L1_misses + 1;
                        L1_reads <= L1_reads + 1;
                        if(write_policy == 1 && cache_op == 7'h57)begin
                            L1_writes <= L1_writes + 1;
                        end     
                    end
                    // Shifts through the current set with the size of the cache line to shift in FIFO order
                    for(i = L1_ASSOC; i > 0; i = i - 1)begin
                        L1_cache[L1_index][i] <= L1_cache[L1_index][i-1];
                    end
                    
                    // If the current inclusion policy is Non-inclusive and L1 & L2 miss, then insert tag in both caches
                    if(inclusion_policy == 2)begin
                    
                        // Pop out last address out of cache before shifting
                        L2_cache[L1_index][L2_ASSOC-1] <= 32'b0;
                                
                        // Shifts through the current set with the size of the cache line to shift in FIFO order
                        for(i = L2_ASSOC; i > 0; i = i - 1)begin
                            L2_cache[L1_index][i] <= L2_cache[L1_index][i-1];
                        end
                            
                        L2_cache[L1_index][0] <= L1_tag;
                        
                    end    
                    // Insert new address at beginning of cache line
                    L1_cache[L1_index][0] <= L1_tag;
                end

                // L2 Cache
                else begin
                    // Shifts through the current set with the size of the cache line to shift in FIFO order
                    for(i = L2_ASSOC; i > 0; i = i - 1)begin
                        L2_cache[L2_index][i] <= L2_cache[L2_index][i-1];
                    end
                    
                    // If the current inclusion policy is Non-inclusive and L1 & L2 miss, then insert tag in both caches
                    if(inclusion_policy == 2)begin
                        //if(L1_found == 0 && L2_found == 0)
                            if(L2_index > L1_ASSOC)begin
                                // Pop out last address out of cache before shifting
                                L1_cache[0][L1_ASSOC-1] <= 32'b0;
                                    
                                // Shifts through the current set with the size of the cache line to shift in FIFO order
                                for(i = L1_ASSOC; i > 0; i = i - 1)begin
                                    L1_cache[0][i] <= L1_cache[0][i-1];
                                end
                                L1_cache[0][0] <= L2_tag;
                            end
                            
                            else begin
                                // Pop out last address out of cache before shifting
                                L1_cache[L2_index][L1_ASSOC-1] <= 32'b0;
                                    
                                // Shifts through the current set with the size of the cache line to shift in FIFO order
                                for(i = L1_ASSOC; i > 0; i = i - 1)begin
                                    L1_cache[L2_index][i] <= L1_cache[L2_index][i-1];
                                end
                                L1_cache[L2_index][0] <= L2_tag;
                            end
                                
                    end
                    // Insert new address at beginning of cache line
                    L2_cache[L2_index][0] <= L2_tag;
                end
        end

        // Shift logic for LRU hit
        else if(next_state == LRUHIT)begin

                // L1 cache
                if(cache_lvl)begin
                    // Pop out LRU hit tag out of cache before shifting
                    L1_cache[L1_index][L1_lru_index] <= 32'b0;
                        
                    // Shifts through the current set with the size of the cache line to shift in LRU order
                    for(i = L1_ASSOC; i > 0; i = i - 1)begin
                    
                        if(i <= L1_lru_index)
                            L1_cache[L1_index][i] <= L1_cache[L1_index][i-1];
                    end
                        
                    // Insert new address at beginning of cache line
                    L1_cache[L1_index][0] <= L1_tag;     
                end

                // L2 cache
                else begin

                    // Pop out LRU hit tag out of cache before shifting
                    L2_cache[L2_index][L2_lru_index] <= 32'b0;
                        
                    // Shifts through the current set with the size of the cache line to shift in LRU order
                    for(i = L2_ASSOC; i > 0; i = i - 1)begin
                        
                        if(i <= L2_lru_index)
                            L2_cache[L2_index][i] <= L2_cache[L2_index][i-1];
                    end
                        
                    // Insert new address at beginning of cache line
                    L2_cache[L2_index][0] <= L2_tag;     
                end
                L1_found <= 1'b0;
                L2_found <= 1'b0;
        end
        
        // Finish LRU or FIFO address insertion and calculate cache miss rate
        else if(next_state == DONE)begin
            prev_addr <= cache_addr;
        end
    end
  end
endmodule
