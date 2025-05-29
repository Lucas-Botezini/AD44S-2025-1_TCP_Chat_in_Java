import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int porta;
    private ServerSocket conexao;

    public Server(int porta) {
        this.porta = porta;
    }

    public void run () throws IOException {

        // Cria o socket do servidor, onde o servidor irá "escutar" as requisições
        System.out.println("[Servidor] Esperando conexão.");
        conexao = new ServerSocket(porta);

        try {
            // Enquanto o servidor estiver rodando, ele irá aceitar conexões
            while (true) {
                // Aguarda a conexão do cliente
                Socket socket = conexao.accept();
                // Inicia uma nova thread para processar a requisição do cliente
                Thread client = new Thread(new Processor(socket));
                client.start();
            }

        } catch (IOException e) {
            System.out.println("[Servidor] Erro ao aceitar conexão: " + e.getMessage());
            conexao.close();
        }
    }

    public static void main(String[] args) {
        // Cria uma instância do servidor na porta 1234
        Server servidor = new Server(1234);
        try {
            servidor.run();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
