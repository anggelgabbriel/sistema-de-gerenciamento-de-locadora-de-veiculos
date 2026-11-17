package locadora.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static locadora.modelo.Validacao.*;

/** Coordena operações que envolvem mais de uma entidade da locadora. */
public final class Locadora {
    private final List<Carro> carros = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Funcionario> funcionarios = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<Aluguel> alugueis = new ArrayList<>();
    private final List<Pagamento> pagamentos = new ArrayList<>();

    public Carro cadastrarCarro(String chassi, String placa, String marca, String modelo,
                               int ano, String cor, BigDecimal diaria) {
        Carro carro = new Carro(carros.size() + 1, chassi, placa, marca, modelo, ano, cor, diaria);
        exigir(carros.stream().noneMatch(c -> c.getPlaca().equals(carro.getPlaca())
                || c.getChassi().equals(carro.getChassi())), "Já existe um carro com essa placa ou chassi.");
        carros.add(carro);
        return carro;
    }

    public Cliente cadastrarCliente(String nome, String cpf, String email, String telefone,
                                    String cnh, LocalDate nascimento) {
        Cliente cliente = new Cliente(clientes.size() + 1, nome, cpf, email, telefone, cnh, nascimento);
        verificarCliente(null, cpf, cnh);
        clientes.add(cliente);
        return cliente;
    }

    public void editarCliente(Cliente cliente, String nome, String cpf, String email, String telefone,
                              String cnh, LocalDate nascimento) {
        exigir(clientes.contains(cliente), "Cliente não cadastrado.");
        verificarCliente(cliente, cpf, cnh);
        cliente.atualizarDados(nome, cpf, email, telefone, cnh, nascimento);
    }

    private void verificarCliente(Cliente atual, String cpf, String cnh) {
        String cpfLimpo = documento(cpf, 11, "CPF");
        String cnhLimpa = documento(cnh, 11, "CNH");
        exigir(clientes.stream().noneMatch(c -> c != atual && (c.getCpf().equals(cpfLimpo)
                || c.getCnh().equals(cnhLimpa))), "Já existe um cliente com esse CPF ou essa CNH.");
    }

    public Funcionario cadastrarFuncionario(String nome, String cpf, String email, String telefone, LocalDate admissao) {
        verificarCpfFuncionario(null, cpf);
        Funcionario funcionario = new Funcionario(funcionarios.size() + 1, nome, cpf, email, telefone, admissao);
        funcionarios.add(funcionario);
        return funcionario;
    }

    public void editarFuncionario(Funcionario funcionario, String nome, String cpf,
                                  String email, String telefone, LocalDate admissao) {
        exigir(funcionarios.contains(funcionario), "Funcionário não cadastrado.");
        verificarCpfFuncionario(funcionario, cpf);
        funcionario.atualizarCadastro(nome, cpf, email, telefone, admissao);
    }

    private void verificarCpfFuncionario(Funcionario atual, String cpf) {
        String cpfLimpo = documento(cpf, 11, "CPF");
        exigir(funcionarios.stream().noneMatch(f -> f != atual && f.getCpf().equals(cpfLimpo)),
                "Já existe um funcionário com esse CPF.");
    }

    public Reserva solicitarReserva(Carro carro, Cliente cliente, LocalDate inicio, LocalDate fim) {
        verificarCadastro(carro, cliente);
        Reserva reserva = new Reserva(reservas.size() + 1, carro, cliente, inicio, fim);
        exigir(reserva.verificarDisponibilidade(), "O carro está em manutenção.");
        verificarAgenda(carro, inicio, fim, null);
        reservas.add(reserva);
        return reserva;
    }

    public void confirmarReserva(Reserva reserva) {
        exigir(reservas.contains(reserva), "Reserva não cadastrada.");
        verificarAgenda(reserva.getCarro(), reserva.getDataInicioPrevista(), reserva.getDataFimPrevista(), reserva);
        reserva.confirmarReserva();
    }

    public Aluguel registrarAluguel(Carro carro, Cliente cliente, Funcionario funcionario, LocalDate fim) {
        return abrirAluguel(carro, cliente, funcionario, fim, null);
    }

    public Aluguel retirarReserva(Reserva reserva, Funcionario funcionario) {
        exigir(reservas.contains(reserva), "Reserva não cadastrada.");
        exigir(reserva.getStatus() == Reserva.Status.CONFIRMADA, "Confirme a reserva antes da retirada.");
        LocalDate hoje = LocalDate.now();
        exigir(!hoje.isBefore(reserva.getDataInicioPrevista()) && !hoje.isAfter(reserva.getDataFimPrevista()),
                "A retirada precisa ocorrer dentro do período reservado.");
        Aluguel aluguel = abrirAluguel(reserva.getCarro(), reserva.getCliente(), funcionario,
                reserva.getDataFimPrevista(), reserva);
        reserva.converter();
        return aluguel;
    }

