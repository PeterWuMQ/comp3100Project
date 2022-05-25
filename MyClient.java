
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

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

            if (args[1].equals("atl")) {
                fc(din, dout);
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
        int serverNo = Integer.parseInt(serverData[1]);
        List<Server> servers = new ArrayList<>();

        writeData(dout, "OK");

        // parse every message and store as a List of servers
        for (int i = 0; i < serverNo; i++) {
            data = din.readLine();
            String[] serverMessage = data.split(" ");
            servers.add(new Server(serverMessage[0], Integer.valueOf(serverMessage[1]),
                    Integer.valueOf(serverMessage[4])));
        }

        writeData(dout, "OK");
        data = din.readLine();

        return servers;
    }

    private static void fc(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        int largest = 0;
        Server largestServer = null;

        while (true) {
            writeData(dout, "REDY");

            data = din.readLine();
            String[] message = data.split(" ");

            if (message[0].equals("JOBN")) {
                // If largest server hasn't been found yet then get all servers and find the
                // largest
                if (largestServer.getType() == "null") {
                    List<Server> servers = getServers(din, dout);
                    for (int i = 0; i < servers.size(); i++) {
                        int temp = servers.get(i).getCores();
                        if (largest < temp) {
                            largest = temp;
                            largestServer = servers.get(i);
                        }
                    }
                }

                writeData(dout, "OK");
                data = din.readLine();

                scheduleJob(dout, message[2], largestServer.getType(), largestServer.getId());

                data = din.readLine();

            } else if (message[0].equals("JCPL")) {
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
