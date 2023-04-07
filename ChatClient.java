import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClient(String serverHost, int serverPort) {
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String username;
            while (true) {
                System.out.print("Enter your username (max 20 characters): ");
                username = userInput.readLine(); // Get username from user
                if (username.trim().isEmpty()) {
                    System.out.println("Username cannot be blank.");
                } else if (username.length() > 20) {
                    System.out.println("Username exceeds 20 characters. Please enter a valid username.");
                } else {
                    break;
                }
            }

            while (true) {
                try {
                    socket = new Socket(serverHost, serverPort);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    System.out.println("Connected to server: " + serverHost + ":" + serverPort);
                    out.println(username); // Send username to server
                    break; // break out of the loop if connection is successful
                } catch (IOException e) {
                    System.out.println("Failed to connect to server. Retrying in 5 seconds...");
                    try {
                        Thread.sleep(5000); // wait for 5 seconds before retrying
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            // Start a separate thread to receive messages from the server
            Thread receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String message;
                    try {
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            receiveThread.start();

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String userMessage;
            while (true) {
                userMessage = userInput.readLine();
                if (userMessage.equalsIgnoreCase("exit")) {
                    break;
                } else if (userMessage.trim().isEmpty()) {
                    System.out.println("Message cannot be blank.");
                } else {
                    out.println(userMessage);
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverHost = "192.168.1.140";
        int serverPort = 5555;

        ChatClient chatClient = new ChatClient(serverHost, serverPort);
        chatClient.start();
    }
}
