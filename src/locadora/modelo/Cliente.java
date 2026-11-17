package locadora.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static locadora.modelo.Validacao.*;

public final class Cliente extends Pessoa {
    private final int idCliente;
    private String cnh;
    private LocalDate dataNascimento;
    private final List<Aluguel> alugueis = new ArrayList<>();

    public Cliente(int id, String nome, String cpf, String email, String telefone,
                   String cnh, LocalDate nascimento) {
        super(nome, cpf, email, telefone);
        idCliente = id;
        this.cnh = documento(cnh, 11, "CNH");
        dataNascimento = dataPassadaOuHoje(nascimento, "O nascimento");
    }

    public void atualizarDados(String nome, String cpf, String email, String telefone,
                               String cnh, LocalDate nascimento) {
        String novaCnh = documento(cnh, 11, "CNH");
        dataPassadaOuHoje(nascimento, "O nascimento");
        atualizarContato(nome, cpf, email, telefone);
        this.cnh = novaCnh;
        dataNascimento = nascimento;
    }

    public boolean verificarCNH() { return cnh.matches("[0-9]{11}"); }
    public List<Aluguel> consultarAlugueis() { return Collections.unmodifiableList(alugueis); }
    void vincularAluguel(Aluguel aluguel) { alugueis.add(aluguel); }
    public int getIdCliente() { return idCliente; }
    public String getCnh() { return cnh; }
    public LocalDate getDataNascimento() { return dataNascimento; }
}
