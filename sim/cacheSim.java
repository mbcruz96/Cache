import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

class Block {
    int tag;
    boolean dirty;
    boolean valid;

    public Block() {
        tag = 0;
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
    int misses;
    int hits;
    int writes;
    int reads;


    public CacheLevel(int assoc, long size, int block, int replacement)
    {
        this.cacheSize = size;
        this.blockSize = block;
        this.associativity = assoc;
        this.replacementPolicy = replacement;
        this.misses = 0;
        this.hits = 0;
        this.writes = 0;
        this.reads = 0;

        this.numSets = (int)(this.cacheSize / (long)(this.blockSize * this.associativity));
        this.tagArray = new ArrayList<LinkedList<Integer>>();
        this.blockArray = new ArrayList<LinkedList<Block>>();

        for (int i = 0; i < this.numSets; i++) {
            this.tagArray.add(new LinkedList<Integer>());
            this.blockArray.add(new LinkedList<Block>());
        }
    }

    void performOperation(char op, int setNumber, int tag)
    {
        if (op == 'w')
        {
            performWrite(setNumber, tag);
        }
        else if (op == 'r')
        {
            performRead(setNumber, tag);
        }
    }

    void performWrite(int setNumber, int tag)
    {   
        int index = getIndexOfTag(setNumber, tag);

        //write hit
        if (index != -1)
        {
            //update the dirty bit on a write hit
            Block curBlock = blockArray.get(setNumber).get(index);
            curBlock.dirty = true;
            blockArray.get(setNumber).set(index, curBlock);

            if (replacementPolicy == 1)
            {
                updateLRU(setNumber, tag);
            }
            hits++;
        }
        else
        {
            misses++;
            reads++;
            Block newBlock = new Block();
            newBlock.dirty = true;
            newBlock.tag = tag;
            newBlock.valid = true;

            if (tagArray.get(setNumber).size() < this.associativity)
            {
                //insert to both without worrying, no need to evict because there is space left
                addToLinkedLists(setNumber, tag, newBlock);
                return;
            }

            if (replacementPolicy == 3)
            {

            }
            else
            {
                //perform LRU/FIFO replacement
                LinkedList<Integer> curSet1 = tagArray.get(setNumber);
                LinkedList<Block> curSet2 = blockArray.get(setNumber);

                int removedTag = curSet1.removeLast();
                Block removedBlock = curSet2.removeLast();

                //deal with dirty bits as necessary
                if (removedBlock.dirty == true)
                {
                    writes++;
                }
                //add new elements to replacement linked lists
                curSet1.addFirst(tag);
                curSet2.addFirst(newBlock);

                tagArray.set(setNumber, curSet1);
                blockArray.set(setNumber, curSet2);
            }
        }
    }

    void performRead(int setNumber, int tag) {
        int index = getIndexOfTag(setNumber, tag);

        // the tag was found in the cache
        if (index != -1) {
            if (replacementPolicy == 1) {
                updateLRU(setNumber, tag);
            }
            hits++;
        }
        else 
        {
            misses++;
            reads++;
            //create the new block to insert
            Block newBlock = new Block();
            newBlock.tag = tag;
            newBlock.dirty = false;
            newBlock.valid = true;

            // cache set is not full
            if (tagArray.get(setNumber).size() < this.associativity) {
                // insert to both without worrying, no need to evict because there is space left
                addToLinkedLists(setNumber, tag, newBlock);
                return;
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
                Block removedBlock = curSet2.removeLast();

                //deal with dirty bits as necessary
                if (removedBlock.dirty == true)
                {
                    writes++;
                }
                //add new elements to replacement linked lists
                curSet1.addFirst(tag);
                curSet2.addFirst(newBlock);

                tagArray.set(setNumber, curSet1);
                blockArray.set(setNumber, curSet2);
            }
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
        System.out.printf("Miss Ratio: %.6f\n", (double)misses / (misses + hits));
        System.out.println("Writes: " + writes);
        System.out.println("Reads: " + reads);
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

    void startOperation(char op, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag)
    {
        int state = -1;
        boolean L1Contains = L1.contains(L1SetNumber, L1Tag);
        boolean L2Contains = L2.contains(L2SetNumber, L2Tag);

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
            executeNoninclusive(op, state, L1SetNumber, L1Tag, L2SetNumber, L2Tag);
        }
        else if (this.inclusion == 2)
        {
            executeInclusive(op, state, L1SetNumber, L1Tag, L2SetNumber, L2Tag);
        }
    }

    void executeNoninclusive(char op, int state, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag)
    {
        if (state == 0 || state == 3)
        {
            L1.performOperation(op, L1SetNumber, L1Tag);
            L2.performOperation(op, L2SetNumber, L2Tag);
        }
        else if (state == 1)
        {
            L2.misses++;
            L1.performOperation(op, L1SetNumber, L1Tag);
        }
        else if (state == 2)
        {
            L2.performOperation(op, L2SetNumber, L2Tag);
            L1.performOperation(op, L1SetNumber, L1Tag);
            L1.reads--; //subtracting one to account for copying from L2, not memory
        }
    }

    void executeInclusive(char op, int state, int L1SetNumber, int L1Tag, int L2SetNumber, int L2Tag)
    {
        if (state == 0 || state == 3)
        {
            L1.performOperation(op, L1SetNumber, L1Tag);
            L2.performOperation(op, L2SetNumber, L2Tag);
        }
        else if (state == 2)
        {
            L2.performOperation(op, L2SetNumber, L2Tag);
            L1.performOperation(op, L1SetNumber, L1Tag);
            L1.reads--; //subtracting one to account for copying from L2, not memory
        }

        //if l2 doesnt contain tag and l1 contains tag
        boolean L2Contains = L2.contains(L2SetNumber, L2Tag);
        boolean L1Contains = L1.contains(L1SetNumber, L1Tag);

        if (L1Contains && !L2Contains)
        {
            LinkedList<Block> curSet = L1.blockArray.get(L1SetNumber);
            for (int i = 0; i < curSet.size(); i++)
            {
                Block curBlock = curSet.get(i);
                if (curBlock.tag == L1Tag)
                {
                    curBlock.valid = false;
                    curSet.set(i, curBlock);
                    break;
                }
            }
            L1.blockArray.set(L1SetNumber, curSet);
        }
    }
}

class CacheSim
{
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

        if (L2Exists)
        {
            while (in.hasNext())
            {
                String nextLine = in.nextLine();
                char op = nextLine.charAt(0);
                long address = Long.parseLong(nextLine.substring(2), 16);
                int L1SetNumber = (int)(address % cache.L1.numSets);
                int L2SetNumber = (int)(address % cache.L2.numSets);
                int L1Tag = (int)(address / cache.L1.numSets);
                int L2Tag = (int)(address / cache.L2.numSets);
                
                //execute operation
                cache.startOperation(op, L1SetNumber, L1Tag, L2SetNumber, L2Tag);
                
            }
            cache.L1.printStats();
            cache.L2.printStats();
        }
        else
        {
            //l2 does not exist execution
            while (in.hasNext())
            {
                String nextLine = in.nextLine();
                char op = nextLine.charAt(0);
                long address = Long.parseLong(nextLine.substring(2), 16);
                int L1SetNumber = (int)(address % cache.L1.numSets);
                int L1Tag = (int)(address / cache.L1.numSets);
                
                //execute operation
                cache.L1.performOperation(op, L1SetNumber, L1Tag);
                
            }
            cache.L1.printStats();
            // cache.L2.printStats();
        }
    }
}