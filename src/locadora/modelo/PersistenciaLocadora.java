package locadora.modelo;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import locadora.database.Autenticacao;
import locadora.database.Banco;

final class PersistenciaLocadora implements AutoCloseable {
    private final Banco banco;

    PersistenciaLocadora(Banco banco) { this.banco = banco; }

    void inicializar(Locadora locadora, Supplier<Locadora> dadosIniciais) {
        banco.inicializar(() -> {
            int versao = banco.versaoDoEsquema();
            if (versao < 0 || versao > 2) {
                throw new Banco.Erro("O banco foi criado com uma versão diferente do Vértice.");
            }
            if (versao == 0) {
                banco.criarTabelas();
                Locadora exemplo = dadosIniciais.get();
                exemplo.getCarros().forEach(this::inserir);
                exemplo.getClientes().forEach(this::inserir);
                exemplo.getFuncionarios().forEach(this::inserir);
                exemplo.getReservas().forEach(this::inserir);
                exemplo.getAlugueis().forEach(this::inserir);
                exemplo.getPagamentos().forEach(this::inserir);
            }
            if (versao < 2) banco.criarTabelaAcesso();
            carregar(locadora);
        });
    }

    private void carregar(Locadora locadora) {
        Map<Integer, Carro> carros = lerTabela("carro", r -> {
            Carro carro = new Carro(r.getInt("id"), r.getString("chassi"), r.getString("placa"), r.getString("marca"),
                    r.getString("modelo"), r.getInt("ano"), r.getString("cor"), moeda(r, "valor_diario"));
            carro.alterarStatus(Carro.Status.valueOf(r.getString("status")));
            return carro;
        });
        Map<Integer, Cliente> clientes = lerTabela("cliente", r -> new Cliente(r.getInt("id"), r.getString("nome"),
                r.getString("cpf"), r.getString("email"), r.getString("telefone"), r.getString("cnh"),
                data(r, "data_nascimento")));
        Map<Integer, Funcionario> funcionarios = lerTabela("funcionario", r -> new Funcionario(r.getInt("id"),
                r.getString("nome"), r.getString("cpf"), r.getString("email"), r.getString("telefone"),
                data(r, "data_admissao")));
        Map<Integer, Reserva> reservas = lerTabela("reserva", r -> new Reserva(r.getInt("id"),
                referencia(carros, r, "carro_id"), referencia(clientes, r, "cliente_id"),
                data(r, "data_reserva"), data(r, "data_inicio_prevista"), data(r, "data_fim_prevista"),
                Reserva.Status.valueOf(r.getString("status"))));
        Map<Integer, Aluguel> alugueis = lerTabela("aluguel", r -> new Aluguel(r.getInt("id"),
                referencia(carros, r, "carro_id"), referencia(clientes, r, "cliente_id"),
                referencia(funcionarios, r, "funcionario_id"), data(r, "data_inicio"), data(r, "data_fim"),
                data(r, "data_devolucao"), moeda(r, "diaria_contratada"), Aluguel.Status.valueOf(r.getString("status"))));
        Map<Integer, Pagamento> pagamentos = lerTabela("pagamento", r -> new Pagamento(r.getInt("id"),
                referencia(alugueis, r, "aluguel_id"), moeda(r, "valor"), data(r, "data_pagamento"),
                Pagamento.Forma.valueOf(r.getString("forma_pagamento")), Pagamento.Status.valueOf(r.getString("status"))));

        carros.values().forEach(locadora::adicionar);
        clientes.values().forEach(locadora::adicionar);
        funcionarios.values().forEach(locadora::adicionar);
        reservas.values().forEach(locadora::adicionar);
        alugueis.values().forEach(locadora::adicionar);
        pagamentos.values().forEach(locadora::adicionar);
    }

    private <T> Map<Integer, T> lerTabela(String tabela, Banco.Leitura<T> lerLinha) {
        return banco.consultar("SELECT * FROM " + tabela + " ORDER BY id", linhas -> {
            Map<Integer, T> registros = new LinkedHashMap<>();
            while (linhas.next()) registros.put(linhas.getInt("id"), lerLinha.ler(linhas));
            return registros;
        });
    }

    private static <T> T referencia(Map<Integer, T> registros, ResultSet linha, String coluna) throws SQLException {
        T registro = registros.get(linha.getInt(coluna));
        if (registro == null) throw new SQLException("Referência inválida na coluna " + coluna + ".");
        return registro;
    }

    private static LocalDate data(ResultSet linha, String coluna) throws SQLException {
        String valor = linha.getString(coluna);
        return valor == null ? null : LocalDate.parse(valor);
    }

    private static BigDecimal moeda(ResultSet linha, String coluna) throws SQLException {
        return new BigDecimal(linha.getString(coluna));
    }

    void inserir(Carro carro) {
        banco.executar("INSERT INTO carro (id, chassi, placa, marca, modelo, ano, cor, valor_diario, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                carro.getIdCarro(), carro.getChassi(), carro.getPlaca(), carro.getMarca(), carro.getModelo(),
                carro.getAno(), carro.getCor(), carro.getValorDiario().toPlainString(), carro.getStatus().name());
    }

