package stage2;

public class Job {
    private String id;      // id of a Job
    private int cores;      // amount of cores required for a job
    private int memory;     // amount of memory required for a job                  
    private int disk;       // amount of disk required for a job                 
    private int estTime;    // estimated runtime of a job

    public Job(String id, int cores, int memory, int disk, int estTime) {
        setId(id);
        setMemory(memory);
        setDisk(disk);
        setCores(cores);
        setEstTime(estTime);
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

    public void setEstTime(int estTime) {
        this.estTime = estTime;
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

    public int getEstTime() {
        return estTime;
    }
}
