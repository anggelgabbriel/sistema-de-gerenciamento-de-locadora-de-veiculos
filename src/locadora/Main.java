package locadora;

import java.util.Arrays;
import javax.swing.SwingUtilities;
import locadora.modelo.Locadora;
import locadora.ui.JanelaLocadora;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            boolean exemplo = !Arrays.asList(args).contains("--vazio");
            Locadora locadora = new Locadora();
            if (exemplo) Demonstracao.carregar(locadora);
            new JanelaLocadora(locadora, exemplo).setVisible(true);
        });
    }
}
