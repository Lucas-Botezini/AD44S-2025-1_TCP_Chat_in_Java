public class OnlineUsers {
    private String ip;
    private String name;

    public OnlineUsers() {
    }

    public OnlineUsers(String ip, String name) {
        this.ip = ip;
        this.name = name;
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
