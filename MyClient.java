import java.io.*;
import java.net.*;

public class MyClient {
    public static void main(String[] args) {
        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);
            
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            
            ProtocolHandshake(din, dout);

            String[] servers = GetServers(din, dout);

            ALT(din, dout, servers);
        
            WriteData(dout, "QUIT");
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

    private static void ProtocolHandshake(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        String username = System.getProperty("user.name");

        WriteData(dout, "HELO");

        data = ReadData(din);

        if (data.equals("OK")) {
            WriteData(dout, "AUTH " + username);
        }

        data = ReadData(din);

        if (data.equals("OK")) {
            WriteData(dout, "REDY");
        }
    }

    private static String[] GetServers(BufferedReader din, DataOutputStream dout) throws IOException {
            String data = "";

            data = ReadData(din);

            WriteData(dout, "GETS All");

            data = ReadData(din);
            String[] serverData = data.split(" ");
            int serverNo = Integer.parseInt(serverData[1]);
            String[] servers = new String[serverNo];

            WriteData(dout, "OK");

            for(int i = 0; i < serverNo; i++) {
                data = ReadData(din);
                servers[i] = data;
            }

            WriteData(dout, "OK");
            data = ReadData(din);

            return servers;
    }

    private static void ALT(BufferedReader din, DataOutputStream dout, String[] servers) throws IOException {
        int largest = 0;
        String largestServer = null;
        String data = "";

        for(int i = 0; i < servers.length; i++) {
            String[] splitServer = servers[i].split(" ");
            int temp = Integer.parseInt(splitServer[4]);
            if(largest < temp) {
                largest = temp;
                largestServer = servers[i];
            }
        }


        while(true) {
            WriteData(dout, "REDY");

            data = ReadData(din);

            String[] jobSplit = data.split(" ");
            String[] lsSplit = largestServer.split(" ");

            if(jobSplit[0].equals("JOBN")) {
                String schd = "SCHD " + jobSplit[2] + " " + lsSplit[0] + " " + lsSplit[1];
                WriteData(dout, schd);
                data = ReadData(din);
            } else if(jobSplit[0].equals("JCPL")){
            } else {
                break;
            }
        }
    }

    private static void WriteData(DataOutputStream dout, String data) throws IOException {
        System.out.println("Sending: " + data);
        dout.write((data + "\n").getBytes());
        dout.flush();
    }

    private static String ReadData(BufferedReader din) throws IOException {
        String data = "";
        data = din.readLine();
        System.out.println("Received: " + data);
        return data;
    }
}