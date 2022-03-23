import java.io.*;
import java.net.*;

public class MyClient {
    public static void main(String[] args) {
        Socket s = null;
        try {
            int serverPort = 50001;
            s = new Socket("127.0.0.1", serverPort);
            String username = System.getProperty("user.name");
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            String data = "";
            
            dout.write(("HELO\n").getBytes());
            dout.flush();
            System.out.println("Sending: HELO");
            data = din.readLine();
            System.out.println("Received: " + data);
            if (data.equals("OK")) {
                System.out.println("Sending: AUTH");
                dout.write(("AUTH " + username + "\n").getBytes());
                dout.flush();
            }
            data = din.readLine();
            System.out.println("Received: " + data);
            if (data.equals("OK")) {
                System.out.println("Sending: REDY");
                dout.write(("REDY\n").getBytes());
                dout.flush();
            }
            
            Boolean check = true;
            data = din.readLine();
            System.out.println("Received: " + data);
            dout.write(("GETS all\n").getBytes());
            dout.flush();
            data = din.readLine();
            System.out.println("Received: " + data);
           /*  String[] split = data.split(" ");

            while(check) {
                if(split[0].equals("JOBN")) {
                    dout.write(("REDY\n").getBytes());
                    dout.flush();
                } else {
                    check = false;
                }
            } */
            
            dout.write(("QUIT\n").getBytes());
            dout.flush();
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