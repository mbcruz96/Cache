import sys

input_file = sys.argv[1]
address_output_file = input_file.split('.')[0] + "_addresses.txt"
action_output_file = input_file.split('.')[0] + "_actions.txt"

file = open(input_file, "r")
out_file_1 = open(address_output_file, "w")
out_file_2 = open(action_output_file, "w")

arr = file.readlines()
for i in arr:
    address = ""
    action = ""
    if (i[0:1] == 'w'):
        action = action + '57' #set write
    else:
        action = action + '52' #set read
    
    temp = i[2:]
    num_chars_needed = 12 - len(temp) + 1
    for i in range(num_chars_needed):
        temp = '0' + temp
    
    address = address + temp
    # print(action)
    # print(address)
    
    out_file_1.write(address)
    out_file_2.write(action + '\n')

file.close()
out_file_1.close()
out_file_2.close()