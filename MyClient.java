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
            
            
            data = din.readLine();
            System.out.println("Received: " + data);
            dout.write(("GETS All\n").getBytes());
            dout.flush();
            data = din.readLine();
            System.out.println("Received: " + data);
            String[] serverData = data.split(" ");
            int serverNo = Integer.parseInt(serverData[1]);
            String[] servers = new String[serverNo];

            dout.write(("OK\n").getBytes());
            dout.flush();

            for(int i = 0; i < serverNo; i++) {
                data = din.readLine();
                System.out.println("Received: " + data);
                servers[i] = data;
            }

            int largest = 0;
            String largestServer = null;

            for(int i = 0; i < serverNo; i++) {
                String[] splitServer = servers[i].split(" ");
                int temp = Integer.parseInt(splitServer[4]);
                if(largest < temp) {
                    largest = temp;
                    largestServer = servers[i];
                }
            }

            dout.write(("OK\n").getBytes());
            dout.flush();
            data = din.readLine();
            System.out.println("Received: " + data);

            while(true) {
                dout.write(("REDY\n").getBytes());
                dout.flush();
                data = din.readLine();
                System.out.println("Received: " + data);
                String[] jobSplit = data.split(" ");
                String[] lsSplit = largestServer.split(" ");
                if(jobSplit[0].equals("JOBN")) {
                    String schd = "SCHD " + jobSplit[2] + " " + lsSplit[0] + " " + lsSplit[1] + "\n";
                    dout.write((schd).getBytes());
                    dout.flush();
                    data = din.readLine();
                    System.out.println("Received: " + data);
                } else if(jobSplit[0].equals("JCPL")){
                } else {
                    break;
                }
            }
            
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