package locadora.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

final class Tema {
    static final Color FUNDO = cor(0xF1F5F9);
    static final Color BRANCO = Color.WHITE;
    static final Color MARINHO = cor(0x11253A);
    static final Color TEXTO = cor(0x152B40);
    static final Color SUAVE = cor(0x52677C);
    static final Color LINHA = cor(0xDCE5ED);
    static final Color PETROLEO = cor(0x087C72);
    static final Color MENTA = cor(0x57E0BB);
    static final Color CLARO = cor(0xE0F6F0);

    private Tema() {}

    static Color cor(int rgb) { return new Color(rgb); }
    static Font fonte(int tamanho, int estilo) { return new Font("Segoe UI", estilo, tamanho); }

    static JLabel texto(String texto, int tamanho, int estilo, Color cor) {
        JLabel label = new JLabel(texto);
        label.setFont(fonte(tamanho, estilo));
        label.setForeground(cor);
        return label;
    }

    static JPanel painel(LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setOpaque(false);
        return painel;
    }

    static JPanel coluna() {
        JPanel painel = painel(null);
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        return painel;
    }

    static JPanel cartao(LayoutManager layout, int espaco) {
        JPanel painel = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D desenho = suavizar(g);
                desenho.setColor(BRANCO);
                desenho.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                desenho.setColor(LINHA);
                desenho.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                desenho.dispose();
            }
        };
        painel.setOpaque(false);
        painel.setBorder(new EmptyBorder(espaco, espaco, espaco, espaco));
        return painel;
    }

    static JButton botao(String texto, boolean primario, Runnable acao) {
        JButton botao = new Botao(texto, primario ? PETROLEO : BRANCO,
                primario ? BRANCO : TEXTO, primario ? PETROLEO : LINHA);
        botao.addActionListener(e -> acao.run());
        return botao;
    }

    static Graphics2D suavizar(Graphics g) {
        Graphics2D desenho = (Graphics2D) g.create();
        desenho.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return desenho;
    }

    static final class Botao extends JButton {
        private final Color fundo, borda;

        Botao(String texto, Color fundo, Color textoCor, Color borda) {
            super(texto);
            this.fundo = fundo;
            this.borda = borda;
            setForeground(textoCor);
            setFont(fonte(13, Font.BOLD));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(9, 14, 9, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D desenho = suavizar(g);
            desenho.setColor(getModel().isPressed() || getModel().isRollover()
                    ? (BRANCO.equals(fundo) ? FUNDO : fundo.brighter()) : fundo);
            desenho.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            desenho.setColor(isFocusOwner() ? MENTA : borda);
            desenho.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            desenho.dispose();
            super.paintComponent(g);
        }
    }
}
