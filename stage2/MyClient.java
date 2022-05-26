package stage2;

import java.io.*;
import java.net.*;
import java.util.*;

import stage2.Server.ServerSortingComparator;

public class MyClient {
    public static void main(String[] args) {
        if (args.length != 2 || !args[0].equals("-a")) {
            System.out.println("Usage: java MyClient -a");
            System.exit(0);
        }

        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            initiateHandshake(din, dout);

            if (args[1].equals("sbf")) {
                sbf(din, dout);
            }

            writeData(dout, "QUIT");
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

    // Performs initial handshake with server to establish connection
    private static void initiateHandshake(BufferedReader din, DataOutputStream dout) throws IOException {
        String username = System.getProperty("user.name");

        writeData(dout, "HELO");

        readData(din);

        writeData(dout, "AUTH " + username);

        readData(din);
    }

    // Sends request to server for all server information and returns a List of all
    // servers
    private static List<Server> getServers(BufferedReader din, DataOutputStream dout) throws IOException {
        writeData(dout, "GETS All");

        // Get the amount of incoming server messages and initialise a empty list of
        // servers
        String[] serverData = readData(din).split(" ");
        System.out.println(Arrays.toString(serverData));
        int serverNo = Integer.parseInt(serverData[1]);
        List<Server> servers = new ArrayList<>();

        writeData(dout, "OK");

        // parse every message and store as a List of servers
        for (int i = 0; i < serverNo; i++) {
            String[] serverMessage = readData(din).split(" ");

            String sType = serverMessage[0];
            String sId = serverMessage[1];
            int sCores = Integer.valueOf(serverMessage[4]);
            int sMem = Integer.valueOf(serverMessage[5]);
            int sDisk = Integer.valueOf(serverMessage[6]);
            System.out.println(Arrays.toString(serverMessage));

            servers.add(new Server(sType, sId, sCores, sMem, sDisk));
        }

        Collections.sort(servers, new ServerSortingComparator());

        for(int i = 0; i < servers.size(); i++) {
            System.out.println(servers.get(i).getCores() + " " + servers.get(i).getMemory() + " " +  servers.get(i).getDisk());
        }

        writeData(dout, "OK");
        readData(din);

        return servers;
    }

    private static void sbf(BufferedReader din, DataOutputStream dout) throws IOException {
        List<Server> servers = new ArrayList<>();
        Server best = new Server("type", "id", 0, 0, 0);

        while (true) {
            writeData(dout, "REDY");

            String[] message = readData(din).split(" ");

            if (message[0].equals("JOBN")) {
                
                if(servers.size() == 0) {
                    servers = getServers(din, dout);
                    writeData(dout, "OK");
                    readData(din);
                }

                String jId = message[2];
                int jCores = Integer.valueOf(message[4]);
                int jMem = Integer.valueOf(message[5]);
                int jDisk = Integer.valueOf(message[6]);
                
                for(int i = 0; i < servers.size(); i++) {     
                    int sCores = servers.get(i).getAvailCores();
                    int sMem = servers.get(i).getMemory();
                    int sDisk = servers.get(i).getDisk();

                    if(jCores <= sCores && jMem <= sMem && jDisk <= sDisk) {
                        best = servers.get(i);
                        servers.get(i).addJob(new Job(jId, jCores));
                        System.out.println("job cores " + jCores);
                        System.out.println("subtracted " + servers.get(i).getType() + " " + servers.get(i).getId() + " " + servers.get(i).getAvailCores());
                        break;
                    } 
                }

                scheduleJob(dout, message[2], best.getType(), best.getId());

                readData(din);

            } else if (message[0].equals("JCPL")) {
                String cType = message[3];
                String cId = message[4];
                String jId = message[2];

                for(int i = 0; i < servers.size(); i++) {
                    String sType = servers.get(i).getType();
                    String sId = servers.get(i).getId();
                    
                    if(sId.equals(cId) && sType.equals(cType)) {
                        servers.get(i).removeJob(jId);
                        System.out.println("added " + sType + " " + sId + " " + servers.get(i).getAvailCores());
                        break;
                    }
                }
            } else if (message[0].equals("NONE")) {
                break;
            }
        }
    }

    public static void scheduleJob(DataOutputStream dout, String jobID, String serverType, String serverID)
            throws IOException {
        String schd = "SCHD " + jobID + " " + serverType + " " + serverID;
        writeData(dout, schd);
    }

    // Writes data to server appending newline at the end
    private static void writeData(DataOutputStream dout, String data) throws IOException {
        dout.write((data + "\n").getBytes());
        dout.flush();
    }

    private static String readData(BufferedReader din) throws IOException {
        return din.readLine();
    }
}
