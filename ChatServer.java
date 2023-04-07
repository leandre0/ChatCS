import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatServer {
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;
    private FileWriter logWriter;

    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy--HH-mm");
        String currentDateAndTime = sdf.format(new Date());
        String logFileName = currentDateAndTime + "-log.txt";
        logWriter = new FileWriter(logFileName, true);
    }

    public void start() {
        System.out.println("Server started on port " + serverSocket.getLocalPort());

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New connection: " + socket);
                logOutgoingConnection(socket);
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                clientHandler.start();

                // Broadcast message to all clients that a new client has connected
                String message = "New client connected : ";
                broadcast(message, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String logMessage = currentDateAndTime + " - " + message + "\n";
        System.out.print(logMessage);
        logToFile(logMessage);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);

        // Broadcast message to all clients that a client has disconnected
        String message = "Client disconnected: " + client.getUsername();
        broadcast(message, null);
    }

    public void logOutgoingConnection(Socket socket) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String logMessage = currentDateAndTime + " - Outgoing connection: " + socket + "\n";
        System.out.print(logMessage);
        logToFile(logMessage);
    }

    public void logToFile(String message) {
        try {
            logWriter.write(message);
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
        logWriter.close();
    }

    public static void main(String[] args) {
        int port = 5555;
        try {
            ChatServer chatServer = new ChatServer(port);
            chatServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logIncomingConnection(Socket socket, String username) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String logMessage = currentDateAndTime + " - Incoming connection: " + socket + " - " + username + "\n";
        System.out.print(logMessage);
        logToFile(logMessage);
    }
}
