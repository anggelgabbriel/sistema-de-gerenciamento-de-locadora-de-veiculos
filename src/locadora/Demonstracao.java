package locadora;

import java.math.BigDecimal;
import java.time.LocalDate;
import locadora.modelo.*;

final class Demonstracao {
    private Demonstracao() {}

    static void carregar(Locadora locadora) {
        LocalDate hoje = LocalDate.now();
        Carro polo = locadora.cadastrarCarro("9BWZZZ6RZPT000001", "RGA1A21", "Volkswagen", "Polo", 2024, "Branco", new BigDecimal("129.90"));
        Carro onix = locadora.cadastrarCarro("9BGKS48U0PG000002", "SFP2B42", "Chevrolet", "Onix", 2023, "Prata", new BigDecimal("119.90"));
        locadora.cadastrarCarro("9BFZZZ54ZPB000003", "RNV3C63", "Hyundai", "HB20", 2024, "Cinza", new BigDecimal("139.90"));
        locadora.cadastrarCarro("8APZZZ17ZPT000004", "SLC4D84", "Fiat", "Argo", 2025, "Vermelho", new BigDecimal("124.90"));
        Carro versa = locadora.cadastrarCarro("3N1CN8AE0PL000005", "RSM5E15", "Nissan", "Versa", 2024, "Preto", new BigDecimal("159.90"));
        locadora.alternarManutencao(versa);

        Cliente ana = locadora.cadastrarCliente("Ana Ribeiro", "04100000001", "ana@example.com", "(84) 90000-1001", "00100000001", LocalDate.of(1998, 3, 8));
        Cliente bruno = locadora.cadastrarCliente("Bruno Lima", "04100000002", "bruno@example.com", "(84) 90000-1002", "00100000002", LocalDate.of(1994, 7, 14));
        Funcionario clara = locadora.cadastrarFuncionario("Clara Santos", "04100000003", "clara@example.com", "(84) 90000-1003", hoje.minusMonths(6));

        Aluguel aluguel = locadora.registrarAluguel(polo, ana, clara, hoje.plusDays(2));
        locadora.registrarPagamento(aluguel, new BigDecimal("100.00"), Pagamento.Forma.PIX);
        Reserva reserva = locadora.solicitarReserva(onix, bruno, hoje, hoje.plusDays(1));
        locadora.confirmarReserva(reserva);
    }
}
