package locadora.database;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Autenticacao {
    private static final int ITERACOES = 600_000;
    private final Banco banco;

    public Autenticacao(Banco banco) {
        this.banco = banco;
    }

    public boolean possuiConta() {
        return banco.consultar("SELECT 1 FROM acesso WHERE id = 1", linhas -> linhas.next());
    }

    public void criarConta(String usuario, char[] senha) {
        String nome = usuario.trim();
        if (nome.isEmpty() || nome.length() > 50) {
            throw new IllegalArgumentException("Informe um usuário com até 50 caracteres.");
        }
        if (senha.length < 8 || senha.length > 128 || soEspacos(senha)) {
            throw new IllegalArgumentException("Escolha uma senha de 8 a 128 caracteres.");
        }

        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] hash = derivarSenha(senha, sal);
        try {
            banco.gravar(() -> {
                if (possuiConta()) throw new IllegalArgumentException("Já existe uma conta. Reabra o Vértice para entrar.");
                banco.executar("INSERT INTO acesso (id, usuario, sal, senha_hash) VALUES (1, ?, ?, ?)", nome, sal, hash);
            });
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    public boolean verificar(String usuario, char[] senha) {
        if (usuario.trim().isEmpty() || senha.length == 0 || senha.length > 128) {
            return false;
        }
        return banco.consultar("SELECT usuario, sal, senha_hash FROM acesso WHERE id = 1", linhas -> {
            if (!linhas.next()) return false;
            byte[] hash = derivarSenha(senha, linhas.getBytes("sal"));
            try {
                return MessageDigest.isEqual(linhas.getBytes("senha_hash"), hash)
                        && linhas.getString("usuario").equals(usuario.trim());
            } finally {
                Arrays.fill(hash, (byte) 0);
            }
        });
    }

    private static boolean soEspacos(char[] caracteres) {
        for (char caractere : caracteres) {
            if (!Character.isWhitespace(caractere)) return false;
        }
        return true;
    }

    private static byte[] derivarSenha(char[] senha, byte[] sal) {
        PBEKeySpec parametros = new PBEKeySpec(senha, sal, ITERACOES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(parametros).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new Banco.Erro("O Java não conseguiu proteger a senha. Confira a instalação do Java.", e);
        } finally {
            parametros.clearPassword();
        }
    }
}
