package util;

/**
 * Validaciones sencillas de texto y números.
 */
public final class Validador {

    private Validador() {
    }

    public static boolean noVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean esNumero(String texto) {
        if (!noVacio(texto)) {
            return false;
        }
        try {
            double valor = aDouble(texto);
            return Double.isFinite(valor);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean esEntero(String texto) {
        if (!noVacio(texto)) {
            return false;
        }
        try {
            aEntero(texto);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean noNegativo(double valor) {
        return Double.isFinite(valor) && valor >= 0;
    }

    /**
     * Convierte un texto en double aceptando coma o punto como separador
     * decimal.
     */
    public static double aDouble(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    public static int aEntero(String texto) {
        return Integer.parseInt(texto.trim());
    }
}
