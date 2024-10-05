// File: src/peer/PerformanceTester.java
package peer;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * The PerformanceTester class is responsible for testing the performance of the P2P file-sharing system.
 * It performs both sequential and concurrent search requests, logs response times, and writes the results to a CSV file.
 */
public class PerformanceTester {

    private String serverIp;          // IP address of the central indexing server
    private int serverPort;           // Port of the central indexing server
    private String filename;          // Filename to search
    private int numberOfRequests;     // Number of search requests to perform
    private int numberOfThreads;      // Number of threads for concurrent testing
    private String logFilePath;       // Path to the log file where results are saved

    // Constructor
    public PerformanceTester(String serverIp, int serverPort, String filename, int numberOfRequests, int numberOfThreads, String logFilePath) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.filename = filename;
        this.numberOfRequests = numberOfRequests;
        this.numberOfThreads = numberOfThreads;
        this.logFilePath = logFilePath;
    }

    /**
     * Perform a single search request to the indexing server and measure the response time.
     *
     * @return Response time in milliseconds, or -1 if the search fails.
     */
    public double performSearch() {
        double responseTime = 0.0;
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Start the timer
            long startTime = System.nanoTime();

            // Send search request to server
            out.println("SEARCH " + filename);

            // Read server response
            String response = in.readLine(); // "FOUND" or "NOT_FOUND"
            if ("FOUND".equalsIgnoreCase(response)) {
                String countStr = in.readLine(); // Number of peers found with the file
                int count = Integer.parseInt(countStr);

                // Optionally read peer details
                for (int i = 0; i < count; i++) {
                    in.readLine(); // Read peer info
                }
            }

            // End the timer
            long endTime = System.nanoTime();
            responseTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

        } catch (IOException e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();  // Log the full stack trace for better error understanding
            responseTime = -1.0;  // Return -1 to indicate failure
        }
        return responseTime;
    }

    /**
     * Perform a series of sequential search requests and record the response times.
     *
     * @return A list of response times for each search request.
     */
    public List<Double> performSequentialTests() {
        List<Double> responseTimes = new ArrayList<>();
        System.out.println("Starting Sequential Tests...");

        // Perform multiple sequential requests
        for (int i = 1; i <= numberOfRequests; i++) {
            double rt = performSearch();
            responseTimes.add(rt);
            if (rt >= 0) {
                System.out.printf("Sequential Search #%d: %.2f ms%n", i, rt);
            } else {
                System.out.printf("Sequential Search #%d: ERROR%n", i);
            }
        }
        return responseTimes;
    }

    /**
     * Perform a series of concurrent search requests using multiple threads.
     *
     * @return A list of response times for each search request.
     * @throws InterruptedException if the thread execution is interrupted.
     */
    public List<Double> performConcurrentTests() throws InterruptedException {
        List<Double> responseTimes = new CopyOnWriteArrayList<>();
        System.out.println("Starting Concurrent Tests with " + numberOfThreads + " threads...");

        // Create a thread pool for concurrent execution
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);

        // Submit multiple tasks to the thread pool
        for (int i = 0; i < numberOfRequests; i++) {
            executor.execute(() -> {
                double rt = performSearch();
                responseTimes.add(rt);
                if (rt >= 0) {
                    System.out.printf("Concurrent Search: %.2f ms%n", rt);
                } else {
                    System.out.println("Concurrent Search: ERROR");
                }
                latch.countDown(); // Decrease the latch count
            });
        }

        latch.await(); // Wait for all tasks to finish
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        return responseTimes;
    }

    /**
     * Log the response times to a CSV file.
     *
     * @param responseTimes List of response times.
     * @param testType Type of test (e.g., Sequential, Concurrent).
     * @param threads Number of threads used for the test.
     */
    public synchronized void logResponseTimes(List<Double> responseTimes, String testType, int threads) {
        File logFile = new File(logFilePath);
        boolean fileExists = logFile.exists();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            // Write headers if file does not exist
            if (!fileExists) {
                writer.println("N,Timestamp,TestType,Threads,ResponseTime");
            }

            // Log each response time
            for (Double rt : responseTimes) {
                String timestamp = LocalDateTime.now().format(formatter);
                if (rt >= 0) {
                    writer.printf("%d,%s,%s,%d,%.2f%n", threads, timestamp, testType, threads, rt);
                } else {
                    writer.printf("%d,%s,%s,%d,ERROR%n", threads, timestamp, testType, threads);
                }
            }
            System.out.println("Logged " + responseTimes.size() + " " + testType + " response times.");
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Main method to run performance tests using sequential and concurrent requests.
     *
     * @param args Command-line arguments for configuring the test.
     */
    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: java peer.PerformanceTester <serverIp> <serverPort> <filename> <sequentialRequests> <concurrentRequestsPerThread> <logFilePath>");
            System.exit(1);
        }

        // Parse command-line arguments
        String serverIp = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String filename = args[2];
        int sequentialRequests = Integer.parseInt(args[3]);
        int concurrentRequestsPerThread = Integer.parseInt(args[4]);
        String logFilePath = args[5];

        // Define thread pool sizes for concurrent testing
        int[] concurrentThreadPools = {5, 10, 20, 50};

        // Perform Sequential Tests
        PerformanceTester tester = new PerformanceTester(serverIp, serverPort, filename, sequentialRequests, 1, logFilePath);
        List<Double> seqResponseTimes = tester.performSequentialTests();
        tester.logResponseTimes(seqResponseTimes, "Sequential", 1);

        // Perform Concurrent Tests for different thread pool sizes
        for (int threads : concurrentThreadPools) {
            int totalConcurrentRequests = threads * concurrentRequestsPerThread;
            PerformanceTester concurrentTester = new PerformanceTester(serverIp, serverPort, filename, totalConcurrentRequests, threads, logFilePath);
            try {
                List<Double> concResponseTimes = concurrentTester.performConcurrentTests();
                concurrentTester.logResponseTimes(concResponseTimes, "Concurrent", threads);
            } catch (InterruptedException e) {
                System.err.println("Concurrent testing interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Performance Testing Completed. Check " + logFilePath + " for results.");
    }
}
