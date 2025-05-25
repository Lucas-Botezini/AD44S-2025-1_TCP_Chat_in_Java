public class OnlineUsers {
    private String ip;
    private String name;

    public OnlineUsers() {
    }

    public OnlineUsers(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return name + " : " + ip + "\n";
    }
}
