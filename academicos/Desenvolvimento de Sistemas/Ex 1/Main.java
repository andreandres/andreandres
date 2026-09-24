public class Main {
    public static void main(String[] args) {
        ChaveApi chave = new ChaveApi("abc-123", "Basic", 2);

        System.out.println("Chamada 1: " + chave.registrarChamada());
        System.out.println("Chamada 2: " + chave.registrarChamada());
        System.out.println("Chamada acima do limite: " + chave.registrarChamada());

        chave.fazerUpgrade("Pro", 4);
        System.out.println("Chamada apos upgrade: " + chave.registrarChamada());

        chave.bloquearChave();
        System.out.println("Chamada com chave bloqueada: " + chave.registrarChamada());

        chave.desbloquearChave();
        chave.resetarCiclo();
        System.out.println("Chamada apos desbloqueio e reset: " + chave.registrarChamada());
    }
}
