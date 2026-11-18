package locadora.modelo;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import locadora.database.Autenticacao;
import locadora.database.Banco;
import static locadora.modelo.Validacao.*;

public final class Locadora implements AutoCloseable {
    private final PersistenciaLocadora persistencia;
    private final List<Carro> carros = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Funcionario> funcionarios = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();
    private final List<Aluguel> alugueis = new ArrayList<>();
    private final List<Pagamento> pagamentos = new ArrayList<>();

    public Locadora() { this(null); }

    private Locadora(PersistenciaLocadora persistencia) { this.persistencia = persistencia; }

    public static Locadora abrir(Path arquivo, Supplier<Locadora> dadosIniciais) {
        PersistenciaLocadora persistencia = new PersistenciaLocadora(new Banco(arquivo));
        Locadora locadora = new Locadora(persistencia);
        try {
            persistencia.inicializar(locadora, dadosIniciais);
            return locadora;
        } catch (RuntimeException e) {
            try { persistencia.close(); } catch (Banco.Erro falha) { e.addSuppressed(falha); }
            throw e;
        }
    }

    public Autenticacao autenticacao() {
        return persistencia.autenticacao();
    }

    public Carro cadastrarCarro(String chassi, String placa, String marca, String modelo,
                               int ano, String cor, BigDecimal diaria) {
        Carro carro = new Carro(proximoId(carros, Carro::getIdCarro), chassi, placa, marca, modelo, ano, cor, diaria);
        verificarCarro(null, carro);
        gravar(p -> p.inserir(carro));
        adicionar(carro);
        return carro;
    }

    public void editarCarro(Carro carro, String chassi, String placa, String marca, String modelo,
                            int ano, String cor, BigDecimal diaria) {
        exigir(carros.contains(carro), "Veículo não cadastrado.");
        Carro dados = new Carro(carro.getIdCarro(), chassi, placa, marca, modelo, ano, cor, diaria);
        verificarCarro(carro, dados);
        gravar(p -> p.atualizar(dados));
        carro.atualizarCadastro(dados);
    }

    private void verificarCarro(Carro atual, Carro dados) {
        exigir(carros.stream().noneMatch(c -> c != atual && (c.getPlaca().equals(dados.getPlaca())
                || c.getChassi().equals(dados.getChassi()))), "Já existe um carro com essa placa ou chassi.");
    }

    public Cliente cadastrarCliente(String nome, String cpf, String email, String telefone,
                                    String cnh, LocalDate nascimento) {
        Cliente cliente = new Cliente(proximoId(clientes, Cliente::getIdCliente), nome, cpf, email, telefone, cnh, nascimento);
        verificarCliente(null, cpf, cnh);
        gravar(p -> p.inserir(cliente));
        adicionar(cliente);
        return cliente;
    }

    public void editarCliente(Cliente cliente, String nome, String cpf, String email, String telefone,
                              String cnh, LocalDate nascimento) {
        exigir(clientes.contains(cliente), "Cliente não cadastrado.");
        verificarCliente(cliente, cpf, cnh);
        Cliente atualizado = new Cliente(cliente.getIdCliente(), nome, cpf, email, telefone, cnh, nascimento);
        gravar(p -> p.atualizar(atualizado));
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
        Funcionario funcionario = new Funcionario(proximoId(funcionarios, Funcionario::getIdFuncionario),
                nome, cpf, email, telefone, admissao);
        gravar(p -> p.inserir(funcionario));
        adicionar(funcionario);
        return funcionario;
    }

    public void editarFuncionario(Funcionario funcionario, String nome, String cpf,
                                  String email, String telefone, LocalDate admissao) {
        exigir(funcionarios.contains(funcionario), "Funcionário não cadastrado.");
        verificarCpfFuncionario(funcionario, cpf);
        Funcionario atualizado = new Funcionario(funcionario.getIdFuncionario(), nome, cpf, email, telefone, admissao);
        gravar(p -> p.atualizar(atualizado));
        funcionario.atualizarCadastro(nome, cpf, email, telefone, admissao);
    }

    private void verificarCpfFuncionario(Funcionario atual, String cpf) {
        String cpfLimpo = documento(cpf, 11, "CPF");
        exigir(funcionarios.stream().noneMatch(f -> f != atual && f.getCpf().equals(cpfLimpo)),
                "Já existe um funcionário com esse CPF.");
    }

    public Reserva solicitarReserva(Carro carro, Cliente cliente, LocalDate inicio, LocalDate fim) {
        verificarCadastro(carro, cliente);
        Reserva reserva = new Reserva(proximoId(reservas, Reserva::getIdReserva), carro, cliente, inicio, fim);
        exigir(reserva.verificarDisponibilidade(), "O carro está em manutenção.");
        verificarAgenda(carro, inicio, fim, null);
        gravar(p -> p.inserir(reserva));
        adicionar(reserva);
        return reserva;
    }

    public void confirmarReserva(Reserva reserva) {
        exigir(reservas.contains(reserva), "Reserva não cadastrada.");
        verificarAgenda(reserva.getCarro(), reserva.getDataInicioPrevista(), reserva.getDataFimPrevista(), reserva);
        reserva.validarConfirmacao();
        gravar(p -> p.atualizar(reserva, Reserva.Status.CONFIRMADA));
        reserva.confirmarReserva();
    }

