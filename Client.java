import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private JTextArea areaTexto;
    private JTextField campoEntrada;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String nomeUsuario;

    public Client() {
        setTitle("Chat TCP - Cliente");
        setSize(500, 500);
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
            if (!texto.isBlank()) {
                try {
                    out.writeObject(new Mensagem(nomeUsuario, null, texto));
                    // Limpa a caixa de texto após enviar a mensagem
                    areaTexto.append("\nMensagem enviada: " + texto +"\n");
                    campoEntrada.setText("");
                } catch (IOException er) {
                    areaTexto.append("[Erro ao enviar mensagem]\n");
                }
            }
        });
    }

    public void conectar(String host, int porta, String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
        try {
            socket = new Socket(host, porta);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Envia nome do usuário
            Mensagem mensagemInicial = new Mensagem(nomeUsuario, null, null);
            out.writeObject(mensagemInicial);

            // Thread para receber mensagens
            new Thread(() -> {
                try {
                    while (true) {
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