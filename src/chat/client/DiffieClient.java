package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import chat.net.Connection;

public class DiffieClient {
    private Connection server;

    public static void main(String args[]) {
        DiffieClient client = new DiffieClient("192.168.1.139", 5051);
        client.start();
    }

    public DiffieClient(String hostname, int port) {
        try {
            server = new Connection(new Socket(hostname, port));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        server.getKey();
        BroadcastListener bcl = new BroadcastListener();
        bcl.start();

        BufferedReader sysin = new BufferedReader(new InputStreamReader(
                System.in));
        while (true) {
            try {
                server.sendMessage(sysin.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private class BroadcastListener extends Thread {
        public void run() {
            try {
                while (true) {
                    if (server.hasMessage()) {
                        System.out.println(server.getMessage());
                    }
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
