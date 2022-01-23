//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 3-Task 1
//        This is a TCP Server which takes data from client and performs block-chain operations
//        All the execution times are calculated on server and sent back to client
//        External jar file is used for JSON.
//        It was added in Intellij as File->Project Structure->Libraries-> Add-> jar_files.jar

package edu.cmu.andrew.srinke;


import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Scanner;

public class BlockChainTCPServer {

    Socket socket = null;
    ServerSocket listenSocket = null;
    BlockChain blockChain = new BlockChain();

    public static void main(String[] args) {
        // write your code here
        System.out.println("Server started!");
        BlockChainTCPServer blockChainTCPServer = new BlockChainTCPServer();
        //initialize sockets
        blockChainTCPServer.init();
        Scanner in;
        //add genesis block
        Block genesisBlock = new Block(0, blockChainTCPServer.blockChain.getTime(), "Genesis", 2);
        blockChainTCPServer.blockChain.addBlock(genesisBlock);
        blockChainTCPServer.blockChain.computeHashesPerSecond();
        try {
            while (true) {
                //accept connection
                blockChainTCPServer.socket = blockChainTCPServer.listenSocket.accept();
                //get input from client
                in = new Scanner(blockChainTCPServer.socket.getInputStream());
                while (in.hasNextLine()) {
                    String data = in.nextLine();
                    //send data to getClientData to process
                    blockChainTCPServer.getClientData(data);
                }
                //close socket
                blockChainTCPServer.socket.close();
            }
            // Handle exceptions
        } catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
//            // If quitting (typically by you sending quit signal) clean up sockets
        } finally {
            try {
                if (blockChainTCPServer.socket != null) {
                    blockChainTCPServer.socket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    //process the JSON data coming from client
    private void getClientData(String incomingMessage) {
        try {
            //parse incoming json request
            JSONObject request = new JSONObject(incomingMessage);
            int operation = Integer.parseInt(request.get("choice").toString());
            Timestamp startTime;
            Timestamp endTime;
            JSONObject response = new JSONObject();
            switch (operation) {
                case 0:
                    //create a response JSON object
                    System.out.println("Sending basic blockchain status");
                    response.put("chainSize", blockChain.getChainSize());
                    response.put("recentDiff", blockChain.getLatestBlock().getDifficulty());
                    response.put("totDiff", blockChain.getTotalDifficulty());
                    response.put("approxHash", blockChain.getHashesPerSecond());
                    response.put("expectedHash", blockChain.getTotalExpectedHashes());
                    response.put("nonce", blockChain.getLatestBlock().getNonce());
                    response.put("chainHash", blockChain.chainHash);
                    break;
                case 1:
                    //parse incoming request
                    System.out.println("Adding block");
                    int diff = Integer.parseInt(request.get("difficulty").toString());
                    String transaction = request.get("data").toString();
                    Block b = new Block(blockChain.getChainSize(), blockChain.getTime(), transaction, diff);
                    startTime = blockChain.getTime();
                    blockChain.addBlock(b);
                    endTime = blockChain.getTime();
                    //create a response JSON object
                    response.put("addTime", (endTime.getTime() - startTime.getTime()));
                    break;
                case 2:
                    System.out.println("Verifying blockchain");
                    startTime = blockChain.getTime();
                    if (blockChain.isChainValid() == true) {
                        //create a response JSON object
                        response.put("result", true);
                    } else {
                        //create a response JSON object
                        response.put("result", false);
                        response.put("corruptNode", blockChain.getCorruptNodeId());
                        response.put("corruptNodeDiff", blockChain.getBlock(blockChain.getCorruptNodeId()).getDifficulty());
                    }
                    endTime = blockChain.getTime();
                    response.put("verificationTime", (endTime.getTime() - startTime.getTime()));
                    break;
                case 3:
                    System.out.println("Viewing blockchain");
                    response.put("blockChain", blockChain);
                    break;
                case 4:
                    System.out.println("Corrupting blockchain");
                    //parse incoming request
                    int id = Integer.parseInt(request.get("corruptID").toString());
                    String data = request.get("newData").toString();
                    blockChain.blocks.get(id).setData(data);
                    //create a response JSON object
                    response.put("newData", blockChain.blocks.get(id).getData());
                    break;
                case 5:
                    System.out.println("Repairing blockchain");
                    startTime = blockChain.getTime();
                    blockChain.repairChain();
                    endTime = blockChain.getTime();
                    //create a response JSON object
                    response.put("repairTime", (endTime.getTime() - startTime.getTime()));
                    break;
            }
            //send data to client
            send(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //send data back to client
    private void send(String message) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //initialize socket
    private void init() {
        int serverPort = 7777;
        try {
            listenSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
