// File: src/peer/PeerServer3.java
package peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * PeerServer3 handles incoming file requests from other peers.
 * This class represents Peer3's server, which listens for file requests on a specified port.
 * When a request is received, it delegates the request to a handler in a separate thread.
 */
public class PeerServer3 {
    public static void main(String[] args) {
        int port = 6002; // Unique port for Peer3 to listen on
        String sharedDirectory = "C:\\Users\\dattu\\P2PFileSharing\\shared_peer3"; // Directory containing files shared by Peer3

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Log the start of Peer3's server
            System.out.println("PeerServer3 started on port " + port);

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
            // Log any errors that occur during Peer3's server operation
            System.err.println("Error in PeerServer3:");
            e.printStackTrace();
        }
    }
}
