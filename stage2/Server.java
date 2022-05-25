package stage2;

import java.util.Comparator;

public class Server {
    private String type;
    private String id;
    private int cores;
    private int memory;
    private int disk;

    public Server(String type, String id, int cores, int memory, int disk) {
        setType(type);
        setId(id);
        setCores(cores);
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

    public int getMemory() {
        return memory;
    }

    public int getDisk() {
        return disk;
    }

    static class ServerSortingComparator implements Comparator<Server> {
        @Override
        public int compare(Server a, Server b) {
            int CoreCompare = a.getCores() - b.getCores();

            int MemoryCompare = a.getMemory() - b.getMemory();

            return (CoreCompare == 0) ? MemoryCompare : CoreCompare;
        }
    }
}
