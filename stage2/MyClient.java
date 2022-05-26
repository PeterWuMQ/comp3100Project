package stage2;

import java.io.*;
import java.net.*;
import java.util.*;

import stage2.Server.ServerSortingComparator;

public class MyClient {
    public static void main(String[] args) {
        if (args.length != 2 || !args[0].equals("-a") || !args[1].equals("sbf")) {
            System.out.println("Usage: java MyClient -a sbf");
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
        String data = "";
        writeData(dout, "GETS All");

        // Get the amount of incoming server messages and initialise a empty list of
        // servers
        data =  readData(din);
        String[] serverData = data.split(" ");
        int serverNo = Integer.parseInt(serverData[1]);
        List<Server> servers = new ArrayList<>();

        writeData(dout, "OK");

        // parse every message and store as a List of servers
        for (int i = 0; i < serverNo; i++) {
            data =  readData(din);
            String[] serverMessage = data.split(" ");

            String sType = serverMessage[0];
            String sId = serverMessage[1];
            int sCores = Integer.valueOf(serverMessage[4]);
            int sMem = Integer.valueOf(serverMessage[5]);
            int sDisk = Integer.valueOf(serverMessage[6]);

            servers.add(new Server(sType, sId, sCores, sMem, sDisk));
        }

        Collections.sort(servers, new ServerSortingComparator());

        writeData(dout, "OK");
        data =  readData(din);

        return servers;
    }

    private static void sbf(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        List<Server> servers = new ArrayList<>();
        
        while (true) {
            Server best = new Server("null", "id", 0, 0, 0);

            writeData(dout, "REDY");

            data = readData(din);
            String[] message = data.split(" ");

            if (message[0].equals("JOBN")) {
                
                if(servers.size() == 0) {
                    servers = getServers(din, dout);
                    writeData(dout, "OK");
                    data = readData(din);
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
                        break;
                    } 
                }

                int tempServer = servers.size() - 1;
                int check = servers.get(tempServer).getAvailCores();

                if(best.getId().equals("id")) {
                    for(int i = 0; i < servers.size(); i++) {     
                        int sCores = servers.get(i).getCores();
                        int sAvaCores = servers.get(i).getAvailCores();
                        int sMem = servers.get(i).getMemory();
                        int sDisk = servers.get(i).getDisk();
                        
                        if(jCores <= sCores && jMem <= sMem && jDisk <= sDisk && check < sAvaCores) {
                            check = sAvaCores;
                            tempServer = i; 
                        } 
                    }
                    best = servers.get(tempServer);
                    servers.get(tempServer).addJob(new Job(jId, jCores));
                }

                scheduleJob(dout, jId, best.getType(), best.getId());

                data = readData(din);

            } else if (message[0].equals("JCPL")) {
                String cType = message[3];
                String cId = message[4];
                String jId = message[2];

                for(int i = 0; i < servers.size(); i++) {
                    String sType = servers.get(i).getType();
                    String sId = servers.get(i).getId();

                    if(sId.equals(cId) && sType.equals(cType)) {
                        servers.get(i).removeJob(jId);
                        break;
                    }
                }
            } else if (message[0].equals("NONE")) {
                break;
            }
        }
    }

    public static void migrateJob(DataOutputStream dout, String jobID, String sType, String sID, String tType, String tID)
            throws IOException {
        String migj = "MIGJ " + jobID + " " + sType + " " + sID + " " + tType + " " + tID;
        writeData(dout, migj);
    }

    public static void scheduleJob(DataOutputStream dout, String jobID, String serverType, String serverID)
            throws IOException {
        String schd = "SCHD " + jobID + " " + serverType + " " + serverID;
        writeData(dout, schd);
    }

    // Writes data to server appending newline at the end
    private static void writeData(DataOutputStream dout, String data) throws IOException {
        System.out.println("S: " + data);
        dout.write((data + "\n").getBytes());
        dout.flush();
    }

    private static String readData(BufferedReader din) throws IOException {
        String data = din.readLine();
        System.out.println("R: " + data);
        return data;
    }
}
