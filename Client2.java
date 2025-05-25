import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    private int porta;
    private String host;
    private String nome;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client2(String host, int porta, String nome) {
        this.host = host;
        this.porta = porta;
        this.nome = nome;
    }

    public void run() {
        try {
            InetAddress endereco = InetAddress.getByName(host);
            socket = new Socket(endereco, porta);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Envia a mensagem inicial com o nome
            Mensagem mensagemInicial = new Mensagem(nome, null, null);
            out.writeObject(mensagemInicial);

            // Inicia a thread de recebimento
            new Thread(new Receiver()).start();

            // Inicia a thread de envio
            new Thread(new Sender()).start();

        } catch (IOException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
        }
    }

    private class Receiver implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Mensagem recebida = (Mensagem) in.readObject();
                    System.out.println(recebida);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Conexão com o servidor encerrada.");
            }
        }
    }

    private class Sender implements Runnable {
        @Override
        public void run() {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("Escreva a sua mensagem: ");
                    String msg = scanner.nextLine();

                    if (msg.equalsIgnoreCase("/sair")) {
                        System.out.println("Encerrando conexão...");
                        out.writeObject(new Mensagem(nome, null, msg));
                        socket.close();
                        break;
                    }

                    out.writeObject(new Mensagem(nome, null, msg));
                }
            } catch (IOException e) {
                System.out.println("Erro ao enviar mensagem: " + e.getMessage());
            }
        }
    }
}
