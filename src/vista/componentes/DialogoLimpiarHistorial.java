package vista.componentes;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Diálogo para elegir entre eliminar un registro del historial o limpiar
 * todos los registros de la pestaña actual.
 */
public final class DialogoLimpiarHistorial {

    public enum Modo {
        SELECCIONADO,
        TODO
    }

    private DialogoLimpiarHistorial() {
    }

    public static Optional<Modo> mostrar(Window propietario, String nombreHistorial,
            String descripcionSeleccion, int totalRegistros,
            boolean permiteSeleccion, String nota) {
        Dialog<Modo> dialogo = new Dialog<>();
        dialogo.initOwner(propietario);
        dialogo.setTitle("Limpiar historial");
        dialogo.setHeaderText(null);

        DialogPane panel = dialogo.getDialogPane();
        panel.getStyleClass().add("dialogo-sistema");
        panel.setMinWidth(500);
        Estilos.aplicar(panel);

        Label titulo = Ui.etiqueta("Historial de " + nombreHistorial,
                "tarjeta-titulo");
        Label advertencia = Ui.etiqueta(
                "Esta acción es irreversible y no se puede deshacer.",
                "advertencia-irreversible");
        advertencia.setWrapText(true);

        ToggleGroup grupo = new ToggleGroup();

        RadioButton seleccionado = new RadioButton(
                "Eliminar únicamente el registro seleccionado");
        seleccionado.setToggleGroup(grupo);
        seleccionado.setDisable(!permiteSeleccion);

        Label detalleSeleccion = Ui.etiqueta(
                permiteSeleccion && descripcionSeleccion != null
                        ? descripcionSeleccion
                        : "No hay una fila seleccionada en esta pestaña.",
                "tarjeta-subtitulo");
        detalleSeleccion.setWrapText(true);
        detalleSeleccion.setMaxWidth(440);

        RadioButton todo = new RadioButton(
                "Eliminar todo el historial de esta pestaña ("
                + totalRegistros + (totalRegistros == 1 ? " registro)" : " registros)"));
        todo.setToggleGroup(grupo);
        todo.setDisable(totalRegistros <= 0);

        Label detalleTodo = Ui.etiqueta(
                totalRegistros > 0
                        ? "Se eliminarán todos los registros de esta pestaña, "
                        + "incluidos los que no se ven por el filtro de fechas."
                        : "No hay registros para eliminar en esta pestaña.",
                "tarjeta-subtitulo");
        detalleTodo.setWrapText(true);
        detalleTodo.setMaxWidth(440);

        Label notaLabel = Ui.etiqueta(nota == null ? "" : nota,
                "tarjeta-subtitulo");
        notaLabel.setWrapText(true);
        notaLabel.setMaxWidth(440);

        VBox contenido = new VBox(10, titulo, advertencia, seleccionado,
                detalleSeleccion, todo, detalleTodo, notaLabel);
        contenido.setPadding(new Insets(8, 4, 4, 4));
        panel.setContent(contenido);

        ButtonType eliminar = new ButtonType("Eliminar",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        panel.getButtonTypes().setAll(eliminar, cancelar);

        Button btnEliminar = (Button) panel.lookupButton(eliminar);
        btnEliminar.getStyleClass().add("button-peligro");
        Button btnCancelar = (Button) panel.lookupButton(cancelar);
        btnCancelar.getStyleClass().add("button-secundario");

        if (permiteSeleccion) {
            grupo.selectToggle(seleccionado);
        } else if (totalRegistros > 0) {
            grupo.selectToggle(todo);
        } else {
            btnEliminar.setDisable(true);
        }

        btnEliminar.addEventFilter(ActionEvent.ACTION, evento -> {
            if (grupo.getSelectedToggle() == null) {
                evento.consume();
                Dialogo.error(propietario, "Limpiar historial",
                        "Debes seleccionar una de las opciones de limpieza.");
            }
        });

        dialogo.setResultConverter(boton -> {
            if (boton != eliminar) {
                return null;
            }
            return grupo.getSelectedToggle() == seleccionado
                    ? Modo.SELECCIONADO : Modo.TODO;
        });

        return dialogo.showAndWait();
    }
}
