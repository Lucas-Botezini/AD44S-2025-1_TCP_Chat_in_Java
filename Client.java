import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client{
    private int porta;
    private String host;
    private Mensagem msg;

    public Client(int porta, String host, Mensagem msg) {
        this.porta = porta;
        this.host = host;
        this.msg = msg;
    }

    public void run() {
        try{
            InetAddress endereco;
            endereco = InetAddress.getByName(host);

            Socket socket = new Socket(endereco, porta);
            ObjectOutputStream outObject = new ObjectOutputStream(socket.getOutputStream());

            outObject.writeObject(msg);

            while(msg.getConteudo().equals("sair")){
//                if (usauriodigitou) {
//                    Mensagem novaMensagem = new Mensagem(this.msg.getRemetente(), null, "usari")
//                    outObject.writeObject(novaMensagem);
//                }
            }
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
