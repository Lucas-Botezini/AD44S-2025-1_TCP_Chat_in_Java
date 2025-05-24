import java.util.Scanner;

public class MainClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome de usuário: ");
        String nome = scanner.nextLine();

        Client cliente = new Client("localhost", 12345, nome);
        cliente.run();
    }
}
