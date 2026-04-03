public class Main {
    public static void main(String[] args) {
        
        Produto perfume = new Produto("Vulcan Feu", 150.50);

        System.out.println("\nEstado Inicial");
        perfume.exibirResumo();

        System.out.println("\nAdição Válida");
        System.out.println("Tentando adicionar 10 unidades ao estoque");
        perfume.adicionarEstoque(10);
        
        System.out.println("\nAdição Inválida");
        System.out.println("Tentando adicionar -5 unidades ao estoque");
        perfume.adicionarEstoque(-5);
        System.out.println("\nTentando adicionar 0 unidades ao estoque");
        perfume.adicionarEstoque(0);

        System.out.println("\nVenda Válida");
        System.out.println("Tentando vender 3 unidades");
        perfume.vender(3);

        System.out.println("\nVenda Acima do Estoque");
        System.out.println("Tentando vender 10 unidades(Tem apenas 7 unidades no momento)");
        perfume.vender(10);

        System.out.println("\nVenda Inválida");
        System.out.println("Tentando vender -2 unidades");
        perfume.vender(-2);

        System.out.println("\nEstado do Objeto");
        perfume.exibirResumo();
    }
}