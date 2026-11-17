package locadora.ui;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import static locadora.ui.Tema.*;

final class ListaRegistros<T> {
    private final List<T> registros;
    private final JTable tabela;
    private final JPanel visual;

    ListaRegistros(List<T> registros, String[] colunas, Function<T, Object[]> extrair, boolean busca) {
        this.registros = registros;
        AbstractTableModel modelo = new AbstractTableModel() {
            @Override public int getRowCount() { return registros.size(); }
            @Override public int getColumnCount() { return colunas.length; }
            @Override public String getColumnName(int coluna) { return colunas[coluna]; }
            @Override public Object getValueAt(int linha, int coluna) { return extrair.apply(registros.get(linha))[coluna]; }
            @Override public Class<?> getColumnClass(int coluna) {
                for (T registro : registros) {
                    Object valor = extrair.apply(registro)[coluna];
                    if (valor != null) return valor.getClass();
                }
                return Object.class;
            }
        };
        tabela = new JTable(modelo);
        tabela.setFont(fonte(13, Font.PLAIN));
        tabela.setRowHeight(43);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(0, 0));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.setBackground(BRANCO);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object valor, boolean selecionado,
                    boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(table, valor, selecionado, foco, linha, coluna);
                setBackground(FUNDO);
                setForeground(SUAVE);
                setFont(fonte(12, Font.BOLD));
                setBorder(new EmptyBorder(0, 10, 0, 4));
                return this;
            }
        });
        Celula celula = new Celula();
        for (int i = 0; i < colunas.length; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(celula);
            tabela.getColumnModel().getColumn(i).setPreferredWidth("ID".equals(colunas[i]) ? 36 : 116);
        }
        TableRowSorter<AbstractTableModel> ordenar = new TableRowSorter<>(modelo);
        tabela.setRowSorter(ordenar);

        visual = cartao(new BorderLayout(0, 12), 16);
        CardLayout layout = new CardLayout();
        JPanel miolo = Tema.painel(layout);
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(new EmptyBorder(0, 0, 0, 0));
        rolagem.getViewport().setBackground(BRANCO);
        miolo.add(rolagem, "tabela");
        JLabel vazio = texto("Nenhum registro por aqui.", 15, Font.PLAIN, SUAVE);
        vazio.setHorizontalAlignment(SwingConstants.CENTER);
        miolo.add(vazio, "vazio");
        layout.show(miolo, registros.isEmpty() ? "vazio" : "tabela");
        visual.add(miolo, BorderLayout.CENTER);
        if (busca) {
            JPanel ferramentas = Tema.painel(new BorderLayout(16, 0));
            JPanel pesquisa = Tema.painel(new FlowLayout(FlowLayout.LEFT, 9, 0));
            JLabel rotulo = texto("Buscar", 13, Font.BOLD, SUAVE);
            JTextField campo = new JTextField(19);
            rotulo.setLabelFor(campo);
            campo.setFont(fonte(14, Font.PLAIN));
            campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINHA),
                    new EmptyBorder(6, 8, 6, 8)));
            JLabel contagem = texto(contagem(registros.size()), 12, Font.BOLD, SUAVE);
            ferramentas.add(contagem, BorderLayout.EAST);
            pesquisa.add(rotulo);
            pesquisa.add(campo);
            ferramentas.add(pesquisa, BorderLayout.WEST);
            visual.add(ferramentas, BorderLayout.NORTH);
            campo.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
                @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
                @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
                private void filtrar() {
                    String termo = Formatos.normalizar(campo.getText().trim());
                    ordenar.setRowFilter(new RowFilter<AbstractTableModel, Integer>() {
                        @Override public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> item) {
                            for (int i = 0; i < item.getValueCount(); i++) {
                                Object valor = item.getValue(i);
                                if (Formatos.normalizar(exibir(valor)).contains(termo)) return true;
                            }
                            return false;
                        }
                    });
                    int total = tabela.getRowCount();
                    vazio.setText("Nenhum registro encontrado.");
                    layout.show(miolo, total == 0 ? "vazio" : "tabela");
                    contagem.setText(contagem(total));
                }
            });
        }
    }

    JPanel painel() { return visual; }

    T selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) throw new IllegalArgumentException("Selecione um registro na tabela.");
        return registros.get(tabela.convertRowIndexToModel(linha));
    }

    private static String contagem(int total) { return total == 1 ? "1 registro" : total + " registros"; }

    private static String exibir(Object valor) {
        if (valor instanceof Enum<?>) return Formatos.nome((Enum<?>) valor);
        if (valor instanceof BigDecimal) return Formatos.moeda((BigDecimal) valor);
        if (valor instanceof LocalDate) return Formatos.data((LocalDate) valor);
        return valor == null ? "—" : valor.toString();
    }

    private static final class Celula extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object valor, boolean selecionado,
                boolean foco, int linha, int coluna) {
            String exibido = exibir(valor);
            super.getTableCellRendererComponent(table, exibido, selecionado, foco, linha, coluna);
            setFont(fonte(13, Font.PLAIN));
            setBorder(new EmptyBorder(0, 10, 0, 4));
            setToolTipText(exibido);
            setForeground(TEXTO);
            setBackground(selecionado ? cor(0xD9E8FA) : linha % 2 == 0 ? BRANCO : cor(0xF7FAFD));
            if (!(valor instanceof Enum<?>) || valor instanceof locadora.modelo.Pagamento.Forma) return this;
            String estado = ((Enum<?>) valor).name();
            Color fundo;
            Color tinta;
            switch (estado) {
                case "DISPONIVEL": case "CONFIRMADA": case "CONFIRMADO": case "FINALIZADO":
                    fundo = CLARO; tinta = cor(0x096355); break;
                case "MANUTENCAO": case "PENDENTE":
                    fundo = cor(0xFFF1D8); tinta = cor(0x824900); break;
                case "ESTORNADO": case "CANCELADA":
                    fundo = cor(0xFBE6EA); tinta = cor(0x9D2841); break;
                default:
                    fundo = cor(0xE4EEFB); tinta = cor(0x28548D); break;
            }
            JPanel celula = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 9));
            celula.setBackground(getBackground());
            celula.setToolTipText(exibido);
            JLabel selo = texto(exibido, 12, Font.BOLD, tinta);
            selo.setOpaque(true);
            selo.setBackground(fundo);
            selo.setBorder(new EmptyBorder(3, 8, 3, 8));
            celula.add(selo);
            return celula;
        }
    }
}
