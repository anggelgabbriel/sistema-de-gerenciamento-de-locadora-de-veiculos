package locadora.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import static locadora.modelo.Validacao.*;

public final class Pagamento {
    public enum Status { PENDENTE, CONFIRMADO, ESTORNADO }
    public enum Forma { PIX, CREDITO, DEBITO, DINHEIRO }

    private final int idPagamento;
    private final BigDecimal valor;
    private final LocalDate dataPagamento;
    private final Forma formaPagamento;
    private final Aluguel aluguel;
    private Status status;

    Pagamento(int id, Aluguel aluguel, BigDecimal valor, Forma forma) {
        this(id, aluguel, valor, LocalDate.now(), forma, Status.PENDENTE);
    }

    Pagamento(int id, Aluguel aluguel, BigDecimal valor, LocalDate data, Forma forma, Status status) {
        idPagamento = id;
        this.aluguel = aluguel;
        this.valor = dinheiro(valor);
        dataPagamento = data;
        formaPagamento = java.util.Objects.requireNonNull(forma);
        this.status = status;
    }

    public boolean processarPagamento() {
        if (status != Status.PENDENTE || aluguel.getStatus() == Aluguel.Status.FINALIZADO
                || valor.compareTo(aluguel.getSaldo()) > 0) return false;
        confirmarPagamento();
        return true;
    }

    private void confirmarPagamento() { status = Status.CONFIRMADO; }

    public void estornarPagamento() {
        validarEstorno();
        status = Status.ESTORNADO;
    }

    void validarEstorno() {
        exigir(status == Status.CONFIRMADO && aluguel.getStatus() != Aluguel.Status.FINALIZADO,
                "Só é possível estornar um pagamento confirmado antes de finalizar o aluguel.");
    }

    public int getIdPagamento() { return idPagamento; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public Forma getFormaPagamento() { return formaPagamento; }
    public Aluguel getAluguel() { return aluguel; }
    public Status getStatus() { return status; }
}
