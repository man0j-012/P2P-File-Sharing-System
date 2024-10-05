P2P File Sharing System
This project implements a simple Peer-to-Peer (P2P) file sharing system that includes a central indexing server and multiple peers. Peers can register files, search for files, and download files from other peers. Additionally, a performance testing module is included to evaluate the behavior of the system.

Features
Central Indexing Server:

Registers peers and their shared files.
Searches for files in the registered peers.
Deregisters peers and their files.
Peers:

Acts as both a client and a server.
Registers its files with the indexing server.
Searches for files in the indexing server and downloads them from other peers.
Monitors a directory for file changes and updates the indexing server automatically.
Directory Monitoring:

Each peer monitors a shared folder for file additions or deletions and updates the indexing server accordingly.
Performance Testing:

Measures the response time for sequential and concurrent requests to the indexing server.
Outputs response time statistics and visual plots for performance analysis.
Project Structure
bash

Requirements
Java 8+: Ensure Java is installed on your system.
IntelliJ IDEA (or any other IDE with Java support).
Git: To manage version control and push to GitHub.
Setup Instructions
Clone the Repository:

bash
Copy code
git clone https://github.com/your-username/P2PFileSharing.git
cd P2PFileSharing
Open in IntelliJ:

Import the project into IntelliJ as a Maven or Gradle project (if applicable) or as a simple Java project.
Compile the Code:

Go to the src folder and compile the files using IntelliJ's "Build" option or use the following command:
bash
Copy code
javac -d bin src/server/IndexingServer.java src/peer/PeerClient.java src/peer/PeerServer.java
How to Run the Program
Start the Indexing Server:

bash
Copy code
java -cp bin server.IndexingServer
The server listens on port 5000.

Start Peer Servers: Each peer server listens for file requests on a different port.

Peer 1:

bash
Copy code
java -cp bin peer.PeerServer
Peer 2:

bash
Copy code
java -cp bin peer.PeerServer2
Peer 3:

bash
Copy code
java -cp bin peer.PeerServer3
Start Peer Clients: Each peer client can register files and search for files on the indexing server.

Peer 1:

bash
Copy code
java -cp bin peer.PeerClientMain
Peer 2:

bash
Copy code
java -cp bin peer.PeerClientMain2
Peer 3:

bash
Copy code
java -cp bin peer.PeerClientMain3
Perform File Search and Download: In the Peer Client terminal:

Choose option 1 to search for a file.
Enter the filename, and if found, it will be downloaded from another peer.
Performance Testing
You can measure the response time for sequential and concurrent requests.

Run the Performance Tester:

bash
Copy code
java -cp bin peer.PerformanceTester localhost 5000 file1.txt 500 100 performance_log.csv
This will log the performance of 500 sequential and 100 concurrent requests.

View the Results: Check the performance_log.csv for response times. You can also generate plots using a Python script or any other plotting tool.

Example Output
mathematica
Copy code
Choose an action:
1. Search and Download a File
2. Exit
Enter choice: 1
Enter filename to search: file1.txt
Found 3 peer(s) with the file.
Downloading from Peer2 at localhost:6001
Receiving file of size 3402 bytes.
File file1.txt downloaded successfully.
Potential Improvements
Implement more sophisticated file search algorithms (e.g., partial match, fuzzy search).
Introduce peer-to-peer communication without a central server for a fully decentralized model.
Improve file transfer efficiency by adding compression or parallel downloads.
Add fault tolerance to handle peer failures during downloads.
Known Issues
If a peer fails during a download, the client doesn't automatically retry other peers with the same file.
Response times may vary significantly depending on network latency and system load.
