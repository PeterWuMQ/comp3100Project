package assignment;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import assignment.Server;

public class MyClient {
    public static void main(String[] args) {
        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);
            
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            
            ProtocolHandshake(din, dout);

            List<Server> servers = GetServers(din, dout);

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

    private static List<Server> GetServers(BufferedReader din, DataOutputStream dout) throws IOException {
            String data = "";

            data = ReadData(din);

            WriteData(dout, "GETS All");

            data = ReadData(din);
            String[] serverData = data.split(" ");
            int serverNo = Integer.parseInt(serverData[1]);
            List<Server> servers = new ArrayList<>();

            WriteData(dout, "OK");

            for(int i = 0; i < serverNo; i++) {
                data = ReadData(din);
                String[] splitServerData = data.split(" ");
                servers.add(new Server(splitServerData[0], Integer.valueOf(splitServerData[1]), Integer.valueOf(splitServerData[4])));
            }

            WriteData(dout, "OK");
            data = ReadData(din);

            return servers;
    }

    private static void ALT(BufferedReader din, DataOutputStream dout, List<Server> servers) throws IOException {
        int largest = 0;
        Server largestServer = new Server("null", 0, 0);
        String data = "";

        for(int i = 0; i < servers.size(); i++) {
            int temp = servers.get(i).getCores();
            if(largest < temp) {
                largest = temp;
                largestServer = servers.get(i);
            }
        }


        while(true) {
            WriteData(dout, "REDY");

            data = ReadData(din);

            String[] jobSplit = data.split(" ");

            if(jobSplit[0].equals("JOBN")) {
                String schd = "SCHD " + jobSplit[2] + " " + largestServer.getType() + " " + Integer.valueOf(largestServer.getId());
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