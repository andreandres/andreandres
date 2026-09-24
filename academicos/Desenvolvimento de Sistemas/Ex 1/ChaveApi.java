public class ChaveApi {
    private final String token;
    private String plano;
    private int limiteRequisicoes;
    private int requisicoesRealizadas;
    private boolean ativa;

    public ChaveApi(String token, String plano, int limiteRequisicoes) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("O token deve ser informado.");
        }
        validarPlano(plano);
        if (limiteRequisicoes <= 0) {
            throw new IllegalArgumentException("O limite deve ser maior que zero.");
        }

        this.token = token;
        this.plano = plano;
        this.limiteRequisicoes = limiteRequisicoes;
        this.requisicoesRealizadas = 0;
        this.ativa = true;
    }

    public boolean registrarChamada() {
        if (!ativa || requisicoesRealizadas >= limiteRequisicoes) {
            return false;
        }

        requisicoesRealizadas++;
        return true;
    }

    public void fazerUpgrade(String novoPlano, int novoLimite) {
        validarPlano(novoPlano);
        if (novoLimite <= limiteRequisicoes || novoLimite <= requisicoesRealizadas) {
            throw new IllegalArgumentException(
                    "O novo limite deve ser maior que o limite atual e que as requisicoes realizadas.");
        }

        this.plano = novoPlano;
        this.limiteRequisicoes = novoLimite;
    }

    public void resetarCiclo() {
        requisicoesRealizadas = 0;
    }

    public void bloquearChave() {
        ativa = false;
    }

    public void desbloquearChave() {
        ativa = true;
    }

    public String getToken() {
        return token;
    }

    public String getPlano() {
        return plano;
    }

    private void validarPlano(String plano) {
        if (!"Basic".equals(plano) && !"Pro".equals(plano) && !"Enterprise".equals(plano)) {
            throw new IllegalArgumentException("Plano invalido. Use Basic, Pro ou Enterprise.");
        }
    }
}