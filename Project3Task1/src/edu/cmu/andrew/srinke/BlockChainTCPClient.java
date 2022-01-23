//        Name: Sanjana Rinke
//        Andrew ID: srinke
//        Email: srinke@andrew.cmu.edu
//        Project 3-Task 1
//        This is a TCP Client which takes input from user and sends data to server for block-chain operations
//        All the execution times are calculated on server and sent back to client
//        External jar file is used for JSON.
//        It was added in Intellij as File->Project Structure->Libraries-> Add-> jar_files.jar

package edu.cmu.andrew.srinke;


import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class BlockChainTCPClient {

    //socket
    Socket clientSocket = null;

    public static void main(String[] args) {
        BlockChainTCPClient blockChainTCPClient = new BlockChainTCPClient();
        //initialize sockets
        blockChainTCPClient.init();
        try {
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
            JSONObject request = new JSONObject();
            JSONObject response;
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
                        //create request object
                        request.put("choice", 0);
                        //send data to server
                        blockChainTCPClient.send(request.toString());
                        //receive data from server and parse it
                        response = blockChainTCPClient.receiveServerData();
                        System.out.println("Current size of chain: " + response.get("chainSize"));
                        System.out.println("Difficulty of most recent block: " + response.get("recentDiff"));
                        System.out.println("Total difficulty for all blocks: " + response.get("totDiff"));
                        System.out.println("Approximate hashes per second on this machine: " + response.get("approxHash"));
                        System.out.println("Expected total hashes required for the whole chain: " + response.get("expectedHash"));
                        System.out.println("Nonce for most recent block: " + response.get("nonce"));
                        System.out.println("Chain hash: " + response.get("chainHash"));
                        break;
                    case 1:
                        //create request object
                        request.put("choice", 1);
                        System.out.println("Enter Difficulty>0");
                        int diff = Integer.parseInt(typed.readLine());
                        request.put("difficulty", diff);
                        System.out.println("Enter the transaction");
                        String transaction = typed.readLine();
                        request.put("data", transaction);
                        //send data to server
                        blockChainTCPClient.send(request.toString());
                        //receive data from server and parse it
                        response = blockChainTCPClient.receiveServerData();
                        System.out.println("Time taken to add this block::" + response.get("addTime") + " ms");
                        break;
                    case 2:
                        //create request object
                        request.put("choice", 2);
                        //send data to server
                        blockChainTCPClient.send(request.toString());
                        //receive data from server and parse it
                        response = blockChainTCPClient.receiveServerData();
                        if (response.get("result").toString().equalsIgnoreCase("true")) {
                            System.out.print("Chain verification: true");
                        } else {
                            System.out.println("Chain verification: false");
                            System.out.print("..Improper hash on node " + response.get("corruptNode") + ". Does not begin with ");
                            for (int i = 0; i < Integer.parseInt(response.get("corruptNodeDiff").toString()); i++) {
                                System.out.print("0");
                            }
                        }
                        System.out.println("\nVerification took " + response.get("verificationTime") + " ms");
                        break;
                    case 3:
                        //create request object
                        request.put("choice", 3);
                        //send data to server
                        blockChainTCPClient.send(request.toString());
                        //receive data from server and parse it
                        response = blockChainTCPClient.receiveServerData();
                        JSONObject json=new JSONObject(response.get("blockChain").toString());
                        System.out.println(json.toString(4));
                        break;
                    case 4:
                        //create request object
                        request.put("choice", 4);
                        System.out.println("Enter the id to corrupt");
                        int id = Integer.parseInt(typed.readLine());
                        request.put("corruptID", id);
                        System.out.println("Enter the data");
                        String newData = typed.readLine();
                        request.put("newData", newData);
                        //send data to server
                        blockChainTCPClient.send(request.toString());
                        //receive data from server and parse it
                        response = blockChainTCPClient.receiveServerData();
                        System.out.println("The block "+id+" now holds " + response.get("newData"));
                        break;
                    case 5:
                        //create request object
                        request.put("choice", 5);
                        System.out.println("Repairing the entire chain");
                        //send data to server
                        blockChainTCPClient.send(request.toString());
                        //receive data from server and parse it
                        response = blockChainTCPClient.receiveServerData();
                        System.out.println("Repairing took " + (response.get("repairTime")) + " ms");
                        break;
                    case 6:
                        System.out.println("Exiting!");
                        System.exit(0);
                        break;
                }


            } while (ch != 6);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            blockChainTCPClient.close();
        }
    }

    //receive JSON data from server
    public JSONObject receiveServerData() {
        BufferedReader in = null;
        JSONObject response = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String data = in.readLine();
            response = new JSONObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    //initialize socket
    private void init() {
        int serverPort = 7777;
        try {
            clientSocket = new Socket("localhost", serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //send data to server
    private void send(String message) {

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //close socket
    private void close() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // ignore exception on close
        }
    }
}