    void atualizar(Carro carro) {
        banco.executar("UPDATE carro SET chassi = ?, placa = ?, marca = ?, modelo = ?, ano = ?, "
                        + "cor = ?, valor_diario = ? WHERE id = ?",
                carro.getChassi(), carro.getPlaca(), carro.getMarca(), carro.getModelo(), carro.getAno(),
                carro.getCor(), carro.getValorDiario().toPlainString(), carro.getIdCarro());
    }

    void inserir(Cliente cliente) {
        banco.executar("INSERT INTO cliente (id, nome, cpf, email, telefone, cnh, data_nascimento) VALUES (?, ?, ?, ?, ?, ?, ?)",
                cliente.getIdCliente(), cliente.getNome(), cliente.getCpf(), cliente.getEmail(), cliente.getTelefone(),
                cliente.getCnh(), cliente.getDataNascimento().toString());
    }

    void atualizar(Cliente cliente) {
        banco.executar("UPDATE cliente SET nome = ?, cpf = ?, email = ?, telefone = ?, cnh = ?, data_nascimento = ? WHERE id = ?",
                cliente.getNome(), cliente.getCpf(), cliente.getEmail(), cliente.getTelefone(), cliente.getCnh(),
                cliente.getDataNascimento().toString(), cliente.getIdCliente());
    }

    void inserir(Funcionario funcionario) {
        banco.executar("INSERT INTO funcionario (id, nome, cpf, email, telefone, data_admissao) VALUES (?, ?, ?, ?, ?, ?)",
                funcionario.getIdFuncionario(), funcionario.getNome(), funcionario.getCpf(), funcionario.getEmail(),
                funcionario.getTelefone(), funcionario.getDataAdmissao().toString());
    }

    void atualizar(Funcionario funcionario) {
        banco.executar("UPDATE funcionario SET nome = ?, cpf = ?, email = ?, telefone = ?, data_admissao = ? WHERE id = ?",
                funcionario.getNome(), funcionario.getCpf(), funcionario.getEmail(), funcionario.getTelefone(),
                funcionario.getDataAdmissao().toString(), funcionario.getIdFuncionario());
    }

    void inserir(Reserva reserva) {
        banco.executar("INSERT INTO reserva (id, carro_id, cliente_id, data_reserva, data_inicio_prevista, data_fim_prevista, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                reserva.getIdReserva(), reserva.getCarro().getIdCarro(), reserva.getCliente().getIdCliente(),
                reserva.getDataReserva().toString(), reserva.getDataInicioPrevista().toString(),
                reserva.getDataFimPrevista().toString(), reserva.getStatus().name());
    }

    void inserir(Aluguel aluguel) {
        banco.executar("INSERT INTO aluguel "
                        + "(id, carro_id, cliente_id, funcionario_id, data_inicio, data_fim, data_devolucao, diaria_contratada, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                aluguel.getIdAluguel(), aluguel.getCarro().getIdCarro(), aluguel.getCliente().getIdCliente(),
                aluguel.getFuncionario().getIdFuncionario(), aluguel.getDataInicio().toString(), aluguel.getDataFim().toString(),
                dataSql(aluguel.getDataDevolucao()), aluguel.getDiariaContratada().toPlainString(), aluguel.getStatus().name());
    }

    void inserir(Pagamento pagamento) {
        banco.executar("INSERT INTO pagamento (id, aluguel_id, valor, data_pagamento, forma_pagamento, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                pagamento.getIdPagamento(), pagamento.getAluguel().getIdAluguel(), pagamento.getValor().toPlainString(),
                pagamento.getDataPagamento().toString(), pagamento.getFormaPagamento().name(), pagamento.getStatus().name());
    }

    void atualizar(Carro carro, Carro.Status status) {
        banco.executar("UPDATE carro SET status = ? WHERE id = ?", status.name(), carro.getIdCarro());
    }

    void atualizar(Reserva reserva, Reserva.Status status) {
        banco.executar("UPDATE reserva SET status = ? WHERE id = ?", status.name(), reserva.getIdReserva());
    }

    void atualizar(Aluguel aluguel, Aluguel.Status status, LocalDate devolucao) {
        banco.executar("UPDATE aluguel SET status = ?, data_devolucao = ? WHERE id = ?",
                status.name(), dataSql(devolucao), aluguel.getIdAluguel());
    }

    void atualizar(Pagamento pagamento, Pagamento.Status status) {
        banco.executar("UPDATE pagamento SET status = ? WHERE id = ?", status.name(), pagamento.getIdPagamento());
    }

    private static String dataSql(LocalDate data) { return data == null ? null : data.toString(); }

    void gravar(Runnable operacao) { banco.gravar(operacao); }
    Autenticacao autenticacao() { return new Autenticacao(banco); }
    @Override public void close() { banco.close(); }
}
