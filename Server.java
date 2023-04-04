import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static final String USER_FILE = "users.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        int port = 5400;

        // Load the list of users from the user file
        List<String> users;
        try {
            users = Files.readAllLines(Paths.get(USER_FILE));
        } catch (IOException ex) {
            System.err.println("Error loading user file: " + ex.getMessage());
            return;
        }

        // Start the server socket and listen for connections
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress().getHostAddress());
                new ClientThread(socket, users).start();
            }
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private static class ClientThread extends Thread {
        private Socket socket;
        private List<String> users;
        private String username;
        private BufferedReader input;
        private PrintWriter output;

        public ClientThread(Socket socket, List<String> users) {
            this.socket = socket;
            this.users = users;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ex) {
                System.err.println("Error creating input/output streams: " + ex.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // Get the client's username
                while (true) {
                    output.println("Enter your username:");
                    String username = input.readLine().trim();
                    if (username.isEmpty()) {
                        continue;
                    }
                    synchronized (users) {
                        if (!users.contains(username)) {
                            users.add(username);
                            output.println("NAMEACCEPTED");
                            this.username = username;
                            System.out.println(username + " connected.");
                            Files.write(Paths.get(USER_FILE), users);
                            break;
                        } else {
                            output.println("INVALIDNAME");
                            System.out.println(username + " tried to connect with invalid name.");
                        }
                    }
                }

                // Handle messages from the client
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    String timestamp = DATE_FORMAT.format(new Date());
                    String message = "[" + timestamp + "] " + username + ": " + inputLine;
                    System.out.println(message);
                    Files.write(Paths.get("messages.txt"), Collections.singletonList(message), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                }

            } catch (IOException ex) {
                System.err.println("Error handling client input: " + ex.getMessage());
            } finally {
                // Remove the client's username from the list of users
                synchronized (users) {
                    if (username != null) {
                        users.remove(username);
                        System.out.println(username + " disconnected.");
                        try {
                            Files.write(Paths.get(USER_FILE), users);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
