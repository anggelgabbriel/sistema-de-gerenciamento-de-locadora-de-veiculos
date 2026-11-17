package locadora.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

final class Validacao {
    private Validacao() {}

    static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new IllegalArgumentException(mensagem);
    }

    static String texto(String valor, String campo) {
        exigir(valor != null && !valor.trim().isEmpty(), "Preencha o campo " + campo + ".");
        return valor.trim();
    }

    static String documento(String valor, int tamanho, String campo) {
        String digitos = texto(valor, campo).replaceAll("[.\\- ]", "");
        exigir(digitos.matches("[0-9]{" + tamanho + "}"), campo + " deve ter " + tamanho + " dígitos.");
        return digitos;
    }

    static LocalDate dataPassadaOuHoje(LocalDate data, String campo) {
        exigir(data != null && !data.isAfter(LocalDate.now()), campo + " não pode estar no futuro.");
        return data;
    }

    static void periodo(LocalDate inicio, LocalDate fim) {
        exigir(inicio != null && fim != null && !fim.isBefore(inicio),
                "A data final não pode ser anterior à inicial.");
    }

    static BigDecimal dinheiro(BigDecimal valor) {
        exigir(valor != null && valor.signum() > 0 && valor.stripTrailingZeros().scale() <= 2,
                "Informe um valor positivo com até duas casas decimais.");
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
