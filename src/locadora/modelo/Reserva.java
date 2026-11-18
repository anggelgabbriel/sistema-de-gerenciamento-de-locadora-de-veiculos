package locadora.modelo;

import java.time.LocalDate;
import static locadora.modelo.Validacao.*;

public final class Reserva {
    public enum Status { PENDENTE, CONFIRMADA, CANCELADA, CONVERTIDA }

    private final int idReserva;
    private final Carro carro;
    private final Cliente cliente;
    private final LocalDate dataReserva, dataInicioPrevista, dataFimPrevista;
    private Status status;

    Reserva(int id, Carro carro, Cliente cliente, LocalDate inicio, LocalDate fim) {
        this(id, carro, cliente, LocalDate.now(), inicio, fim, Status.PENDENTE);
    }

    Reserva(int id, Carro carro, Cliente cliente, LocalDate dataReserva,
            LocalDate inicio, LocalDate fim, Status status) {
        periodo(inicio, fim);
        exigir(!inicio.isBefore(dataReserva), "A reserva precisa começar hoje ou em uma data futura.");
        idReserva = id;
        this.carro = carro;
        this.cliente = cliente;
        this.dataReserva = dataReserva;
        dataInicioPrevista = inicio;
        dataFimPrevista = fim;
        this.status = status;
    }

    public boolean verificarDisponibilidade() {
        return carro.getStatus() != Carro.Status.MANUTENCAO && !dataFimPrevista.isBefore(LocalDate.now());
    }

    void confirmarReserva() {
        validarConfirmacao();
        status = Status.CONFIRMADA;
    }

    public void cancelarReserva() {
        validarCancelamento();
        status = Status.CANCELADA;
    }

    void validarConfirmacao() {
        exigir(status == Status.PENDENTE && verificarDisponibilidade(), "Esta reserva não pode ser confirmada.");
    }

    void validarCancelamento() {
        exigir(status == Status.PENDENTE || status == Status.CONFIRMADA, "Esta reserva não pode ser cancelada.");
    }

    void converter() { status = Status.CONVERTIDA; }
    public int getIdReserva() { return idReserva; }
    public Carro getCarro() { return carro; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getDataReserva() { return dataReserva; }
    public LocalDate getDataInicioPrevista() { return dataInicioPrevista; }
    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public Status getStatus() { return status; }
}
