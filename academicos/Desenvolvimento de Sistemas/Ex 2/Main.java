public class Main {
    public static void main(String[] args) {
        GatewayPagamento gateway = new GatewayPagamento();

        Pagamento pagamentoPix = new PagamentoPix(150.00);
        Pagamento pagamentoCartao = new PagamentoCartao(6000.00);

        gateway.realizarCobranca(pagamentoPix);
        gateway.realizarCobranca(pagamentoCartao);
    }
}
