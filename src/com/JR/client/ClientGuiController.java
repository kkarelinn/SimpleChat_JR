package com.JR.client;

public class ClientGuiController extends Client{
    private final ClientGuiModel model = new ClientGuiModel();
    private final ClientGuiView view = new ClientGuiView(this);

    public static void main(String[] args) {
        ClientGuiController controller = new ClientGuiController();
        controller.run();
    }

    public ClientGuiModel getModel(){
        return model;
    }

    @Override
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.run();
    }

    @Override
    protected String getUserName() {
        return view.getUserName();
    }

    @Override
    protected int getServerPort() {
        return view.getServerPort();
    }

    @Override
    protected String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    public class GuiSocketThread extends SocketThread{

        @Override
        protected void processIncomingMessage(String message) {
           model.setNewMessage(message);
           view.refreshMessages();
        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            model.addUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
           view.notifyConnectionStatusChanged(clientConnected);
        }
    }
}
