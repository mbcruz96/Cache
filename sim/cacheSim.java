import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

class Block {
    int tag;
    int address;
    boolean dirty;
    boolean valid;

    public Block() {
        tag = 0;
        address = 0;
        dirty = true;
        valid = false;
    }
}

class CacheLevel {
    long cacheSize;
    int blockSize;
    int associativity;
    int numSets;
    ArrayList<LinkedList<Integer>> tagArray;
    ArrayList<LinkedList<Block>> blockArray;
    ArrayList<Integer> allTraces;
    int replacementPolicy; // 1 = lru, 2 = fifo, 3 = optimal

    int reads;
    int readMisses;
    int writes;
    int writeMisses;
    int writebacks;
    int counter;

    // int misses;
    // int hits;
    // int writes;
    // int reads;


    public CacheLevel(int assoc, long size, int block, int replacement, ArrayList<Integer> allTraces)
    {
        this.cacheSize = size;
        this.blockSize = block;
        this.associativity = assoc;
        this.replacementPolicy = replacement;
        // this.misses = 0;
        // this.hits = 0;
        this.writes = 0;
        this.reads = 0;
        this.readMisses = 0;
        this.writeMisses = 0;
        this.allTraces = allTraces;
        this.counter = 0;

        this.numSets = (int) (this.cacheSize / (long) (this.blockSize * this.associativity));
        this.tagArray = new ArrayList<LinkedList<Integer>>();
        this.blockArray = new ArrayList<LinkedList<Block>>();

        for (int i = 0; i < this.numSets; i++) {
            this.tagArray.add(new LinkedList<Integer>());
            this.blockArray.add(new LinkedList<Block>());
        }
    }

    Block performOperation(char op, int setNumber, int tag, int address) {
        if (op == 'w') {
            return performWrite(setNumber, tag, address);
        } else {
            return performRead(setNumber, tag, address);
        }
    }

