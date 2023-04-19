import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

class Block
{
    int tag;
    boolean dirty;
    boolean valid;
    
    public Block()
    {
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
    int replacementPolicy; //1 = lru, 2 = fifo, 3 = optimal
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

        for (int i = 0; i < this.numSets; i++)
        {
            this.tagArray.add(new LinkedList<Integer>());
            this.blockArray.add(new LinkedList<Block>());
        }
    }

    void performOperation(char op, int setNumber, int tag)
    {
        if (op == 'W')
        {
            performWrite(setNumber, tag);
        }
        else if (op == 'R')
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

    void performRead(int setNumber, int tag)
    {
        int index = getIndexOfTag(setNumber, tag);

        //the tag was found in the cache
        if (index != -1)
        {
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
            //create the new block to insert
            Block newBlock = new Block();
            newBlock.tag = tag;
            newBlock.dirty = false;
            newBlock.valid = true;

            //cache set is not full
            if (tagArray.get(setNumber).size() < this.associativity)
            {
                //insert to both without worrying, no need to evict because there is space left
                addToLinkedLists(setNumber, tag, newBlock);
                return;
            }
            
            //cache set is full
            //optimal replacement policy placeholder
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

    int getIndexOfTag(int setNumber, int tag)
    {
        return tagArray.get(setNumber).indexOf(tag);
    }

    void updateLRU(int setNumber, int tag)
    {
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

    void addToLinkedLists(int setNumber, int tag, Block block)
    {
        LinkedList<Integer> curSet1 = tagArray.get(setNumber);
        curSet1.addFirst(tag);
        tagArray.set(setNumber, curSet1);

        LinkedList<Block> curSet2 = blockArray.get(setNumber);
        curSet2.addFirst(block);
        blockArray.set(setNumber, curSet2);
    }

    void printStats()
    {
        System.out.printf("Miss Ratio: %.6f\n", (double)misses / (misses + hits));
        System.out.println("Writes: " + writes);
        System.out.println("Reads: " + reads);
    }
}

class CacheSim
{
    public static void main(String[] args) throws FileNotFoundException
    {
        long size = Integer.parseInt(args[0]);
        int assoc = Integer.parseInt(args[1]);
        int replacement = Integer.parseInt(args[2]);
        int writeBack = Integer.parseInt(args[3]);
        String fileTrace = args[4];
        // System.out.println(size + " " + associativity + " " + replacement + " " + writeBack + " " + fileTrace);
        Scanner in = new Scanner(new File(fileTrace));

        CacheLevel cache = new CacheLevel(assoc, size, 64, replacement);
        int counter = 1;
        while(in.hasNext()) 
        {
            // System.out.println("iteration " + counter);
            String line = in.nextLine();
            char op = line.charAt(0);
            String temp = "0" + line.substring(4);
            BigInteger bigAddress = new BigInteger(temp, 16);
            bigAddress = bigAddress.divide(new BigInteger("" + cache.blockSize));
            //address /= cache.blockSize;
            long address = bigAddress.longValue();
            int setNumber = (int)(address % cache.numSets);
            long tag = address / cache.numSets;

            // System.out.println(setNumber);
            // System.out.println(tag);
            cache.performOperation(op, setNumber, (int)tag);
            counter++;
        }
        cache.printStats();
    }
}