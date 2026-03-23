import java.util.Scanner;

public class Calc {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
       
        // pedir pra digitar o numero

        System.out.print("Digite um numero inteiro: ");
        int numero = sc.nextInt();
        
        // ver se o numero eh valido  

        if (numero <= 0){
            System.out.println("Número inválido. Por favor, digite um número maior que zero.");
        }else {
            System.out.println("\nTabuada do " + numero + ":");
            
        // for pra contar de 1 a 10 e multiplicar    
            
            for (int i = 1; i <= 10; i++){
                int resultado = numero * i;
        
        // verificar se eh impar ou par
                
                String tipo;
                if (resultado % 2 == 0){
                    tipo = "par";
                }else {
                    tipo = "impar";
                }

                System.out.println(numero + " x " + i + " = " + resultado + " (" + tipo + ")");
                
            }
        }
        sc.close();
    }
}