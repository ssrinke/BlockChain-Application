//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 3-Task 0
//        This is a Block class which contains all the instance variables for the block and its getters/setters

package edu.cmu.andrew.srinke;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

public class Block {
    MessageDigest md = null;
    int index;
    Timestamp timestamp;
    String data;
    String previousHash;
    BigInteger nonce;
    int difficulty;
    String hash;

    public Block(int index, Timestamp t, String data, int difficulty) {
        this.index = index;
        this.timestamp = t;
        this.data = data;
        this.previousHash = "";
        this.nonce = new BigInteger("0");
        this.difficulty = difficulty;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    //calculate SHA-256 hash for block
    public String calculateHash() {
        String hexaDecimalStr = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            String concatStr = index + "," + timestamp + "," + data + "," + previousHash + "," + nonce + "," + difficulty;
            md.update(concatStr.getBytes("UTF-8"));
            byte[] digest = md.digest();
            hexaDecimalStr = bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hexaDecimalStr;
    }

    //calculate proof of work for a particular block
    // pseudocode reference taken from Lecture-1
    public void proofOfWork() {
        setNonce(new BigInteger("0"));
        setHash(calculateHash());
        while (!checkLeading0(getHash())) {
            setNonce(getNonce().add(new BigInteger("1")));
            setHash(calculateHash());
        }
    }

    //check if the no of '0' in the hex string is same as or more than the difficulty
    private boolean checkLeading0(String hexStr) {
        int count = 0;
        for (char c : hexStr.toCharArray()) {
            if (c == '0')
                count++;
            else
                break;
        }
        if (count >= difficulty)
            return true;
        return false;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("index", index);
        obj.put("time stamp", timestamp.toString());
        obj.put("Tx ", data);
        obj.put("PrevHash", previousHash);
        obj.put("nonce", nonce);
        obj.put("difficulty", difficulty);
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
        // write your code here
    }

}
