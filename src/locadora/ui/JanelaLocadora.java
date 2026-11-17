package locadora.ui;

import java.awt.*;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import locadora.modelo.*;
import static locadora.ui.Formatos.*;
import static locadora.ui.Tema.*;

public final class JanelaLocadora extends JFrame {
    private enum Tela {
        INICIO("Visão geral", "Acompanhe o dia a dia da sua locadora."),
        FROTA("Frota", "Cadastre veículos e acompanhe a disponibilidade da frota."),
        CLIENTES("Clientes", "Gerencie os cadastros e consulte o histórico de cada cliente."),
        EQUIPE("Funcionários", "Mantenha atualizados os dados de quem registra os aluguéis."),
        RESERVAS("Reservas", "Organize a agenda da frota e registre a retirada de veículos reservados."),
        ALUGUEIS("Aluguéis", "Acompanhe retiradas, devoluções e saldos em aberto."),
        PAGAMENTOS("Pagamentos", "Consulte os lançamentos e registre pagamentos ou estornos.");

        private final String titulo, descricao;
        Tela(String titulo, String descricao) { this.titulo = titulo; this.descricao = descricao; }
    }

    private final Locadora locadora;
    private final boolean exemplo;
    private final Operacoes operacoes;
    private final Map<Tela, JButton> atalhos = new EnumMap<>(Tela.class);
    private final JPanel centro = painel(new BorderLayout(0, 18));
    private final JLabel mensagem = texto(" ", 12, Font.PLAIN, PETROLEO);
    private Tela tela = Tela.INICIO;

    public JanelaLocadora(Locadora locadora, boolean exemplo) {
        super("Vértice • Locadora de veículos");
        this.locadora = locadora;
        this.exemplo = exemplo;
        operacoes = new Operacoes(getRootPane(), locadora, this::atualizar);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        raiz.add(menu(), BorderLayout.WEST);
        JPanel conteudo = painel(new BorderLayout(0, 10));
        conteudo.setBorder(new EmptyBorder(20, 26, 12, 26));
        conteudo.add(centro, BorderLayout.CENTER);
        conteudo.add(mensagem, BorderLayout.SOUTH);
        raiz.add(conteudo, BorderLayout.CENTER);
        setContentPane(raiz);
        mostrar(Tela.INICIO);
        setMinimumSize(new Dimension(1030, 635));
        setSize(1190, 735);
        setLocationRelativeTo(null);
    }

    private JPanel menu() {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(MARINHO);
        menu.setPreferredSize(new Dimension(198, 0));
        menu.setBorder(new EmptyBorder(22, 16, 19, 16));

        JPanel itens = coluna();
        JLabel marca = texto("VÉRTICE", 26, Font.BOLD, BRANCO);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        itens.add(marca);
        itens.add(texto("LOCADORA DE VEÍCULOS", 10, Font.BOLD, MENTA));
        itens.add(Box.createVerticalStrut(34));
        itens.add(texto("NAVEGAÇÃO", 11, Font.BOLD, cor(0xA7B9CB)));
        itens.add(Box.createVerticalStrut(10));
        for (Tela pagina : Tela.values()) {
            JButton botao = new JButton(pagina.titulo);
            botao.setFont(fonte(14, Font.BOLD));
            botao.setBorder(new EmptyBorder(9, 12, 9, 12));
            botao.setHorizontalAlignment(SwingConstants.LEFT);
            botao.setAlignmentX(Component.LEFT_ALIGNMENT);
            botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            botao.setContentAreaFilled(false);
            botao.setOpaque(true);
            botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            botao.addActionListener(e -> mostrar(pagina));
            atalhos.put(pagina, botao);
            itens.add(botao);
            itens.add(Box.createVerticalStrut(4));
        }
        menu.add(itens, BorderLayout.NORTH);

        JPanel rodape = coluna();
        rodape.add(texto(exemplo ? "●  Modo demonstração" : "●  Sessão local", 12, Font.BOLD, MENTA));
        rodape.add(Box.createVerticalStrut(4));
        rodape.add(texto("Dados desta sessão", 12, Font.PLAIN, cor(0xB9CBD9)));
        menu.add(rodape, BorderLayout.SOUTH);
        return menu;
    }

    private void mostrar(Tela pagina) {
        tela = pagina;
        mensagem.setText(" ");
        atualizar(" ");
    }

