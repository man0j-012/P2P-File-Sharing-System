// File: src/peer/PeerClientMain2.java
package peer;

import common.PeerInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Main class to run Peer2's client operations.
 * This class allows Peer2 to register files, search and download files, and deregister from the indexing server.
 */
public class PeerClientMain2 {
    public static void main(String[] args) {
        // Set up the server IP and port where the central indexing server is running
        String serverIp = "localhost";  // The indexing server is running locally
        int serverPort = 5000;          // Port for the central indexing server

        // Set up peer-specific information for Peer2
        String peerId = "Peer2";        // Unique identifier for this peer
        String ipAddress = "localhost"; // IP address for Peer2
        int peerPort = 6001;            // Port for Peer2's server
        double bandwidth = 100.0;       // Bandwidth for this peer

        // Create a PeerInfo object for Peer2
        PeerInfo peerInfo = new PeerInfo(peerId, ipAddress, peerPort, bandwidth);
        // Instantiate PeerClient to handle communication with the central server
        PeerClient client = new PeerClient(serverIp, serverPort, peerInfo);

        // List of files to be registered by Peer2
        List<String> filesToRegister = Arrays.asList(
                "file1.txt", "file3.txt", "file4.txt", "file5.txt",
                "file6.txt", "file7.txt", "file8.txt", "file9.txt",
                "file10.txt", "file11.txt", "file12.txt"
        );
        // Register the files with the central indexing server
        client.registerFiles(filesToRegister);

        // Start DirectoryWatcher to monitor file changes in Peer2's shared directory
        String sharedDirectory = "C:\\Users\\dattu\\P2PFileSharing\\shared_peer2";
        DirectoryWatcher watcher = new DirectoryWatcher(sharedDirectory, client, peerInfo);
        // Run the directory watcher in a separate thread
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();

        // Interactive Command Loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Display menu options to the user
            System.out.println("\nChoose an action:");
            System.out.println("1. Search and Download a File");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");

            // Read and parse user input for the menu choice
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine()); // Parse input as an integer
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;  // Prompt the user again if input is invalid
            }

            // Handle user's choice
            if (choice == 1) {
                // User chooses to search and download a file
                System.out.print("Enter filename to search: ");
                String filename = scanner.nextLine().trim();
                if (!filename.isEmpty()) {
                    // Search for and download the file if filename is provided
                    client.searchAndDownload(filename);
                } else {
                    System.out.println("Filename cannot be empty.");
                }
            } else if (choice == 2) {
                // User chooses to exit the program
                // Deregister all files before exiting
                client.deregisterFiles(filesToRegister);
                System.out.println("Exiting...");
                System.exit(0);  // Exit the program
            } else {
                System.out.println("Invalid choice.");  // Inform user of invalid menu option
            }
        }
    }
}
