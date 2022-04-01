
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MyClient {
    public static void main(String[] args) {
        Socket s = null;
        try {
            int serverPort = 50000;
            s = new Socket("127.0.0.1", serverPort);

            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            protocolHandshake(din, dout);

            LRR(din, dout);

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
    private static void protocolHandshake(BufferedReader din, DataOutputStream dout) throws IOException {
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

    // All To Largest - Schedules all jobs to the "largest" server (most amount of
    // cores)
    private static void ALT(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        int largest = 0;
        Server largestServer = new Server("null", 0, 0);

        while (true) {
            writeData(dout, "REDY");

            // Get the next message
            data = din.readLine();
            String[] message = data.split(" ");

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

            // If message is a job submission then schedule it to the largest server
            // If message is a job completion then do nothing
            // If message is no more jobs then end
            if (message[0].equals("JOBN")) {
                String schd = "SCHD " + message[2] + " " + largestServer.getType() + " "
                        + Integer.valueOf(largestServer.getId());
                writeData(dout, schd);
                data = din.readLine();
            } else if (message[0].equals("JCPL")) {
            } else if (message[0].equals("NONE")) {
                break;
            }
        }
    }

    // Largest Round Robin - Schedules all jobs to servers of the same type as the
    // server with the "largest" server (most amount of cores) in a round robin
    // fashion
    private static void LRR(BufferedReader din, DataOutputStream dout) throws IOException {
        String data = "";
        int current = 0;
        List<Server> largestServers = new ArrayList<>();

        while (true) {
            writeData(dout, "REDY");

            // Get the current
            data = din.readLine();
            String[] message = data.split(" ");

            // If largest servers hasn't been found yet then get all servers
            if (largestServers.isEmpty()) {
                int largest = 0;
                String largestType = " ";
                List<Server> servers = getServers(din, dout);

                // Find the largest type of server
                for (int i = 0; i < servers.size(); i++) {
                    int temp = servers.get(i).getCores();
                    if (largest < temp) {
                        largest = temp;
                        largestType = servers.get(i).getType();
                    }
                }

                // Create a List of all server of the largest type
                for (int i = 0; i < servers.size(); i++) {
                    if (servers.get(i).getType().equals(largestType)) {
                        largestServers.add(servers.get(i));
                    }
                }
            }

            // If message is a job submission then schedule it to a server of the largest
            // type and then increment current so the next job is scheduled to the next
            // server of that type
            // If message is a job completion then do nothing
            // If message is no jobs then end
            if (message[0].equals("JOBN")) {
                String schd = "SCHD " + message[2] + " " + largestServers.get(current).getType() + " "
                        + Integer.valueOf(largestServers.get(current).getId());
                writeData(dout, schd);
                data = din.readLine();
                current++;
                if (current >= largestServers.size()) {
                    current = 0;
                }
            } else if (message[0].equals("JCPL")) {
            } else if (message[0].equals("NONE")) {
                break;
            }
        }
    }

    // Writes data to server appending newline at the end
    private static void writeData(DataOutputStream dout, String data) throws IOException {
        dout.write((data + "\n").getBytes());
        dout.flush();
    }

    static class Server {
        private String type;
        private int id;
        private int cores;

        public Server(String type, int id, int cores) {
            setType(type);
            setId(id);
            setCores(cores);
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setCores(int cores) {
            this.cores = cores;
        }

        public String getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public int getCores() {
            return cores;
        }
    }
}