    private void atualizar(String aviso) {
        mensagem.setText(aviso);
        atalhos.forEach((pagina, botao) -> {
            botao.setBackground(pagina == tela ? cor(0x1E4754) : MARINHO);
            botao.setForeground(pagina == tela ? MENTA : cor(0xD9E4ED));
        });
        centro.removeAll();
        centro.add(cabecalho(), BorderLayout.NORTH);
        switch (tela) {
            case INICIO:
                centro.add(PainelInicial.criar(locadora, () -> executar(operacoes::novoAluguel),
                        () -> mostrar(Tela.ALUGUEIS)), BorderLayout.CENTER);
                break;
            case FROTA: centro.add(frota()); break;
            case CLIENTES: centro.add(clientes()); break;
            case EQUIPE: centro.add(funcionarios()); break;
            case RESERVAS: centro.add(reservas()); break;
            case ALUGUEIS: centro.add(alugueis()); break;
            case PAGAMENTOS: centro.add(pagamentos()); break;
        }
        centro.revalidate();
        centro.repaint();
    }

    private JPanel cabecalho() {
        JPanel topo = painel(new BorderLayout(10, 0));
        JPanel titulos = coluna();
        titulos.add(texto(tela.titulo, 29, Font.BOLD, TEXTO));
        titulos.add(Box.createVerticalStrut(3));
        titulos.add(texto(tela.descricao, 13, Font.PLAIN, SUAVE));
        topo.add(titulos, BorderLayout.WEST);
        topo.add(texto("HOJE  /  " + data(LocalDate.now()), 12, Font.BOLD, SUAVE), BorderLayout.EAST);
        return topo;
    }

    private JPanel frota() {
        ListaRegistros<Carro> lista = new ListaRegistros<>(locadora.getCarros(),
                new String[]{"ID", "Placa", "Marca", "Modelo", "Ano", "Cor", "Diária", "Status"},
                c -> new Object[]{c.getIdCarro(), c.getPlaca(), c.getMarca(), c.getModelo(),
                        c.getAno(), c.getCor(), c.getValorDiario(), c.getStatus()}, true);
        return pagina(lista, acao("+ Novo veículo", true, operacoes::novoCarro),
                acao("Alternar manutenção", false, () -> {
                    Carro carro = lista.selecionado();
                    locadora.alternarManutencao(carro);
                    atualizar(carro.getPlaca() + " · " + nome(carro.getStatus()));
                }), acao("Ver chassi", false, () -> {
                    Carro carro = lista.selecionado();
                    JOptionPane.showMessageDialog(this, carro.getChassi(), carro.toString(), JOptionPane.INFORMATION_MESSAGE);
                }));
    }

    private JPanel clientes() {
        ListaRegistros<Cliente> lista = new ListaRegistros<>(locadora.getClientes(),
                new String[]{"ID", "Nome", "CPF", "Telefone", "E-mail", "CNH", "Nascimento"},
                c -> new Object[]{c.getIdCliente(), c.getNome(), c.getCpf(), c.getTelefone(),
                        c.getEmail(), c.getCnh(), c.getDataNascimento()}, true);
        return pagina(lista, acao("+ Novo cliente", true, () -> operacoes.cliente(null)),
                acao("Editar cadastro", false, () -> operacoes.cliente(lista.selecionado())),
                acao("Ver histórico", false, () -> {
                    Cliente cliente = lista.selecionado();
                    ListaRegistros<Aluguel> historico = new ListaRegistros<>(cliente.consultarAlugueis(),
                            new String[]{"ID", "Veículo", "Retirada", "Status"},
                            a -> new Object[]{a.getIdAluguel(), a.getCarro().getPlaca(), a.getDataInicio(), a.getStatus()}, false);
                    historico.painel().setPreferredSize(new Dimension(515, 236));
                    JOptionPane.showMessageDialog(this, historico.painel(), "Aluguéis de " + cliente.getNome(),
                            JOptionPane.PLAIN_MESSAGE);
                }));
    }

    private JPanel funcionarios() {
        ListaRegistros<Funcionario> lista = new ListaRegistros<>(locadora.getFuncionarios(),
                new String[]{"ID", "Nome", "CPF", "Telefone", "E-mail", "Admissão"},
                f -> new Object[]{f.getIdFuncionario(), f.getNome(), f.getCpf(), f.getTelefone(),
                        f.getEmail(), f.getDataAdmissao()}, true);
        return pagina(lista, acao("+ Novo funcionário", true, () -> operacoes.funcionario(null)),
                acao("Editar cadastro", false, () -> operacoes.funcionario(lista.selecionado())));
    }

