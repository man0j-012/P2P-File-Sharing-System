// File: src/server/ClientHandler.java
package server;

import common.PeerInfo;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles communication with a connected peer.
 * Manages registration, search, and deregistration of files.
 * Implements the Runnable interface to allow handling each peer in a separate thread.
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;

    // Maps to store peer information and the files they share
    private static Map<String, PeerInfo> peerMap = new ConcurrentHashMap<>(); // Maps peer ID to PeerInfo
    private static Map<String, Set<PeerInfo>> fileMap = new ConcurrentHashMap<>(); // Maps file name to a set of peers sharing that file

    /**
     * Constructor that initializes the ClientHandler with a connected client socket.
     * @param clientSocket - The socket representing the connection to the peer.
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * The main run method that listens for requests from the peer and processes them.
     * Handles registration, search, and deregistration commands.
     */
    @Override
    public void run() {
        try (
                // Input and Output streams for communication with the peer
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()), true);
        ) {
            String request;
            // Continuously listen for incoming requests from the peer
            while ((request = in.readLine()) != null) {
                System.out.println("Received request: " + request);
                String[] parts = request.split(" "); // Split the request into parts
                String command = parts[0]; // The first part is the command

                // Handle different commands (REGISTER, SEARCH, DEREGISTER)
                switch (command.toUpperCase()) {
                    case "REGISTER":
                        handleRegister(parts, out); // Handle peer registration
                        break;
                    case "SEARCH":
                        handleSearch(parts, out); // Handle file search requests
                        break;
                    case "DEREGISTER":
                        handleDeregister(parts, out); // Handle peer deregistration
                        break;
                    default:
                        out.println("INVALID_COMMAND"); // Send error if the command is not recognized
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client:");
            e.printStackTrace();
        } finally {
            // Close the client connection once the session is over
            try {
                clientSocket.close();
                System.out.println("Closed connection with client.");
            } catch (IOException e) {
                System.err.println("Error closing client socket:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle peer registration, including registering the peer's files.
     * @param parts - The parts of the registration command.
     * @param out - Output stream to send responses to the peer.
     */
    private void handleRegister(String[] parts, PrintWriter out) {
        if (parts.length < 5) { // Ensure the command has enough arguments
            out.println("REGISTER_FAILURE Invalid number of arguments.");
            return;
        }

        String peerId = parts[1]; // Peer ID
        String ipAddress = parts[2]; // Peer IP address
        int port = Integer.parseInt(parts[3]); // Peer port number
        double bandwidth = Double.parseDouble(parts[4]); // Peer bandwidth

        // Create a new PeerInfo object and add it to the peerMap
        PeerInfo peerInfo = new PeerInfo(peerId, ipAddress, port, bandwidth);
        peerMap.put(peerId, peerInfo);
        System.out.println("Registered peer: " + peerId);

        // Register each file shared by the peer
        for (int i = 5; i < parts.length; i++) {
            String filename = parts[i];
            fileMap.putIfAbsent(filename, ConcurrentHashMap.newKeySet());
            fileMap.get(filename).add(peerInfo); // Add the peer to the set of peers sharing the file
            System.out.println("Registered file '" + filename + "' for peer " + peerId);
        }

        out.println("REGISTER_SUCCESS"); // Acknowledge successful registration
    }

    /**
     * Handle a search request from a peer.
     * @param parts - The parts of the search command.
     * @param out - Output stream to send responses to the peer.
     */
    private void handleSearch(String[] parts, PrintWriter out) {
        if (parts.length != 2) { // Ensure the search command has a file name
            out.println("SEARCH_FAILURE Invalid number of arguments.");
            return;
        }

        String filename = parts[1]; // The file being searched for
        Set<PeerInfo> peersWithFile = fileMap.get(filename); // Retrieve the peers who have this file

        if (peersWithFile == null || peersWithFile.isEmpty()) {
            out.println("SEARCH_RESULTS 0"); // No peers have the requested file
            return;
        }

        out.println("SEARCH_RESULTS " + peersWithFile.size()); // Send the number of peers found
        for (PeerInfo peer : peersWithFile) {
            // Send details of each peer who has the file
            out.println(peer.getPeerId() + " " + peer.getIpAddress() + " " + peer.getPort() + " " + peer.getBandwidth());
        }
    }

    /**
     * Handle deregistration of a peer, removing its files and information.
     * @param parts - The parts of the deregistration command.
     * @param out - Output stream to send responses to the peer.
     */
    private void handleDeregister(String[] parts, PrintWriter out) {
        if (parts.length < 2) { // Ensure the command has a peer ID
            out.println("DEREGISTER_FAILURE Invalid number of arguments.");
            return;
        }

        String peerId = parts[1]; // The peer ID to deregister
        PeerInfo peerInfo = peerMap.remove(peerId); // Remove the peer from the peerMap

        if (peerInfo == null) {
            out.println("DEREGISTER_FAILURE Peer not found.");
            return;
        }

        // Remove the peer from all file entries
        for (Set<PeerInfo> peers : fileMap.values()) {
            peers.remove(peerInfo);
        }

        System.out.println("Deregistered peer: " + peerId);
        out.println("DEREGISTER_SUCCESS"); // Acknowledge successful deregistration
    }
}
