package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import chat.net.Connection;

public class DiffieServer {
    private ServerSocket ssock;
    private ArrayList<Connection> clients;

    public static void main(String args[]) {
        DiffieServer server = new DiffieServer(5050);
        server.start();
    }

    public DiffieServer(int port) {
        try {
            ssock = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        System.out.println("Server started...");

        clients = new ArrayList<Connection>();
        BroadcastThread bct = new BroadcastThread();
        bct.start();

        try {
            while (true) {
                Socket csock = ssock.accept();
                Connection newClient = new Connection(csock);
                newClient.sendKey();
                clients.add(newClient);
                System.out.println("** New Client: " + csock.toString()); 
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private class BroadcastThread extends Thread {
        public void run() {
            while (true) {
                try {
                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).hasMessage()) {
                            String message = clients.get(i).getMessage();
                            broadcastMessage(message);
                            System.out.println(message);
                        }
                    }
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        private void broadcastMessage(String message) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).sendMessage(message);
            }
        }
    }
}
