import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Processor implements Runnable{
    private Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    private static Map<String , ObjectOutputStream> onlineUsers = new ConcurrentHashMap<>();

    public Processor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (!socket.isClosed()) {

                // Lê a mensagem do cliente e insere em uma classe mensagem
                Mensagem mensagem = (Mensagem) in.readObject();

                // Grava no map o usuário e seu out, envia a mensagem que o usuário se conectou
                String nomeUsuario = mensagem.getRemetente();
                if (
                        !onlineUsers.containsKey(nomeUsuario) &&
                                (mensagem.getConteudo() == null || mensagem.getConteudo().isEmpty())
                ) {
                    synchronized (Processor.class) {
                        onlineUsers.put(nomeUsuario, out);
                        System.out.println("[Servidor] Usuário conectado: " + nomeUsuario);
                        broadcastMessage(new Mensagem(
                                mensagem.getRemetente(),
                                null,
                                mensagem.getRemetente() + " se conectou no chat."));
                    }
                }

                if (mensagem.getConteudo() != null) {
                    // Quando o usuário fecha a conexão o seu nome é limpo da lista
                    if (mensagem.getConteudo().startsWith("/sair")) {
                        synchronized (Processor.class) {
                            onlineUsers.remove(mensagem.getRemetente());
                            System.out.println("[Servidor] Usuário desconectado: " + mensagem.getRemetente());
                        }
                        socket.close();
                        return;
                    }

                    StringBuilder listaUsuarios = new StringBuilder("Usuários online:\n");
                    for (String user : onlineUsers.keySet()) {
                        listaUsuarios.append("- ").append(user).append("\n");
                    }

                    // Mostra no console do servidor os usuários online
                    System.out.println(listaUsuarios);

                    // Verifica se a mensagem começa com /usuarios, se sim retorna os usuários onlines
                    if (mensagem.getConteudo().startsWith("/usuarios")) {
                        String novoConteudo = listaUsuarios.toString();
                        Mensagem newMessage = new Mensagem(mensagem.getRemetente(), mensagem.getRemetente(), novoConteudo);
                        out.writeObject(newMessage);

                    } else if (mensagem.getConteudo().startsWith("/privado")) {
                        // Verifica se a mensagem começa com /private, se começar envia a mensagem de forma privada, se não envia para todos.
                        privateMessage(mensagem);

                    } else {
                        broadcastMessage(mensagem);
                    }
                }
            }
        }catch (SocketException ex) {
            System.out.println("[Servidor] Conexao perdida com o cliente: " + ex.getMessage());
        }catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Se a conexão é fechada sem o envio da mensagem de fechamento remove o usuário da lista
        finally {
            try {
                for (Map.Entry<String, ObjectOutputStream> entry : onlineUsers.entrySet()) {
                    if (entry.getValue().equals(out)) {
                        String nomeRemovido = entry.getKey();
                        onlineUsers.remove(nomeRemovido);
                        System.out.println("[Servidor] Usuário desconectado : " + nomeRemovido);
                        broadcastMessage(new Mensagem("Servidor", null, nomeRemovido + " saiu do chat."));
                        break;
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("[Servidor] Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    private void privateMessage(Mensagem mensagem) {
        // Apaga o "/privado:" do conteúdo da mensagem
        String novoConteudo = mensagem.getConteudo().replaceFirst("/privado:", "");

        try {
            // Salva o destinatário dessa mensagem em uma string
            String destinatario = novoConteudo.substring(0, novoConteudo.indexOf(":"));

            // Monta o novo conteúdo da mensagem
            String conteudo = novoConteudo.substring(novoConteudo.indexOf(":") + 1);;

            ObjectOutputStream destinoOut = onlineUsers.get(destinatario);
            if (destinoOut != null) {
                Mensagem newMessage = new Mensagem(mensagem.getRemetente(), destinatario, conteudo);
                destinoOut.writeObject(newMessage);
            } else {
                Mensagem erro = new Mensagem("Servidor", mensagem.getRemetente(), "Usuário '" + destinatario + "' não encontrado.");
                out.writeObject(erro);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer o envio da mensagem privada: " + e.getMessage());
        }
    }

    private void broadcastMessage(Mensagem mensagem) {
        Mensagem newMessage = new Mensagem(mensagem.getRemetente(), null, mensagem.getConteudo());
        for (Map.Entry<String, ObjectOutputStream> entry : onlineUsers.entrySet()) {
            try {
                entry.getValue().writeObject(newMessage);
            } catch (IOException e) {
                System.out.println("Erro ao enviar mensagem para " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

}
