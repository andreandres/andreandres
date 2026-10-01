public class PagamentoCartao extends Pagamento {
    public PagamentoCartao(double valor) {
        super(valor);
    }

    @Override
    public boolean processar() {
        status = "APROVADO";
        return true;
    }
}
