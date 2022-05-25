package stage2;

public class Job {
    private String id;
    private int cores;

    public Job(String id, int cores) {
        setId(id);
        setCores(cores);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public String getId() {
        return id;
    }

    public int getCores() {
        return cores;
    }
}
