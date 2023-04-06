import socket
import threading
import time

# Constants
HOST = '127.0.0.1'  # Server IP
PORT = 5000         # Server port
BUFFER_SIZE = 1024  # Buffer size for receiving messages
ENCODING = 'utf-8'  # Encoding for messages


class ChatServer:
    def __init__(self):
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.clients = {}  # Stores connected clients as key-value pairs (client_socket: username)
        self.log_file = None  # Log file object

    def start(self):
        self.server_socket.bind((HOST, PORT))
        self.server_socket.listen(5)
        print('Chat server started on {}:{}'.format(HOST, PORT))

        while True:
            client_socket, client_address = self.server_socket.accept()
            print('New connection from {}:{}'.format(client_address[0], client_address[1]))
            client_thread = threading.Thread(target=self.handle_client, args=(client_socket,))
            client_thread.start()

    def handle_client(self, client_socket):
        # Send welcome message to client
        client_socket.send('Welcome to the chat! Please enter your username: '.encode(ENCODING))

        # Request and set username
        username = client_socket.recv(BUFFER_SIZE).decode(ENCODING).strip()
        self.clients[client_socket] = username

        # Broadcast user connection
        self.broadcast('{} has joined the chat.'.format(username).encode(ENCODING))

        # Log file name with timestamp
        timestamp = time.strftime('%d-%m-%Y--%H:%M', time.localtime())
        log_file_name = '{}--log.txt'.format(timestamp)
        self.log_file = open(log_file_name, 'a')

        while True:
            try:
                message = client_socket.recv(BUFFER_SIZE).decode(ENCODING).strip()
                if message:
                    # Add timestamp, username, and user IP to the message
                    timestamp = time.strftime('%d-%m-%Y %H:%M:%S', time.localtime())
                    user_ip = client_socket.getpeername()[0]
                    log_message = '[{}] {} ({}): {}'.format(timestamp, username, user_ip, message)
                    self.log_file.write(log_message + '\n')
                    self.log_file.flush()
                    broadcast_message = '[{}] {}: {}'.format(timestamp, username, message)
                    self.broadcast(broadcast_message.encode(ENCODING))
                else:
                    # Empty message indicates client has disconnected
                    raise Exception()
            except:
                # Client has disconnected
                client_socket.close()
                username = self.clients.pop(client_socket)
                self.broadcast('{} has left the chat.'.format(username).encode(ENCODING))
                print('{} has left the chat.'.format(username))
                self.log_file.close()
                break

    def broadcast(self, message):
        for client_socket in self.clients.keys():
            client_socket.send(message)
            print(message.decode(ENCODING))


if __name__ == '__main__':
    chat_server = ChatServer()
    chat_server.start()
