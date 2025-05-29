import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private JTextArea areaTexto;
    private JTextField campoEntrada;

    // Variáveis para conexão com o servidor
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String nomeUsuario;

    public Client() {
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);

        campoEntrada = new JTextField();
        add(campoEntrada, BorderLayout.SOUTH);

        //Atribui um a ação ao campo, por exemplo toda vez digitar entra nessa rotina.
        campoEntrada.addActionListener(e -> {
            //Lê o texto do campo de entrada
            String texto = campoEntrada.getText();

            // Verifica se o texto é um comando "/sair"
            if(texto.equals("/sair")){
                try {
                    // Envia mensagem para o servidor
                    out.writeObject(new Mensagem(nomeUsuario, null, "/sair"));
                    areaTexto.append("[Você saiu do chat]\n");
                    System.exit(0);
                } catch (IOException ex) {
                    areaTexto.append("[Erro ao sair do chat]\n");
                }
                return;
            }

            // Verifica se o texto é um comando "/help"
            if(texto.equals("/help")){
                // Exibe mensagens de ajuda
                areaTexto.append(" Comandos disponíveis:\n");
                areaTexto.append("1- /help - Mostrar esta mensagem de ajuda\n");
                areaTexto.append("2- /sair - Sair do chat\n");
                areaTexto.append("3- /privado:<destinatario>:<mensagem> - Envia uma mensagem privada para determinado usuario\n");
                areaTexto.append("4- /usuarios - Lista os usuários online\n");
                campoEntrada.setText("");
                return;
            }

            // Verifica se nao for igual nenhum comando acima, vai enviar a mensagem normalmente
            if (!texto.isBlank()) {
                try {
                    String[] separador = texto.split(":", 3);
                    if (separador[0].equalsIgnoreCase("/privado") || separador[0].equalsIgnoreCase("/private")) {
                        out.writeObject(new Mensagem(nomeUsuario, separador[1], separador[2]));
                    } else {
                        out.writeObject(new Mensagem(nomeUsuario, null, texto));
                    }
                    // O método flush() força o "despejo" (envio) de quaisquer dados que estejam acumulados no buffer para o destino.
                    out.flush();
                    // Limpa a caixa de texto após enviar a mensagem
                    areaTexto.append("\nMensagem enviada: " + texto +"\n");
                    campoEntrada.setText("");
                } catch (IOException er) {
                    areaTexto.append("[Erro ao enviar mensagem]\n");
                }
            }
        });
    }

    // Método para conectar ao servidor
    public void conectar(String host, int porta, String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
        setTitle("Chat TCP - Cliente:" + nomeUsuario);
        try {
            // Cria socket e streams de entrada e saída
            socket = new Socket(host, porta);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Envia nome do usuário
            Mensagem mensagemInicial = new Mensagem(nomeUsuario, null, null);
            out.writeObject(mensagemInicial);
            out.flush();

            // Thread para receber mensagens
            new Thread(() -> {
                try {
                    // Loop para receber mensagens do servidor
                    while (true) {
                        // Lê a mensagem recebida do servidor
                        Mensagem recebida = (Mensagem) in.readObject();
                        areaTexto.append(recebida + "\n");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    areaTexto.append("[Desconectado do servidor]\n");
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String nome = JOptionPane.showInputDialog("Digite seu nome:");
            if (nome == null || nome.trim().isEmpty()) return;

            Client cliente = new Client();
            cliente.setVisible(true);
            cliente.conectar("localhost", 1234, nome);
        });
    }
}