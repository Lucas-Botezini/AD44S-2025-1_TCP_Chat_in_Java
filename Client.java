import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
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
            InetAddress endereco = InetAddress.getByName(host);
            Socket socket = new Socket(endereco, porta);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            try {
                Mensagem mensagemInical = new Mensagem(nome, null, null);
                out.writeObject(mensagemInical);
            } catch (IOException e) {
                e.printStackTrace();
            }

            new Thread(() -> {
                while (true) {
                    // Vai ficar escutando a mensagem a ser enviada pelo servidor
                    try {
                        Mensagem recebida = (Mensagem) in.readObject();
                        System.out.println(recebida.toString());
                    } catch (IOException e) {
                        //                    System.out.println("Conecao perdida");
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        //                    System.out.println("Conecao perdida");
                        e.printStackTrace();
                    }
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.println("Escreva a sua mensagem: ");
                String msg = scanner.nextLine();

                if(msg.equalsIgnoreCase("/sair")){
                    System.out.println("Conexao encerrada");
                    socket.close();
                    break;
                }
                Mensagem mensagem = new Mensagem(nome,null, msg);
                out.writeObject(mensagem);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }


}
