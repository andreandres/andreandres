import java.util.UUID;

public abstract class Pagamento {
    protected String idTransacao;
    protected double valor;
    protected String status;

    protected Pagamento(double valor) {
        this.idTransacao = UUID.randomUUID().toString();
        this.valor = valor;
        this.status = "PENDENTE";
    }

    public void imprimirRecibo() {
        System.out.println("ID da transacao: " + idTransacao);
        System.out.printf("Valor: R$ %.2f%n", valor);
        System.out.println("Status: " + status);
        System.out.println();
    }

    public abstract boolean processar();
}
