P2P File Sharing System
This project implements a Peer-to-Peer (P2P) file-sharing system using Java. The system comprises a central indexing server and several peer clients, each functioning as a client and a server. Peers can register files, search for files across other peers, and download files from them. The system also includes a performance tester to measure the system's behavior under different load conditions, such as sequential and concurrent file requests.

System Overview
The project consists of two main components:

Indexing Server: The central server that keeps track of all registered peers and the files they are sharing. The indexing server manages peer registration, search, and deregistration requests. Peers connect to the indexing server to register their files, search for available files, and request a list of peers with the desired file.

Peer Clients and Servers: Each peer operates as a client (which can request files from peers) and a server (which can provide files to others). Peers can:

Register the files they are sharing with the indexing server.
Search for files that other peers are sharing.
Download files from other peers that are sharing the requested file.
Each peer also includes a directory watcher service, which monitors the shared directory for file additions or deletions and updates the indexing server when changes occur.

Project Structure
The project files are organized into different directories for ease of understanding and separation of concerns. The main components of the project include:

Indexing Server: The system's core manages the directory of shared files and peers. It handles peer requests such as file registration, search, and deregistration.
Peer Clients: Each can register its shared files with the indexing server, search for files, and download them from others.
Performance Tester: This component tests the system's performance by measuring response times for sequential and concurrent requests and logging the results for analysis.
Setting Up the System
Before running the system, please ensure you have Java installed on your machine and any required development tools like IntelliJ IDEA. The project files should be opened and compiled within the IDE, ensuring all dependencies are correctly configured.

Once the project is compiled, the system can run in several steps. First, the indexing server should be started, which will begin listening for connections from peers. After the server runs, the peer clients should be launched, each of which will register their files and be ready to serve or request files.

Running the System
To use the system, the following steps are typically followed:

Start the Indexing Server: The indexing server listens for incoming peer connections and processes file registration, search, and deregistration requests.
Start the Peer Servers: Each peer acts as a server that can send files to other peers. These peer servers should be started, each on its port.
Start the Peer Clients: Peer clients connect to the indexing server, registering their shared files, searching for available files, and downloading them from peers. The peer clients also monitor their directories for any changes and keep the indexing server updated.
Once the system is running, a peer client can interact with the system by searching for files and downloading them from other peers. The peers can be started in any order, and each peer operates independently.

Performance Testing
The system includes a Performance Tester to evaluate how the system handles various loads. This includes measuring the response time for sequential file requests (one request at a time) and concurrent file requests (multiple peers requesting files simultaneously). The test results are logged in a CSV file and can be analyzed for further insights into system performance.

Testing Example
A simple test case involves starting the indexing server and launching several peers. One peer registers a file, and another searches for and downloads that file. The system prints messages indicating the progress of each operation, such as the number of peers found for a particular file and whether a file was successfully downloaded.

Known Limitations and Future Improvements
There are a few known limitations in the current implementation:

If a peer goes offline during a file download, there is no automatic retry from another peer offering the same file.
The system's performance may degrade under extremely high concurrent loads.
Potential improvements to the system include adding more advanced file search algorithms, improving fault tolerance (e.g., retries or failover strategies), and allowing peers to communicate with each other directly without relying on the central indexing server. Additionally, file transfer efficiency could be improved by introducing compression or parallel download features.

