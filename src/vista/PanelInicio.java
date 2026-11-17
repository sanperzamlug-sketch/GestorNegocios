package vista;

import controlador.ControladorProductos;
import controlador.ControladorReportes;
import controlador.ControladorVentas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Producto;
import modelo.Venta;
import util.Formato;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Panel de resumen: indicadores del día, últimas ventas y alertas de stock.
 */
public class PanelInicio extends VBox {

    private final ControladorProductos controladorProductos;
    private final ControladorVentas controladorVentas;
    private final ControladorReportes controladorReportes;
    private final VentanaPrincipal ventana;

    private final Ui.Kpi kpiIngresos = Ui.kpi("Ingresos de hoy", Icono.DINERO,
            "acento-indigo");
    private final Ui.Kpi kpiTicket = Ui.kpi("Ticket promedio", Icono.CARRITO,
            "acento-verde");
    private final Ui.Kpi kpiProductos = Ui.kpi("Productos", Icono.CAJA,
            "acento-violeta");
    private final Ui.Kpi kpiUtilidad = Ui.kpi("Utilidad acumulada", Icono.TENDENCIA,
            "acento-azul");

    private final TableView<Venta> tablaRecientes = Ui.tabla(
            "Aún no hay ventas registradas hoy.");
    private final VBox listaStock = new VBox(0);
    private final Label etiquetaStock = new Label();
    private final Label etiquetaVentas = new Label();

    public PanelInicio(ControladorProductos controladorProductos,
            ControladorVentas controladorVentas,
            ControladorReportes controladorReportes,
            VentanaPrincipal ventana) {
        this.controladorProductos = controladorProductos;
        this.controladorVentas = controladorVentas;
        this.controladorReportes = controladorReportes;
        this.ventana = ventana;

        getStyleClass().add("page");
        construirInterfaz();
        actualizar();
    }

    private void construirInterfaz() {
        HBox filaIndicadores = new HBox(16,
                kpiIngresos.raiz, kpiTicket.raiz, kpiProductos.raiz, kpiUtilidad.raiz);
        filaIndicadores.setAlignment(Pos.TOP_LEFT);

        configurarTablaVentas();
        etiquetaVentas.getStyleClass().add("tarjeta-subtitulo");

        var botonVentas = Ui.boton("Ver todas", Icono.FLECHA, "button-secundario");
        botonVentas.setOnAction(evento -> ventana.irAVentas());

        VBox tarjetaVentas = Ui.tarjeta("Ventas de hoy",
                "Últimos movimientos registrados durante la jornada",
                tablaRecientes, botonVentas);
        tarjetaVentas.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(tarjetaVentas, Priority.ALWAYS);

        VBox acciones = new VBox(10);
        var botonNuevaVenta = Ui.boton("Nueva venta", Icono.MAS, "button-primario");
        botonNuevaVenta.setMaxWidth(Double.MAX_VALUE);
        botonNuevaVenta.setOnAction(evento -> ventana.nuevaVentaRapida());

        var botonNuevoProducto = Ui.boton("Registrar producto", Icono.CAJA,
                "button-secundario");
        botonNuevoProducto.setMaxWidth(Double.MAX_VALUE);
        botonNuevoProducto.setOnAction(evento -> ventana.nuevoProductoRapido());

        var botonFinanzas = Ui.boton("Analizar finanzas", Icono.DINERO,
                "button-fantasma");
        botonFinanzas.setMaxWidth(Double.MAX_VALUE);
        botonFinanzas.setOnAction(evento -> ventana.irAFinanzas());

        var botonHistorial = Ui.boton("Consultar historial", Icono.RELOJ,
                "button-fantasma");
        botonHistorial.setMaxWidth(Double.MAX_VALUE);
        botonHistorial.setOnAction(evento -> ventana.irAHistorial());

        acciones.getChildren().addAll(botonNuevaVenta, botonNuevoProducto,
                botonFinanzas, botonHistorial);

        VBox tarjetaAcciones = Ui.tarjeta("Acciones rápidas",
                "Atajos para las tareas más frecuentes", acciones);

        configurarListaStock();
        ScrollPane scrollStock = new ScrollPane(listaStock);
        scrollStock.setFitToWidth(true);
        scrollStock.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollStock.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollStock, Priority.ALWAYS);

        var botonRevisar = Ui.boton("Revisar", Icono.FLECHA, "button-fantasma");
        botonRevisar.setOnAction(evento -> ventana.irAProductos());

        VBox tarjetaStock = Ui.tarjeta("Stock bajo",
                "Productos por reponer", scrollStock, botonRevisar);
        tarjetaStock.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(tarjetaStock, Priority.ALWAYS);

