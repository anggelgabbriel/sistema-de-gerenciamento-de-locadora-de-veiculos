package locadora;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import locadora.database.Banco;
import locadora.modelo.Locadora;
import locadora.ui.DialogoAcesso;
import locadora.ui.JanelaLocadora;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Path arquivo = Paths.get("sistema.db").toAbsolutePath();
            boolean exemplo = !Arrays.asList(args).contains("--vazio");
            try {
                iniciar(arquivo, exemplo);
            } catch (Banco.Erro | IllegalArgumentException | java.time.DateTimeException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Não foi possível abrir a locadora em:\n" + arquivo
                                + "\n\nConfira a pasta lib e o acesso ao arquivo do banco.",
                        "Vértice · Banco SQLite", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void iniciar(Path arquivo, boolean exemplo) {
        Locadora locadora = abrirLocadora(arquivo, exemplo);
        boolean interfaceAberta = false;
        try {
            if (!DialogoAcesso.liberar(locadora.autenticacao())) return;
            JanelaLocadora janela = new JanelaLocadora(locadora);
            janela.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent evento) {
                    try { locadora.close(); }
                    catch (Banco.Erro e) { e.printStackTrace(); }
                }
            });
            janela.setVisible(true);
            interfaceAberta = true;
        } finally {
            if (!interfaceAberta) locadora.close();
        }
    }

    static Locadora abrirLocadora(Path arquivo, boolean exemplo) {
        return Locadora.abrir(arquivo, () -> {
            Locadora dadosIniciais = new Locadora();
            if (exemplo) Demonstracao.carregar(dadosIniciais);
            return dadosIniciais;
        });
    }
}
