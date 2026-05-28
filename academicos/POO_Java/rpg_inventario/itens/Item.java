package itens;

public class Item {
    private static int proximoId = 1;
    private final int id;
    private String nome;
    private String tipo;
    private int quantidade;

    public Item(String nome, String tipo, int quantidade) {
        this.id = proximoId++;
        this.nome = nome;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Nome: " + nome + " | Tipo: " + tipo + " | Quantidade: " + quantidade;
    }
}
