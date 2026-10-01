public class GatewayPagamento {
    public void realizarCobranca(Pagamento pagamento) {
        pagamento.processar();
        pagamento.imprimirRecibo();
    }
}
