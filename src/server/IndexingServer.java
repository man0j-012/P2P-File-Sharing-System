// File: src/server/IndexingServer.java
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The IndexingServer class is responsible for managing the central indexing server
 * that peers communicate with to register, search, and deregister files.
 * This server listens for incoming connections from peers and processes their requests.
 */
public class IndexingServer {
    public static void main(String[] args) {
        int port = 5000; // Central server port that peers will connect to

        // Try-with-resources block to automatically close the server socket when done
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Indexing Server started on port " + port);

            // The server will continuously run, accepting client connections
            while (true) {
                // Accept incoming peer connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Each client connection is handled by a separate thread using ClientHandler
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread handlerThread = new Thread(handler);
                handlerThread.start(); // Start a new thread to handle the connection
            }
        } catch (IOException e) {
            // In case of an error during server startup or while accepting clients
            System.err.println("Error in Indexing Server:");
            e.printStackTrace();
        }
    }
}
