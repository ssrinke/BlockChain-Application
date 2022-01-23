//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 3-Task 0
//        This is a Blockchain class which takes input from user to perform various operations

package edu.cmu.andrew.srinke;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class BlockChain {
    List<Block> blocks = null;
    String chainHash;
    int hashPerSec;
    //created a field to store the corrupt nodeID
    int corruptNodeId;
    MessageDigest md = null;

    //initialize blockchain
    public BlockChain() {
        corruptNodeId = -1;
        this.blocks = new ArrayList<>();
        this.chainHash = "";
        this.hashPerSec = 0;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public int getHashesPerSecond() {
        return hashPerSec;
    }

    public Block getLatestBlock() {
        return blocks.get(getChainSize() - 1);
    }

    public Timestamp getTime() {
        return new Timestamp(System.currentTimeMillis());
    }

    public int getCorruptNodeId() {
        return corruptNodeId;
    }

    public void setCorruptNodeId(int corruptNodeId) {
        this.corruptNodeId = corruptNodeId;
    }

    //adds a block at the end of the list
    public void addBlock(Block newBlock) {
        //if chain has many blocks, sets previous hash as the hash of the previous block
        if ((getChainSize() + 1) > 1) {
            newBlock.setPreviousHash(blocks.get(newBlock.getIndex() - 1).getHash());
        } else
            //if the chain has 1 block, sets previous hash as "".
            newBlock.setPreviousHash("");
        newBlock.proofOfWork();
        //sets chainHash has the hash of the new block
        chainHash = newBlock.getHash();
        blocks.add(newBlock);
    }

    //computes hashes per second by hashing a constant string '00000000'
    public void computeHashesPerSecond() {
        String input = "00000000";
        Long startTime = getTime().getTime();
        for (int i = 0; i < 100000; i++) {
            //calculate hash for the input
            calculateHash(input);
        }
        Long endTime = getTime().getTime();
        Long timeDiff = endTime - startTime;
        //find the time diff in seconds
        float diffInSecs = (float) timeDiff / 1000;
        //divide million by time taken in secs
        hashPerSec = (int) (1000000 / diffInSecs);
    }

    //calculate SHA-256 hash for the passed input
    public String calculateHash(String input) {
        byte[] digest = null;
        try {
            md.update(input.getBytes("UTF-8"));
            digest = md.digest();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return digest.toString();
    }

    //get the block at ith position
    public Block getBlock(int i) {
        return blocks.get(i);
    }

    //return chain size
    public int getChainSize() {
        return blocks.size();
    }

    //get the combined difficulty of all blocks
    public int getTotalDifficulty() {
        int totalDiff = 0;
        for (int i = 0; i < getChainSize(); i++) {
            totalDiff += getBlock(i).getDifficulty();
        }
        return totalDiff;
    }

    //get total expected hashes
    public double getTotalExpectedHashes() {
        double totHash = 0;
        for (int i = 0; i < getChainSize(); i++) {
            totHash += Math.pow(16, getBlock(i).difficulty);
        }
        return totHash;
    }

    //check if entire chain is valid or not
    public boolean isChainValid() {
        Block b = null;
        boolean flag = false;
        try {
//            md = MessageDigest.getInstance("SHA-256");
            //will execute if the chain has only 1 block
            if (getChainSize() == 1) {
                b = getBlock(0);
                //check for valid block and if the hash of the block is same as chainhash
                if (b.getHash().equalsIgnoreCase(chainHash) && checkValidBlock(b))
                    flag = true;
                else {
                    flag = false;
                    //if the block is corrupt, set the corrupt node ID
                    setCorruptNodeId(b.getIndex());
                }
            }
            //will execute if the chain has more than 1 block
            else {
                for (int i = 0; i < getChainSize(); i++) {
                    if (i != getChainSize() - 1) {
                        b = getBlock(i);
                        if (b.getHash().equalsIgnoreCase(blocks.get(i + 1).getPreviousHash()) && checkValidBlock(b))
                            flag = true;
                        else {
                            flag = false;
                            setCorruptNodeId(b.getIndex());
                            break;
                        }
                    } else {
                        b = getBlock(i);
                        if (b.getHash().equalsIgnoreCase(chainHash) && checkValidBlock(b))
                            flag = true;
                        else {
                            flag = false;
                            setCorruptNodeId(b.getIndex());
                            break;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
    //checking if the hash of the block as same or more no of of leading 0 as difficulty
    // Eg: if difficulty=2 then valid hash='00...'
    private boolean checkProofOfWork(String hexaDecimalStr, int difficulty) {
        int count = 0;
        for (char c : hexaDecimalStr.toCharArray()) {
            if (c == '0')
                count++;
            else
                break;
        }
        if (count >= difficulty) {
            return true;
        }
        return false;
    }

    //repairs the invalid chain
    public void repairChain() {
        for (int i = 0; i < getChainSize(); i++) {
            if (!checkValidBlock(getBlock(i))) {
                getBlock(i).proofOfWork();
                reAssignPrevHash(getBlock(i));
            }
        }
    }

    //re-assigns updated previous hash for all the blocks after the corrupt block
    private void reAssignPrevHash(Block block) {
        for (int i = block.getIndex(); i < getChainSize(); i++) {
            if (i + 1 < getChainSize())
                blocks.get(i + 1).setPreviousHash(getBlock(i).getHash());
        }
        chainHash = blocks.get(getChainSize() - 1).getHash();
    }

    //check if a block is valid by verifying its proof of work
    public boolean checkValidBlock(Block b) {
        String message = b.getIndex() + "," + b.getTimestamp() + "," + b.getData() + "," + b.getPreviousHash() + "," + b.getNonce() + "," + b.getDifficulty();
        try {
            md.update(message.getBytes("UTF-8"));
            byte[] digest = md.digest();
            String hexaDecimalStr = bytesToHex(digest);
            if (checkProofOfWork(hexaDecimalStr, b.getDifficulty())) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray(blocks.toString());
        obj.put("ds_chain", jsonArray);
        obj.put("chainHash", chainHash);
        return obj.toString();
    }

    //Lab 1-https://github.com/CMU-Heinz-95702/Lab1-InstallationAndRaft
    //converts the bytes to hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main(String[] args) {
        //While adding blocks of increasing difficulty, the time taken to add increases.
        // For example- time taken to add block of difficulty:2 is 6ms and time taken for difficulty: 5 is 211ms
        //While the chain becomes longer the the block-chain verification takes similar amount of time
        //for chain size- 2: 0ms ; for chain size-8 1ms
        //While the chain becomes longer with increasing difficulty, the repair takes longer times.
        //Repair time for 8 blocks with difficulty- 23: 1342ms
        // Repair time for 2 blocks with difficulty-5: 1ms
        BlockChain blockChain = new BlockChain();
        //compute hashPerSecond on startup
        blockChain.computeHashesPerSecond();
        Timestamp startTime;
        Timestamp endTime;
        //adding the genesis block
        Block genesisBlock = new Block(0, blockChain.getTime(), "Genesis", 2);
        blockChain.addBlock(genesisBlock);
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
        try {
            int ch = -1;
            do {
                System.out.println("\nBlock chain menu");
                System.out.println("0. View basic blockchain status.");
                System.out.println("1. Add a transaction to the blockchain.");
                System.out.println("2. Verify the blockchain.");
                System.out.println("3. View the blockchain.");
                System.out.println("4. Corrupt the chain.");
                System.out.println("5. Hide the corruption by recomputing the hashes.");
                System.out.println("6. Exit.");
                ch = Integer.parseInt(typed.readLine());
                switch (ch) {
                    case 0:
                        System.out.println("Current size of chain: " + blockChain.getChainSize());
                        System.out.println("Difficulty of most recent block: " + blockChain.getLatestBlock().getDifficulty());
                        System.out.println("Total difficulty for all blocks: " + blockChain.getTotalDifficulty());
                        System.out.println("Approximate hashes per second on this machine: " + blockChain.getHashesPerSecond());
                        System.out.println("Expected total hashes required for the whole chain: " + blockChain.getTotalExpectedHashes());
                        System.out.println("Nonce for most recent block: " + blockChain.getLatestBlock().getNonce());
                        System.out.println("Chain hash: " + blockChain.chainHash);
                        break;
                    case 1:
                        System.out.println("Enter Difficulty>0");
                        int diff = Integer.parseInt(typed.readLine());
                        System.out.println("Enter the transaction");
                        String transaction = typed.readLine();
                        Block b = new Block(blockChain.getChainSize(), blockChain.getTime(), transaction, diff);
                        startTime = blockChain.getTime();
                        blockChain.addBlock(b);
                        endTime = blockChain.getTime();
                        System.out.println("Time taken to add this block::" + (endTime.getTime() - startTime.getTime()) + " ms");
                        break;
                    case 2:
                        startTime = blockChain.getTime();
                        if (blockChain.isChainValid() == true) {
                            System.out.print("Chain verification: true");
                        } else {
                            System.out.println("Chain verification: false");
                            System.out.print("..Improper hash on node " + blockChain.getCorruptNodeId() + ". Does not begin with ");
                            for (int i = 0; i < blockChain.getBlock(blockChain.getCorruptNodeId()).getDifficulty(); i++) {
                                System.out.print("0");
                            }
                        }
                        endTime = blockChain.getTime();
                        System.out.println("\nVerification took " + (endTime.getTime() - startTime.getTime()) + " ms");
                        break;
                    case 3:
                        JSONObject json = new JSONObject(blockChain.toString());
                        System.out.println(json.toString(4));
                        break;
                    case 4:
                        System.out.println("Enter the block id to corrupt");
                        int id = Integer.parseInt(typed.readLine());
                        System.out.println("Enter the new data for block id " + id);
                        String data = typed.readLine();
                        blockChain.blocks.get(id).setData(data);
                        System.out.println("The block "+id+" now holds " + blockChain.blocks.get(id).getData());
                        break;
                    case 5:
                        startTime = blockChain.getTime();
                        System.out.println("Repairing the entire chain");
                        blockChain.repairChain();
                        endTime = blockChain.getTime();
                        System.out.println("Repairing took " + (endTime.getTime() - startTime.getTime()) + " ms");
                        break;
                    case 6:
                        System.out.println("Exiting!");
                        System.exit(0);
                        break;
                }


            } while (ch != 6);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
