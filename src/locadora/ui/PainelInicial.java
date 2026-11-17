package locadora.ui;

import java.awt.*;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import locadora.modelo.*;
import static locadora.ui.Tema.*;

final class PainelInicial {
    private PainelInicial() {}

    static JPanel criar(Locadora locadora, Runnable novoAluguel, Runnable verAlugueis) {
        JPanel corpo = painel(new BorderLayout(0, 19));
        JPanel topo = coluna();
        JPanel hero = new Capa();
        JPanel textoCapa = coluna();
        textoCapa.add(texto("CENTRAL DE OPERAÇÕES", 12, Font.BOLD, MENTA));
        textoCapa.add(Box.createVerticalStrut(3));
        textoCapa.add(texto("Sua locadora em movimento.", 27, Font.BOLD, BRANCO));
        textoCapa.add(Box.createVerticalStrut(6));
        textoCapa.add(texto("Acompanhe as retiradas, devoluções e reservas da sua frota.", 13, Font.PLAIN, cor(0xD5E4EE)));
        textoCapa.add(Box.createVerticalStrut(14));
        JButton novo = new Tema.Botao("+ Novo aluguel", MENTA, MARINHO, MENTA);
        novo.addActionListener(e -> novoAluguel.run());
        novo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textoCapa.add(novo);
        hero.add(textoCapa, BorderLayout.WEST);
        topo.add(hero);
        topo.add(Box.createVerticalStrut(14));

        long livres = locadora.getCarros().stream().filter(Carro::verificarDisponibilidade).count();
        long ativos = locadora.getAlugueis().stream().filter(a -> a.getStatus() == Aluguel.Status.ATIVO).count();
        long confirmadas = locadora.getReservas().stream().filter(r -> r.getStatus() == Reserva.Status.CONFIRMADA
                && !r.getDataFimPrevista().isBefore(LocalDate.now())).count();
        JPanel indicadores = painel(new GridLayout(1, 4, 12, 0));
        indicadores.add(indicador("Veículos na frota", locadora.getCarros().size(), "Todos os veículos", cor(0x316AAB)));
        indicadores.add(indicador("Em pátio", livres, "Fora de aluguel e oficina", PETROLEO));
        indicadores.add(indicador("Aluguéis ativos", ativos, "Veículos com clientes", cor(0x88531D)));
        indicadores.add(indicador("Reservas confirmadas", confirmadas, "Aguardando retirada", cor(0x694CA8)));
        topo.add(indicadores);
        corpo.add(topo, BorderLayout.NORTH);

        JPanel atividade = painel(new BorderLayout(0, 8));
        JPanel titulos = painel(new BorderLayout());
        titulos.add(texto("Aluguéis recentes", 18, Font.BOLD, TEXTO), BorderLayout.WEST);
        titulos.add(botao("Ver todos  →", false, verAlugueis), BorderLayout.EAST);
        atividade.add(titulos, BorderLayout.NORTH);
        List<Aluguel> recentes = new ArrayList<>(locadora.getAlugueis());
        Collections.reverse(recentes);
        ListaRegistros<Aluguel> lista = new ListaRegistros<>(recentes.subList(0, Math.min(5, recentes.size())),
                new String[]{"ID", "Veículo", "Cliente", "Retirada", "Status"}, aluguel -> new Object[]{
                    aluguel.getIdAluguel(), aluguel.getCarro().getModelo() + " · " + aluguel.getCarro().getPlaca(),
                    aluguel.getCliente().getNome(), aluguel.getDataInicio(), aluguel.getStatus()}, false);
        atividade.add(lista.painel(), BorderLayout.CENTER);
        corpo.add(atividade, BorderLayout.CENTER);
        return corpo;
    }

    private static JPanel indicador(String titulo, long numero, String legenda, Color destaque) {
        JPanel caixa = cartao(new BorderLayout(), 14);
        JPanel textos = coluna();
        textos.add(texto(titulo, 12, Font.BOLD, SUAVE));
        textos.add(texto(Long.toString(numero), 28, Font.BOLD, destaque));
        textos.add(texto(legenda, 11, Font.PLAIN, SUAVE));
        caixa.add(textos, BorderLayout.WEST);
        return caixa;
    }

    private static final class Capa extends JPanel {
        Capa() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(17, 23, 17, 23));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D arte = suavizar(g);
            int largura = getWidth(), altura = getHeight();
            arte.setPaint(new GradientPaint(0, 0, MARINHO, largura, altura, cor(0x135E62)));
            arte.fillRoundRect(0, 0, largura, altura, 24, 24);
            if (largura >= 590) {
                arte.translate(largura - 153, altura / 2.0);
                arte.setColor(new Color(126, 236, 208, 38));
                arte.setStroke(new BasicStroke(1.2f));
                for (int raio = 48; raio <= 72; raio += 8) arte.drawOval(-raio, -raio, 2 * raio, 2 * raio);
                arte.setColor(MENTA);
                arte.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D carro = new Path2D.Double();
                carro.moveTo(-47, 16);
                carro.lineTo(-47, 4);
                carro.quadTo(-46, -4, -32, -7);
                carro.lineTo(-22, -20);
                carro.quadTo(-19, -24, -11, -24);
                carro.lineTo(12, -24);
                carro.quadTo(21, -23, 25, -16);
                carro.lineTo(33, -6);
                carro.quadTo(47, -4, 47, 5);
                carro.lineTo(47, 16);
                carro.closePath();
                arte.draw(carro);
                arte.drawLine(-32, -7, 33, -7);
                arte.drawLine(4, -22, 4, -8);
                for (int x : new int[]{-30, 29}) {
                    arte.setColor(cor(0x155255));
                    arte.fillOval(x - 8, 9, 16, 16);
                    arte.setColor(MENTA);
                    arte.drawOval(x - 8, 9, 16, 16);
                }
                arte.drawLine(-47, 4, -38, 4);
                arte.drawLine(38, 4, 47, 4);
            }
            arte.dispose();
        }
    }
}
