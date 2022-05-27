package stage2;

import java.util.*;

public class Server {
    private String type;                            // type of Server
    private String id;                              // id of Server
    private int cores;                              // amount of cores for a Server 
    private int availCores;                         // amount of cores avaliable for a Server
    private int memory;                             // amount of memory for a Server
    private int disk;                               // amount of disk for a Server
    private List<Job> jobs = new ArrayList<>();     // Jobs running or waiting for a Server

    // Create a new Server
    public Server(String type, String id, int cores, int memory, int disk) {
        setType(type);
        setId(id);
        setCores(cores);
        setAvailCores(cores);
        setMemory(memory);
        setDisk(disk);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public void setAvailCores(int availCores) {
        this.availCores = availCores;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public int getCores() {
        return cores;
    }

    public int getAvailCores() {
        return availCores;
    }

    public int getMemory() {
        return memory;
    }

    public int getDisk() {
        return disk;
    }
    
    public List<Job> getJobs() {
        return jobs;
    }

    // Add a job to the Job List for a Server and update the Avaliable cores
    public void addJob(Job job) {
        jobs.add(job);
        setAvailCores(getAvailCores() - job.getCores());
    }

    // Remove a job from the Job List for a Server and update the Avaliable cores
    public void removeJob(String id) {
        int c;
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId().equals(id)) {
                c = jobs.get(i).getCores();
                setAvailCores(getAvailCores() + c);
                jobs.remove(jobs.get(i));
                break;
            }
        }
    }

    // Sort a List of Servers by Cores, then Memory, then Disk
    static class ServerSortingComparator implements Comparator<Server> {
        @Override
        public int compare(Server a, Server b) {
            int compare = a.getCores() - b.getCores();

            if (compare == 0) {
                compare = a.getMemory() - b.getMemory();
            }

            if (compare == 0) {
                compare = a.getDisk() - b.getDisk();
            }

            return compare;
        }
    }
}
