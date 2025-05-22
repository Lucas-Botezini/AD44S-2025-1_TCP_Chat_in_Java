import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o seu nome:");
        String remetente = scanner.nextLine();

        Mensagem msg = new Mensagem(remetente, null, "");
        Client client = new Client(1234, "localhost", msg);
        client.run();
    }
}
