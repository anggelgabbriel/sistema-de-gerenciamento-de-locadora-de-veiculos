package locadora.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static locadora.modelo.Validacao.*;

public final class Aluguel {
    public enum Status { ATIVO, DEVOLVIDO, FINALIZADO }

    private final int idAluguel;
    private final Carro carro;
    private final Cliente cliente;
    private final Funcionario funcionario;
    private final LocalDate dataInicio, dataFim;
    private final BigDecimal diaria;
    private final List<Pagamento> pagamentos = new ArrayList<>();
    private LocalDate dataDevolucao;
    private Status status;

    Aluguel(int id, Carro carro, Cliente cliente, Funcionario funcionario, LocalDate fim) {
        this(id, carro, cliente, funcionario, LocalDate.now(), fim, null, carro.getValorDiario(), Status.ATIVO);
    }

    Aluguel(int id, Carro carro, Cliente cliente, Funcionario funcionario, LocalDate inicio, LocalDate fim,
            LocalDate devolucao, BigDecimal diaria, Status status) {
        periodo(inicio, fim);
        idAluguel = id;
        this.carro = carro;
        this.cliente = cliente;
        this.funcionario = funcionario;
        dataInicio = inicio;
        dataFim = fim;
        dataDevolucao = devolucao;
        this.diaria = dinheiro(diaria);
        this.status = status;
    }

    public BigDecimal calcularValorTotal() {
        LocalDate fimCobrado = dataDevolucao != null && dataDevolucao.isAfter(dataFim) ? dataDevolucao : dataFim;
        return diaria.multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(dataInicio, fimCobrado) + 1));
    }

    public void registrarDevolucao(LocalDate data) {
        validarDevolucao(data);
        dataDevolucao = data;
        status = Status.DEVOLVIDO;
        carro.alterarStatus(Carro.Status.DISPONIVEL);
    }

    public void finalizarAluguel() {
        validarFinalizacao();
        status = Status.FINALIZADO;
    }

    void validarDevolucao(LocalDate data) {
        exigir(status == Status.ATIVO, "Este aluguel já foi devolvido.");
        periodo(dataInicio, data);
        dataPassadaOuHoje(data, "A devolução");
    }

    void validarFinalizacao() {
        exigir(status == Status.DEVOLVIDO, "Registre a devolução antes de finalizar.");
        exigir(getSaldo().signum() == 0, "Quite o saldo antes de finalizar o aluguel.");
    }

    void adicionarPagamento(Pagamento pagamento) { pagamentos.add(pagamento); }
    BigDecimal getDiariaContratada() { return diaria; }
    public BigDecimal getSaldo() {
        return pagamentos.stream().filter(p -> p.getStatus() == Pagamento.Status.CONFIRMADO)
                .map(Pagamento::getValor).reduce(calcularValorTotal(), BigDecimal::subtract);
    }

    public int getIdAluguel() { return idAluguel; }
    public Carro getCarro() { return carro; }
    public Cliente getCliente() { return cliente; }
    public Funcionario getFuncionario() { return funcionario; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public Status getStatus() { return status; }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
    @Override public String toString() { return "#" + idAluguel + " · " + carro.getPlaca() + " · " + cliente; }
}
