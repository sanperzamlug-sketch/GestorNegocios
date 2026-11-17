package vista;

import controlador.ControladorProductos;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Producto;
import util.Formato;
import util.Validador;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Gestión del catálogo y del inventario.
 */
public class PanelProductos extends VBox {

    private static final PseudoClass STOCK_BAJO
            = PseudoClass.getPseudoClass("stock-bajo");

    private final ControladorProductos controlador;
    private final VentanaPrincipal ventana;

    private final ObservableList<Producto> datos = FXCollections.observableArrayList();
    private final FilteredList<Producto> filtrados = new FilteredList<>(datos, p -> true);
    private final SortedList<Producto> ordenados = new SortedList<>(filtrados);

    private final TableView<Producto> tabla = Ui.tabla(
            "Registra tu primer producto para comenzar.");
    private final TextField txtBuscar = Ui.campo("Buscar por nombre o ID...");
    private final ToggleButton filtroStock = new ToggleButton("Stock bajo");
    private final Label lblConteo = new Label();

    private final TextField txtId = Ui.campo("Automático");
    private final TextField txtNombre = Ui.campo("Ej. Camiseta algodón");
    private final TextField txtCosto = Ui.campo("0,00");
    private final TextField txtPrecio = Ui.campo("0,00");
    private final TextField txtStock = Ui.campo("0");
    private final TextField txtStockMinimo = Ui.campo("0");

    private final Button btnGuardar = Ui.boton("Registrar producto", Icono.GUARDAR,
            "button-primario");
    private final Button btnNuevo = Ui.boton("Nuevo", Icono.MAS, "button-secundario");
    private final Button btnEliminar = Ui.boton("Eliminar", Icono.ELIMINAR,
            "button-peligro-suave");

    private final Label bannerTitulo = Ui.etiqueta("", "banner-texto");
    private final Label bannerDetalle = Ui.etiqueta("", "banner-detalle");
    private final Button btnBanner = Ui.boton("Filtrar alertas", null,
            "button-peligro-suave");
    private final HBox banner = new HBox(10);

    private int idEdicion;

    public PanelProductos(ControladorProductos controlador, VentanaPrincipal ventana) {
        this.controlador = controlador;
        this.ventana = ventana;

        getStyleClass().add("page");
        construirInterfaz();
        actualizar();
    }

    private void construirInterfaz() {
        configurarTabla();
        configurarFormulario();

        HBox contenido = new HBox(18, crearTarjetaCatalogo(), crearTarjetaFormulario());
        contenido.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        getChildren().add(contenido);
    }

    private VBox crearTarjetaCatalogo() {
        filtroStock.setGraphic(Icono.crear(Icono.ALERTA, 15));
        filtroStock.getStyleClass().add("filtro-stock");
        filtroStock.selectedProperty().addListener((observable, anterior, actual) -> {
            aplicarFiltro();
            actualizarBanner();
        });

        HBox buscador = new HBox(8, Icono.crear(Icono.BUSCAR, 16), txtBuscar);
        buscador.getStyleClass().add("buscador");
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);
        buscador.setMaxWidth(330);
        buscador.setPrefWidth(300);

        txtBuscar.textProperty().addListener((observable, anterior, actual)
                -> aplicarFiltro());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barraFiltros = new HBox(10, buscador, espacio, filtroStock);
        barraFiltros.setAlignment(Pos.CENTER_LEFT);

        lblConteo.getStyleClass().addAll("badge", "badge-neutro");

        configurarBanner();
        banner.setVisible(false);
        banner.setManaged(false);

