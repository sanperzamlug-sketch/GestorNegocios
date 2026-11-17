package vista.componentes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import util.Formato;

/**
 * Fabrica de componentes visuales compartidos por las pantallas.
 */
public final class Ui {

    private Ui() {
    }

    public static Label etiqueta(String texto, String... clases) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().addAll(clases);
        return etiqueta;
    }

    public static Button boton(String texto, String rutaIcono, String... clases) {
        Button boton = new Button(texto);
        if (rutaIcono != null) {
            boton.setGraphic(Icono.crear(rutaIcono, 16));
        }
        boton.getStyleClass().addAll(clases);
        return boton;
    }

    public static Button botonIcono(String rutaIcono, String tooltip, String... clases) {
        Button boton = new Button();
        boton.setGraphic(Icono.crear(rutaIcono, 17));
        boton.getStyleClass().addAll("boton-icono");
        boton.getStyleClass().addAll(clases);
        boton.setTooltip(new Tooltip(tooltip));
        boton.setAccessibleText(tooltip);
        return boton;
    }

    public static TextField campo(String prompt) {
        TextField campo = new TextField();
        campo.setPromptText(prompt);
        campo.setMaxWidth(Double.MAX_VALUE);
        return campo;
    }

    public static void configurarFecha(DatePicker campo) {
        campo.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate valor) {
                return valor == null ? "" : Formato.fecha(valor);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), Formato.formatoFecha());
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        });
    }

    public static VBox campoEtiquetado(String texto, Node control) {
        Label etiqueta = etiqueta(texto, "etiqueta-campo");
        VBox caja = new VBox(6, etiqueta, control);
        if (control instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return caja;
    }

    public static HBox cabeceraTarjeta(String titulo, String subtitulo, Node... acciones) {
        Label tituloLabel = etiqueta(titulo, "tarjeta-titulo");
        tituloLabel.setWrapText(true);

        VBox textos = new VBox(3, tituloLabel);
        if (subtitulo != null && !subtitulo.isBlank()) {
            Label subtituloLabel = etiqueta(subtitulo, "tarjeta-subtitulo");
            subtituloLabel.setWrapText(true);
            textos.getChildren().add(subtituloLabel);
        }
        textos.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textos, Priority.ALWAYS);

        HBox cabecera = new HBox(12, textos);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        for (Node accion : acciones) {
            if (accion != null) {
                cabecera.getChildren().add(accion);
            }
        }
        return cabecera;
    }

    public static VBox tarjeta(String titulo, String subtitulo, Node cuerpo, Node... acciones) {
        VBox tarjeta = new VBox(16);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.getChildren().add(cabeceraTarjeta(titulo, subtitulo, acciones));
        if (cuerpo != null) {
            VBox.setVgrow(cuerpo, Priority.ALWAYS);
            tarjeta.getChildren().add(cuerpo);
        }
        return tarjeta;
    }

    public static VBox tarjetaSimple(Node... contenido) {
        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.getChildren().addAll(contenido);
        return tarjeta;
    }

    public static <T> TableView<T> tabla(String mensajeVacio) {
        TableView<T> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tabla.setFixedCellSize(44);

        VBox vacio = estadoVacio(Icono.CAJA, "Sin información", mensajeVacio);
        Node iconoVacio = vacio.getChildren().get(0);
        iconoVacio.visibleProperty().bind(
                tabla.heightProperty().greaterThanOrEqualTo(150));
        iconoVacio.managedProperty().bind(iconoVacio.visibleProperty());

        Node detalleVacio = vacio.getChildren().get(2);
        detalleVacio.visibleProperty().bind(
                tabla.heightProperty().greaterThanOrEqualTo(100));
        detalleVacio.managedProperty().bind(detalleVacio.visibleProperty());

        tabla.setPlaceholder(vacio);
        return tabla;
    }

    public static <T> TableColumn<T, String> columna(String titulo, double ancho,
            Function<T, String> valor) {
        return columna(titulo, ancho, valor, false);
    }

    public static <T> TableColumn<T, String> columnaDerecha(String titulo, double ancho,
            Function<T, String> valor) {
        return columna(titulo, ancho, valor, true);
    }

    private static <T> TableColumn<T, String> columna(String titulo, double ancho,
            Function<T, String> valor, boolean derecha) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(fila -> new ReadOnlyStringWrapper(
                fila.getValue() == null ? "" : valor.apply(fila.getValue())));
        columna.setPrefWidth(ancho);
        columna.setMinWidth(50);
        columna.setCellFactory(lista -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(vacio ? null : item);
                setAlignment(derecha ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            }
        });
        return columna;
    }

    public static Label badge(String texto, String variante) {
        Label badge = new Label(texto);
        badge.getStyleClass().addAll("badge", variante);
        return badge;
    }

    public static VBox estadoVacio(String rutaIcono, String titulo, String detalle) {
        StackPane icono = new StackPane(Icono.crear(rutaIcono, 26));
        icono.getStyleClass().add("estado-vacio-icono");

        Label tituloLabel = etiqueta(titulo, "estado-vacio-titulo");
        Label detalleLabel = etiqueta(detalle, "estado-vacio-detalle");
        detalleLabel.setWrapText(true);
        detalleLabel.setMaxWidth(320);

        VBox caja = new VBox(6, icono, tituloLabel, detalleLabel);
        caja.setAlignment(Pos.CENTER);
        caja.getStyleClass().add("estado-vacio");
        return caja;
    }

    public static Kpi kpi(String texto, String rutaIcono, String acento) {
        Label valor = etiqueta("—", "kpi-valor");
        Label pista = etiqueta(" ", "kpi-pista");

        StackPane icono = new StackPane(Icono.crear(rutaIcono, 19));
        icono.getStyleClass().addAll("kpi-icono", acento);

        HBox cabecera = new HBox(9, icono, etiqueta(texto, "kpi-etiqueta"));
        cabecera.setAlignment(Pos.CENTER_LEFT);

        VBox raiz = new VBox(8, cabecera, valor, pista);
        raiz.setMaxWidth(Double.MAX_VALUE);
        raiz.getStyleClass().add("kpi-card");
        HBox.setHgrow(raiz, Priority.ALWAYS);
        return new Kpi(raiz, valor, pista);
    }

    /**
     * Tarjeta de indicador con referencias a sus etiquetas mutables.
     */
    public static final class Kpi {

        public final VBox raiz;
        public final Label valor;
        public final Label pista;

        private Kpi(VBox raiz, Label valor, Label pista) {
            this.raiz = raiz;
            this.valor = valor;
            this.pista = pista;
        }
    }
}
