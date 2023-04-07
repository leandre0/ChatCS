import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends Thread {
    private Socket socket;
    private ChatServer chatServer;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket, ChatServer chatServer) throws IOException {
        this.socket = socket;
        this.chatServer = chatServer;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            // Read username from client
            username = reader.readLine();
            System.out.println("Client connected with username: " + username);
            chatServer.logIncomingConnection(socket, username);

            // Send welcome message to client
            writer.println("Welcome to the chat room, " + username + "!");

            String message;
            while ((message = reader.readLine()) != null) {
                // Broadcast message to all clients
                chatServer.broadcast(username + ": " + message, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Remove client from chat server's client list and close resources
            chatServer.removeClient(this);
            try {
                reader.close();
                writer.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}
