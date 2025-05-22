import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Processor implements Runnable{
    private Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;

    public Processor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(30000);

            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            // Lê a mensagem do cliente e insere em uma classe mensagem
            Mensagem mensagem = (Mensagem) in.readObject();

            // Verifica se a mensagem começa com /private, se começar envia a mensagem de forma privada, se não envia para todos.
            if (mensagem.getConteudo().startsWith("/privado")) {
                privateMessage(mensagem);
            } else {
                broadcastMessage(mensagem);
            }

            socket.close();

        } catch(SocketTimeoutException e) {
            System.out.println("[Servidor] Conexão com o cliente "+socket.getInetAddress().getHostAddress() +":"+ socket.getPort()  +" encerrada por inatividade.");
            try {
                socket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void privateMessage(Mensagem mensagem) {
        String novoConteudo = mensagem.getConteudo().replaceFirst("/privado:", "");

//        String destinatario = novoConteudo.
//        Mensagem newMessage = new Mensagem()

        try {
            out.writeObject(mensagem);
        } catch (IOException e) {
            throw new ChatPrivateException("Erro ao fazer o envio da mensagem privada: " + e.getMessage());
        }
    }

    private void broadcastMessage(Mensagem mensagem) {
        try {
            out.writeObject(mensagem);
        } catch (IOException e) {
            throw new ChatBroadcastException("Erro ao fazer o envio: " + e.getMessage());
        }
    }
}
