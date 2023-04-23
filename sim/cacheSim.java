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

class CacheLevel
{
    long cacheSize;
    int blockSize;
    int associativity;
    int numSets;
    ArrayList<LinkedList<Integer>> tagArray;
    ArrayList<LinkedList<Block>> blockArray;
    int replacementPolicy; // 1 = lru, 2 = fifo, 3 = optimal

    int reads;
    int readMisses;
    int writes;
    int writeMisses;
    int writebacks;

    // int misses;
    // int hits;
    // int writes;
    // int reads;


    public CacheLevel(int assoc, long size, int block, int replacement)
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

        this.numSets = (int)(this.cacheSize / (long)(this.blockSize * this.associativity));
        this.tagArray = new ArrayList<LinkedList<Integer>>();
        this.blockArray = new ArrayList<LinkedList<Block>>();

        for (int i = 0; i < this.numSets; i++) {
            this.tagArray.add(new LinkedList<Integer>());
            this.blockArray.add(new LinkedList<Block>());
        }
    }

    Block performOperation(char op, int setNumber, int tag, int address)
    {
        if (op == 'w')
        {
            return performWrite(setNumber, tag, address);
        }
        else
        {
            return performRead(setNumber, tag, address);
        }
    }

    Block performWrite(int setNumber, int tag, int address)
    {   
        int index = getIndexOfTag(setNumber, tag);

        Block removedBlock = new Block();
        removedBlock.valid = false;

        //write hit
        if (index != -1)
        {
            // update the dirty bit on a write hit
            Block curBlock = blockArray.get(setNumber).get(index);
            curBlock.dirty = true;
            blockArray.get(setNumber).set(index, curBlock);

            if (replacementPolicy == 1)
            {
                updateLRU(setNumber, tag);
            }

            this.writes++;

            return removedBlock;
        }
        else
        {
            this.writeMisses++;
            this.writes++;

            Block newBlock = new Block();
            newBlock.dirty = true;
            newBlock.tag = tag;
            newBlock.valid = true;
            newBlock.address = address;

            if (tagArray.get(setNumber).size() < this.associativity)
            {

                //insert to both without worrying, no need to evict because there is space left
                addToLinkedLists(setNumber, tag, newBlock);

                
                return removedBlock;
            }

            if (replacementPolicy == 3)
            {
                //perform optimal replacement

            }
            else
            {
                //perform LRU/FIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                int removedTag = curSet1.removeLast();
                removedBlock = curSet2.removeLast();

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

            //make sure its valid so I can use the evicted block later
            removedBlock.valid = true;
            return removedBlock;
        }
    }

    Block performRead(int setNumber, int tag, int address) {
        int index = getIndexOfTag(setNumber, tag);

        Block removedBlock = new Block();
        removedBlock.valid = false;

        // the tag was found in the cache
        if (index != -1) {
            if (replacementPolicy == 1) {
                updateLRU(setNumber, tag);
            }
            
            this.reads++;
            return removedBlock;
        }
        else 
        {
            this.readMisses++;
            this.reads++;

            //create the new block to insert
            Block newBlock = new Block();
            newBlock.tag = tag;
            newBlock.dirty = false;
            newBlock.valid = true;
            newBlock.address = address;

            // cache set is not full
            if (tagArray.get(setNumber).size() < this.associativity) {
                // insert to both without worrying, no need to evict because there is space left
                addToLinkedLists(setNumber, tag, newBlock);
                return removedBlock;
            }

            // cache set is full
            // optimal replacement policy placeholder
            if (replacementPolicy == 3) {
                // perform optimal replacement

            } else {
                // perform LRU/FIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                int removedTag = curSet1.removeLast();
                removedBlock = curSet2.removeLast();

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
        for (Block currBlock : curSet)
        {
            if (currBlock.valid && currBlock.tag == tag)
            {
                condition = true;
            }
        }

        if (condition)
        {
            return tagArray.get(setNumber).indexOf(tag);
        }
        else
        {
            return -1;
        }
    }

    void updateLRU(int setNumber, int tag) {
        LinkedList<Integer> curSet1 = tagArray.get(setNumber);
        LinkedList<Block> curSet2 = blockArray.get(setNumber);

        int index = curSet1.indexOf(tag);

        int tagToUpdate = curSet1.remove(index);
        Block blockToUpdate = curSet2.remove(index);

        curSet1.addFirst(tagToUpdate);
        curSet2.addFirst(blockToUpdate);

        tagArray.set(setNumber, curSet1);
        blockArray.set(setNumber, curSet2);
        
    }

    void addToLinkedLists(int setNumber, int tag, Block block) {
        LinkedList<Integer> curSet1 = tagArray.get(setNumber);
        curSet1.addFirst(tag);
        tagArray.set(setNumber, curSet1);

        LinkedList<Block> curSet2 = blockArray.get(setNumber);
        curSet2.addFirst(block);
        blockArray.set(setNumber, curSet2);
    }

    void doOptimalReplacement()
    {

    }

    Boolean contains(int setNumber, int tag)
    {
        boolean condition = false; 
        LinkedList<Block> curSet = blockArray.get(setNumber);
        for (Block currBlock : curSet)
        {
            if (currBlock.valid && currBlock.tag == tag)
            {
                condition = true;
            }
        }
        
        return /*tagArray.get(setNumber).contains(tag) &&*/ condition;
    }

    void printStats()
    {
        System.out.println("Reads: " + reads);
        System.out.println("Read Misses: " + this.readMisses);
        System.out.println("Writes: " + writes);
        System.out.println("Write Misses: " + this.writeMisses);
        System.out.printf("Miss Ratio: %.6f\n", (double)(this.readMisses + this.writeMisses) / (this.reads + this.writes));
        System.out.println("Writebacks: " + this.writebacks);
    }

    void printCache()
    {
        System.out.println("======== Contents =======");
        for (int i = 0; i < this.numSets; i++)
        {
            LinkedList<Block> curSet = blockArray.get(i);
            System.out.print("Set " + i + "    ");
            for (Block curBlock : curSet)
            {
                System.out.print(Integer.toHexString(curBlock.tag) + " ");
                if(curBlock.dirty == true)
                {
                    System.out.print("D  ");
                }
                else
                {
                    System.out.print("   ");
                }
            }
            System.out.println();
        }
    }
}

class OverallCache
{
    CacheLevel L1;
    CacheLevel L2;
    int inclusion;

    public OverallCache(int l1Assoc, int l1Size, int l2Assoc, int l2Size, int block, int replacement, int inclusion)
    {
        this.L1 = new CacheLevel(l1Assoc, l1Size, block, replacement);
        this.L2 = new CacheLevel(l2Assoc, l2Size, block, replacement);
        this.inclusion = inclusion;
    }

    public OverallCache(int l1Assoc, int l1Size, int block, int replacement, int inclusion)
    {
        this.L1 = new CacheLevel(l1Assoc, l1Size, block, replacement);
        this.inclusion = inclusion;
    }

    void startOperation(char op, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag, int address)
    {
        int state = -1;
        boolean L1Contains = L1.contains(L1SetNumber, L1Tag);
        boolean L2Contains = false;
        if(!L1Contains){
            L2Contains = L2.contains(L2SetNumber, L2Tag);
        }

        if (L1Contains && L2Contains)
        {
            state = 0;

        }
        else if (L1Contains && !L2Contains)
        {
            state = 1;

        }
        else if (!L1Contains && L2Contains)
        {
            state = 2;

        }
        else
        {
            state = 3;

        }

        if (this.inclusion == 1)
        {
            executeNoninclusive(op, state, L1SetNumber, L1Tag, L2SetNumber, L2Tag, address);
        }
        else if (this.inclusion == 2)
        {
            executeInclusive(op, state, L1SetNumber, L1Tag, L2SetNumber, L2Tag, address);
        }
    }

    void executeNoninclusive(char op, int state, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag, int address)
    {

        if (state == 0)
        {
            //exists in both, nothing will be evicted, just leave it alone
            L1.performOperation(op, L1SetNumber, L1Tag, address);
        }
        else if (state == 1)
        {
            // exists only in l1, deal with eviction but do nothing else
            // L2.misses++;
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if(evicted.valid && evicted.dirty)
            {
                int newL2SetNumber = ((int)evicted.address >> (CacheSim.blockOffsetBits)) & ((1 << CacheSim.indexBits2)-1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

            }
        }
        else if (state == 2)
        {
            //exists only in l2, move it into l1, deal with the eviction it causes
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty)
            {

                int newL2SetNumber = ((int)evicted.address >> (CacheSim.blockOffsetBits)) & ((1 << CacheSim.indexBits2)-1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                
                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

            }
            L2.performOperation(op, L2SetNumber, L2Tag, address);

            // L1.reads--; //subtracting one to account for copying from L2, not memory
        }
        else if (state == 3)
        {
            //doesnt exist in either, handle the eviction from l1
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty)
            {
                int newL2SetNumber = ((int)evicted.address >> (CacheSim.blockOffsetBits)) & ((1 << CacheSim.indexBits2)-1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;

                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

            }
            // todo: handle difference in operation based on evict
            // l2 should be write if its a write eviction, otherwise a read
            L2.performOperation('r', L2SetNumber, L2Tag, address);

        }
    }

    void executeInclusive(char op, int state, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag, int address)
    {
        if (state == 0)
        {
            //exists in both, nothing will be evicted, just leave it alone
            L1.performOperation(op, L1SetNumber, L1Tag, address);
            //L2.performOperation(op, L2SetNumber, L2Tag, address);
        }
        else if (state == 1)
        {
            // exists only in l1, deal with eviction but do nothing else
            // L2.misses++;
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if(evicted.valid && evicted.dirty)
            {
                //print out the evicted block

                int newL2SetNumber = ((int)evicted.address >> (CacheSim.blockOffsetBits)) & ((1 << CacheSim.indexBits2)-1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);
            }
        }
        else if (state == 2)
        {
            //exists only in l2, move it into l1, deal with the eviction it causes
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty)
            {

                int newL2SetNumber = ((int)evicted.address >> (CacheSim.blockOffsetBits)) & ((1 << CacheSim.indexBits2)-1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;
                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);
            }
            L2.performOperation('r', L2SetNumber, L2Tag, address);

            // L1.reads--; //subtracting one to account for copying from L2, not memory
        }
        else if (state == 3)
        {
            //doesnt exist in either, handle the eviction from l1
            Block evicted = L1.performOperation(op, L1SetNumber, L1Tag, address);

            if (evicted.valid && evicted.dirty)
            {

                int newL2SetNumber = ((int)evicted.address >> (CacheSim.blockOffsetBits)) & ((1 << CacheSim.indexBits2)-1);
                int newL2Tag = evicted.address >> CacheSim.tagBits2;

                // todo: if this bit is dirty its a write else read
                this.L2.performOperation('w', newL2SetNumber, newL2Tag, evicted.address);

                
            }
            // todo: handle difference in operation based on evict
            // l2 should be write if its a write eviction, otherwise a read
            L2.performOperation('r', L2SetNumber, L2Tag, address);

        }
    }
}

class CacheSim
{
    public static int blockOffsetBits;
    public static int indexBits1;
    public static int indexBits2;
    public static int tagBits1;
    public static int tagBits2;

    public static void main(String[] args) throws FileNotFoundException
    {
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

        // convert the replacement policy to an integer
        int replacementPolicyInt = -1;
        if (replacementPolicy.equals("LRU")) {
            replacementPolicyInt = 1;
        } else if (replacementPolicy.equals("FIFO")) {
            replacementPolicyInt = 2;
        } else if (replacementPolicy.equals("OPTIMAL")) {
            replacementPolicyInt = 3;
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
        System.out.println(L2Exists);
        OverallCache cache;
        if (L2Exists)
        {
            cache = new OverallCache(l1Assoc, l1Size, l2Assoc, l2Size, blockSize, replacementPolicyInt, inclusionPropertyInt);
        }
        else
        {
            cache = new OverallCache(l1Assoc, l1Size, blockSize, replacementPolicyInt, inclusionPropertyInt);
        }

        Scanner in = new Scanner(new File(traceFile));
        int numLines = 1;
        if (L2Exists)
        {
            while (in.hasNext())
            {
                String nextLine = in.nextLine();
                char op = nextLine.charAt(0);
                long address = Long.parseLong(nextLine.substring(2), 16);

                // calculate bits and values
                blockOffsetBits = (int) (Math.log(blockSize) / Math.log(2));
                int offsetBits = (int)address & ((1 << blockOffsetBits)-1);

                indexBits1 = (int) (Math.log(cache.L1.numSets) / Math.log(2));
                indexBits2 = (int) (Math.log(cache.L2.numSets) / Math.log(2));
                tagBits1 = indexBits1 + blockOffsetBits;
                tagBits2 = indexBits2 + blockOffsetBits;
                //get working address
                address = (int)address ^ offsetBits;

                // shift address by offset and & with mask to get index
                int index1 = ((int)address >> (blockOffsetBits)) & ((1 << indexBits1)-1);
                int index2 = ((int)address >> (blockOffsetBits)) & ((1 << indexBits2)-1);
                //extract tag bits by shifting
                int tag1 = ((int)address >> tagBits1);
                int tag2 = ((int)address >> tagBits2);

                numLines++;
                //execute operation
                //cache.startOperation(op, L1SetNumber, L1Tag, L2SetNumber, L2Tag, (int)address);
                cache.startOperation(op, index1, tag1, index2, tag2, (int)address);
                
            }
            System.out.println("L1:");
            cache.L1.printStats();
            cache.L1.printCache();
            System.out.println();
            System.out.println("L2:");
            cache.L2.printStats();
            cache.L2.printCache();
        }
        else
        {
            //l2 does not exist execution
            while (in.hasNext())
            {
                String nextLine = in.nextLine();
                char op = nextLine.charAt(0);
                long address = Long.parseLong(nextLine.substring(2), 16);
                address = address / cache.L1.blockSize;
                // System.out.println(String.format("0x%08X", address));
                int L1SetNumber = (int)(address % cache.L1.numSets);
                int L1Tag = (int)(address / cache.L1.numSets);
                
                //execute operation
                cache.L1.performOperation(op, L1SetNumber, L1Tag, (int)address);
                
            }

            cache.L1.printStats();
            cache.L1.printCache();
            // cache.L2.printStats();
        }
    }

    void scanInAddresses()
    {
        
    }
}