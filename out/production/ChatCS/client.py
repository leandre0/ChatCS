import socket
from threading import Thread

class ChatClient:
    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.username = None

    def connect(self, host, port):
        self.sock.connect((host, port))

    def send_username(self, username):
        self.sock.sendall(f"{username}\n".encode())
        response = self.sock.recv(1024).decode().strip()
        if response == "NAMEACCEPTED":
            self.username = username
            print("You are now connected to the chat server.")
            self.start_receiving_messages()
        elif response == "INVALIDNAME":
            print(f"The name '{username}' is already taken. Please choose a different name.")
            self.disconnect()

    def start_receiving_messages(self):
        while True:
            message = self.sock.recv(1024).decode().strip()
            if message.startswith("MESSAGE"):
                print(message[8:])
            elif message.startswith("QUIT"):
                self.disconnect()
                break

    def send_message(self, message):
        if self.username:
            self.sock.sendall(f"{message}\n".encode())

    def disconnect(self):
        self.sock.sendall(b"QUIT\n")
        self.sock.close()

if __name__ == "__main__":
    client = ChatClient()
    host = input("Enter the server's hostname or IP address: ")
    port = int(input("Enter the server's port number: "))
    client.connect(host, port)
    username = input("Enter your username: ")
    client.send_username(username)

    while True:
        message = input("> ")
        if message == "/quit":
            client.disconnect()
            break
        elif message:
            client.send_message(message)
