package vista;

import controlador.ControladorVentas;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Venta;
import util.Formato;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Punto de venta: armado de la venta actual y administración del día.
 */
public class PanelVentas extends VBox {

    private final ControladorVentas controlador;
    private final VentanaPrincipal ventana;

    private final Label lblVentaId = new Label("Nueva");
    private final DatePicker fecha = new DatePicker(LocalDate.now());
    private final ComboBox<Producto> comboProducto = new ComboBox<>();
    private final Spinner<Integer> spinnerCantidad = new Spinner<>(1, 999999, 1);
    private final Label lblStock = new Label("Disponible: —");

    private final Button btnNueva = Ui.boton("Nueva", Icono.MAS,
            "button-secundario");
    private final Button btnCancelar = Ui.boton("Cancelar", Icono.CERRAR,
            "button-fantasma");
    private final Button btnGuardar = Ui.boton("Guardar venta", Icono.GUARDAR,
            "button-primario");
    private final Button btnAgregar = Ui.boton("Agregar", Icono.MAS,
            "button-primario");
    private final Button btnQuitar = Ui.boton("Quitar línea", Icono.ELIMINAR,
            "button-peligro-suave");
    private final Button btnEditarVenta = Ui.boton("Editar", Icono.EDITAR,
            "button-secundario");
    private final Button btnEliminarVenta = Ui.boton("Eliminar", Icono.ELIMINAR,
            "button-peligro-suave");

    private final TableView<DetalleVenta> tablaDetalle = Ui.tabla(
            "La venta actual todavía no tiene líneas.");
    private final TableView<Venta> tablaVentas = Ui.tabla(
            "Todavía no se han registrado ventas hoy.");

    private final Label lblTotalVenta = new Label("$0,00");
    private final Label lblTotalDia = new Label("$0,00");
    private final Label lblLineas = new Label();

    public PanelVentas(ControladorVentas controlador, VentanaPrincipal ventana) {
        this.controlador = controlador;
        this.ventana = ventana;

        getStyleClass().add("page");
        construirInterfaz();
        actualizar();
    }

    private void construirInterfaz() {
        configurarFecha();
        configurarProductos();
        configurarTablaDetalle();
        configurarTablaVentas();
        configurarAcciones();

        SplitPane division = new SplitPane(crearTarjetaVentaActual(),
                crearTarjetaVentasDia());
        division.setOrientation(javafx.geometry.Orientation.VERTICAL);
        division.setDividerPositions(0.58);
        division.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(division, Priority.ALWAYS);

        getChildren().add(division);
    }

    private VBox crearTarjetaVentaActual() {
        lblVentaId.getStyleClass().addAll("badge", "badge-neutro");
        HBox tituloFila = new HBox(8,
                Ui.etiqueta("Venta actual", "tarjeta-titulo"), lblVentaId);
        tituloFila.setAlignment(Pos.CENTER_LEFT);

        VBox textos = new VBox(3, tituloFila,
                Ui.etiqueta("Agrega productos y guarda la venta del día",
                        "tarjeta-subtitulo"));

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        VBox cajaFecha = new VBox(4,
                Ui.etiqueta("Fecha", "etiqueta-campo"), fecha);
        fecha.setPrefWidth(146);

        HBox cabecera = new HBox(10, textos, espacio, cajaFecha,
                btnNueva, btnCancelar, btnGuardar);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        HBox filaProducto = new HBox(10);
        filaProducto.setAlignment(Pos.CENTER_LEFT);
        comboProducto.setMaxWidth(Double.MAX_VALUE);
        comboProducto.setPrefWidth(280);
        HBox.setHgrow(comboProducto, Priority.ALWAYS);
        spinnerCantidad.setPrefWidth(96);
        lblStock.getStyleClass().addAll("stock-chip", "stock-neutro");

        filaProducto.getChildren().addAll(
                Ui.etiqueta("Producto", "etiqueta-campo"),
                comboProducto,
                Ui.etiqueta("Cantidad", "etiqueta-campo"),
                spinnerCantidad,
                btnAgregar,
                lblStock);

        Region espacioLineas = new Region();
        HBox.setHgrow(espacioLineas, Priority.ALWAYS);
        HBox cabeceraLineas = new HBox(8,
                Ui.etiqueta("Líneas de la venta", "tarjeta-subtitulo"),
                espacioLineas, btnQuitar);
        cabeceraLineas.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(tablaDetalle, Priority.ALWAYS);
        tablaDetalle.setMinHeight(90);

        lblLineas.getStyleClass().add("tarjeta-subtitulo");
        lblTotalVenta.getStyleClass().add("total-actual");
        VBox cajaTotal = new VBox(0,
                Ui.etiqueta("Total de la venta", "etiqueta-total"), lblTotalVenta);
        cajaTotal.setAlignment(Pos.CENTER_RIGHT);

        Region espacioPie = new Region();
        HBox.setHgrow(espacioPie, Priority.ALWAYS);
        HBox pie = new HBox(8, lblLineas, espacioPie, cajaTotal);
        pie.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(12, cabecera, filaProducto, cabeceraLineas,
                tablaDetalle, pie);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tarjeta.setMinHeight(300);
        return tarjeta;
    }

