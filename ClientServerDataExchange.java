/*
Implement Client-Server Data Exchange
This implementation uses Java Sockets to manage the TCP connections. The server is designed to handle multiple clients using a new thread for each incoming connection, allowing simultaneous handling of requests.

Server Implementation
The server listens on a specified port and handles each client in a separate thread, enabling it to respond to multiple clients simultaneously. If an error occurs, it logs the error but continues accepting new connections.

Server Handling Multiple Clients Simultaneously: The PingPongServer uses ServerSocket to listen for incoming connections. For each connection, it creates a ClientHandler in a new thread, allowing concurrent client handling. This multithreading approach is suitable for TCP-based connection-oriented communication, as it allows the server to manage multiple clients without blocking.

Suitability of TCP for the Task: 

Protocol Choice: TCP is ideal for this task since it provides reliable, ordered, and connection-oriented communication, ensuring that every "ping" from the client gets a "pong" response from the server if the connection remains intact.
Connection-Oriented: TCP maintains a persistent connection between the server and client, which is necessary to support the continuous "ping" messaging cycle.
Network Error Handling:

Server: If an error occurs while handling a client (e.g., client disconnects unexpectedly), the server catches the exception, logs it, and closes the connection gracefully.
Client: If the client encounters an error during communication (e.g., if the server is unreachable), it logs the error and exits the loop, stopping further messages.
*/
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }
       
        int port = Integer.parseInt(args[0]);
       
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
           
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
               
                // Each client connection is handled in a new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = input.readLine()) != null) {
                if ("ping".equalsIgnoreCase(message.trim())) {
                    output.println("pong");
                } else {
                    System.out.println("Received unknown message: " + message);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}

/*
Client Implementation
The client connects to the server, then sends "ping" messages every second, printing "pong" when it receives a response. If a network error occurs, the client logs it and stops sending messages.
*/

import java.io.BufferedReader;  
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(host, port)) {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to the server at " + host + ":" + port);

            // Loop to send "ping" messages every second
            while (true) {
                output.println("ping");
                System.out.println("Client sent: ping");

                String response = input.readLine();
                if (response != null && response.equals("pong")) {
                    System.out.println("Client received: pong");
                }

                // Wait for 1 second before sending the next "ping"
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}