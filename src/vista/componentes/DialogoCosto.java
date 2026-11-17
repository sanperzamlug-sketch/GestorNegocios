package vista.componentes;

import java.util.Optional;
import java.util.function.UnaryOperator;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;
import modelo.Costo;
import util.Validador;

/**
 * Diálogo modal para editar un costo desde cualquier pantalla.
 */
public final class DialogoCosto {

    private DialogoCosto() {
    }

    public static Optional<Costo> mostrar(Window propietario, Costo costo) {
        if (costo == null) {
            throw new IllegalArgumentException("Debe seleccionar un costo para editar.");
        }

        Dialog<Costo> dialogo = new Dialog<>();
        dialogo.initOwner(propietario);
        dialogo.setTitle("Editar costo");
        dialogo.setHeaderText(null);

        DialogPane panel = dialogo.getDialogPane();
        panel.getStyleClass().add("dialogo-sistema");
        panel.setMinWidth(460);
        Estilos.aplicar(panel);

        ComboBox<Costo.Tipo> tipo = new ComboBox<>();
        tipo.getItems().setAll(Costo.Tipo.values());
        tipo.setConverter(new StringConverter<Costo.Tipo>() {
            @Override
            public String toString(Costo.Tipo valor) {
                if (valor == null) {
                    return "";
                }
                return valor == Costo.Tipo.FIJO ? "Fijo" : "Variable";
            }

            @Override
            public Costo.Tipo fromString(String texto) {
                return null;
            }
        });
        tipo.setValue(costo.getTipo());
        tipo.setMaxWidth(Double.MAX_VALUE);

        DatePicker fecha = new DatePicker(costo.getFecha());
        Ui.configurarFecha(fecha);
        fecha.setMaxWidth(Double.MAX_VALUE);

        TextField descripcion = Ui.campo("Ej. Arriendo del local");
        descripcion.setText(costo.getDescripcion());

        TextField monto = Ui.campo("0,00");
        monto.setText(numeroPlano(costo.getMonto()));
        limitarDecimales(monto);

        HBox filaTipoFecha = new HBox(12,
                Ui.campoEtiquetado("Tipo", tipo),
                Ui.campoEtiquetado("Fecha", fecha));
        HBox.setHgrow(filaTipoFecha.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(filaTipoFecha.getChildren().get(1), Priority.ALWAYS);

        VBox contenido = new VBox(12,
                filaTipoFecha,
                Ui.campoEtiquetado("Descripción", descripcion),
                Ui.campoEtiquetado("Monto", monto));
        contenido.setPadding(new Insets(8, 4, 4, 4));
        panel.setContent(contenido);

        ButtonType guardar = new ButtonType("Guardar cambios",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        panel.getButtonTypes().setAll(guardar, cancelar);

        Button btnGuardar = (Button) panel.lookupButton(guardar);
        btnGuardar.getStyleClass().add("button-primario");
        Button btnCancelar = (Button) panel.lookupButton(cancelar);
        btnCancelar.getStyleClass().add("button-secundario");

        btnGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            String error = validar(tipo.getValue(), fecha.getValue(),
                    descripcion.getText(), monto.getText());
            if (error != null) {
                evento.consume();
                Dialogo.error(propietario, "Datos del costo", error);
            }
        });

        dialogo.setResultConverter(boton -> {
            if (boton != guardar) {
                return null;
            }
            return new Costo(costo.getId(), fecha.getValue(), tipo.getValue(),
                    descripcion.getText(), Validador.aDouble(monto.getText()));
        });
        dialogo.setOnShown(evento -> descripcion.requestFocus());

        return dialogo.showAndWait();
    }

    private static String validar(Costo.Tipo tipo, java.time.LocalDate fecha,
            String descripcion, String monto) {
        if (tipo == null) {
            return "Debe seleccionar el tipo de costo.";
        }
        if (fecha == null) {
            return "La fecha del costo es obligatoria.";
        }
        if (!Validador.noVacio(descripcion)) {
            return "La descripción del costo es obligatoria.";
        }
        if (!Validador.esNumero(monto)) {
            return "El monto debe ser un número válido.";
        }
        if (Validador.aDouble(monto) <= 0) {
            return "El monto del costo debe ser mayor que cero.";
        }
        return null;
    }

    private static String numeroPlano(double valor) {
        return java.math.BigDecimal.valueOf(valor)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static void limitarDecimales(TextField campo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio
                -> cambio.getControlNewText().matches("\\d{0,12}([.,]\\d{0,4})?")
                ? cambio : null;
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }
}
