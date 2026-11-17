package vista.componentes;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

/**
 * Dialogos coherentes con el tema visual de la aplicacion.
 */
public final class Dialogo {

    private Dialogo() {
    }

    public static void error(Window propietario, String titulo, String mensaje) {
        Alert alerta = crear(propietario, titulo, mensaje);
        alerta.setGraphic(crearIcono(Icono.ALERTA, "dialogo-icono-error"));

        ButtonType entendido = new ButtonType("Entendido", ButtonBar.ButtonData.OK_DONE);
        alerta.getButtonTypes().setAll(entendido);

        Button boton = (Button) alerta.getDialogPane().lookupButton(entendido);
        boton.getStyleClass().add("button-primario");
        alerta.showAndWait();
    }

    public static boolean confirmar(Window propietario, String titulo, String mensaje,
            String textoAccion) {
        return confirmar(propietario, titulo, mensaje, textoAccion, false);
    }

    public static boolean confirmarPeligro(Window propietario, String titulo, String mensaje,
            String textoAccion) {
        return confirmar(propietario, titulo, mensaje, textoAccion, true);
    }

    private static boolean confirmar(Window propietario, String titulo, String mensaje,
            String textoAccion, boolean peligro) {
        Alert alerta = crear(propietario, titulo, mensaje);
        alerta.setGraphic(crearIcono(Icono.ALERTA, "dialogo-icono-aviso"));

        ButtonType accion = new ButtonType(textoAccion, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerta.getButtonTypes().setAll(accion, cancelar);

        Button botonAccion = (Button) alerta.getDialogPane().lookupButton(accion);
        botonAccion.getStyleClass().add(peligro ? "button-peligro" : "button-primario");

        Button botonCancelar = (Button) alerta.getDialogPane().lookupButton(cancelar);
        botonCancelar.getStyleClass().add("button-secundario");

        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.isPresent() && resultado.get() == accion;
    }

    private static Alert crear(Window propietario, String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.NONE);
        alerta.initOwner(propietario);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setResizable(false);

        DialogPane panel = alerta.getDialogPane();
        panel.getStyleClass().add("dialogo-sistema");
        panel.setMinWidth(460);
        Estilos.aplicar(panel);

        Label contenido = new Label(mensaje);
        contenido.setWrapText(true);
        contenido.setMaxWidth(440);
        contenido.getStyleClass().add("dialogo-mensaje");
        panel.setContent(contenido);

        return alerta;
    }

    private static StackPane crearIcono(String ruta, String claseColor) {
        StackPane caja = new StackPane(Icono.crear(ruta, 20));
        caja.getStyleClass().addAll("dialogo-icono", claseColor);
        return caja;
    }
}
