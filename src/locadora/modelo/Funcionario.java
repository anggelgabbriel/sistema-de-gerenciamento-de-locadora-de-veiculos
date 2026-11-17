package locadora.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static locadora.modelo.Validacao.*;

public final class Funcionario extends Pessoa {
    private final int idFuncionario;
    private LocalDate dataAdmissao;
    private final List<Aluguel> registros = new ArrayList<>();

    public Funcionario(int id, String nome, String cpf, String email, String telefone, LocalDate admissao) {
        super(nome, cpf, email, telefone);
        idFuncionario = id;
        dataAdmissao = dataPassadaOuHoje(admissao, "A admissão");
    }

    public void atualizarCadastro(String nome, String cpf, String email, String telefone, LocalDate admissao) {
        dataPassadaOuHoje(admissao, "A admissão");
        atualizarContato(nome, cpf, email, telefone);
        dataAdmissao = admissao;
    }

    void registrarAluguel(Aluguel aluguel) { registros.add(aluguel); }
    public Optional<Aluguel> consultarAluguel(int id) {
        return registros.stream().filter(aluguel -> aluguel.getIdAluguel() == id).findFirst();
    }

    public int getIdFuncionario() { return idFuncionario; }
    public LocalDate getDataAdmissao() { return dataAdmissao; }
}
