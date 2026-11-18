package locadora.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Banco implements AutoCloseable {
    private final Connection conexao;
    private int versaoDosDados;

    public Banco(Path arquivo) {
        try {
            conexao = DriverManager.getConnection("jdbc:sqlite:" + arquivo.toAbsolutePath());
        } catch (SQLException e) {
            throw new Erro("Não foi possível abrir o banco SQLite.", e);
        }
        try {
            comando("PRAGMA foreign_keys = ON");
            comando("PRAGMA busy_timeout = 3000");
        } catch (SQLException e) {
            try { conexao.close(); } catch (SQLException falha) { e.addSuppressed(falha); }
            throw new Erro("Não foi possível configurar o banco SQLite.", e);
        }
    }

    public int versaoDoEsquema() { return lerValor("PRAGMA user_version"); }

    public void criarTabelas() {
        executar("CREATE TABLE IF NOT EXISTS carro ("
                + "id INTEGER PRIMARY KEY, chassi TEXT NOT NULL UNIQUE, placa TEXT NOT NULL UNIQUE, "
                + "marca TEXT NOT NULL, modelo TEXT NOT NULL, ano INTEGER NOT NULL, cor TEXT NOT NULL, "
                + "valor_diario TEXT NOT NULL, status TEXT NOT NULL "
                + "CHECK (status IN ('DISPONIVEL', 'ALUGADO', 'MANUTENCAO')))");

        executar("CREATE TABLE IF NOT EXISTS cliente ("
                + "id INTEGER PRIMARY KEY, nome TEXT NOT NULL, cpf TEXT NOT NULL UNIQUE, "
                + "email TEXT NOT NULL, telefone TEXT NOT NULL, cnh TEXT NOT NULL UNIQUE, "
                + "data_nascimento TEXT NOT NULL)");

        executar("CREATE TABLE IF NOT EXISTS funcionario ("
                + "id INTEGER PRIMARY KEY, nome TEXT NOT NULL, cpf TEXT NOT NULL UNIQUE, "
                + "email TEXT NOT NULL, telefone TEXT NOT NULL, data_admissao TEXT NOT NULL)");

        executar("CREATE TABLE IF NOT EXISTS reserva ("
                + "id INTEGER PRIMARY KEY, carro_id INTEGER NOT NULL REFERENCES carro(id), "
                + "cliente_id INTEGER NOT NULL REFERENCES cliente(id), data_reserva TEXT NOT NULL, "
                + "data_inicio_prevista TEXT NOT NULL, data_fim_prevista TEXT NOT NULL, "
                + "status TEXT NOT NULL CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'CANCELADA', 'CONVERTIDA')))");

        executar("CREATE TABLE IF NOT EXISTS aluguel ("
                + "id INTEGER PRIMARY KEY, carro_id INTEGER NOT NULL REFERENCES carro(id), "
                + "cliente_id INTEGER NOT NULL REFERENCES cliente(id), "
                + "funcionario_id INTEGER NOT NULL REFERENCES funcionario(id), data_inicio TEXT NOT NULL, "
                + "data_fim TEXT NOT NULL, data_devolucao TEXT, diaria_contratada TEXT NOT NULL, "
                + "status TEXT NOT NULL CHECK (status IN ('ATIVO', 'DEVOLVIDO', 'FINALIZADO')), "
                + "CHECK ((status = 'ATIVO') = (data_devolucao IS NULL)))");

        executar("CREATE TABLE IF NOT EXISTS pagamento ("
                + "id INTEGER PRIMARY KEY, aluguel_id INTEGER NOT NULL REFERENCES aluguel(id), "
                + "valor TEXT NOT NULL, data_pagamento TEXT NOT NULL, forma_pagamento TEXT NOT NULL "
                + "CHECK (forma_pagamento IN ('PIX', 'CREDITO', 'DEBITO', 'DINHEIRO')), "
                + "status TEXT NOT NULL CHECK (status IN ('PENDENTE', 'CONFIRMADO', 'ESTORNADO')))");

    }

    public void criarTabelaAcesso() {
        executar("CREATE TABLE IF NOT EXISTS acesso ("
                + "id INTEGER PRIMARY KEY CHECK (id = 1), usuario TEXT NOT NULL, "
                + "sal BLOB NOT NULL, senha_hash BLOB NOT NULL)");
        executar("PRAGMA user_version = 2");
    }

    public void executar(String sql, Object... valores) {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) comando.setObject(i + 1, valores[i]);
            comando.executeUpdate();
        } catch (SQLException e) {
            throw new Erro("Não foi possível gravar a operação no banco.", e);
        }
    }

    @FunctionalInterface
    public interface Leitura<T> {
        T ler(ResultSet linhas) throws SQLException;
    }

    public <T> T consultar(String sql, Leitura<T> leitura) {
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet linhas = comando.executeQuery()) {
            return leitura.ler(linhas);
        } catch (SQLException e) {
            throw new Erro("Não foi possível ler os registros do banco.", e);
        }
    }

    public void inicializar(Runnable operacao) { transacao(operacao, false); }
    public void gravar(Runnable operacao) { transacao(operacao, true); }

    private void transacao(Runnable operacao, boolean verificarVersao) {
        try {
            comando("BEGIN IMMEDIATE");
            try {
                int versaoAtual = lerValor("PRAGMA data_version");
                if (verificarVersao && versaoAtual != versaoDosDados) {
                    throw new Erro("O banco mudou em outra janela ou programa. Reabra o Vértice antes de salvar.");
                }
                operacao.run();
                comando("COMMIT");
                versaoDosDados = versaoAtual;
            } catch (RuntimeException | SQLException e) {
                try { comando("ROLLBACK"); } catch (SQLException falha) { e.addSuppressed(falha); }
                throw e;
            }
        } catch (SQLException e) {
            throw new Erro("Não foi possível concluir a operação no banco SQLite.", e);
        }
    }

    private int lerValor(String sql) {
        return consultar(sql, linhas -> {
            if (!linhas.next()) throw new SQLException("O SQLite não retornou o valor solicitado.");
            return linhas.getInt(1);
        });
    }

    private void comando(String sql) throws SQLException {
        try (Statement comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }

    @Override
    public void close() {
        try {
            conexao.close();
        } catch (SQLException e) {
            throw new Erro("Não foi possível fechar o banco SQLite.", e);
        }
    }

    public static final class Erro extends RuntimeException {
        public Erro(String mensagem) { super(mensagem); }
        public Erro(String mensagem, Throwable causa) { super(mensagem, causa); }
    }
}
