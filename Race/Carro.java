package Race;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Carro implements Runnable {
    private final int numero;
    private final String nomePiloto;
    private final int voltasTotais;
    private final int comprimentoPista;
    private int voltasCompletadas;
    private double tempoTotal;
    private boolean terminouCorrida;
    private String motivoDesistencia;
    private final CountDownLatch latch;
    private final Random random;

    private static final int VELOCIDADE_BASE = 240; // km/h
    private static final int VARIACAO_VELOCIDADE = 60; // km/h
    private static final int VELOCIDADE_MAXIMA_BOX = 80; // km/h
    private static final int VELOCIDADE_SAFETY_CAR = 140; // km/h
    private static final int TEMPO_MINIMO_BOX = 2; // segundos
    private static final int TEMPO_MAXIMO_BOX = 5; // segundos
    private static final int PENALIDADE_TEMPO = 5; // segundos

    private static final String[] NOMES_PILOTOS = {
            "Hamilton", "Verstappen", "Leclerc", "Norris", "Alonso",
            "Sainz", "Pérez", "Russell", "Piastri", "Gasly",
            "Ocon", "Stroll", "Albon", "Bottas", "Tsunoda"
    };

    public Carro(int numero, int voltasTotais, int comprimentoPista, CountDownLatch latch) {
        this.numero = numero;
        this.nomePiloto = NOMES_PILOTOS[(numero - 1) % NOMES_PILOTOS.length];
        this.voltasTotais = voltasTotais;
        this.comprimentoPista = comprimentoPista;
        this.voltasCompletadas = 0;
        this.tempoTotal = 0;
        this.terminouCorrida = false;
        this.latch = latch;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            System.out.println("Carro " + numero + " ("+nomePiloto+") iniciou a corrida!");

            while (voltasCompletadas < voltasTotais) {
                if (random.nextInt(100) < 3) {
                    motivoDesistencia = "Problemas técnicos";
                    System.out.println("\n[DESISTÊNCIA] Carro " + numero + " ("+nomePiloto+") abandonou a corrida por problemas técnicos na volta " + (voltasCompletadas + 1));
                    break;
                }

                double velocidadeAtual = calcularVelocidadeAtual();

                double tempoVolta = (comprimentoPista / 1000.0) / (velocidadeAtual / 3600.0);

                if (voltasCompletadas >= 3 && random.nextInt(100) < 10) {
                    System.out.println("[BOX] Carro " + numero + " ("+nomePiloto+") está entrando nos boxes na volta " + (voltasCompletadas + 1));

                    if (random.nextInt(100) < 20) {
                        System.out.println("[PENALIDADE] Carro " + numero + " ("+nomePiloto+") recebeu penalidade por excesso de velocidade no box!");
                        tempoVolta += PENALIDADE_TEMPO;
                    }

                    double tempoBox = TEMPO_MINIMO_BOX + (random.nextDouble() * (TEMPO_MAXIMO_BOX - TEMPO_MINIMO_BOX));
                    tempoVolta += tempoBox;
                    System.out.println("[BOX] Carro #" + numero + " ("+nomePiloto+") gastou " + String.format("%.2f", tempoBox) + " segundos nos boxes");
                }

                tempoTotal += tempoVolta;
                voltasCompletadas++;

                Thread.sleep((long)(tempoVolta * 100)); // Escala de 1:100 para não demorar muito

                System.out.println("Carro #" + numero + " ("+nomePiloto+") completou a volta " + voltasCompletadas +
                        " - Tempo: " + String.format("%.2f", tempoVolta) + "s - Velocidade: " +
                        String.format("%.0f", velocidadeAtual) + " km/h");
            }

            if (voltasCompletadas >= voltasTotais) {
                terminouCorrida = true;
                System.out.println("\n[CHEGADA] Carro #" + numero + " ("+nomePiloto+") completou a corrida em " +
                        String.format("%.2f", tempoTotal) + " segundos!");
            }

        } catch (InterruptedException e) {
            motivoDesistencia = "Corrida interrompida";
            Thread.currentThread().interrupt();
        } finally {
            CorridaF1.adicionarClassificacao(this);
            latch.countDown();
        }
    }

    private double calcularVelocidadeAtual() {
        double velocidade = VELOCIDADE_BASE + (random.nextDouble() * VARIACAO_VELOCIDADE);

        if (CorridaF1.isSafetyCar()) {
            velocidade = Math.min(velocidade, VELOCIDADE_SAFETY_CAR);
        }

        return velocidade;
    }

    public int getNumero() {
        return numero;
    }

    public String getNomePiloto() {
        return nomePiloto;
    }

    public int getVoltasCompletadas() {
        return voltasCompletadas;
    }

    public double getTempoTotal() {
        return tempoTotal;
    }

    public boolean isTerminouCorrida() {
        return terminouCorrida;
    }

    public String getMotivoDesistencia() {
        return motivoDesistencia;
    }
}