        VBox columnaDerecha = new VBox(18, tarjetaAcciones, tarjetaStock);
        columnaDerecha.setPrefWidth(332);
        columnaDerecha.setMinWidth(312);
        columnaDerecha.setMaxWidth(370);

        HBox filaContenido = new HBox(18, tarjetaVentas, columnaDerecha);
        filaContenido.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(filaContenido, Priority.ALWAYS);

        getChildren().addAll(filaIndicadores, filaContenido);
        VBox.setVgrow(filaContenido, Priority.ALWAYS);
    }

    private void configurarTablaVentas() {
        TableColumn<Venta, String> colVenta = Ui.columna("Venta", 75,
                venta -> "#" + venta.getId());
        TableColumn<Venta, String> colFecha = Ui.columna("Fecha", 95,
                venta -> Formato.fecha(venta.getFecha()));
        TableColumn<Venta, String> colLineas = Ui.columna("Líneas", 70,
                venta -> String.valueOf(venta.getDetalles().size()));
        TableColumn<Venta, String> colTotal = Ui.columnaDerecha("Total", 115,
                venta -> Formato.dinero(venta.calcularTotal()));

        tablaRecientes.getColumns().setAll(
                List.of(colVenta, colFecha, colLineas, colTotal));
        tablaRecientes.setItems(FXCollections.observableArrayList());
        tablaRecientes.setRowFactory(tabla -> {
            TableRow<Venta> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    ventana.irAVentas();
                }
            });
            return fila;
        });
    }

    private void configurarListaStock() {
        listaStock.setFillWidth(true);
        listaStock.setSpacing(0);
    }

    public void actualizar() {
        ArrayList<Venta> ventasHoy = controladorVentas.ventasDelDia();
        int cantidadVentas = ventasHoy.size();
        double totalHoy = controladorVentas.totalDelDia();

        kpiIngresos.valor.setText(Formato.dinero(totalHoy));
        kpiIngresos.pista.setText(cantidadVentas == 0
                ? "Sin ventas registradas todavía"
                : cantidadVentas + (cantidadVentas == 1
                        ? " venta registrada hoy" : " ventas registradas hoy"));

        kpiTicket.valor.setText(Formato.dinero(
                cantidadVentas == 0 ? 0 : totalHoy / cantidadVentas));
        kpiTicket.pista.setText("Promedio por venta de hoy");

        ArrayList<Producto> productos = controladorProductos.listar();
        ArrayList<Producto> alertas = controladorProductos.alertasStockBajo();
        kpiProductos.valor.setText(String.valueOf(productos.size()));
        kpiProductos.pista.setText(alertas.isEmpty()
                ? "Inventario sin alertas"
                : alertas.size() + (alertas.size() == 1
                        ? " producto requiere reposición"
                        : " productos requieren reposición"));

        kpiUtilidad.valor.setText(Formato.dinero(controladorReportes.utilidad()));
        kpiUtilidad.pista.setText("Ganancia acumulada del negocio");

        ArrayList<Venta> recientes = new ArrayList<>(ventasHoy);
        Collections.reverse(recientes);
        tablaRecientes.setItems(FXCollections.observableArrayList(recientes));

        actualizarStock(alertas);
    }

    private void actualizarStock(ArrayList<Producto> alertas) {
        listaStock.getChildren().clear();
        etiquetaStock.setText(alertas.size() + " alertas");

        if (alertas.isEmpty()) {
            Label mensaje = Ui.etiqueta(
                    "Todo el inventario está por encima del stock mínimo.",
                    "estado-vacio-detalle");
            mensaje.setWrapText(true);
            VBox vacio = new VBox(6, Ui.etiqueta("Inventario saludable",
                    "estado-vacio-titulo"), mensaje);
            vacio.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
            listaStock.getChildren().add(vacio);
            return;
        }

        for (Producto producto : alertas) {
            Label nombre = Ui.etiqueta(producto.getNombre(), "item-alerta-nombre");
            Label detalle = Ui.etiqueta(
                    "Stock actual: " + producto.getStockActual()
                    + " · mínimo: " + producto.getStockMinimo(),
                    "item-alerta-detalle");
            VBox textos = new VBox(2, nombre, detalle);

            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            Label badge = Ui.badge(producto.getStockActual() + " / "
                    + producto.getStockMinimo(), "badge-peligro");

            HBox fila = new HBox(8, textos, espacio, badge);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("item-alerta");
            fila.setCursor(Cursor.HAND);
            fila.setOnMouseClicked(evento -> ventana.irAProductos());
            Tooltip.install(fila, new Tooltip("Abrir la gestión de productos"));

            listaStock.getChildren().add(fila);
        }
    }
}
