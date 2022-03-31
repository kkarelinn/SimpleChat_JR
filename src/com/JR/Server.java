package com.JR;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Enter the server port");
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = new ServerSocket(port);
        try {
            if (serverSocket.isBound()) ConsoleHelper.writeMessage("Server is running...");
            while (true) {
                Socket client = serverSocket.accept();
                Handler handler = new Handler(client);
                handler.start();
            }
        } catch (Exception e) {
            serverSocket.close();
            ConsoleHelper.writeMessage(e.getMessage());
        }

    }


    public static void sendBroadcastMessage(Message message) {
        for (Connection conn : connectionMap.values()) {
            try {
                conn.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage(e.getMessage());
            }
        }
    }


    private static class Handler extends Thread {
        private final Socket socket;

        private Handler(Socket socket) {
            this.socket = socket;
        }

        //send text message FROM CLIENT to all another users in chat
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {


            while (true) {
                Message mess = connection.receive();
                Message newMess = null;

                if (mess.getType() == MessageType.TEXT) {
                    newMess = new Message(MessageType.TEXT, userName + ": " + mess.getData());
                    sendBroadcastMessage(newMess);
                } else {
                    ConsoleHelper.writeMessage("It was not text Message");
                }
            }
        }

        //notify new User about all another users in chat
        private void notifyUsers(Connection connection, String userName) throws IOException {
            String clientName = null;
            Message mess = null;

            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                clientName = entry.getKey();
                mess = new Message(MessageType.USER_ADDED, clientName);
                if (!clientName.equals(userName))
                    connection.send(mess);
            }
        }

        //"рукопожатие" (знакомство сервера с клиентом)
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message answer = null;
            String clientName = null;

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                answer = connection.receive();
                if (answer.getType() != MessageType.USER_NAME) continue;
                clientName = answer.getData();
                if (clientName.isEmpty()) continue;
                if (connectionMap.containsKey(clientName)) continue;
                connectionMap.putIfAbsent(clientName, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return clientName;
            }
        }

        @Override
        public void run() {
            super.run();
            String client = null;
            Connection connection = null;
            String address = socket.getLocalSocketAddress().toString();// getRemoteSocketAddress().toString();

            try {
                connection = new Connection(socket);
                client = serverHandshake(connection);
                ConsoleHelper.writeMessage(connection.getRemoteSocketAddress() + " named "+ client + " have connected to current SERVER");
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, client));
                notifyUsers(connection, client);
                serverMainLoop(connection, client);
                connectionMap.remove(client);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, client));
                ;
                ConsoleHelper.writeMessage("Connection to address " + address + " was closed!");


            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Some error was happened with SERVER ");
            }


        }
    }
}
