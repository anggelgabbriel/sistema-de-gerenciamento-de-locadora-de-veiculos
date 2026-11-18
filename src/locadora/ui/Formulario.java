package locadora.ui;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import locadora.database.Banco;
import static locadora.ui.Tema.*;

final class Formulario {
    private final JPanel campos = new JPanel(new GridBagLayout());
    private final Component janela;
    private final String titulo;
    private int linha;

    Formulario(Component janela, String titulo) {
        this.janela = janela;
        this.titulo = titulo;
        campos.setBorder(new EmptyBorder(4, 0, 8, 0));
    }

    JTextField campo(String rotulo) { return campo(rotulo, ""); }

    JTextField campo(String rotulo, String valor) {
        JTextField campo = new JTextField(valor, 22);
        adicionar(rotulo, campo);
        return campo;
    }

    JPasswordField senha(String rotulo) {
        JPasswordField campo = new JPasswordField(22);
        adicionar(rotulo, campo);
        return campo;
    }

    <T> JComboBox<T> escolha(String rotulo, List<T> valores) {
        JComboBox<T> combo = new JComboBox<>();
        for (T item : valores) combo.addItem(item);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean selecionado, boolean foco) {
                return super.getListCellRendererComponent(lista,
                        valor instanceof Enum<?> ? Formatos.nome((Enum<?>) valor) : valor, indice, selecionado, foco);
            }
        });
        adicionar(rotulo, combo);
        return combo;
    }

    private void adicionar(String rotulo, JComponent campo) {
        JLabel label = texto(rotulo, 13, Font.PLAIN, TEXTO);
        label.setLabelFor(campo);
        GridBagConstraints posicao = new GridBagConstraints();
        posicao.gridy = linha++;
        posicao.anchor = GridBagConstraints.LINE_START;
        posicao.insets = new Insets(5, 0, 5, 12);
        campos.add(label, posicao);
        posicao.gridx = 1;
        posicao.weightx = 1;
        posicao.fill = GridBagConstraints.HORIZONTAL;
        posicao.insets = new Insets(5, 0, 5, 0);
        campo.setFont(fonte(14, Font.PLAIN));
        campos.add(campo, posicao);
    }

    boolean abrir(String acao, Runnable salvar) {
        Window dona = janela == null ? null : SwingUtilities.getWindowAncestor(janela);
        JDialog dialogo = new JDialog(dona, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setBorder(new EmptyBorder(20, 24, 18, 24));
        corpo.setBackground(BRANCO);
        corpo.add(texto(titulo, 22, Font.BOLD, TEXTO), BorderLayout.NORTH);
        campos.setOpaque(false);
        corpo.add(campos, BorderLayout.CENTER);

        JPanel rodape = coluna();
        JLabel erro = texto(" ", 13, Font.PLAIN, cor(0xA52842));
        erro.setAlignmentX(Component.LEFT_ALIGNMENT);
        rodape.add(erro);
        JPanel botoes = painel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        botoes.add(botao("Cancelar", false, dialogo::dispose));
        boolean[] confirmado = {false};
        JButton confirmar = botao(acao, true, () -> {
            try {
                salvar.run();
                confirmado[0] = true;
                dialogo.dispose();
            } catch (IllegalArgumentException | Banco.Erro e) {
                erro.setText("<html><body style='width: 315px'>" + e.getMessage() + "</body></html>");
                dialogo.pack();
            }
        });
        botoes.add(confirmar);
        rodape.add(Box.createVerticalStrut(12));
        rodape.add(botoes);
        corpo.add(rodape, BorderLayout.SOUTH);
        dialogo.setContentPane(corpo);
        dialogo.getRootPane().setDefaultButton(confirmar);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.pack();
        dialogo.setMinimumSize(dialogo.getSize());
        dialogo.setLocationRelativeTo(janela);
        dialogo.setVisible(true);
        return confirmado[0];
    }

    static LocalDate lerData(JTextField campo) {
        try { return Formatos.lerData(campo.getText()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Use uma data válida no formato dd/mm/aaaa."); }
    }

    static BigDecimal lerMoeda(JTextField campo) {
        try { return Formatos.lerMoeda(campo.getText()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Use um valor como 129,90."); }
    }

    static int lerAno(JTextField campo) {
        try { return Integer.parseInt(campo.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Informe o ano com quatro dígitos."); }
    }

    static <T> T selecionado(JComboBox<T> campo) {
        return campo.getItemAt(campo.getSelectedIndex());
    }
}
