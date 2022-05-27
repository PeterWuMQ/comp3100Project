package stage2;

import java.io.*;
import java.net.*;
import java.util.*;

import stage2.Server.ServerSortingComparator;

public class MyClient {
    public static void main(String[] args) {
        // check that the cmd line has the correct argument
        if (args.length != 2 || !args[0].equals("-a") || !args[1].equals("baf")) {
            System.out.println("Usage: java MyClient -a baf");
            System.exit(0);
        }

        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String data = "";

            initiateHandshake(din, dout);

            baf(din, dout);

            // QUIT from server 
            writeData(dout, "QUIT");
            data = readData(din);
            if(!data.equals("QUIT")) {
                writeData(dout, "QUIT");
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

    // Performs initial handshake with server to establish connection
    private static void initiateHandshake(BufferedReader din, DataOutputStream dout) throws IOException {
        String username = System.getProperty("user.name");

        writeData(dout, "HELO");

        readData(din);

        writeData(dout, "AUTH " + username);

        readData(din);
    }

    // Sends request to server for all server information and returns a List of all
    // servers, sorted by cores, then memory, then disk
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

        // parse every message and store as a List of Servers
        for (int i = 0; i < serverNo; i++) {
            data =  readData(din);
            String[] serverMessage = data.split(" ");

            String sType = serverMessage[0];                    // Type of Server
            String sId = serverMessage[1];                      // Id of Server
            int sCores = Integer.valueOf(serverMessage[4]);     // Number of Cores in that Server
            int sMem = Integer.valueOf(serverMessage[5]);       // Amount of Memory in that Server
            int sDisk = Integer.valueOf(serverMessage[6]);      // Amount of Disk in that Server

            servers.add(new Server(sType, sId, sCores, sMem, sDisk));   // add that Server to the list
        }

        Collections.sort(servers, new ServerSortingComparator());   // sort the Servers

        writeData(dout, "OK");
        data =  readData(din);

        return servers;
    }

    private static void baf(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        List<Server> servers = new ArrayList<>();
        
        while (true) {
            Server best = new Server("null", "id", 0, 0, 0);    // Placeholder Server

            writeData(dout, "REDY");

            data = readData(din);
            String[] message = data.split(" ");

            // If message is a job submission
            if (message[0].equals("JOBN")) {
                
                // If Servers haven't already been retrieved then get them
                if(servers.size() == 0) {               
                    servers = getServers(din, dout);
                    writeData(dout, "OK");
                    data = readData(din);
                }

                String jId = message[2];                    // Job Id
                int jCores = Integer.valueOf(message[4]);   // Required Cores for Job
                int jMem = Integer.valueOf(message[5]);     // Required Memory for Job 
                int jDisk = Integer.valueOf(message[6]);    // Required Disk for Job
                
                // Iterating through the List of Servers
                for(int i = 0; i < servers.size(); i++) {     
                    int sCores = servers.get(i).getAvailCores();    // A Servers Avaliable Cores(not running jobs or waiting for jobs) 
                    int sMem = servers.get(i).getMemory();          // A Servers Memory 
                    int sDisk = servers.get(i).getDisk();           // A Servers Disk 
                    
                    // If the Server has enough Avaliable Cores and Memory and Disk for that job
                    // then add the Job to that Server to update it's Avaliable Cores
                    if(jCores <= sCores && jMem <= sMem && jDisk <= sDisk) {
                        best = servers.get(i);
                        servers.get(i).addJob(new Job(jId, jCores));
                        break;
                    } 
                }

                
                // If the there wasn't a Server that had sufficent Resources then:
                if(best.getId().equals("id")) {
                    // Get the last (biggest) Server and it's Available Cores
                    int tempServer = servers.size() - 1;
                    int check = servers.get(tempServer).getAvailCores();

                    // Check every Server in the Servers List
                    for(int i = 0; i < servers.size(); i++) {     
                        int sCores = servers.get(i).getCores();         // A Servers Intial Cores
                        int sAvaCores = servers.get(i).getAvailCores(); // A Servers Avaliable Cores
                        int sMem = servers.get(i).getMemory();          // A Servers Memory 
                        int sDisk = servers.get(i).getDisk();           // A Servers Disk 
                        
                        // Find a Server that has the capacity (Intial Cores, Memory, Disk)
                        // to handle the job AND has the least amount of Avaliable Cores
                        // unused or waiting for queued Jobs
                        if(jCores <= sCores && jMem <= sMem && jDisk <= sDisk && check <= sAvaCores) {
                            check = sAvaCores;
                            tempServer = i; 
                        } 
                    }
                    // Add the Job to the Server to update it's Avaliable Cores
                    best = servers.get(tempServer);
                    servers.get(tempServer).addJob(new Job(jId, jCores));
                }

                // Schedule the Job to the Server
                scheduleJob(dout, jId, best.getType(), best.getId());

                data = readData(din);

            // If message is a job completion
            } else if (message[0].equals("JCPL")) {
                String cType = message[3];  // Completed ServerType
                String cId = message[4];    // Completed ServerId
                String jId = message[2];    // Completed JobId

                // Find the Completed Server in the Servers List
                for(int i = 0; i < servers.size(); i++) {
                    String sType = servers.get(i).getType();
                    String sId = servers.get(i).getId();

                    // Once it's found remove the Job to update it's Available Cores
                    if(sId.equals(cId) && sType.equals(cType)) {
                        servers.get(i).removeJob(jId);
                        break;
                    }
                }
            // If message is none break out of algorithm
            } else if (message[0].equals("NONE")) {
                break;
            }
        }
    }

    // Assemble Schedule String and Write to server
    public static void scheduleJob(DataOutputStream dout, String jobID, String serverType, String serverID)
            throws IOException {
        String schd = "SCHD " + jobID + " " + serverType + " " + serverID;
        writeData(dout, schd);
    }

    // Writes data to server appending newline at the end
    private static void writeData(DataOutputStream dout, String data) throws IOException {
        String send = data + "\n";
        byte[] sendBytes = send.getBytes();
        dout.write(sendBytes);
        dout.flush();
    }

    // Read the data from the server and return it
    private static String readData(BufferedReader din) throws IOException {
        String data = din.readLine();
        return data;
    }
}
