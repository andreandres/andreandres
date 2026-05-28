package inventario;

import itens.Item;
import itens.tipos.Arma;
import itens.tipos.Armadura;
import itens.tipos.Consumivel;
import java.util.ArrayList;
import java.util.Scanner;

public class Inventario {
    private final ArrayList<Item> inventario = new ArrayList<>();

    public Inventario() {
        inventario.add(new Arma("Espada", 1));
        inventario.add(new Consumivel("Pocao de Vida", 5));
        inventario.add(new Armadura("Escudo", 1));
    }

    public void adicionarItem(Scanner sc) {
    System.out.print("Nome do item: ");
    String nome = sc.nextLine();

    System.out.println("Tipo do item:");
    System.out.println("1 - Arma");
    System.out.println("2 - Armadura");
    System.out.println("3 - Consumivel");
    System.out.print("Escolha uma opcao: ");
    int tipo = sc.nextInt();

    System.out.print("Quantidade: ");
    int quantidade = sc.nextInt();
    sc.nextLine();

    if (nome.isEmpty() || quantidade <= 0) {
        System.out.println("Dados invalidos.");
        return;
    }

    switch (tipo) {
        case 1 -> inventario.add(new Arma(nome, quantidade));
        case 2 -> inventario.add(new Armadura(nome, quantidade));
        case 3 -> inventario.add(new Consumivel(nome, quantidade));
        default -> {
            System.out.println("Tipo invalido.");
            return;
        }
    }

    System.out.println("Item adicionado com sucesso.");
}

    public void listarItens() {
        if (inventario.isEmpty()) {
            System.out.println("Inventario vazio.");
        } else {
            for (Item item : inventario) {
                System.out.println(item);
            }
        }
    }

    public void atualizarItem(Scanner sc) {
        System.out.print("Digite o ID do item para atualizar: ");
        int idAtualizar = sc.nextInt();
        sc.nextLine();
        boolean atualizado = false;

        for (Item item : inventario) {
            if (item.getId() == idAtualizar) {
                System.out.print("Novo nome: ");
                String novoNome = sc.nextLine();

                System.out.print("Nova quantidade: ");
                int novaQuantidade = sc.nextInt();
                sc.nextLine();

                if (novoNome.isEmpty() || novaQuantidade <= 0) {
                    System.out.println("Dados invalidos. Item nao atualizado.");
                } else {
                    item.setNome(novoNome);
                    item.setQuantidade(novaQuantidade);
                    System.out.println("Item atualizado com sucesso.");
                }
                atualizado = true;
                break;
            }
        }

        if (!atualizado) {
            System.out.println("Item nao encontrado.");
        }
    }

    public void removerItem(Scanner sc) {
        System.out.print("Digite o ID do item para remover: ");
        int idRemover = sc.nextInt();
        sc.nextLine();
        boolean removido = false;

        for (int i = 0; i < inventario.size(); i++) {
            if (inventario.get(i).getId() == idRemover) {
                inventario.remove(i);
                System.out.println("Item removido com sucesso.");
                removido = true;
                break;
            }
        }

        if (!removido) {
            System.out.println("Item nao encontrado.");
        }
    }
}
