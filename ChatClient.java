import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClient(String serverHost, int serverPort) {
        try {
            socket = new Socket(serverHost, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            out.println(username);

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

            String userMessage;
            while (true) {
                userMessage = userInput.readLine();
                if (userMessage.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(userMessage);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 5555;

        ChatClient chatClient = new ChatClient(serverHost, serverPort);
        chatClient.start();
    }
}
