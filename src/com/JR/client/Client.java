package com.JR.client;

import com.JR.Connection;
import com.JR.ConsoleHelper;
import com.JR.Message;
import com.JR.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void run() {
        String s = null;
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();


        try {
            synchronized (this) {
                wait();
            }
            if (clientConnected) {
                ConsoleHelper.writeMessage("Соединение установлено.\n" +
                        "Для выхода наберите команду 'exit'.");
            } else {
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage(e.getMessage());
            System.exit(0);
        }
        while (true) {
            s = ConsoleHelper.readString();
            if (!clientConnected || s.equals("exit")) break;
            if (shouldSendTextFromConsole())
                sendTextMessage(s);
        }

    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address: ");
        return ConsoleHelper.readString();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter user name: ");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port: ");
        return ConsoleHelper.readInt();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage(e.getMessage());
            clientConnected = false;
        }
    }






    public class SocketThread extends Thread {

        @Override
        public void run() {
            super.run();
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("%s joined the chat.", userName));
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("%s have leaved the chat.", userName));
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this)
            {
                Client.this.clientConnected=clientConnected;
                Client.this.notify();
            }

        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{

            while (true) {
                Message message = connection.receive();
                if (message.getType()==MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }
                else if (message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }
                else { throw new IOException("Unexpected MessageType");

                }
            }

        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {

            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

    }
}

