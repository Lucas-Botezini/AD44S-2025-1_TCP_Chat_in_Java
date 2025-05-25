import java.util.Scanner;

public class MainClient2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome de usuário: ");
        String nome = scanner.nextLine();

        Client cliente = new Client("localhost", 1234, nome);
        cliente.run();
    }
}
