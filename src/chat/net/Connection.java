package chat.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Connection {
    private Socket socket;
    private BufferedReader br;
    private PrintStream ps;
    private BigInteger cipherKey;

    public Connection(Socket sck) {
        try {
            socket = sck;
            br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void sendKey() {
        try {
            // send P and Q to client
            BigInteger p = new BigInteger("5");
            BigInteger q = new BigInteger("23");
            ps.println(p.toString() + " " + q.toString() + "\r");
            /*System.out.println("--> SEND: P=" + p.toString() + " Q="
                    + q.toString());*/

            // receive public key from client
            BigInteger yourKey = new BigInteger(Integer.parseInt(br.readLine())
                    + "");
            //System.out.println("<-- RECEIVE: yourKey=" + yourKey.toString());

            // send public key to client
            Random rand = new Random();
            BigInteger privateKey = new BigInteger((rand.nextInt(100) + 1) + "");
            //System.out.println("(privateKey=" + privateKey.toString() + ")");
            BigInteger publicKey = p.modPow(privateKey, q);
            ps.println(publicKey.toString() + "\r");
            //System.out.println("--> SEND: publicKey=" + publicKey.toString());

            // calculate cipher key
            cipherKey = yourKey.modPow(privateKey, q);
            //System.out.println("(cipherKey=" + cipherKey.toString() + ")");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void getKey() {
        try {
            // receive P and Q from server
            Scanner ioscan = new Scanner(br.readLine());
            BigInteger p = new BigInteger(ioscan.nextInt() + "");
            BigInteger q = new BigInteger(ioscan.nextInt() + "");
            /*System.out.println("<-- RECEIVE: P=" + p.toString() + " Q="
                    + q.toString());*/

            // send public key to server
            Random rand = new Random();
            BigInteger privateKey = new BigInteger((rand.nextInt(100) + 1) + "");
            //System.out.println("(privateKey=" + privateKey.toString() + ")");
            BigInteger publicKey = p.modPow(privateKey, q);
            ps.println(publicKey.toString() + "\r");
            //System.out.println("--> SEND: publicKey=" + publicKey.toString());

            // receive public key from server
            BigInteger yourKey = new BigInteger(Integer.parseInt(br.readLine())
                    + "");
            //System.out.println("<-- RECEIVE: yourKey=" + yourKey.toString());

            // calculate cipher key
            cipherKey = yourKey.modPow(privateKey, q);
            //System.out.println("(cipherKey=" + cipherKey.toString() + ")");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean hasMessage() throws IOException {
        return br.ready();
    }

    public String getMessage() throws IOException {
        return decode(br.readLine());
    }

    public void sendMessage(String message) {
        ps.println(encode(message) + "\r");
    }

    private String encode(String message) {
        message = message.toUpperCase();
        String encodedMessage = "";
        for (int i = 0; i < message.length(); i++) {
            char thisChar = message.charAt(i);
            if (thisChar >= 65 && thisChar <= 90) {
                int shiftValue = 1 + Integer.parseInt(cipherKey.mod(
                        new BigInteger("26")).toString());

                //System.out.println("shiftValue = " + shiftValue);
                //System.out.println("thisChar (before) = " + thisChar);

                thisChar += shiftValue;
                if (thisChar > 90)
                    thisChar -= 26;

                //System.out.println("thisChar (after) = " + thisChar);
            }
            //System.out.println("thisChar (appending) = " + thisChar);
            encodedMessage += thisChar;
        }
        return encodedMessage;
    }

    private String decode(String message) {
        message = message.toUpperCase();
        String decodedMessage = "";
        for (int i = 0; i < message.length(); i++) {
            char thisChar = message.charAt(i);
            if (thisChar >= 65 && thisChar <= 90) {
                int shiftValue = 1 + Integer.parseInt(cipherKey.mod(
                        new BigInteger("26")).toString());
                thisChar -= shiftValue;
                if (thisChar < 65)
                    thisChar += 26;
            }
            decodedMessage += thisChar;
        }
        return decodedMessage;
    }

    public void close() {
        try {
            ps.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}