    private JPanel reservas() {
        ListaRegistros<Reserva> lista = new ListaRegistros<>(locadora.getReservas(),
                new String[]{"ID", "Veículo", "Cliente", "Solicitação", "Início", "Fim", "Status"},
                r -> new Object[]{r.getIdReserva(), r.getCarro().getPlaca(), r.getCliente().getNome(),
                        r.getDataReserva(), r.getDataInicioPrevista(), r.getDataFimPrevista(), r.getStatus()}, true);
        return pagina(lista, acao("+ Nova reserva", true, operacoes::novaReserva),
                acao("Confirmar", false, () -> {
                    locadora.confirmarReserva(lista.selecionado());
                    atualizar("Reserva confirmada.");
                }), acao("Retirar veículo", false, () -> operacoes.retirarReserva(lista.selecionado())),
                acao("Cancelar reserva", false, () -> {
                    Reserva reserva = lista.selecionado();
                    operacoes.confirmar("Cancelar a reserva #" + reserva.getIdReserva() + "?",
                            reserva::cancelarReserva, "Reserva cancelada.");
                }));
    }

    private JPanel alugueis() {
        ListaRegistros<Aluguel> lista = new ListaRegistros<>(locadora.getAlugueis(),
                new String[]{"ID", "Placa", "Cliente", "Retirada", "Fim previsto", "Devolução", "Total", "Saldo", "Status"},
                a -> new Object[]{a.getIdAluguel(), a.getCarro().getPlaca(), a.getCliente().getNome(),
                        a.getDataInicio(), a.getDataFim(), a.getDataDevolucao(), a.calcularValorTotal(),
                        a.getSaldo(), a.getStatus()}, true);
        return pagina(lista, acao("+ Novo aluguel", true, operacoes::novoAluguel),
                acao("Registrar devolução", false, () -> operacoes.devolver(lista.selecionado())),
                acao("Receber pagamento", false, () -> operacoes.pagar(lista.selecionado())),
                acao("Finalizar", false, () -> {
                    lista.selecionado().finalizarAluguel();
                    atualizar("Aluguel finalizado.");
                }), acao("Ver responsável", false, () -> {
                    Aluguel aluguel = lista.selecionado();
                    JOptionPane.showMessageDialog(this, aluguel.getFuncionario().getNome(),
                            "Funcionário responsável pelo aluguel #" + aluguel.getIdAluguel(), JOptionPane.INFORMATION_MESSAGE);
                }));
    }

    private JPanel pagamentos() {
        ListaRegistros<Pagamento> lista = new ListaRegistros<>(locadora.getPagamentos(),
                new String[]{"ID", "Aluguel", "Cliente", "Forma", "Data", "Valor", "Status"},
                p -> new Object[]{p.getIdPagamento(), "#" + p.getAluguel().getIdAluguel(), p.getAluguel().getCliente().getNome(),
                        p.getFormaPagamento(), p.getDataPagamento(), p.getValor(), p.getStatus()}, true);
        return pagina(lista, acao("+ Novo pagamento", true, () -> operacoes.pagar(null)),
                acao("Estornar", false, () -> {
                    Pagamento pagamento = lista.selecionado();
                    operacoes.confirmar("Estornar o pagamento de " + moeda(pagamento.getValor()) + "?",
                            pagamento::estornarPagamento, "Pagamento estornado.");
                }));
    }

    private JPanel pagina(ListaRegistros<?> lista, JButton... acoes) {
        JPanel corpo = painel(new BorderLayout(0, 12));
        JPanel barra = painel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JButton acao : acoes) barra.add(acao);
        corpo.add(barra, BorderLayout.NORTH);
        corpo.add(lista.painel(), BorderLayout.CENTER);
        return corpo;
    }

    private JButton acao(String nome, boolean primario, Runnable acao) {
        return botao(nome, primario, () -> executar(acao));
    }

    private void executar(Runnable acao) {
        try { acao.run(); }
        catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Confira a operação", JOptionPane.WARNING_MESSAGE);
        }
    }
}
