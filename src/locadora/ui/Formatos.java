package locadora.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

final class Formatos {
    static final Locale BRASIL = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private Formatos() {}

    static String data(LocalDate valor) { return valor == null ? "—" : DATA.format(valor); }
    static LocalDate lerData(String texto) { return LocalDate.parse(texto.trim(), DATA); }
    static String moeda(BigDecimal valor) { return NumberFormat.getCurrencyInstance(BRASIL).format(valor); }
    static String decimal(BigDecimal valor) { return valor.toPlainString().replace('.', ','); }

    static BigDecimal lerMoeda(String texto) {
        String valor = texto.trim();
        if (!valor.matches("[0-9]+([.,][0-9]{1,2})?")) throw new NumberFormatException();
        return new BigDecimal(valor.replace(',', '.'));
    }

    static String normalizar(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(BRASIL);
    }

    static String nome(Enum<?> valor) {
        switch (valor.name()) {
            case "DISPONIVEL": return "Disponível";
            case "MANUTENCAO": return "Manutenção";
            case "CREDITO": return "Crédito";
            case "DEBITO": return "Débito";
            case "PIX": return "Pix";
            default: return valor.name().substring(0, 1) + valor.name().substring(1).toLowerCase(BRASIL);
        }
    }
}
