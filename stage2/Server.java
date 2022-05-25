package stage2;

import java.util.*;

public class Server {
    private String type;
    private String id;
    private int cores;
    private int availCores;
    private int memory;
    private int disk;
    private List<Job> jobs = new ArrayList<>();

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

    public void addJob(Job job) {
        jobs.add(job);
    }

    public void removeJob(String id) {
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId().equals(id)) {
                jobs.remove(jobs.get(i));
                break;
            }
        }
    }

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
