package locadora.ui;

import java.awt.Component;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import locadora.modelo.*;
import static locadora.ui.Formatos.*;
import static locadora.ui.Formulario.*;

final class Operacoes {
    private final Component janela;
    private final Locadora locadora;
    private final Consumer<String> atualizado;

    Operacoes(Component janela, Locadora locadora, Consumer<String> atualizado) {
        this.janela = janela;
        this.locadora = locadora;
        this.atualizado = atualizado;
    }

    void carro(Carro atual) {
        Formulario form = new Formulario(janela, atual == null ? "Cadastrar veículo" : "Editar veículo");
        JTextField marca = form.campo("Marca", atual == null ? "" : atual.getMarca());
        JTextField modelo = form.campo("Modelo", atual == null ? "" : atual.getModelo());
        JTextField placa = form.campo("Placa", atual == null ? "" : atual.getPlaca());
        JTextField chassi = form.campo("Chassi", atual == null ? "" : atual.getChassi());
        JTextField ano = form.campo("Ano", atual == null ? "" : Integer.toString(atual.getAno()));
        JTextField cor = form.campo("Cor", atual == null ? "" : atual.getCor());
        JTextField diaria = form.campo("Diária em R$", atual == null ? "" : decimal(atual.getValorDiario()));
        form.abrir("Salvar", () -> {
            if (atual == null) {
                locadora.cadastrarCarro(chassi.getText(), placa.getText(), marca.getText(), modelo.getText(),
                        lerAno(ano), cor.getText(), Formulario.lerMoeda(diaria));
            } else {
                locadora.editarCarro(atual, chassi.getText(), placa.getText(), marca.getText(), modelo.getText(),
                        lerAno(ano), cor.getText(), Formulario.lerMoeda(diaria));
            }
            atualizado.accept("Cadastro do veículo salvo.");
        });
    }

    void cliente(Cliente atual) {
        Formulario form = new Formulario(janela, atual == null ? "Cadastrar cliente" : "Editar cliente");
        Contato contato = new Contato(form, atual);
        JTextField cnh = form.campo("CNH", atual == null ? "" : atual.getCnh());
        JTextField nascimento = form.campo("Nascimento (dd/mm/aaaa)", atual == null ? "" : data(atual.getDataNascimento()));
        form.abrir("Salvar", () -> {
            if (atual == null) {
                locadora.cadastrarCliente(contato.nome(), contato.cpf(), contato.email(), contato.telefone(),
                        cnh.getText(), Formulario.lerData(nascimento));
            } else {
                locadora.editarCliente(atual, contato.nome(), contato.cpf(), contato.email(), contato.telefone(),
                        cnh.getText(), Formulario.lerData(nascimento));
            }
            atualizado.accept("Cadastro de cliente salvo.");
        });
    }

    void funcionario(Funcionario atual) {
        Formulario form = new Formulario(janela, atual == null ? "Cadastrar funcionário" : "Editar funcionário");
        Contato contato = new Contato(form, atual);
        JTextField admissao = form.campo("Admissão (dd/mm/aaaa)",
                data(atual == null ? LocalDate.now() : atual.getDataAdmissao()));
        form.abrir("Salvar", () -> {
            if (atual == null) {
                locadora.cadastrarFuncionario(contato.nome(), contato.cpf(), contato.email(), contato.telefone(),
                        Formulario.lerData(admissao));
            } else {
                locadora.editarFuncionario(atual, contato.nome(), contato.cpf(), contato.email(), contato.telefone(),
                        Formulario.lerData(admissao));
            }
            atualizado.accept("Cadastro de funcionário salvo.");
        });
    }

    void novaReserva() {
        List<Carro> carros = locadora.getCarros().stream().filter(c -> c.getStatus() != Carro.Status.MANUTENCAO)
                .collect(Collectors.toList());
        exigirOpcoes(carros, "Cadastre um veículo fora de manutenção para criar uma reserva.");
        exigirOpcoes(locadora.getClientes(), "Cadastre um cliente antes de criar a reserva.");
        LocalDate hoje = LocalDate.now();
        Formulario form = new Formulario(janela, "Solicitar reserva");
        JComboBox<Carro> carro = form.escolha("Veículo", carros);
        JComboBox<Cliente> cliente = form.escolha("Cliente", locadora.getClientes());
        JTextField inicio = form.campo("Início (dd/mm/aaaa)", data(hoje));
        JTextField fim = form.campo("Fim (dd/mm/aaaa)", data(hoje.plusDays(1)));
        form.abrir("Solicitar", () -> {
            locadora.solicitarReserva(selecionado(carro), selecionado(cliente),
                    Formulario.lerData(inicio), Formulario.lerData(fim));
            atualizado.accept("Reserva solicitada. Selecione o registro e clique em Confirmar.");
        });
    }

