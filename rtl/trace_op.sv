`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 04/14/2023 12:27:20 PM
// Design Name: 
// Module Name: trace_op
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

//parameter SIZE = 1000;
module trace_op(
    reg[7:0] test_ops[0:SIZE-1]
    );
    
    assign test_ops = 
    {
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
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
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
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
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
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
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
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
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
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
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h52,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57,
    8'h52,
    8'h57
    };
endmodule
