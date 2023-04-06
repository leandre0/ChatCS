import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatServer {
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;
    FileWriter logWriter;

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());
        String logMessage = currentDateAndTime + " - " + sender.getUsername() + ": " + message + "\n";
        System.out.print(logMessage);
        logToFile(logMessage);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(sender.getUsername() + ": " + message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
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
}