    void novoAluguel() {
        List<Carro> carros = locadora.getCarros().stream().filter(Carro::verificarDisponibilidade).collect(Collectors.toList());
        exigirOpcoes(carros, "É preciso haver um veículo disponível para iniciar um aluguel.");
        exigirOpcoes(locadora.getClientes(), "Cadastre um cliente antes de iniciar um aluguel.");
        exigirOpcoes(locadora.getFuncionarios(), "Cadastre um funcionário antes de iniciar um aluguel.");
        Formulario form = new Formulario(janela, "Registrar aluguel");
        JComboBox<Carro> carro = form.escolha("Veículo", carros);
        JComboBox<Cliente> cliente = form.escolha("Cliente", locadora.getClientes());
        JComboBox<Funcionario> funcionario = form.escolha("Funcionário", locadora.getFuncionarios());
        JTextField fim = form.campo("Fim (dd/mm/aaaa)", data(LocalDate.now().plusDays(1)));
        form.abrir("Registrar retirada", () -> {
            locadora.registrarAluguel(selecionado(carro), selecionado(cliente), selecionado(funcionario),
                    Formulario.lerData(fim));
            atualizado.accept("Aluguel iniciado e veículo retirado.");
        });
    }

    void retirarReserva(Reserva reserva) {
        exigirOpcoes(locadora.getFuncionarios(), "Cadastre um funcionário antes de registrar a retirada.");
        Formulario form = new Formulario(janela, "Retirar veículo da reserva #" + reserva.getIdReserva());
        JComboBox<Funcionario> funcionario = form.escolha("Funcionário", locadora.getFuncionarios());
        form.abrir("Registrar retirada", () -> {
            locadora.retirarReserva(reserva, selecionado(funcionario));
            atualizado.accept("Reserva convertida em aluguel. Consulte o registro em Aluguéis.");
        });
    }

    void devolver(Aluguel aluguel) {
        Formulario form = new Formulario(janela, "Registrar devolução #" + aluguel.getIdAluguel());
        JTextField data = form.campo("Devolução (dd/mm/aaaa)", data(LocalDate.now()));
        form.abrir("Registrar", () -> {
            locadora.registrarDevolucao(aluguel, Formulario.lerData(data));
            atualizado.accept("Devolução registrada. Saldo do aluguel: " + moeda(aluguel.getSaldo()) + ".");
        });
    }

    void pagar(Aluguel aluguelEscolhido) {
        List<Aluguel> emAberto = locadora.getAlugueis().stream().filter(a -> a.getSaldo().signum() > 0)
                .collect(Collectors.toList());
        if (aluguelEscolhido != null && !emAberto.contains(aluguelEscolhido)) {
            throw new IllegalArgumentException("O aluguel selecionado já está quitado.");
        }
        exigirOpcoes(emAberto, "Não há aluguéis com saldo em aberto.");
        Formulario form = new Formulario(janela, "Registrar pagamento");
        JComboBox<Aluguel> aluguel = form.escolha("Aluguel", aluguelEscolhido == null ? emAberto : Arrays.asList(aluguelEscolhido));
        JComboBox<Pagamento.Forma> forma = form.escolha("Forma", Arrays.asList(Pagamento.Forma.values()));
        JTextField valor = form.campo("Valor em R$", decimal(selecionado(aluguel).getSaldo()));
        aluguel.addActionListener(e -> valor.setText(decimal(selecionado(aluguel).getSaldo())));
        form.abrir("Confirmar pagamento", () -> {
            locadora.registrarPagamento(selecionado(aluguel), Formulario.lerMoeda(valor), selecionado(forma));
            atualizado.accept("Pagamento registrado. Saldo: " + moeda(selecionado(aluguel).getSaldo()) + ".");
        });
    }

    void confirmar(String pergunta, Runnable acao, String sucesso) {
        if (JOptionPane.showConfirmDialog(janela, pergunta, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            acao.run();
            atualizado.accept(sucesso);
        }
    }

    private static void exigirOpcoes(List<?> itens, String mensagem) {
        if (itens.isEmpty()) throw new IllegalArgumentException(mensagem);
    }

    private static final class Contato {
        private final JTextField nome, cpf, email, telefone;

        Contato(Formulario form, Pessoa pessoa) {
            nome = form.campo("Nome", pessoa == null ? "" : pessoa.getNome());
            cpf = form.campo("CPF", pessoa == null ? "" : pessoa.getCpf());
            email = form.campo("E-mail", pessoa == null ? "" : pessoa.getEmail());
            telefone = form.campo("Telefone", pessoa == null ? "" : pessoa.getTelefone());
        }

        String nome() { return nome.getText(); }
        String cpf() { return cpf.getText(); }
        String email() { return email.getText(); }
        String telefone() { return telefone.getText(); }
    }
}
