package locadora.ui;

import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import locadora.database.Autenticacao;

public final class DialogoAcesso {
    private DialogoAcesso() {}

    public static boolean liberar(Autenticacao autenticacao) {
        boolean primeiroAcesso = !autenticacao.possuiConta();
        Formulario form = new Formulario(null, primeiroAcesso ? "Criar acesso ao Vértice" : "Entrar no Vértice");
        JTextField usuario = form.campo("Usuário");
        JPasswordField senha = form.senha("Senha");
        JPasswordField confirmacao = primeiroAcesso ? form.senha("Repita a senha") : null;

        return form.abrir(primeiroAcesso ? "Criar e entrar" : "Entrar", () -> {
            char[] digitada = senha.getPassword();
            try {
                if (primeiroAcesso) {
                    char[] repetida = confirmacao.getPassword();
                    try {
                        if (!Arrays.equals(digitada, repetida)) {
                            throw new IllegalArgumentException("As senhas precisam ser iguais.");
                        }
                        autenticacao.criarConta(usuario.getText(), digitada);
                    } finally {
                        Arrays.fill(repetida, '\0');
                        confirmacao.setText("");
                    }
                } else if (!autenticacao.verificar(usuario.getText(), digitada)) {
                    throw new IllegalArgumentException("Usuário ou senha inválidos.");
                }
            } finally {
                Arrays.fill(digitada, '\0');
                senha.setText("");
            }
        });
    }
}
