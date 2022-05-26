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
        String data = "";
        String username = System.getProperty("user.name");

        writeData(dout, "HELO");

        data = din.readLine();

        writeData(dout, "AUTH " + username);

        data = din.readLine();
    }

    // Sends request to server for all server information and returns a List of all
    // servers
    private static List<Server> getServers(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";

        writeData(dout, "GETS All");

        // Get the amount of incoming server messages and initialise a empty list of
        // servers
        data = din.readLine();
        String[] serverData = data.split(" ");
        System.out.println(Arrays.toString(serverData));
        int serverNo = Integer.parseInt(serverData[1]);
        List<Server> servers = new ArrayList<>();

        writeData(dout, "OK");

        // parse every message and store as a List of servers
        for (int i = 0; i < serverNo; i++) {
            data = din.readLine();
            String[] serverMessage = data.split(" ");
            System.out.println(Arrays.toString(serverMessage));
            servers.add(new Server(serverMessage[0], serverMessage[1],
                    Integer.valueOf(serverMessage[4]), Integer.valueOf(serverMessage[5]),
                    Integer.valueOf(serverMessage[6])));
        }

        Collections.sort(servers, new ServerSortingComparator());

        for(int i = 0; i < servers.size(); i++) {
            System.out.println(servers.get(i).getCores() + " " + servers.get(i).getMemory() + " " +  servers.get(i).getDisk());
        }

        writeData(dout, "OK");
        data = din.readLine();

        return servers;
    }

    private static void sbf(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        List<Server> servers = new ArrayList<>();
        Server best = new Server("type", "id", 0, 0, 0);

        while (true) {
            writeData(dout, "REDY");

            data = din.readLine();
            String[] message = data.split(" ");

            if (message[0].equals("JOBN")) {
                
                if(servers.size() == 0) {
                    servers = getServers(din, dout);
                    writeData(dout, "OK");
                    data = din.readLine();
                }
                
                for(int i = 0; i < servers.size(); i++) {
                    if(Integer.valueOf(message[4]) <= servers.get(i).getAvailCores() && Integer.valueOf(message[5]) <= servers.get(i).getMemory() && Integer.valueOf(message[6]) <= servers.get(i).getDisk()) {
                        best = servers.get(i);
                        Job job = new Job(message[2], Integer.valueOf(message[4]));
                        servers.get(i).addJob(job);
                        System.out.println("job cores " + message[4]);
                        System.out.println("subtracted " + servers.get(i).getType() + " " + servers.get(i).getId() + " " + servers.get(i).getAvailCores());
                        break;
                    } 
                }

                scheduleJob(dout, message[2], best.getType(), best.getId());

                data = din.readLine();

            } else if (message[0].equals("JCPL")) {
                for(int i = 0; i < servers.size(); i++) {
                    if(servers.get(i).getId().equals(message[4]) && servers.get(i).getType().equals(message[3])) {
                        servers.get(i).removeJob(message[2]);
                        System.out.println("added " + servers.get(i).getType() + " " + servers.get(i).getId() + " " + servers.get(i).getAvailCores());
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
}
