package peer;

import common.PeerInfo;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

/**
 * The DirectoryWatcher class monitors a specified directory for file creation or deletion events.
 * When a file is created or deleted in the directory, the changes are reflected by registering or deregistering the file with the central indexing server.
 */
public class DirectoryWatcher implements Runnable {
    private String directoryPath;    // Path of the directory to be watched
    private PeerClient client;       // The PeerClient instance to interact with the indexing server
    private PeerInfo peerInfo;       // Information about the peer, including ID and address

    /**
     * Constructor to initialize the DirectoryWatcher.
     *
     * @param directoryPath The path of the directory to monitor.
     * @param client The PeerClient instance that interacts with the indexing server.
     * @param peerInfo The PeerInfo instance containing peer details.
     */
    public DirectoryWatcher(String directoryPath, PeerClient client, PeerInfo peerInfo) {
        this.directoryPath = directoryPath;
        this.client = client;
        this.peerInfo = peerInfo;
    }

    /**
     * The run method starts the directory monitoring service using the WatchService API.
     * It listens for file creation or deletion events and registers/deregisters files with the indexing server accordingly.
     */
    @Override
    public void run() {
        try {
            // Create a WatchService to monitor the file system
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Convert the directory path into a Path object and register it with the WatchService
            Path path = Paths.get(directoryPath);
            path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,  // Monitor for file creation
                    StandardWatchEventKinds.ENTRY_DELETE   // Monitor for file deletion
            );

            // Start monitoring the directory for changes
            while (true) {
                // Wait for key to be signaled (blocking operation until events occur)
                WatchKey key = watchService.take();

                // Iterate through each event (file created/deleted)
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // Get the filename associated with the event
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    // Handle file creation event
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("File created: " + filename);
                        // Register the newly created file with the indexing server
                        client.registerFiles(Arrays.asList(filename.toString()));
                    }
                    // Handle file deletion event
                    else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("File deleted: " + filename);
                        // Deregister the deleted file from the indexing server
                        client.deregisterFiles(Arrays.asList(filename.toString()));
                    }
                }

                // Reset the key to receive further events
                boolean valid = key.reset();
                if (!valid) {
                    break; // Exit the loop if the key is no longer valid (i.e., directory inaccessible)
                }
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error in DirectoryWatcher:");
            e.printStackTrace();  // Log any exceptions that occur during the directory monitoring process
        }
    }
}
