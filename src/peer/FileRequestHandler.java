package peer;

import java.io.*;
import java.net.Socket;

/**
 * The FileRequestHandler class handles individual file requests from peers.
 * It listens for file requests, checks if the requested file exists in the peer's shared directory, and sends the file to the requesting client.
 * If the file does not exist, it informs the client that the file was not found.
 */
public class FileRequestHandler implements Runnable {
    private Socket clientSocket;      // The socket for client-server communication
    private String sharedDirectory;   // The path to the peer's shared directory

    /**
     * Constructor to initialize the FileRequestHandler with the client socket and shared directory.
     *
     * @param clientSocket The socket connection with the requesting peer.
     * @param sharedDirectory The directory where the peer's shared files are stored.
     */
    public FileRequestHandler(Socket clientSocket, String sharedDirectory) {
        this.clientSocket = clientSocket;
        this.sharedDirectory = sharedDirectory;
    }

    /**
     * The run method processes the client's file request.
     * It sends the requested file if found, or informs the client if the file is not available.
     */
    @Override
    public void run() {
        try (
                // Create input stream to read from the client and output stream to send data back
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
        ) {
            // Read the requested filename from the client
            String requestedFile = in.readLine();  // Read the file request sent by the client
            System.out.println("Client requested file: " + requestedFile);

            // Check if the file exists in the shared directory
            File file = new File(sharedDirectory, requestedFile);
            if (file.exists() && !file.isDirectory()) {
                // If the file exists, inform the client
                out.write("FOUND\n".getBytes());
                out.flush();

                // Send the file size to the client
                long fileSize = file.length();
                out.write((fileSize + "\n").getBytes());
                out.flush();

                // Send the file contents to the client
                FileInputStream fileIn = new FileInputStream(file);
                byte[] buffer = new byte[4096];  // Buffer to hold file chunks
                int bytesRead;

                // Read the file in chunks and send it to the client
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                fileIn.close();
                System.out.println("File " + requestedFile + " sent to client.");
            } else {
                // If the file does not exist, inform the client
                out.write("NOT_FOUND\n".getBytes());
                out.flush();
                System.out.println("File " + requestedFile + " not found.");
            }
        } catch (IOException e) {
            System.err.println("Error handling file request:");
            e.printStackTrace();  // Log the exception stack trace to identify the issue
        } finally {
            // Ensure that the client socket is closed after the request is processed
            try {
                clientSocket.close();
                System.out.println("Closed connection with client.");
            } catch (IOException e) {
                System.err.println("Error closing client socket:");
                e.printStackTrace();
            }
        }
    }
}
