// File: src/peer/PeerClientMain.java
package peer;

import common.PeerInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Main class to run Peer1's client operations.
 * This class is responsible for registering files with the indexing server,
 * searching for and downloading files from other peers, and deregistering files.
 */
public class PeerClientMain {

    // Method to log output to both the console and a file.
    // This method prints messages to the console and also writes them to the output.txt file.
    private static void logToFile(String message) {
        // Specify the absolute path for the output file
        String filePath = "C:\\Users\\dattu\\P2PFileSharing\\output.txt";

        // Print the message to the console
        System.out.println(message);

        // Append the message to the output.txt file
        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(message);  // Write the message to the file
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Set up the server IP and port where the indexing server is running
        String serverIp = "localhost";
        int serverPort = 5000;

        // Set up peer-specific information for Peer1
        String peerId = "Peer1";      // Unique identifier for this peer
        String ipAddress = "localhost";  // IP address of this peer
        int peerPort = 6000;          // Port for Peer1's server
        double bandwidth = 100.0;     // Bandwidth for this peer

        // Create a PeerInfo object for Peer1
        PeerInfo peerInfo = new PeerInfo(peerId, ipAddress, peerPort, bandwidth);
        // Instantiate PeerClient to handle communication with the central server
        PeerClient client = new PeerClient(serverIp, serverPort, peerInfo);

        // List of files to be registered by Peer1
        List<String> filesToRegister = Arrays.asList(
                "file1.txt", "file2.txt", "file3.txt", "file4.txt",
                "file5.txt", "file6.txt", "file7.txt", "file8.txt",
                "file9.txt", "file10.txt"
        );

        // Register the files with the central indexing server and log the output
        client.registerFiles(filesToRegister);
        logToFile("Server response: REGISTER_SUCCESS");

        // Start DirectoryWatcher to monitor file changes in the shared directory
        String sharedDirectory = "C:\\Users\\dattu\\P2PFileSharing\\shared";
        DirectoryWatcher watcher = new DirectoryWatcher(sharedDirectory, client, peerInfo);
        // Run the directory watcher in a separate thread
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();

        // Interactive loop for user commands (search, download, and exit)
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Display menu options to the user
            logToFile("\nChoose an action:");
            logToFile("1. Search and Download a File");
            logToFile("2. Exit");
            logToFile("Enter choice: ");

            // Read and parse user input for the menu choice
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                logToFile("Invalid input. Please enter a number.");
                continue;  // Prompt the user again if input is invalid
            }

            // Handle user's choice
            if (choice == 1) {
                // User chooses to search and download a file
                logToFile("Enter filename to search: ");
                String filename = scanner.nextLine().trim();
                if (!filename.isEmpty()) {
                    // Search for and download the file if filename is provided
                    client.searchAndDownload(filename);
                    logToFile("Search and download for file '" + filename + "' completed.");
                } else {
                    logToFile("Filename cannot be empty.");
                }
            } else if (choice == 2) {
                // User chooses to exit the program
                // Deregister all files before exiting
                client.deregisterFiles(filesToRegister);
                logToFile("Server response: DEREGISTER_SUCCESS");
                logToFile("Exiting...");
                System.exit(0);  // Exit the program
            } else {
                logToFile("Invalid choice.");  // Inform user of invalid menu option
            }
        }
    }
}
