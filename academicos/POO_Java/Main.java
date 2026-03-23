import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        double saldo = 500.00;
        int opcao = 0;

        while (opcao != 4) {
            System.out.println("\n === CAIXA ELETRONICO ===");
            System.out.println("1 - Consultar saldo");
            System.out.println("2 - Depositar");
            System.out.println("3 - Sacar");
            System.out.println("4 - Sair");
            System.out.print("Escolha uma opção: ");
            opcao = sc.nextInt();

            switch (opcao) {
                case 1:
                    System.out.printf("Saldo atual: R$ %.2f\n", saldo);
                    break;
                case 2:
                    System.out.print("Digite o valor a depositar: R$ ");
                    double deposito = sc.nextDouble();
                    if (deposito > 0) {
                        saldo += deposito;
                        System.out.printf("Deposito realizado. Novo saldo: R$ %.2f\n", saldo);
                    } else{
                        System.out.println("Valor invalido. O valor deve ser maior que 0");
                    }
                    break;
                    
                    case 3:
                    System.out.print("Digite o valor a sacar: R$ ");
                    double saque = sc.nextDouble();

                    if (saque > 0 && saque <= saldo){
                        saldo -= saque;
                        System.out.printf("Saque realizado. Novo saldo: R$ %.2f\n", saldo);
                    }else if (saque > saldo){
                        System.out.println("Saldo insuficiente para realizar o saque. Seu saldo atual é: R$ " + saldo);
                    }else{
                        System.out.println("Valor invalido. O valor deve ser maior que 0");
                    }
                    break;
                    
                    case 4:
                    System.out.println("Acabou");
                    break;
            
            }
            if (opcao < 1 || opcao > 4) {
                System.out.println("Opção inválida. Por favor, escolha uma opção entre 1 e 4.");
            }
        }
        sc.close();

    }
}