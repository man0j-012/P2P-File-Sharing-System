// File: src/common/PeerInfo.java
package common;

/**
 * The PeerInfo class represents a peer in the P2P system, storing its details
 * such as the peer's ID, IP address, port, and bandwidth.
 * This class is used to keep track of peer information when peers register with the Indexing Server.
 */
public class PeerInfo {
    private String peerId; // Unique identifier for the peer
    private String ipAddress; // IP address of the peer
    private int port; // Port on which the peer is listening for file requests
    private double bandwidth; // Bandwidth available to the peer, used for downloading files

    /**
     * Constructor to initialize PeerInfo with the provided peer details.
     * @param peerId - Unique identifier for the peer.
     * @param ipAddress - The IP address of the peer.
     * @param port - The port number the peer uses to listen for requests.
     * @param bandwidth - The available bandwidth for this peer.
     */
    public PeerInfo(String peerId, String ipAddress, int port, double bandwidth) {
        this.peerId = peerId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.bandwidth = bandwidth;
    }

    // Getters for each field to allow access to the peer's details
    public String getPeerId() {
        return peerId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Overrides the equals method to compare peers based on their peerId.
     * This ensures that peers are considered equal if they have the same peerId.
     * @param o - The object to compare with the current PeerInfo object.
     * @return boolean - True if the peerId matches, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerInfo peerInfo = (PeerInfo) o;

        return peerId.equals(peerInfo.peerId); // Peers are considered equal if their peerId matches
    }

    /**
     * Overrides the hashCode method to generate a hash based on the peerId.
     * This is important when PeerInfo is used in collections like HashSet or HashMap.
     * @return int - The hash code of the peerId.
     */
    @Override
    public int hashCode() {
        return peerId.hashCode(); // Generate hash code based on peerId
    }
}
