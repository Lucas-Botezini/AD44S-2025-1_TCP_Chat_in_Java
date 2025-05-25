import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {
    private int porta;
    private ServerSocket conexao;
    Socket socket;

    public Server(int porta) {
        this.porta = porta;
    }

    public void run () throws IOException {

        // Cria o socket do servidor, onde o servidor irá "escutar" as requisições
        conexao = new ServerSocket(porta);

        try {
            // Fecha o socket do servidor após 30 segundos sem requisição
            conexao.setSoTimeout(30000);

            // Aguarda a conexão do cliente
            socket = conexao.accept();

            Thread user = new Thread(new Processor(socket));
            user.start();

        } catch (SocketTimeoutException e) {
            // Fecha o socket do servidor após 30 segundos sem requisição
            System.out.println("[Servidor] Conexão encerrada por inatividade.");
            conexao.close();
        }

    }
}