        VBox cuerpo = new VBox(12, barraFiltros, tabla, banner);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        VBox tarjeta = Ui.tarjeta("Catálogo de productos",
                "Consulta, filtra y administra tu inventario", cuerpo, lblConteo);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        return tarjeta;
    }

    private VBox crearTarjetaFormulario() {
        HBox filaDinero = new HBox(12,
                Ui.campoEtiquetado("Costo", txtCosto),
                Ui.campoEtiquetado("Precio de venta", txtPrecio));
        HBox.setHgrow(filaDinero.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(filaDinero.getChildren().get(1), Priority.ALWAYS);

        HBox filaStock = new HBox(12,
                Ui.campoEtiquetado("Stock actual", txtStock),
                Ui.campoEtiquetado("Stock mínimo", txtStockMinimo));
        HBox.setHgrow(filaStock.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(filaStock.getChildren().get(1), Priority.ALWAYS);

        VBox formulario = new VBox(13,
                Ui.campoEtiquetado("ID", txtId),
                Ui.campoEtiquetado("Nombre del producto", txtNombre),
                filaDinero,
                filaStock);

        Label ayuda = Ui.etiqueta(
                "El ID se asigna automáticamente al registrar un producto.",
                "tarjeta-subtitulo");
        ayuda.setWrapText(true);

        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnNuevo.setMaxWidth(Double.MAX_VALUE);
        btnEliminar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnNuevo, Priority.ALWAYS);
        HBox.setHgrow(btnEliminar, Priority.ALWAYS);
        HBox accionesSecundarias = new HBox(10, btnNuevo, btnEliminar);

        VBox cuerpo = new VBox(16, formulario, ayuda, btnGuardar, accionesSecundarias);

        VBox tarjeta = Ui.tarjeta("Datos del producto",
                "Registra un producto nuevo o actualiza el seleccionado", cuerpo);
        tarjeta.setPrefWidth(330);
        tarjeta.setMinWidth(310);
        tarjeta.setMaxWidth(372);
        tarjeta.setMaxHeight(Double.MAX_VALUE);
        return tarjeta;
    }

    private void configurarBanner() {
        bannerTitulo.setWrapText(true);
        bannerDetalle.setWrapText(true);
        VBox textos = new VBox(2, bannerTitulo, bannerDetalle);
        textos.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textos, Priority.ALWAYS);

        btnBanner.setMinWidth(Region.USE_PREF_SIZE);
        banner.getChildren().addAll(Icono.crear(Icono.ALERTA, 17), textos,
                btnBanner);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().add("banner-alerta");

        btnBanner.setOnAction(evento -> filtroStock.setSelected(!filtroStock.isSelected()));
    }

    private void configurarTabla() {
        TableColumn<Producto, String> colId = Ui.columna("ID", 50,
                producto -> String.valueOf(producto.getId()));
        TableColumn<Producto, String> colNombre = Ui.columna("Nombre", 100,
                Producto::getNombre);
        TableColumn<Producto, String> colPrecio = Ui.columnaDerecha("Precio", 105,
                producto -> Formato.dinero(producto.getPrecioVenta()));
        TableColumn<Producto, String> colStock = Ui.columnaDerecha("Stock", 52,
                producto -> String.valueOf(producto.getStockActual()));
        TableColumn<Producto, String> colMinimo = Ui.columnaDerecha("Mín.", 50,
                producto -> String.valueOf(producto.getStockMinimo()));
        TableColumn<Producto, String> colEstado = Ui.columna("Estado", 90,
                producto -> producto.tieneStockBajo() ? "BAJO" : "OK");

        colEstado.setCellFactory(lista -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = "BAJO".equals(item)
                        ? Ui.badge("Stock bajo", "badge-peligro")
                        : Ui.badge("Disponible", "badge-exito");
                setGraphic(badge);
                setText(null);
            }
        });

        tabla.getColumns().setAll(List.of(colId, colNombre, colPrecio, colStock,
                colMinimo, colEstado));
        ordenados.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenados);

        colId.setSortType(TableColumn.SortType.ASCENDING);
        tabla.getSortOrder().add(colId);

        tabla.setRowFactory(vista -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean vacio) {
                super.updateItem(producto, vacio);
                pseudoClassStateChanged(STOCK_BAJO,
                        !vacio && producto != null && producto.tieneStockBajo());
            }
        });

        tabla.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, actual) -> {
                    if (actual != null) {
                        idEdicion = actual.getId();
                        cargarProducto(actual);
                        actualizarEstadoBotones();
                    }
                });
    }

    private void configurarFormulario() {
        txtId.setEditable(false);
        txtId.setDisable(true);

        limitarDecimales(txtCosto);
        limitarDecimales(txtPrecio);
        limitarEnteros(txtStock);
        limitarEnteros(txtStockMinimo);

        btnGuardar.setOnAction(evento -> guardar());
        btnNuevo.setOnAction(evento -> nuevoProducto());
        btnEliminar.setOnAction(evento -> eliminar());
        btnEliminar.setDisable(true);
    }

    public void actualizar() {
        int idActual = idEdicion;
        ArrayList<Producto> productos = controlador.listar();
        datos.setAll(productos);
        aplicarFiltro();
        actualizarBanner();

        if (idActual > 0) {
            Producto producto = buscarEnTabla(idActual);
            if (producto != null) {
                idEdicion = idActual;
                tabla.getSelectionModel().select(producto);
                tabla.scrollTo(producto);
            } else {
                limpiarFormulario();
            }
        }
        actualizarEstadoBotones();
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null
                ? "" : txtBuscar.getText().trim().toLowerCase(Locale.ROOT);
        boolean soloBajos = filtroStock.isSelected();

        filtrados.setPredicate(producto -> {
            if (producto == null) {
                return false;
            }
            if (soloBajos && !producto.tieneStockBajo()) {
                return false;
            }
            if (texto.isEmpty()) {
                return true;
            }
            return producto.getNombre().toLowerCase(Locale.ROOT).contains(texto)
                    || String.valueOf(producto.getId()).contains(texto);
        });

        int visibles = ordenados.size();
        int total = datos.size();
        lblConteo.setText(visibles == total
                ? total + (total == 1 ? " producto" : " productos")
                : visibles + " de " + total + " productos");
    }

    private void actualizarBanner() {
        ArrayList<Producto> alertas = controlador.alertasStockBajo();
        boolean hayAlertas = !alertas.isEmpty();
        banner.setVisible(hayAlertas);
        banner.setManaged(hayAlertas);
        if (!hayAlertas) {
            return;
        }

        bannerTitulo.setText(alertas.size() + (alertas.size() == 1
                ? " producto con stock bajo"
                : " productos con stock bajo"));
        if (filtroStock.isSelected()) {
            bannerDetalle.setText("El catálogo está mostrando solo estos productos.");
            btnBanner.setText("Quitar filtro");
        } else {
            bannerDetalle.setText("Revisa la reposición para no quedarte sin unidades.");
            btnBanner.setText("Filtrar alertas");
        }
    }

    private void guardar() {
        try {
            int id = idEdicion;
            Producto producto = leerProducto(id);

            if (id > 0) {
                if (!controlador.editar(producto)) {
                    throw new IllegalArgumentException(
                            "No se encontró el producto a editar.");
                }
                ventana.mostrarMensaje("Producto actualizado correctamente.");
                ventana.actualizarVista();
                seleccionarProducto(id);
            } else {
                controlador.registrar(producto);
                ventana.mostrarMensaje("Producto registrado con el ID "
                        + producto.getId() + ".");
                ventana.actualizarVista();
                limpiarFormulario();
            }
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void eliminar() {
        int id = idEdicion;
        if (id <= 0) {
            ventana.mostrarError("Seleccione un producto de la tabla para eliminarlo.");
            return;
        }

        Producto producto = controlador.buscar(id);
        if (producto == null) {
            ventana.mostrarError("El producto seleccionado ya no existe.");
            return;
        }

        boolean confirmado = ventana.confirmarPeligro(
                "¿Eliminar el producto '" + producto.getNombre() + "'?\n"
                + "Las ventas históricas conservarán sus datos.");
        if (!confirmado) {
            return;
        }

        try {
            if (!controlador.eliminar(id)) {
                throw new IllegalArgumentException("No se pudo eliminar el producto.");
            }
            ventana.mostrarMensaje("Producto eliminado correctamente.");
            limpiarFormulario();
            ventana.actualizarVista();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private Producto leerProducto(int id) {
        String nombre = txtNombre.getText().trim();
        if (!Validador.noVacio(nombre)) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (!Validador.esNumero(txtCosto.getText())) {
            txtCosto.requestFocus();
            throw new IllegalArgumentException("El costo debe ser un número válido.");
        }
        if (!Validador.esNumero(txtPrecio.getText())) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException(
                    "El precio de venta debe ser un número válido.");
        }
        if (!Validador.esEntero(txtStock.getText())) {
            txtStock.requestFocus();
            throw new IllegalArgumentException(
                    "El stock actual debe ser un número entero.");
        }
        if (!Validador.esEntero(txtStockMinimo.getText())) {
            txtStockMinimo.requestFocus();
            throw new IllegalArgumentException(
                    "El stock mínimo debe ser un número entero.");
        }

        double costo = Validador.aDouble(txtCosto.getText());
        double precio = Validador.aDouble(txtPrecio.getText());
        int stockActual = Validador.aEntero(txtStock.getText());
        int stockMinimo = Validador.aEntero(txtStockMinimo.getText());

        if (!Validador.noNegativo(costo)) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        if (!Validador.noNegativo(precio)) {
            throw new IllegalArgumentException(
                    "El precio de venta no puede ser negativo.");
        }
        if (stockActual < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo.");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }

        return new Producto(id, nombre, costo, precio, stockActual, stockMinimo);
    }

    private void cargarProducto(Producto producto) {
        txtId.setText(String.valueOf(producto.getId()));
        txtNombre.setText(producto.getNombre());
        txtCosto.setText(numero(producto.getCosto()));
        txtPrecio.setText(numero(producto.getPrecioVenta()));
        txtStock.setText(String.valueOf(producto.getStockActual()));
        txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        txtNombre.requestFocus();
    }

    public void nuevoProducto() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        idEdicion = 0;
        txtId.clear();
        txtNombre.clear();
        txtCosto.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtStockMinimo.clear();
        tabla.getSelectionModel().clearSelection();
        actualizarEstadoBotones();
        txtNombre.requestFocus();
    }

    private void actualizarEstadoBotones() {
        boolean editando = idEdicion > 0;
        btnGuardar.setText(editando ? "Actualizar producto" : "Registrar producto");
        btnEliminar.setDisable(!editando);
        txtId.setText(editando ? String.valueOf(idEdicion) : "");
    }

    private void seleccionarProducto(int id) {
        Producto producto = buscarEnTabla(id);
        if (producto != null) {
            idEdicion = id;
            tabla.getSelectionModel().select(producto);
            tabla.scrollTo(producto);
        }
        actualizarEstadoBotones();
    }

    private Producto buscarEnTabla(int id) {
        for (Producto producto : tabla.getItems()) {
            if (producto.getId() == id) {
                return producto;
            }
        }
        return null;
    }

    private static String numero(double valor) {
        String texto = String.valueOf(valor);
        if (texto.endsWith(".0")) {
            texto = texto.substring(0, texto.length() - 2);
        }
        return texto.replace('.', ',');
    }

    private static void limitarDecimales(TextField campo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio
                -> cambio.getControlNewText().matches("\\d{0,12}([.,]\\d{0,4})?")
                ? cambio : null;
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    private static void limitarEnteros(TextField campo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio
                -> cambio.getControlNewText().matches("\\d{0,9}") ? cambio : null;
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }
}
