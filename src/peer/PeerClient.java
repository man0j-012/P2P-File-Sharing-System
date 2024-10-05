// File: src/peer/PeerClient.java
package peer;

import common.PeerInfo;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * PeerClient handles communication with the central indexing server.
 * It can register, search for files, download files from peers, and deregister files.
 */
public class PeerClient {
    private String serverIp;  // IP address of the indexing server
    private int serverPort;   // Port of the indexing server
    private PeerInfo peerInfo;  // Information about the current peer

    /**
     * Constructor to initialize the PeerClient with server information and peer details.
     *
     * @param serverIp   - The IP address of the indexing server.
     * @param serverPort - The port number of the indexing server.
     * @param peerInfo   - Information about the current peer (PeerInfo object).
     */
    public PeerClient(String serverIp, int serverPort, PeerInfo peerInfo) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.peerInfo = peerInfo;
    }

    /**
     * Registers a list of files with the indexing server.
     *
     * This sends the peer's information and the files it wants to share to the central server.
     *
     * @param files List of filenames to register.
     */
    public void registerFiles(List<String> files) {
        try (
                // Create a connection to the indexing server
                Socket socket = new Socket(serverIp, serverPort);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            // Create the REGISTER message format
            StringBuilder sb = new StringBuilder();
            sb.append("REGISTER ").append(peerInfo.getPeerId()).append(" ")
                    .append(peerInfo.getIpAddress()).append(" ")
                    .append(peerInfo.getPort()).append(" ")
                    .append(peerInfo.getBandwidth());

            // Append each file to the message
            for (String file : files) {
                sb.append(" ").append(file);
            }

            // Send the REGISTER message to the server
            out.println(sb.toString());
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            System.err.println("Error registering files:");
            e.printStackTrace();
        }
    }

    /**
     * Searches for a file in the indexing server and downloads it if found.
     *
     * @param filename Name of the file to search for.
     */
    public void searchAndDownload(String filename) {
        try (
                // Create a connection to the indexing server
                Socket socket = new Socket(serverIp, serverPort);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            // Send SEARCH command to the server
            out.println("SEARCH " + filename);
            String response = in.readLine();

            if (response.startsWith("SEARCH_RESULTS")) {
                // Handle the search results
                String[] parts = response.split(" ");
                int numResults = Integer.parseInt(parts[1]);

                if (numResults == 0) {
                    System.out.println("No peers have the file: " + filename);
                    return;
                }

                System.out.println("Found " + numResults + " peer(s) with the file:");

                // Iterate through peers and download the file from each one
                for (int i = 0; i < numResults; i++) {
                    String peerInfoStr = in.readLine();
                    String[] peerParts = peerInfoStr.split(" ");
                    String peerId = peerParts[0];
                    String peerIp = peerParts[1];
                    int peerPort = Integer.parseInt(peerParts[2]);
                    double peerBandwidth = Double.parseDouble(peerParts[3]);

                    // Print details of the peer
                    System.out.println("- " + peerId + " (IP: " + peerIp + ", Port: " + peerPort + ", Bandwidth: " + peerBandwidth + ")");
                    System.out.println("Downloading from " + peerId + " at " + peerIp + ":" + peerPort);

                    // Download the file from the peer
                    downloadFile(peerIp, peerPort, filename);
                }
            } else {
                System.out.println("Unexpected response: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error searching for file:");
            e.printStackTrace();
        }
    }

    /**
     * Downloads a file from a peer.
     *
     * @param peerIp    IP address of the peer.
     * @param peerPort  Port number of the peer.
     * @param filename  Name of the file to download.
     */
    private void downloadFile(String peerIp, int peerPort, String filename) {
        try (
                // Connect to the peer server
                Socket socket = new Socket(peerIp, peerPort);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream fileOut = new BufferedOutputStream(
                        new FileOutputStream("downloads\\" + filename)); // Save the file in the 'downloads' directory
        ) {
            // Request the file from the peer
            out.println(filename);
            String response = in.readLine();

            if ("FOUND".equals(response)) {
                // If the peer has the file, start receiving it
                String sizeStr = in.readLine();
                long fileSize = Long.parseLong(sizeStr);
                System.out.println("Receiving file of size " + fileSize + " bytes.");

                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalRead = 0;

                // Read the file in chunks and save it to disk
                while (totalRead < fileSize && (bytesRead = is.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }

                fileOut.flush(); // Ensure all bytes are written
                System.out.println("File " + filename + " downloaded successfully.");

                // Simulate displaying the file after download
                System.out.println("Display file '" + filename + "'");
            } else if ("NOT_FOUND".equals(response)) {
                System.out.println("Peer does not have the file: " + filename);
            } else {
                System.out.println("Unexpected response: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error downloading file:");
            e.printStackTrace();
        }
    }

    /**
     * Deregisters a list of files from the indexing server.
     *
     * @param files List of filenames to deregister.
     */
    public void deregisterFiles(List<String> files) {
        try (
                // Create a connection to the indexing server
                Socket socket = new Socket(serverIp, serverPort);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            // Create the DEREGISTER message format
            StringBuilder sb = new StringBuilder();
            sb.append("DEREGISTER ").append(peerInfo.getPeerId());

            for (String file : files) {
                sb.append(" ").append(file);
            }

            // Send the DEREGISTER message to the server
            out.println(sb.toString());
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            System.err.println("Error deregistering files:");
            e.printStackTrace();
        }
    }
}
