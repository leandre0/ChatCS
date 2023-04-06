import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final String SERVER_HOST = "127.0.0.1"; // Server IP
    private static final int SERVER_PORT = 5000; // Server port

    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            // Set up input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Request and send the username to the server
            System.out.print("Enter your username: ");
            String username = console.readLine();
            out.println(username);

            // Start a separate thread to listen for incoming messages from the server
            Thread receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message); // Print received messages to console
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            receiveThread.start();

            // Main thread for sending messages to the server
            String input;
            while ((input = console.readLine()) != null) {
                out.println(input); // Send input messages to the server
            }

            socket.close(); // Close the socket when done
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
