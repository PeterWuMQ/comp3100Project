import java.io.*;
import java.net.*;

public class MyServer {
    public static void main(String[] args) {
        try {
            int serverPort = 5000;
            ServerSocket listenSocket = new ServerSocket(serverPort);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                Connection c = new Connection(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Listen: " + e.getMessage());
        }

    }
}

class Connection extends Thread {
    DataInputStream din;
    DataOutputStream dout;
    Socket clientSocket;

    public Connection(Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            din = new DataInputStream(clientSocket.getInputStream());
            dout = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try {
            String str = "";
            while (!str.equals("BYE")) {
                str = din.readUTF();
                System.out.println("CLIENT says: " + str);
                if (str.equals("HELO")) {
                    System.out.println("sending G'DAY");
                    dout.writeUTF("G'DAY");
                    dout.flush();
                }
            }
            System.out.println("sending BYE");
            dout.writeUTF("BYE");
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                /* close failed */}
        }
    }
}