    private VBox crearTarjetaVentasDia() {
        VBox textos = new VBox(3,
                Ui.etiqueta("Ventas del día", "tarjeta-titulo"),
                Ui.etiqueta("Selecciona una venta para editarla o eliminarla",
                        "tarjeta-subtitulo"));

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        lblTotalDia.getStyleClass().add("total-dia");
        VBox cajaTotal = new VBox(0,
                Ui.etiqueta("Total del día", "etiqueta-total"), lblTotalDia);
        cajaTotal.setAlignment(Pos.CENTER_RIGHT);

        HBox cabecera = new HBox(12, textos, espacio, cajaTotal,
                btnEditarVenta, btnEliminarVenta);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(tablaVentas, Priority.ALWAYS);
        tablaVentas.setMinHeight(100);

        VBox tarjeta = new VBox(12, cabecera, tablaVentas);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tarjeta.setMinHeight(200);
        return tarjeta;
    }

    private void configurarFecha() {
        fecha.setConverter(new StringConverter<LocalDate>() {
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
        fecha.setValue(LocalDate.now());
    }

    private void configurarProductos() {
        comboProducto.setPromptText("Seleccione un producto");
        comboProducto.setCellFactory(lista -> new CeldaProducto());
        comboProducto.setButtonCell(new CeldaProducto());
        comboProducto.setVisibleRowCount(9);

        comboProducto.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, actual) -> actualizarStockDisponible());

        SpinnerValueFactory.IntegerSpinnerValueFactory fabrica
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999999, 1);
        spinnerCantidad.setValueFactory(fabrica);
        spinnerCantidad.setEditable(true);