    private Aluguel abrirAluguel(Carro carro, Cliente cliente, Funcionario funcionario, LocalDate fim, Reserva origem) {
        verificarCadastro(carro, cliente);
        exigir(funcionarios.contains(funcionario), "Funcionário não cadastrado.");
        Aluguel aluguel = new Aluguel(alugueis.size() + 1, carro, cliente, funcionario, fim);
        exigir(carro.verificarDisponibilidade(), "O carro não está disponível para retirada.");
        exigir(cliente.verificarCNH(), "A CNH do cliente precisa ter 11 dígitos.");
        verificarAgenda(carro, aluguel.getDataInicio(), fim, origem);
        carro.alterarStatus(Carro.Status.ALUGADO);
        alugueis.add(aluguel);
        cliente.vincularAluguel(aluguel);
        funcionario.registrarAluguel(aluguel);
        return aluguel;
    }

    private void verificarAgenda(Carro carro, LocalDate inicio, LocalDate fim, Reserva origem) {
        for (Reserva reserva : reservas) {
            if (reserva != origem && reserva.getCarro() == carro && reserva.getStatus() == Reserva.Status.CONFIRMADA
                    && sobrepoe(inicio, fim, reserva.getDataInicioPrevista(), reserva.getDataFimPrevista())) {
                throw new IllegalArgumentException("Já existe uma reserva confirmada para esse carro no período.");
            }
        }
        for (Aluguel aluguel : alugueis) {
            if (aluguel.getCarro() == carro && aluguel.getStatus() == Aluguel.Status.ATIVO) {
                LocalDate fimOcupado = aluguel.getDataFim();
                LocalDate hoje = LocalDate.now();
                if (fimOcupado.isBefore(hoje)) fimOcupado = hoje;
                exigir(!sobrepoe(inicio, fim, aluguel.getDataInicio(), fimOcupado),
                        "O carro está comprometido com um aluguel no período.");
            }
        }
    }

    private boolean sobrepoe(LocalDate inicio, LocalDate fim, LocalDate outroInicio, LocalDate outroFim) {
        return !inicio.isAfter(outroFim) && !outroInicio.isAfter(fim);
    }

    public void alternarManutencao(Carro carro) {
        exigir(carros.contains(carro), "Veículo não cadastrado.");
        exigir(carro.getStatus() != Carro.Status.ALUGADO, "Registre a devolução antes de enviar o carro à manutenção.");
        if (carro.verificarDisponibilidade()) {
            exigir(reservas.stream().noneMatch(r -> r.getCarro() == carro && r.getStatus() == Reserva.Status.CONFIRMADA
                    && !r.getDataFimPrevista().isBefore(LocalDate.now())),
                    "Este carro tem uma reserva confirmada. Cancele a reserva antes de enviá-lo à manutenção.");
            carro.alterarStatus(Carro.Status.MANUTENCAO);
        } else {
            carro.alterarStatus(Carro.Status.DISPONIVEL);
        }
    }

    public Pagamento registrarPagamento(Aluguel aluguel, BigDecimal valor, Pagamento.Forma forma) {
        exigir(alugueis.contains(aluguel), "Aluguel não cadastrado.");
        exigir(forma != null, "Selecione a forma de pagamento.");
        Pagamento pagamento = new Pagamento(pagamentos.size() + 1, aluguel, valor, forma);
        exigir(pagamento.processarPagamento(), "O valor excede o saldo ou o aluguel já foi finalizado.");
        pagamentos.add(pagamento);
        aluguel.adicionarPagamento(pagamento);
        return pagamento;
    }

    private void verificarCadastro(Carro carro, Cliente cliente) {
        exigir(carros.contains(carro), "Veículo não cadastrado.");
        exigir(clientes.contains(cliente), "Cliente não cadastrado.");
    }

    public List<Carro> getCarros() { return Collections.unmodifiableList(carros); }
    public List<Cliente> getClientes() { return Collections.unmodifiableList(clientes); }
    public List<Funcionario> getFuncionarios() { return Collections.unmodifiableList(funcionarios); }
    public List<Reserva> getReservas() { return Collections.unmodifiableList(reservas); }
    public List<Aluguel> getAlugueis() { return Collections.unmodifiableList(alugueis); }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
}
