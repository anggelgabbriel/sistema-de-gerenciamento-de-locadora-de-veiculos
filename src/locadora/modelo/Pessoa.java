package locadora.modelo;

import static locadora.modelo.Validacao.*;

public abstract class Pessoa {
    private Dados dados;

    protected Pessoa(String nome, String cpf, String email, String telefone) {
        dados = new Dados(nome, cpf, email, telefone);
    }

    protected final void atualizarContato(String nome, String cpf, String email, String telefone) {
        dados = new Dados(nome, cpf, email, telefone);
    }

    public String getNome() { return dados.nome; }
    public String getCpf() { return dados.cpf; }
    public String getEmail() { return dados.email; }
    public String getTelefone() { return dados.telefone; }
    @Override public String toString() { return getNome(); }

    private static final class Dados {
        private final String nome, cpf, email, telefone;

        private Dados(String nome, String cpf, String email, String telefone) {
            this.nome = texto(nome, "Nome");
            this.cpf = documento(cpf, 11, "CPF");
            this.email = texto(email, "E-mail");
            exigir(this.email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Informe um e-mail válido.");
            this.telefone = texto(telefone, "Telefone");
        }
    }
}
