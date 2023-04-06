import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends Thread {
    private Socket socket;
    private ChatServer chatServer;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            out.println("Welcome to the chat room! Please enter your username:");
            username = in.readLine();
            chatServer.broadcast(username + " has joined the chat.", this);

            String message;
            while ((message = in.readLine()) != null) {
                // Get current timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy--HH-mm");
                String timestamp = sdf.format(new Date());

                // Log message with username and timestamp
                String logMessage = timestamp + " " + message;
                chatServer.logWriter.write(logMessage + "\n");
                chatServer.logWriter.flush();

                // Broadcast message without timestamp and username to all clients
                chatServer.broadcast(message, this);
            }

            chatServer.broadcast(username + " has left the chat.", this);
            chatServer.removeClient(this);
            chatServer.logOutgoingConnection(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }
}