    public void cancelarReserva(Reserva reserva) {
        exigir(reservas.contains(reserva), "Reserva não cadastrada.");
        reserva.validarCancelamento();
        gravar(p -> p.atualizar(reserva, Reserva.Status.CANCELADA));
        reserva.cancelarReserva();
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
        return abrirAluguel(reserva.getCarro(), reserva.getCliente(), funcionario,
                reserva.getDataFimPrevista(), reserva);
    }

    private Aluguel abrirAluguel(Carro carro, Cliente cliente, Funcionario funcionario, LocalDate fim, Reserva origem) {
        verificarCadastro(carro, cliente);
        exigir(funcionarios.contains(funcionario), "Funcionário não cadastrado.");
        Aluguel aluguel = new Aluguel(proximoId(alugueis, Aluguel::getIdAluguel), carro, cliente, funcionario, fim);
        exigir(carro.verificarDisponibilidade(), "O carro não está disponível para retirada.");
        exigir(cliente.verificarCNH(), "A CNH do cliente precisa ter 11 dígitos.");
        verificarAgenda(carro, aluguel.getDataInicio(), fim, origem);
        gravar(p -> {
            p.inserir(aluguel);
            p.atualizar(carro, Carro.Status.ALUGADO);
            if (origem != null) p.atualizar(origem, Reserva.Status.CONVERTIDA);
        });
        carro.alterarStatus(Carro.Status.ALUGADO);
        if (origem != null) origem.converter();
        adicionar(aluguel);
        return aluguel;
    }

    public void registrarDevolucao(Aluguel aluguel, LocalDate data) {
        exigir(alugueis.contains(aluguel), "Aluguel não cadastrado.");
        aluguel.validarDevolucao(data);
        gravar(p -> {
            p.atualizar(aluguel, Aluguel.Status.DEVOLVIDO, data);
            p.atualizar(aluguel.getCarro(), Carro.Status.DISPONIVEL);
        });
        aluguel.registrarDevolucao(data);
    }

    public void finalizarAluguel(Aluguel aluguel) {
        exigir(alugueis.contains(aluguel), "Aluguel não cadastrado.");
        aluguel.validarFinalizacao();
        gravar(p -> p.atualizar(aluguel, Aluguel.Status.FINALIZADO, aluguel.getDataDevolucao()));
        aluguel.finalizarAluguel();
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
        Carro.Status novoStatus = carro.verificarDisponibilidade() ? Carro.Status.MANUTENCAO : Carro.Status.DISPONIVEL;
        if (novoStatus == Carro.Status.MANUTENCAO) {
            exigir(reservas.stream().noneMatch(r -> r.getCarro() == carro && r.getStatus() == Reserva.Status.CONFIRMADA
                    && !r.getDataFimPrevista().isBefore(LocalDate.now())),
                    "Este carro tem uma reserva confirmada. Cancele a reserva antes de enviá-lo à manutenção.");
        }
        gravar(p -> p.atualizar(carro, novoStatus));
        carro.alterarStatus(novoStatus);
    }

    public Pagamento registrarPagamento(Aluguel aluguel, BigDecimal valor, Pagamento.Forma forma) {
        exigir(alugueis.contains(aluguel), "Aluguel não cadastrado.");
        exigir(forma != null, "Selecione a forma de pagamento.");
        Pagamento pagamento = new Pagamento(proximoId(pagamentos, Pagamento::getIdPagamento), aluguel, valor, forma);
        exigir(pagamento.processarPagamento(), "O valor excede o saldo ou o aluguel já foi finalizado.");
        gravar(p -> p.inserir(pagamento));
        adicionar(pagamento);
        return pagamento;
    }

    public void estornarPagamento(Pagamento pagamento) {
        exigir(pagamentos.contains(pagamento), "Pagamento não cadastrado.");
        pagamento.validarEstorno();
        gravar(p -> p.atualizar(pagamento, Pagamento.Status.ESTORNADO));
        pagamento.estornarPagamento();
    }

    private void verificarCadastro(Carro carro, Cliente cliente) {
        exigir(carros.contains(carro), "Veículo não cadastrado.");
        exigir(clientes.contains(cliente), "Cliente não cadastrado.");
    }

    void adicionar(Carro carro) { carros.add(carro); }
    void adicionar(Cliente cliente) { clientes.add(cliente); }
    void adicionar(Funcionario funcionario) { funcionarios.add(funcionario); }
    void adicionar(Reserva reserva) { reservas.add(reserva); }

    void adicionar(Aluguel aluguel) {
        alugueis.add(aluguel);
        aluguel.getCliente().vincularAluguel(aluguel);
        aluguel.getFuncionario().registrarAluguel(aluguel);
    }

    void adicionar(Pagamento pagamento) {
        pagamentos.add(pagamento);
        pagamento.getAluguel().adicionarPagamento(pagamento);
    }

    private static <T> int proximoId(List<T> registros, ToIntFunction<T> id) {
        return registros.stream().mapToInt(id).max().orElse(0) + 1;
    }

    private void gravar(Consumer<PersistenciaLocadora> operacao) {
        if (persistencia != null) persistencia.gravar(() -> operacao.accept(persistencia));
    }

    @Override public void close() {
        if (persistencia != null) persistencia.close();
    }

    public List<Carro> getCarros() { return Collections.unmodifiableList(carros); }
    public List<Cliente> getClientes() { return Collections.unmodifiableList(clientes); }
    public List<Funcionario> getFuncionarios() { return Collections.unmodifiableList(funcionarios); }
    public List<Reserva> getReservas() { return Collections.unmodifiableList(reservas); }
    public List<Aluguel> getAlugueis() { return Collections.unmodifiableList(alugueis); }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
}