    Block performWrite(int setNumber, int tag, int address) {
        int index = getIndexOfTag(setNumber, tag);

        //print tag and setnumber
        System.out.println("tag: " + Integer.toHexString(tag) + " set: " + setNumber);

        Block removedBlock = new Block();
        removedBlock.valid = false;

        // write hit
        if (index != -1) {
            // update the dirty bit on a write hit
            Block curBlock = blockArray.get(setNumber).get(index);
            curBlock.dirty = true;
            blockArray.get(setNumber).set(index, curBlock);

            //move MRU to front on access
            if (this.replacementPolicy == 1 || this.replacementPolicy == 4)
            {
                updateLRU(setNumber, tag);
                System.out.println("found tag: " + Integer.toHexString(tag) + " in set " + setNumber);
            }

            this.writes++;

            return removedBlock;
        } else {
            this.writeMisses++;
            this.writes++;

            Block newBlock = new Block();
            newBlock.dirty = true;
            newBlock.tag = tag;
            newBlock.valid = true;
            newBlock.address = address;
            // print address
            System.out.println("address: " + Integer.toHexString(address));
            if (tagArray.get(setNumber).size() < this.associativity) {

                // insert to both without worrying, no need to evict because there is space left
                addToLinkedLists(setNumber, tag, newBlock);
                System.out.print("after: ");
        
                for (int i = 0; i < tagArray.get(setNumber).size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");
                System.out.println("-------------------------------");

                return removedBlock;
            }

            if (this.replacementPolicy == 3)
            {
                Block evictedBlock = doOptimalReplacement(setNumber, newBlock);
                evictedBlock.valid = true;
                if (evictedBlock.dirty == true)
                {
                    writebacks++;
                }
                return evictedBlock;
            }
            else if (this.replacementPolicy == 1 || this.replacementPolicy == 2)
            {
                //perform LRU/FIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                int removedTag = curSet1.removeLast();
                removedBlock = curSet2.removeLast();

                // deal with dirty bits as necessary
                if (removedBlock.dirty == true) {
                    this.writebacks++;
                }
                // add new elements to replacement linked lists
                curSet1.addFirst(tag);
                curSet2.addFirst(newBlock);

                tagArray.set(setNumber, curSet1);
                blockArray.set(setNumber, curSet2);

            }
            else
            {
                //perform MRU/LIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                int removedTag = curSet1.removeFirst();
                removedBlock = curSet2.removeFirst();

                //deal with dirty bits as necessary
                if (removedBlock.dirty == true)
                {
                    this.writebacks++;
                }
                //add new elements to replacement linked lists
                curSet1.addFirst(tag);
                curSet2.addFirst(newBlock);

                //delroy's comment
                if(setNumber == 37){
                    System.out.println("trying to add L2 tag: " + Integer.toHexString(tag)); 
                    System.out.println("trying to evict the last L2 tag: " + Integer.toHexString(curSet1.peekLast())); 
                    //print operation
                    System.out.println("write");
                    // print out the curset1
                    for (int i = 0; i < curSet1.size(); i++) {
                        System.out.println(Integer.toHexString(curSet1.get(i)));
                    }
                }
                tagArray.set(setNumber, curSet1);
                blockArray.set(setNumber, curSet2);

                // print out the curset1
                for (int i = 0; i < curSet1.size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");
                
            }

            // make sure its valid so I can use the evicted block later
            removedBlock.valid = true;
            return removedBlock;
        }
    }

    Block performRead(int setNumber, int tag, int address) {
        int index = getIndexOfTag(setNumber, tag);
        
        //print tag and setnumber
        System.out.println("tag: " + Integer.toHexString(tag) + " set: " + setNumber);

        Block removedBlock = new Block();
        removedBlock.valid = false;

        // the tag was found in the cache
        if (index != -1) {
            if (this.replacementPolicy == 1 || this.replacementPolicy == 3) {
                updateLRU(setNumber, tag);
                System.out.println("found tag: " + Integer.toHexString(tag) + " in set " + setNumber);
            }

            this.reads++;
            return removedBlock;
        } else {
            this.readMisses++;
            this.reads++;
            System.out.println("create new block to add to cache");
            // create the new block to insert
            Block newBlock = new Block();
            newBlock.tag = tag;
            newBlock.dirty = false;
            newBlock.valid = true;
            newBlock.address = address;

            System.out.println("address: " + Integer.toHexString(address));


            //print the tag that was a miss
            System.out.println("missed tag: " + Integer.toHexString(tag));
            // cache set is not full
            if (tagArray.get(setNumber).size() < this.associativity) {
                // insert to both without worrying, no need to evict because there is space left
                System.out.print("looking for tag "+ Integer.toHexString(newBlock.tag) + " in set " + setNumber);
                System.out.println("");
                System.out.print("before: ");
    
                for (int i = 0; i < blockArray.get(setNumber).size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");

                addToLinkedLists(setNumber, tag, newBlock);

                System.out.print("after: ");
        
                for (int i = 0; i < tagArray.get(setNumber).size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");
                System.out.println("-----------------------------");

                return removedBlock;
            }

            // cache set is full
            // optimal replacement policy placeholder
            System.out.println("right before optimal replacement policy is: " + replacementPolicy);
            if (this.replacementPolicy == 3) {
                // perform optimal replacement
                System.out.println("gonna try to evict to add new block");
                // set before eviction
                System.out.print("before: ");
                for (int i = 0; i < tagArray.get(setNumber).size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");
                Block evictedBlock = doOptimalReplacement(setNumber, newBlock);
                evictedBlock.valid = true;
                if (evictedBlock.dirty == true)
                {
                    writebacks++;
                }
                // set after eviction
                System.out.print("after: ");
                for (int i = 0; i < tagArray.get(setNumber).size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");
                System.out.println("-----------------------------");
                
                return evictedBlock;
            } 
            else if (this.replacementPolicy == 1 || this.replacementPolicy == 2)
            {
                // perform LRU/FIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                System.out.println("trying to add L2 tag: " + Integer.toHexString(tag)); 
                System.out.println("trying to evict the last L2 tag: " + Integer.toHexString(curSet1.peekLast())); 
                //print operation
                System.out.println("write");
                // print out the curset1
                for (int i = 0; i < curSet1.size(); i++) {
                    System.out.println(Integer.toHexString(curSet1.get(i)));
                }

                int removedTag = curSet1.removeLast();
                removedBlock = curSet2.removeLast();

                // deal with dirty bits as necessary
                if (removedBlock.dirty == true) {
                    this.writebacks++;
                }
                // add new elements to replacement linked lists
                curSet1.addFirst(tag);
                curSet2.addFirst(newBlock);

                tagArray.set(setNumber, curSet1);
                blockArray.set(setNumber, curSet2);
                // print out the curset1
                for (int i = 0; i < curSet1.size(); i++) {
                    System.out.print(" " + Integer.toHexString(blockArray.get(setNumber).get(i).tag) + 
                        " :"+blockArray.get(setNumber).get(i).dirty+" -> ");
                }
                System.out.println("");

            }
            else
            {
                //perform MRU/LIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                int removedTag = curSet1.removeFirst();
                removedBlock = curSet2.removeFirst();

                //deal with dirty bits as necessary
                if (removedBlock.dirty == true)
                {
                    this.writebacks++;
                }
                //add new elements to replacement linked lists
                curSet1.addFirst(tag);
                curSet2.addFirst(newBlock);

                tagArray.set(setNumber, curSet1);
                blockArray.set(setNumber, curSet2);
            }

            removedBlock.valid = true;
            return removedBlock;
        }
    }

    int getIndexOfTag(int setNumber, int tag) {
        boolean condition = false;
        LinkedList<Block> curSet = blockArray.get(setNumber);
        for (Block currBlock : curSet) {
            if (currBlock.valid && currBlock.tag == tag) {
                condition = true;
            }
        }

        if (condition) {
            return tagArray.get(setNumber).indexOf(tag);
        } else {
            return -1;
        }
    }

    void updateLRU(int setNumber, int tag) {
        LinkedList<Integer> curSet1 = tagArray.get(setNumber);
        LinkedList<Block> curSet2 = blockArray.get(setNumber);

        System.out.print("looking for tag "+ Integer.toHexString(tag) + " in set " + setNumber);
        System.out.println("");
        System.out.print("before: ");

        for (int i = 0; i < curSet1.size(); i++) {
            System.out.print(" " + Integer.toHexString(curSet1.get(i)) + " :"+curSet2.get(i).dirty +" -> ");
        }
        System.out.println("");

        int index = curSet1.indexOf(tag);

        int tagToUpdate = curSet1.remove(index);
        Block blockToUpdate = curSet2.remove(index);

        curSet1.addFirst(tagToUpdate);
        curSet2.addFirst(blockToUpdate);

        tagArray.set(setNumber, curSet1);
        blockArray.set(setNumber, curSet2);
        
        System.out.print("after: ");
        for (int i = 0; i < curSet1.size(); i++) {
            System.out.print(" " + Integer.toHexString(curSet1.get(i)) + " "+curSet2.get(i).dirty +"-> ");
        }
        System.out.println("");
        System.out.println("--------------------");

    }

    void addToLinkedLists(int setNumber, int tag, Block block) {
        LinkedList<Integer> curSet1 = tagArray.get(setNumber);
        curSet1.addFirst(tag);
        tagArray.set(setNumber, curSet1);

        LinkedList<Block> curSet2 = blockArray.get(setNumber);
        curSet2.addFirst(block);
        blockArray.set(setNumber, curSet2);
    }

    Block doOptimalReplacement(int setNumber, Block newBlock)
    {
        //get necessary sublist
        ArrayList<Integer> currentTrace = new ArrayList<Integer>(this.allTraces.subList(counter, this.allTraces.size()));
        int maxIndex = -1;
        int indexToRemove = -1;
        int i = 0;
        boolean found = false;
        
        // System.out.println("Current set: " + this.tagArray.get(setNumber).toString());
        //loop over all blocks
        LinkedList<Integer> setTags = this.tagArray.get(setNumber);
        LinkedList<Block> setBlocks = this.blockArray.get(setNumber);
        // System.out.println("At line " + (counter + 1));
        // System.out.println("The current set " + setNumber + " is: " + String.format("0x%08X", setBlocks.get(0).address) + " and " + String.format("0x%08X", setBlocks.get(1).address));
        for (i = 0; i < setBlocks.size(); i++)
        {
            //get the next time the memory address is used
            Block currBlock = setBlocks.get(i);
            int indexInList = currentTrace.indexOf(currBlock.address);
            System.out.println("Address " + Integer.toHexString(setBlocks.get(i).address) + " was found at index " + indexInList);
            //if the next time is further than the current furthest, mark as furthest
            //save the index in the list to remove
            if (indexInList == -1)
            {
                indexToRemove = i;
                found = true;
                break;
            }
            if (indexInList > maxIndex)
            {
                maxIndex = indexInList;
                indexToRemove = i;
            }
            
            //increase the current index being searched
        }

        //catch statement for when not found in trace
        if ((indexToRemove == -1) && (!found))
        {
            indexToRemove = this.blockArray.get(setNumber).size() - 1;
        }

        //remove the old element from the linked list and tag arrays
        LinkedList<Block> curBlockArray= blockArray.get(setNumber);
        LinkedList<Integer> curTagArray = tagArray.get(setNumber);

        Block removedBlock = curBlockArray.remove(indexToRemove);
        curTagArray.remove(indexToRemove);

        curBlockArray.add(indexToRemove, newBlock);
        curTagArray.add(indexToRemove, newBlock.tag);

        this.tagArray.set(setNumber, curTagArray);
        this.blockArray.set(setNumber, curBlockArray);

        // System.out.println("Evicted: " + String.format("0x%08X", removedBlock.address));
        return removedBlock;
    }

    Boolean contains(int setNumber, int tag) {
        boolean condition = false;
        LinkedList<Block> curSet = blockArray.get(setNumber);
        for (Block currBlock : curSet) {
            if (currBlock.valid && currBlock.tag == tag) {
                condition = true;
            }
        }

        return /* tagArray.get(setNumber).contains(tag) && */ condition;
    }

    void printCache() {
        for (int i = 0; i < this.numSets; i++) {
            LinkedList<Block> curSet = blockArray.get(i);
            System.out.print("Set\t" + i + ":\t");
            for (Block curBlock : curSet) {
                System.out.print(Integer.toHexString(curBlock.tag) + " ");
                if (curBlock.dirty == true) {
                    System.out.print("D\t");
                } else {
                    System.out.print(" \t");
                }
            }
            System.out.println();
        }
    }
}

class OverallCache {
    CacheLevel L1;
    CacheLevel L2;
    int inclusion;

    public OverallCache(int l1Assoc, int l1Size, int l2Assoc, int l2Size, int block, int replacement, int inclusion, ArrayList<Integer> allTraces)
    {
        this.L1 = new CacheLevel(l1Assoc, l1Size, block, replacement, allTraces);
        this.L2 = new CacheLevel(l2Assoc, l2Size, block, replacement, allTraces);
        this.inclusion = inclusion;
    }

    public OverallCache(int l1Assoc, int l1Size, int block, int replacement, int inclusion, ArrayList<Integer> allTraces)
    {
        this.L1 = new CacheLevel(l1Assoc, l1Size, block, replacement, allTraces);
        this.inclusion = inclusion;
    }

    void startOperation(char op, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag, int address) {
        int state = -1;
        boolean L1Contains = L1.contains(L1SetNumber, L1Tag);
        boolean L2Contains = false;
        if (!L1Contains) {
            L2Contains = L2.contains(L2SetNumber, L2Tag);
        }

        if (L1Contains && L2Contains) {
            state = 0;

        } else if (L1Contains && !L2Contains) {
            state = 1;

        } else if (!L1Contains && L2Contains) {
            state = 2;
            if (op == 'w') {
                this.L2.reads++;
            }

        } else {
            // if(op == 'w') {
            // L1.writeMisses++;
            // L1.writeMisses++;
            // }
            state = 3;

        }

        if (this.inclusion == 1) {
            executeNoninclusive(op, state, L1SetNumber, L1Tag, L2SetNumber, L2Tag, address);
        } else if (this.inclusion == 2) {
            executeInclusive(op, state, L1SetNumber, L1Tag, L2SetNumber, L2Tag, address);
        }
    }

    void executeNoninclusive(char op, int state, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag, int address) {

        if (state == 0) {
            // exists in both, nothing will be evicted, just leave it alone
            L1.performOperation(op, L1SetNumber, L1Tag, address);
        } else if (state == 1) {
            // exists only in l1, deal with eviction but do nothing else
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty) {
                int newL2SetNumber = ((int) evicted.address >> (CacheSim.blockOffsetBits))
                        & ((1 << CacheSim.indexBits2) - 1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;

                // taylors comments
                // L2.writeMisses++;
                System.out.println("trying to access L2 tag: " + Integer.toHexString(newL2Tag));        

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);
            }
        } else if (state == 2) {
            // exists only in l2, move it into l1, deal with the eviction it causes
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

                        //delroys comment
            
            System.out.println("L1 victim : "+ (evicted.valid ? evicted.address : "none"));

            if (evicted.valid && evicted.dirty) {

                int newL2SetNumber = ((int) evicted.address >> (CacheSim.blockOffsetBits))
                        & ((1 << CacheSim.indexBits2) - 1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;

                System.out.println("trying to access L2 tag: " + Integer.toHexString(newL2Tag));        

                // taylors comments
                // if (op == 'r') {
                // L2.writeMisses++;
                // }

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

            }
            L2.performOperation(op, L2SetNumber, L2Tag, address);

            // L1.reads--; //subtracting one to account for copying from L2, not memory
        } else if (state == 3) {
            // doesnt exist in either, handle the eviction from l1
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty) {
                int newL2SetNumber = ((int) evicted.address >> (CacheSim.blockOffsetBits))
                        & ((1 << CacheSim.indexBits2) - 1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                System.out.println("trying to access L2 tag: " + Integer.toHexString(newL2Tag));        

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

            }
            // todo: handle difference in operation based on evict
            // l2 should be write if its a write eviction, otherwise a read
            L2.performOperation('r', L2SetNumber, L2Tag, address);

        }
    }

    void executeInclusive(char op, int state, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag, int address) {
        if (state == 0) {
            // exists in both, nothing will be evicted, just leave it alone
            L1.performOperation(op, L1SetNumber, L1Tag, address);
            // L2.performOperation(op, L2SetNumber, L2Tag, address);
        } else if (state == 1) {
            // exists only in l1, deal with eviction but do nothing else
            // L2.misses++;
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty) {
                // print out the evicted block

                int newL2SetNumber = ((int) evicted.address >> (CacheSim.blockOffsetBits))
                        & ((1 << CacheSim.indexBits2) - 1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                System.out.println("trying to access L2 tag: " + Integer.toHexString(newL2Tag));        

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);
            }
        } else if (state == 2) {
            // exists only in l2, move it into l1, deal with the eviction it causes
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty) {

                int newL2SetNumber = ((int) evicted.address >> (CacheSim.blockOffsetBits))
                        & ((1 << CacheSim.indexBits2) - 1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                System.out.println("trying to access L2 tag: " + Integer.toHexString(newL2Tag));        

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);
            }
            L2.performOperation('r', L2SetNumber, L2Tag, address);

            // L1.reads--; //subtracting one to account for copying from L2, not memory
        } else if (state == 3) {
            // doesnt exist in either, handle the eviction from l1
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty) {

                int newL2SetNumber = ((int) evicted.address >> (CacheSim.blockOffsetBits))
                        & ((1 << CacheSim.indexBits2) - 1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                System.out.println("trying to access L2 tag: " + Integer.toHexString(newL2Tag));        

                // todo: if this bit is dirty its a write else read
                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

            }
            // todo: handle difference in operation based on evict
            // l2 should be write if its a write eviction, otherwise a read
            L2.performOperation('r', L2SetNumber, L2Tag, address);

        }
    }
}

class CacheSim {
    public static int blockOffsetBits;
    public static int indexBits1;
    public static int indexBits2;
    public static int tagBits1;
    public static int tagBits2;

    public static void main(String[] args) throws FileNotFoundException {
        // get the input from the command line in the following order:
        // <BLOCKSIZE> <L1_SIZE> <L1_ASSOC> <L2_SIZE> <L2_ASSOC> <REPLACEMENT_POLICY>
        // <INCLUSION_PROPERTY> <trace_file>
        int blockSize = Integer.parseInt(args[0]);
        int l1Size = Integer.parseInt(args[1]);
        int l1Assoc = Integer.parseInt(args[2]);
        int l2Size = Integer.parseInt(args[3]);
        int l2Assoc = Integer.parseInt(args[4]);
        String replacementPolicy = args[5];
        String inclusionProperty = args[6];
        String traceFile = args[7];

        ArrayList<Integer> allTraces = scanInAddresses(traceFile);
        // System.out.println(allTraces.toString());

        // convert the replacement policy to an integer
        int replacementPolicyInt = -1;
        if (replacementPolicy.equals("LRU")) {
            replacementPolicyInt = 1;
        } else if (replacementPolicy.equals("FIFO")) {
            replacementPolicyInt = 2;
        } else if (replacementPolicy.equals("optimal")) {
            replacementPolicyInt = 3;
        } else if (replacementPolicy.equals("MRU")) {
            replacementPolicyInt = 4;
        } else if (replacementPolicy.equals("LIFO")) {
            replacementPolicyInt = 5;
        }

        // convert the inclusion property to an integer
        int inclusionPropertyInt = -1;
        if (inclusionProperty.equals("non-inclusive")) {
            inclusionPropertyInt = 1;
        } else if (inclusionProperty.equals("inclusive")) {
            inclusionPropertyInt = 2;
        } else if (inclusionProperty.equals("exclusive")) {
            inclusionPropertyInt = 3;
        }

        boolean L2Exists = (l2Size > 0 ? true : false);
        // System.out.println(L2Exists);
        OverallCache cache;
        if (L2Exists)
        {
            cache = new OverallCache(l1Assoc, l1Size, l2Assoc, l2Size, blockSize, replacementPolicyInt, inclusionPropertyInt, allTraces);
        }
        else
        {
            cache = new OverallCache(l1Assoc, l1Size, blockSize, replacementPolicyInt, inclusionPropertyInt, allTraces);
        }

        Scanner in = new Scanner(new File(traceFile));

        int numLines = 1;

        if (L2Exists) {
            while (in.hasNext()) {
                String nextLine = in.nextLine();
                char op = nextLine.charAt(0);
                long address = Long.parseLong(nextLine.substring(2), 16);

                // calculate bits and values
                blockOffsetBits = (int) (Math.log(blockSize) / Math.log(2));
                int offsetBits = (int) address & ((1 << blockOffsetBits) - 1);

                indexBits1 = (int) (Math.log(cache.L1.numSets) / Math.log(2));
                indexBits2 = (int) (Math.log(cache.L2.numSets) / Math.log(2));
                tagBits1 = indexBits1 + blockOffsetBits;
                tagBits2 = indexBits2 + blockOffsetBits;
                // get working address
                address = (int) address ^ offsetBits;

                // shift address by offset and & with mask to get index
                int index1 = ((int) address >> (blockOffsetBits)) & ((1 << indexBits1) - 1);
                int index2 = ((int) address >> (blockOffsetBits)) & ((1 << indexBits2) - 1);
                // extract tag bits by shifting
                int tag1 = ((int) address >> tagBits1);
                int tag2 = ((int) address >> tagBits2);


                                //delroy's comment
                // if number of lines is 91428 , print the numlines
                // if (numLines == 91428 || numLines == 88576)
                // if (numLines == 91428 || numLines == 88576)
                // {
                    System.out.println("--------------------");
                    System.out.println("# "+numLines+" : "+op+" "+Integer.toHexString((int)address));
                    // }
                    // System.out.println("L1 write : "+Integer.toHexString((int)address)+
                    //     " (tag "+Integer.toHexString(L1Tag)+", index "+L1SetNumber+")");
                    numLines++;
    
                // execute operation
                // cache.startOperation(op, L1SetNumber, L1Tag, L2SetNumber, L2Tag,
                // (int)address);
                cache.startOperation(op, index1, tag1, index2, tag2, (int) (address / blockSize));
                cache.L1.counter++;
                cache.L2.counter++;

            }
        } else {
            // l2 does not exist execution
            while (in.hasNext()) {
                String nextLine = in.nextLine();
                char op = nextLine.charAt(0);
                long address = Long.parseLong(nextLine.substring(2), 16);

                // calculate bits and values
                blockOffsetBits = (int) (Math.log(blockSize) / Math.log(2));
                int offsetBits = (int) address & ((1 << blockOffsetBits) - 1);

                indexBits1 = (int) (Math.log(cache.L1.numSets) / Math.log(2));
                tagBits1 = indexBits1 + blockOffsetBits;
                // get working address
                address = (int) address ^ offsetBits;

                // shift address by offset and & with mask to get index
                int index1 = ((int) address >> (blockOffsetBits)) & ((1 << indexBits1) - 1);
                // extract tag bits by shifting
                int tag1 = ((int) address >> tagBits1);
                System.out.println("--------------------");
                System.out.println("# "+numLines+" : "+op+" "+Integer.toHexString((int)address));
                // }
                // System.out.println("L1 write : "+Integer.toHexString((int)address)+
                //     " (tag "+Integer.toHexString(L1Tag)+", index "+L1SetNumber+")");
                numLines++;

                // execute operation
                // cache.startOperation(op, L1SetNumber, L1Tag, L2SetNumber, L2Tag,
                // (int)address);
                cache.L1.performOperation(op, index1, tag1, (int)(address / blockSize));
                cache.L1.counter++;

                
            }
        }
        finalPrint(cache.L1, cache.L2, L2Exists, inclusionPropertyInt, traceFile);
    }

    private static void finalPrint(CacheLevel L1, CacheLevel L2, boolean L2Exists, int inclusionPropertyInt,
            String traceFile) {
        System.out.println("===== Simulator configuration =====");
        System.out.println("BLOCKSIZE:\t\t" + L1.blockSize);
        System.out.println("L1_SIZE:\t\t" + L1.cacheSize);
        System.out.println("L1_ASSOC:\t\t" + L1.associativity);
        if (L2Exists) {
            System.out.println("L2_SIZE:\t\t" + L2.cacheSize);
            System.out.println("L2_ASSOC:\t\t" + L2.associativity);
        } else {
            System.out.println("L2_SIZE:\t\t" + 0);
            System.out.println("L2_ASSOC:\t\t" + 0);
        }

        if (L1.replacementPolicy == 1) {
            System.out.println("REPLACEMENT POLICY:\tLRU");
        } else if (L1.replacementPolicy == 2) {
            System.out.println("REPLACEMENT POLICY:\tFIFO");
        } else if (L1.replacementPolicy == 3) {
            System.out.println("REPLACEMENT POLICY:\toptimal");
        }

        if (inclusionPropertyInt == 1) {
            System.out.println("INCLUSION PROPERTY:\tnon-inclusive");
        } else if (inclusionPropertyInt == 2) {
            System.out.println("INCLUSION PROPERTY:\tinclusive");
        }
        System.out.println("trace_file:\t\t" + traceFile);

        System.out.println("===== L1 contents =====");
        L1.printCache();

        if (L2Exists) {
            System.out.println("===== L2 contents =====");
            L2.printCache();
        }

        System.out.println("===== Simulation results (raw) =====");
        System.out.println("a. number of L1 reads:\t\t" + L1.reads);
        System.out.println("b. number of L1 read misses:\t" + L1.readMisses);
        System.out.println("c. number of L1 writes:\t\t" + L1.writes);
        System.out.println("d. number of L1 write misses:\t" + L1.writeMisses);
        System.out.printf(
                "e. L1 miss rate:\t\t%.6f\n", (double) (L1.readMisses + L1.writeMisses) / (L1.reads + L1.writes));
        System.out.println("f. number of L1 writebacks:\t" + L1.writebacks);

        if (L2Exists) {
            System.out.println("g. number of L2 reads:\t\t" + L2.reads);
            System.out.println("h. number of L2 read misses:\t" + L2.readMisses);
            // the only way for something to write to L2 is a writeback from a lower cache
            System.out.println("i. number of L2 writes:\t\t" + L1.writebacks);
            System.out.println("j. number of L2 write misses:\t" + L2.writeMisses);
            System.out.printf(
                    "k. L2 miss rate:\t\t%.6f\n",
                    (double) (L2.readMisses + L2.writeMisses) / (L2.reads));
            System.out.println("l. number of L2 writebacks:\t" + L2.writebacks);
        } else {
            System.out.println("g. number of L2 reads:\t\t" + 0);
            System.out.println("h. number of L2 read misses:\t" + 0);
            System.out.println("i. number of L2 writes:\t\t" + 0);
            System.out.println("j. number of L2 write misses:\t" + 0);
            System.out.println("k. L2 miss rate:\t\t" + 0);
            System.out.println("l. number of L2 writebacks:\t" + 0);
        }
        if (L2Exists) {
            System.out.println("m. total memory traffic:\t" + (L2.readMisses + L2.writebacks));
        } else {
            System.out.println("m. total memory traffic:\t" + (L1.readMisses + L1.writeMisses + L1.writebacks));
        }
    }
    static ArrayList<Integer> scanInAddresses(String traceFile) throws FileNotFoundException
    {
        Scanner scanFile = new Scanner(new File(traceFile));
        ArrayList<Integer> listOfAddresses = new ArrayList<Integer>();
        while(scanFile.hasNext())
        {
            String nextLine = scanFile.nextLine();
            nextLine = nextLine.substring(2);
            long address = Long.parseLong(nextLine, 16);
            address /= 16;
            listOfAddresses.add((int)address);
        }

        return listOfAddresses;
    }

}