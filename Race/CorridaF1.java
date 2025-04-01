package Race;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;

public class CorridaF1 {
    private static final int NUM_CARROS = 5;
    private static final int VOLTAS_TOTAIS = 10;
    private static final int COMPRIMENTO_PISTA = 5000; // metros
    private static boolean safetyCar = false;
    private static final CopyOnWriteArrayList<Carro> classificacao = new CopyOnWriteArrayList<>();
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("Iniciando a corrida de F1!");
        System.out.println("Número de carros: " + NUM_CARROS);
        System.out.println("Número de voltas: " + VOLTAS_TOTAIS);
        System.out.println("\nPilotes na pista:");

        // Criar e iniciar as threads dos carros
        List<Thread> threads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(NUM_CARROS);

        for (int i = 1; i <= NUM_CARROS; i++) {
            Carro carro = new Carro(i, VOLTAS_TOTAIS, COMPRIMENTO_PISTA, latch);
            Thread thread = new Thread(carro);
            threads.add(thread);
            System.out.println("Carro " + i + " - Piloto: " + carro.getNomePiloto());
            thread.start();
        }

        // Thread para controlar eventos aleatórios na corrida
        Thread eventoThread = new Thread(() -> {
            while (classificacao.size() < NUM_CARROS) {
                try {
                    Thread.sleep(5000); // Verifica a cada 5 segundos
                    // 10% de chance de acionar o safety car
                    if (random.nextInt(100) < 10 && !safetyCar) {
                        safetyCar = true;
                        System.out.println("\n[EVENTO] Safety Car acionado! Todos os carros reduzindo a velocidade.");
                        // Safety car fica ativo por um tempo aleatório entre 10 e 20 segundos
                        Thread.sleep(10000 + random.nextInt(10000));
                        safetyCar = false;
                        System.out.println("\n[EVENTO] Safety Car recolhido! Corrida normalizada.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        eventoThread.start();

        // Aguardar todas as threads terminarem
        try {
            latch.await();
            eventoThread.interrupt(); // Interrompe a thread de eventos quando a corrida terminar

            // Mostrar a classificação final
            System.out.println("\n===== RESULTADO FINAL DA CORRIDA =====\n");
            if (classificacao.isEmpty()) {
                System.out.println("Nenhum carro completou a corrida!");
            } else {
                for (int i = 0; i < classificacao.size(); i++) {
                    Carro carro = classificacao.get(i);
                    if (carro.isTerminouCorrida()) {
                        System.out.println((i + 1) + "º lugar:Carro " + carro.getNumero() +
                                " - Piloto: " + carro.getNomePiloto() +
                                " - Tempo: " + String.format("%.2f", carro.getTempoTotal()) + " segundos");
                    }
                }

                System.out.println("\n----- CARROS QUE NÃO TERMINARAM A CORRIDA -----\n");
                boolean algumDesistente = false;
                for (Carro carro : classificacao) {
                    if (!carro.isTerminouCorrida()) {
                        algumDesistente = true;
                        System.out.println("Carro " + carro.getNumero() +
                                " - Piloto: " + carro.getNomePiloto() +
                                " - Motivo: " + carro.getMotivoDesistencia() +
                                " - Voltas completadas: " + carro.getVoltasCompletadas());
                    }
                }

                if (!algumDesistente) {
                    System.out.println("Todos os carros completaram a corrida com sucesso!");
                }
            }

        } catch (InterruptedException e) {
            System.out.println("Corrida interrompida!");
        }
    }

    // Método para verificar se o safety car está ativo
    public static boolean isSafetyCar() {
        return safetyCar;
    }

    // Método para adicionar um carro à classificação
    public static void adicionarClassificacao(Carro carro) {
        classificacao.add(carro);
    }
}