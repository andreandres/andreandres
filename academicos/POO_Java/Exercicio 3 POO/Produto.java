public class Produto {
    private String nome;
    private double preco;
    private int quantidadeEmEstoque;

    public Produto(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
        this.quantidadeEmEstoque = 0; 
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }
    
    public void adicionarEstoque(int quantidade) {
        if (quantidade > 0) {
            quantidadeEmEstoque += quantidade;
            System.out.println(quantidade + " unidades de " + nome + " adicionadas ao estoque.");
        } else {
            System.out.println("Erro: A quantidade para adicionar deve ser maior que zero.");
        }
    }

    public void vender(int quantidade) {
        if (quantidade <= 0) {
            System.out.println("Erro: Quantidade de venda inválida.");
        } else if (quantidade > quantidadeEmEstoque) {
            System.out.println("Estoque insuficiente para a venda."); 
        } else {
            quantidadeEmEstoque -= quantidade;
            System.out.println("Venda realizada com sucesso: " + quantidade + " unidades de " + nome + ".");
        }
    }

    public void exibirResumo() {
        System.out.println("-------------------------");
        System.out.println("Resumo do Produto:");
        System.out.println("Nome: " + nome);
        System.out.println("Preço: R$ " + preco);
        System.out.println("Estoque atual: " + quantidadeEmEstoque + " unidades");
        System.out.println("-------------------------");
    }
}