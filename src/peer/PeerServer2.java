// File: src/peer/PeerServer2.java
package peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * PeerServer2 handles incoming file requests from other peers.
 * This class represents Peer2's server, which listens for file requests on a specified port.
 * When a request is received, the server delegates the request to a handler in a separate thread.
 */
public class PeerServer2 {
    public static void main(String[] args) {
        int port = 6001; // Unique port for Peer2 to listen on
        String sharedDirectory = "C:\\Users\\dattu\\P2PFileSharing\\shared_peer2"; // Directory containing files shared by Peer2

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Log the start of Peer2's server
            System.out.println("PeerServer2 started on port " + port);

            // Continuously listen for incoming file requests
            while (true) {
                // Accept incoming connections from other peers
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Create a new thread to handle each file request using the FileRequestHandler
                FileRequestHandler handler = new FileRequestHandler(clientSocket, sharedDirectory);
                Thread handlerThread = new Thread(handler);
                handlerThread.start(); // Start the thread to process the file request
            }
        } catch (IOException e) {
            // Log any errors that occur during Peer2's server operation
            System.err.println("Error in PeerServer2:");
            e.printStackTrace();
        }
    }
}
