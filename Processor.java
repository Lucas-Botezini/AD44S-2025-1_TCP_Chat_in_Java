import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public class Processor implements Runnable{
    private Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    private static List<OnlineUsers> onlineUsers;

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

            // Grava em uma lista o nome e ip do usuário que enviou a mensagem
            OnlineUsers newUser = new OnlineUsers(mensagem.getRemetente(), socket.getInetAddress().getHostAddress());
            if (!onlineUsers.contains(newUser)){
                synchronized (onlineUsers) {
                    onlineUsers.add(newUser);
                }
            }

            // Quando o usuário fecha a conexão o seu nome é limpo da lista
            if (mensagem.getConteudo().startsWith("/exit")) {
                synchronized (onlineUsers) {
                    onlineUsers.remove(newUser);
                }
                socket.close();
            }

            // Verifica se a mensagem começa com /usuarios, se sim retorna os usuários onlines
            if (mensagem.getConteudo().startsWith("/usuarios")) {
                String novoConteudo = onlineUsers.toString();
                Mensagem newMessage = new Mensagem(mensagem.getRemetente(), mensagem.getRemetente(), novoConteudo);
                out.writeObject(newMessage);
            } else if (mensagem.getConteudo().startsWith("/privado")) {
                // Verifica se a mensagem começa com /private, se começar envia a mensagem de forma privada, se não envia para todos.
                privateMessage(mensagem);

            } else {
                broadcastMessage(mensagem);
            }

            /////////////////////////// Acredito que não vai precisar pois não vai fechar a conexão com o cliente
            socket.close();
            /////////////////////////// Acredito que não vai precisar pois não vai fechar a conexão com o cliente

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
        // Apaga o /privado e os : do conteúdo da mensagem
        String novoConteudo = mensagem.getConteudo().replaceFirst("/privado:", "");

        try {
            // Salva o destinatário dessa mensagem em uma string
            String destinatario = novoConteudo.substring(0, novoConteudo.indexOf(":"));
            novoConteudo = mensagem.getConteudo().replaceFirst(destinatario+":", "");

            Mensagem newMessage = new Mensagem(mensagem.getRemetente(), destinatario, novoConteudo);
            out.writeObject(newMessage);
        } catch (IOException e) {
            throw new ChatPrivateException("Erro ao fazer o envio da mensagem privada: " + e.getMessage());
        }
    }

    private void broadcastMessage(Mensagem mensagem) {
        Mensagem newMessage = new Mensagem(mensagem.getRemetente(), null, mensagem.getConteudo());
        try {
            out.writeObject(newMessage);
        } catch (IOException e) {
            throw new ChatBroadcastException("Erro ao fazer o envio: " + e.getMessage());
        }
    }

}
