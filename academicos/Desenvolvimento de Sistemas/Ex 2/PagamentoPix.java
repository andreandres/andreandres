public class PagamentoPix extends Pagamento {
    public PagamentoPix(double valor) {
        super(valor);
    }

    @Override
    public boolean processar() {
        status = "APROVADO";
        return true;
    }
}
