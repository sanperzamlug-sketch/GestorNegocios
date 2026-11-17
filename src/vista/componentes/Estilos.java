package vista.componentes;

import java.net.URL;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

/**
 * Punto unico de carga de la hoja de estilos de la aplicacion.
 */
public final class Estilos {

    public static final String RUTA = "/recursos/estilos.css";

    private Estilos() {
    }

    public static void aplicar(Scene escena) {
        String hoja = url();
        if (hoja != null) {
            escena.getStylesheets().add(hoja);
        }
    }

    public static void aplicar(DialogPane panel) {
        String hoja = url();
        if (hoja != null) {
            panel.getStylesheets().add(hoja);
        }
    }

    private static String url() {
        URL recurso = Estilos.class.getResource(RUTA);
        return recurso == null ? null : recurso.toExternalForm();
    }
}
