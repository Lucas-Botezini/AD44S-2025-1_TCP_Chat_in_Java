import java.io.IOException;

public class MainServer {

    public static void main(String[] args) {

        Server servidor = new Server(1234);

        try {
            servidor.run();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
