public class Server {
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
