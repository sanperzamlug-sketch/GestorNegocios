package util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

/**
 * Utilidades de formato usadas por la vista y por la persistencia.
 */
public final class Formato {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_LARGA
            = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", LOCALE_ES);
    private static final DateTimeFormatter FORMATO_FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_MES
            = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", LOCALE_ES);

    private Formato() {
    }

    /**
     * Codifica un texto en Base64 para que pueda guardarse dentro de una línea
     * separada por tabuladores sin conflictos.
     */
    public static String codificar(String texto) {
        String valor = texto == null ? "" : texto;
        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodificar(String texto) {
        byte[] datos = Base64.getDecoder().decode(texto);
        return new String(datos, StandardCharsets.UTF_8);
    }

    public static String dinero(double valor) {
        return String.format(LOCALE_ES, "$%,.2f", valor);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FORMATO_FECHA.format(fecha);
    }

    public static String fechaLarga(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        String texto = FORMATO_FECHA_LARGA.format(fecha);
        return texto.isEmpty()
                ? texto
                : texto.substring(0, 1).toUpperCase(LOCALE_ES) + texto.substring(1);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : FORMATO_FECHA_HORA.format(fechaHora);
    }

    public static String mes(YearMonth mes) {
        if (mes == null) {
            return "";
        }
        String texto = FORMATO_MES.format(mes);
        return texto.isEmpty()
                ? texto
                : texto.substring(0, 1).toUpperCase(LOCALE_ES) + texto.substring(1);
    }

    public static DateTimeFormatter formatoFecha() {
        return FORMATO_FECHA;
    }

    public static String porcentaje(double valor) {
        return String.format(LOCALE_ES, "%.2f %%", valor);
    }
}
