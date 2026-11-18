package locadora.modelo;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Locale;
import static locadora.modelo.Validacao.*;

public final class Carro {
    public enum Status { DISPONIVEL, ALUGADO, MANUTENCAO }

    private final int idCarro;
    private int ano;
    private String chassi, placa, modelo, marca, cor;
    private BigDecimal valorDiario;
    private Status status = Status.DISPONIVEL;

    public Carro(int id, String chassi, String placa, String marca, String modelo,
                 int ano, String cor, BigDecimal valorDiario) {
        this.idCarro = id;
        this.chassi = texto(chassi, "Chassi").toUpperCase(Locale.ROOT);
        this.placa = texto(placa, "Placa").toUpperCase(Locale.ROOT).replace("-", "");
        exigir(this.chassi.matches("[A-HJ-NPR-Z0-9]{17}"), "Informe um chassi válido de 17 caracteres.");
        exigir(this.placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}"), "Informe uma placa brasileira válida.");
        this.marca = texto(marca, "Marca");
        this.modelo = texto(modelo, "Modelo");
        exigir(ano >= 1900 && ano <= Year.now().getValue() + 1, "Informe um ano de fabricação válido.");
        this.ano = ano;
        this.cor = texto(cor, "Cor");
        this.valorDiario = dinheiro(valorDiario);
    }

    public boolean verificarDisponibilidade() { return status == Status.DISPONIVEL; }
    void alterarStatus(Status novo) { status = java.util.Objects.requireNonNull(novo); }

    void atualizarCadastro(Carro dados) {
        chassi = dados.chassi;
        placa = dados.placa;
        modelo = dados.modelo;
        marca = dados.marca;
        cor = dados.cor;
        ano = dados.ano;
        valorDiario = dados.valorDiario;
    }

    public BigDecimal calcularValor(long dias) {
        exigir(dias > 0, "O período deve ter pelo menos um dia.");
        return valorDiario.multiply(BigDecimal.valueOf(dias));
    }

    public int getIdCarro() { return idCarro; }
    public String getChassi() { return chassi; }
    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public String getMarca() { return marca; }
    public int getAno() { return ano; }
    public String getCor() { return cor; }
    public Status getStatus() { return status; }
    public BigDecimal getValorDiario() { return valorDiario; }
    @Override public String toString() { return marca + " " + modelo + " · " + placa; }
}