        spinnerCantidad.getEditor().focusedProperty().addListener(
                (observable, anterior, enfocado) -> {
                    if (!enfocado) {
                        confirmarCantidad();
                    }
                });
        spinnerCantidad.getEditor().setOnAction(evento -> {
            confirmarCantidad();
            agregarLinea();
        });
    }

    private void configurarTablaDetalle() {
        TableColumn<DetalleVenta, String> colProducto = Ui.columna("Producto", 235,
                DetalleVenta::getNombreProducto);
        TableColumn<DetalleVenta, String> colCantidad = Ui.columnaDerecha(
                "Cantidad", 82, detalle -> String.valueOf(detalle.getCantidad()));
        TableColumn<DetalleVenta, String> colPrecio = Ui.columnaDerecha(
                "Precio unitario", 115,
                detalle -> Formato.dinero(detalle.getPrecioUnitario()));
        TableColumn<DetalleVenta, String> colCosto = Ui.columnaDerecha(
                "Costo unitario", 115,
                detalle -> Formato.dinero(detalle.getCostoUnitario()));
        TableColumn<DetalleVenta, String> colSubtotal = Ui.columnaDerecha(
                "Subtotal", 120,
                detalle -> Formato.dinero(detalle.calcularSubtotal()));

        tablaDetalle.getColumns().setAll(List.of(colProducto, colCantidad,
                colPrecio, colCosto, colSubtotal));
        tablaDetalle.setItems(FXCollections.observableArrayList());
        tablaDetalle.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, actual) -> actualizarBotonQuitar());
    }

    private void configurarTablaVentas() {
        TableColumn<Venta, String> colVenta = Ui.columna("Venta", 78,
                venta -> "#" + venta.getId());
        TableColumn<Venta, String> colFecha = Ui.columna("Fecha", 105,
                venta -> Formato.fecha(venta.getFecha()));
        TableColumn<Venta, String> colLineas = Ui.columna("Líneas", 72,
                venta -> String.valueOf(venta.getDetalles().size()));
        TableColumn<Venta, String> colTotal = Ui.columnaDerecha("Total", 118,
                venta -> Formato.dinero(venta.calcularTotal()));

        tablaVentas.getColumns().setAll(
                List.of(colVenta, colFecha, colLineas, colTotal));
        tablaVentas.setItems(FXCollections.observableArrayList());
        tablaVentas.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, actual) -> actualizarBotonesVenta());
    }

    private void configurarAcciones() {
        btnNueva.setOnAction(evento -> nuevaVenta());
        btnCancelar.setOnAction(evento -> cancelarVenta());
        btnGuardar.setOnAction(evento -> guardarVenta());
        btnAgregar.setOnAction(evento -> agregarLinea());
        btnQuitar.setOnAction(evento -> quitarLinea());
        btnEditarVenta.setOnAction(evento -> editarVentaSeleccionada());
        btnEliminarVenta.setOnAction(evento -> eliminarVentaSeleccionada());

        btnQuitar.setDisable(true);
        btnGuardar.setDisable(true);
        btnEditarVenta.setDisable(true);
        btnEliminarVenta.setDisable(true);
    }

    public void actualizar() {
        cargarProductos();
        actualizarTablaDetalle();
        actualizarTablaVentas();
        actualizarEstado();
    }

    private void cargarProductos() {
        Producto seleccionado = comboProducto.getSelectionModel().getSelectedItem();
        int idSeleccionado = seleccionado == null ? 0 : seleccionado.getId();

        ArrayList<Producto> productos = controlador.listarProductos();
        productos.sort(Comparator.comparing(Producto::getNombre,
                String.CASE_INSENSITIVE_ORDER));
        comboProducto.getItems().setAll(productos);

        if (idSeleccionado > 0) {
            for (Producto producto : comboProducto.getItems()) {
                if (producto.getId() == idSeleccionado) {
                    comboProducto.getSelectionModel().select(producto);
                    break;
                }
            }
        }
        actualizarStockDisponible();
    }

    private void actualizarTablaDetalle() {
        Venta venta = controlador.getVentaActual();
        tablaDetalle.setItems(FXCollections.observableArrayList(
                venta.getDetalles()));
        int lineas = venta.getDetalles().size();
        lblLineas.setText(lineas + (lineas == 1 ? " línea" : " líneas"));
        actualizarBotonQuitar();
    }

    private void actualizarTablaVentas() {
        Venta seleccionada = tablaVentas.getSelectionModel().getSelectedItem();
        int idSeleccionado = seleccionada == null ? 0 : seleccionada.getId();

        ArrayList<Venta> ventas = controlador.ventasDelDia();
        java.util.Collections.reverse(ventas);
        tablaVentas.setItems(FXCollections.observableArrayList(ventas));

        if (idSeleccionado > 0) {
            for (Venta venta : tablaVentas.getItems()) {
                if (venta.getId() == idSeleccionado) {
                    tablaVentas.getSelectionModel().select(venta);
                    break;
                }
            }
        }
        lblTotalDia.setText(Formato.dinero(controlador.totalDelDia()));
        actualizarBotonesVenta();
    }

    private void actualizarEstado() {
        Venta venta = controlador.getVentaActual();
        boolean nueva = venta.getId() <= 0;
        lblVentaId.setText(nueva ? "Nueva" : "#" + venta.getId());
        lblVentaId.getStyleClass().setAll("badge",
                nueva ? "badge-neutro" : "badge-info");

        lblTotalVenta.setText(Formato.dinero(venta.calcularTotal()));
        btnGuardar.setDisable(venta.estaVacia());
        btnCancelar.setDisable(venta.estaVacia() && venta.getId() <= 0);
        actualizarBotonQuitar();
        actualizarBotonesVenta();
    }

    private void actualizarStockDisponible() {
        Producto producto = comboProducto.getSelectionModel().getSelectedItem();
        if (producto == null) {
            lblStock.setText("Disponible: —");
            lblStock.getStyleClass().setAll("stock-chip", "stock-neutro");
            btnAgregar.setDisable(true);
            return;
        }

        int disponible = controlador.stockDisponibleParaAgregar(producto.getId());
        lblStock.setText("Disponible: " + disponible);
        lblStock.getStyleClass().setAll("stock-chip",
                disponible > 0 ? "stock-ok" : "stock-bajo");
        btnAgregar.setDisable(disponible <= 0);
    }

    private void actualizarBotonQuitar() {
        btnQuitar.setDisable(tablaDetalle.getSelectionModel()
                .getSelectedItem() == null);
    }

    private void actualizarBotonesVenta() {
        boolean hayVenta = tablaVentas.getSelectionModel()
                .getSelectedItem() != null;
        btnEditarVenta.setDisable(!hayVenta);
        btnEliminarVenta.setDisable(!hayVenta);
    }

    private void agregarLinea() {
        confirmarCantidad();
        Producto producto = comboProducto.getSelectionModel().getSelectedItem();
        if (producto == null) {
            ventana.mostrarError("Seleccione un producto para agregar a la venta.");
            return;
        }

        int cantidad = spinnerCantidad.getValue();
        try {
            controlador.agregarLinea(producto.getId(), cantidad);
            spinnerCantidad.getValueFactory().setValue(1);
            actualizar();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void quitarLinea() {
        int indice = tablaDetalle.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            ventana.mostrarError(
                    "Seleccione una línea de la venta actual para quitarla.");
            return;
        }

        try {
            controlador.quitarLinea(indice);
            actualizar();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void guardarVenta() {
        try {
            LocalDate fechaVenta = leerFecha();
            int id = controlador.getVentaActual().getId();
            controlador.getVentaActual().setFecha(fechaVenta);
            controlador.guardarVenta();
            if (id == 0) {
                id = controlador.getVentaActual().getId();
            }

            ventana.mostrarMensaje("Venta #" + id + " guardada correctamente.");
            controlador.iniciarVenta();
            fecha.setValue(LocalDate.now());
            spinnerCantidad.getValueFactory().setValue(1);
            comboProducto.getSelectionModel().clearSelection();
            ventana.actualizarVista();
            comboProducto.requestFocus();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    /**
     * Inicia una venta vacía. Se expone para el acceso rápido del panel de
     * inicio.
     */
    public void nuevaVenta() {
        if (!controlador.getVentaActual().estaVacia()
                && !ventana.confirmar("Se descartará la venta actual. "
                        + "¿Desea continuar?")) {
            return;
        }
        controlador.iniciarVenta();
        fecha.setValue(LocalDate.now());
        spinnerCantidad.getValueFactory().setValue(1);
        comboProducto.getSelectionModel().clearSelection();
        actualizar();
        comboProducto.requestFocus();
    }

    private void cancelarVenta() {
        if (!controlador.getVentaActual().estaVacia()
                && !ventana.confirmar("Se descartarán los cambios de la venta "
                        + "actual. ¿Continuar?")) {
            return;
        }
        controlador.iniciarVenta();
        fecha.setValue(LocalDate.now());
        spinnerCantidad.getValueFactory().setValue(1);
        comboProducto.getSelectionModel().clearSelection();
        actualizar();
    }

    private void editarVentaSeleccionada() {
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            ventana.mostrarError(
                    "Seleccione una venta de la tabla para editarla.");
            return;
        }

        if (!controlador.getVentaActual().estaVacia()
                && !ventana.confirmar("Se reemplazará la venta actual por la "
                        + "venta #" + venta.getId() + ". ¿Desea continuar?")) {
            return;
        }

        try {
            controlador.cargarVentaParaEditar(venta);
            fecha.setValue(venta.getFecha());
            actualizar();
            ventana.mostrarMensaje("Venta #" + venta.getId()
                    + " cargada para edición. Modifica las líneas y guarda los "
                    + "cambios.");
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void eliminarVentaSeleccionada() {
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            ventana.mostrarError(
                    "Seleccione una venta de la tabla para eliminarla.");
            return;
        }

        boolean confirmado = ventana.confirmarPeligro(
                "¿Eliminar la venta #" + venta.getId() + "?\n"
                + "El stock de sus productos será repuesto.");
        if (!confirmado) {
            return;
        }

        try {
            if (!controlador.eliminarVenta(venta.getId())) {
                throw new IllegalArgumentException("No se pudo eliminar la venta.");
            }
            ventana.mostrarMensaje("Venta #" + venta.getId()
                    + " eliminada correctamente.");
            ventana.actualizarVista();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private LocalDate leerFecha() {
        String texto = fecha.getEditor().getText();
        texto = texto == null ? "" : texto.trim();
        if (texto.isEmpty()) {
            fecha.requestFocus();
            throw new IllegalArgumentException("La fecha de la venta es obligatoria.");
        }
        try {
            return LocalDate.parse(texto, Formato.formatoFecha());
        } catch (DateTimeParseException ex) {
            fecha.requestFocus();
            throw new IllegalArgumentException(
                    "Fecha inválida. Use el formato dd/MM/aaaa.");
        }
    }

    private void confirmarCantidad() {
        String texto = spinnerCantidad.getEditor().getText();
        try {
            int valor = Integer.parseInt(texto == null ? "" : texto.trim());
            valor = Math.max(1, Math.min(999999, valor));
            spinnerCantidad.getValueFactory().setValue(valor);
            spinnerCantidad.getEditor().setText(String.valueOf(valor));
        } catch (NumberFormatException ex) {
            spinnerCantidad.getEditor().setText(
                    String.valueOf(spinnerCantidad.getValue()));
        }
    }

    private static final class CeldaProducto extends ListCell<Producto> {

        @Override
        protected void updateItem(Producto producto, boolean vacio) {
            super.updateItem(producto, vacio);
            if (vacio || producto == null) {
                setText(null);
                return;
            }
            setText(producto.getId() + " · " + producto.getNombre()
                    + "    Stock: " + producto.getStockActual());
        }
    }
}
