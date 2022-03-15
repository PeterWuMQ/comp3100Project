import java.io.*;
import java.net.*;

public class MyClient {
    public static void main(String[] args) {
        Socket s = null;
        try {
            int serverPort = 5000;
            s = new Socket("192.168.56.1", serverPort);
            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String str = "";
            dout.writeUTF("HELO");
            dout.flush();
            System.out.println("sending: HELO");
            while (!str.equals("BYE")) {
                str = din.readUTF();
                System.out.println("SERVER says: " + str);
                if (str.equals("G'DAY")) {
                    System.out.println("sending: BYE");
                    dout.writeUTF("BYE");
                    dout.flush();
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
        }
    }
}