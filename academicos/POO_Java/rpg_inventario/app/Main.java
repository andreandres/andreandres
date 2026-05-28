package app;

import inventario.Inventario;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Inventario sistema = new Inventario();

        try (Scanner sc = new Scanner(System.in)) {
            int opcao;
            do {
                System.out.println("\n===== RPG DE INVENTARIO =====");
                System.out.println("1 - Adicionar item");
                System.out.println("2 - Listar itens");
                System.out.println("3 - Atualizar item");
                System.out.println("4 - Remover item");
                System.out.println("0 - Sair");
                System.out.print("Escolha uma opcao: ");
                opcao = sc.nextInt();
                sc.nextLine();

                switch (opcao) {
                    case 1 -> sistema.adicionarItem(sc);
                    case 2 -> sistema.listarItens();
                    case 3 -> sistema.atualizarItem(sc);
                    case 4 -> sistema.removerItem(sc);
                    case 0 -> System.out.println("Encerrando o programa.");
                    default -> System.out.println("Opcao invalida.");
                }
            } while (opcao != 0);
        }
    }
}
