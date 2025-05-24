import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client{
    private int porta;
    private String host;
    private String nome;

    public Client(String host, int porta, String nome) {
        this.host = host;
        this.porta = porta;
        this.nome = nome;
    }

    public void run() {
        try{
        InetAddress endereco;
        endereco = InetAddress.getByName(host);

        Socket socket = new Socket(endereco, porta);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        new Thread(() -> {
            while(true){
                try {
                    Mensagem recebida = (Mensagem) in.readObject();
                    System.out.println(recebida);
                } catch (Exception e) {
                    System.out.println("Conecao perdida");
                }
            }
        }).start();

        while(true){
            Scanner scanner = new Scanner(System.in);
            String msg = scanner.nextLine();
            if(msg.equalsIgnoreCase("/sair")){
                break;
            }
            Mensagem mensagem = new Mensagem(nome,null, msg);
            out.writeObject(mensagem);
        }
        socket.close();
        System.out.println("Conexao encerrada");
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
