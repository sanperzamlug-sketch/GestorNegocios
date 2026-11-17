package vista;

import controlador.ControladorFinanzas;
import controlador.ControladorFinanzas.ResumenMensual;
import controlador.ControladorHistorial;
import controlador.ControladorVentas;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Costo;
import modelo.DetalleVenta;
import modelo.MovimientoInventario;
import modelo.RegistroProducto;
import modelo.Venta;
import util.Formato;
import vista.componentes.DialogoCosto;
import vista.componentes.DialogoLimpiarHistorial;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Consulta y limpieza de los historiales de ventas, productos, inventario,
 * costos y evolución financiera.
 */
public class PanelHistorial extends VBox {

    private static final int TAB_VENTAS = 0;
    private static final int TAB_PRODUCTOS = 1;
    private static final int TAB_INVENTARIO = 2;
    private static final int TAB_COSTOS = 3;
    private static final int TAB_FINANCIERO = 4;

    private final ControladorHistorial controlador;
    private final ControladorFinanzas controladorFinanzas;
    private final ControladorVentas controladorVentas;
    private final VentanaPrincipal ventana;

    private final DatePicker desde = new DatePicker();
    private final DatePicker hasta = new DatePicker();
    private final TabPane pestanas = new TabPane();

    private final TableView<Venta> tablaVentas = Ui.tabla(
            "No hay ventas registradas en el periodo seleccionado.");
    private final TableView<DetalleVenta> tablaDetalleVenta = Ui.tabla(
            "Selecciona una venta para ver sus líneas.");
    private final TableView<RegistroProducto> tablaProductos = Ui.tabla(
            "Aún no hay cambios registrados en el catálogo.");
    private final TableView<MovimientoInventario> tablaMovimientos = Ui.tabla(
            "Aún no hay movimientos de inventario registrados.");
    private final TableView<Costo> tablaCostos = Ui.tabla(
            "No hay costos registrados en el periodo seleccionado.");
    private final TableView<ResumenMensual> tablaFinanciero = Ui.tabla(
            "Aún no hay información financiera para mostrar.");

    private BarChart<String, Number> grafico;
    private final Label lblSinDatosFinancieros = new Label(
            "Registra ventas o costos para ver la evolución mensual.");

    public PanelHistorial(ControladorHistorial controlador,
            ControladorFinanzas controladorFinanzas,
            ControladorVentas controladorVentas, VentanaPrincipal ventana) {
        this.controlador = controlador;
        this.controladorFinanzas = controladorFinanzas;
        this.controladorVentas = controladorVentas;
        this.ventana = ventana;

        getStyleClass().add("page");
        construirInterfaz();
        actualizar();
    }

    private void construirInterfaz() {
        pestanas.getStyleClass().add("tabs-historial");
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestanas.getTabs().addAll(
                crearPestana("Ventas", crearContenidoVentas()),
                crearPestana("Productos", crearContenidoProductos()),
                crearPestana("Inventario", crearContenidoInventario()),
                crearPestana("Costos", crearContenidoCostos()),
                crearPestana("Financiero", crearContenidoFinanciero()));

        VBox.setVgrow(pestanas, Priority.ALWAYS);
        getChildren().addAll(crearBarraPeriodo(), pestanas);
    }

    private Tab crearPestana(String titulo, VBox contenido) {
        Tab pestana = new Tab(titulo);
        pestana.setClosable(false);
        pestana.setContent(contenido);
        return pestana;
    }

    private HBox crearBarraPeriodo() {
        Ui.configurarFecha(desde);
        Ui.configurarFecha(hasta);

        LocalDate hoy = LocalDate.now();
        desde.setValue(hoy.withDayOfMonth(1));
        hasta.setValue(hoy);
        desde.setPrefWidth(126);
        hasta.setPrefWidth(126);
        desde.setMinWidth(126);
        hasta.setMinWidth(126);

        Button btnMes = Ui.boton("Este mes", null, "button-secundario");
        btnMes.setTooltip(new Tooltip("Ver el mes en curso"));
        btnMes.setOnAction(evento -> {
            desde.setValue(hoy.withDayOfMonth(1));
            hasta.setValue(hoy);
            actualizar();
        });

        Button btnTreinta = Ui.boton("30 días", null, "button-secundario");
        btnTreinta.setTooltip(new Tooltip("Ver los últimos 30 días"));
        btnTreinta.setOnAction(evento -> {
            desde.setValue(hoy.minusDays(29));
            hasta.setValue(hoy);
            actualizar();
        });

        Button btnTodo = Ui.boton("Todo", null, "button-fantasma");
        btnTodo.setTooltip(new Tooltip("Ver todo el historial"));
        btnTodo.setOnAction(evento -> {
            desde.setValue(null);
            hasta.setValue(null);
            actualizar();
        });

        Button btnActualizar = Ui.botonIcono(Icono.ACTUALIZAR,
                "Actualizar historial", "button-icono");
        btnActualizar.setOnAction(evento -> {
            actualizar();
            ventana.mostrarMensaje("Historial actualizado correctamente.");
        });

        Button btnLimpiar = Ui.boton("Limpiar historial", Icono.ELIMINAR,
                "button-peligro-suave");
        btnLimpiar.setTooltip(new Tooltip(
                "Eliminar un registro o todo el historial de la pestaña activa"));
        btnLimpiar.setOnAction(evento -> abrirDialogoLimpiar());

        Label etiquetaPeriodo = Ui.etiqueta("Periodo", "etiqueta-campo");
        Label etiquetaA = Ui.etiqueta("a", "tarjeta-subtitulo");
        for (Label etiqueta : List.of(etiquetaPeriodo, etiquetaA)) {
            etiqueta.setMinWidth(Region.USE_PREF_SIZE);
        }
        for (Button boton : List.of(btnMes, btnTreinta, btnTodo, btnLimpiar)) {
            boton.setMinWidth(Region.USE_PREF_SIZE);
        }

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(8,
                etiquetaPeriodo,
                desde,
                etiquetaA,
                hasta,
                btnMes, btnTreinta, btnTodo,
                espacio, btnActualizar, btnLimpiar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-filtros");
        return barra;
    }

    private VBox crearContenidoVentas() {
        TableColumn<Venta, String> colId = Ui.columna("Venta", 76,
                venta -> "#" + venta.getId());
        TableColumn<Venta, String> colFecha = Ui.columna("Fecha", 100,
                venta -> Formato.fecha(venta.getFecha()));
        TableColumn<Venta, String> colLineas = Ui.columnaDerecha("Líneas", 70,
                venta -> String.valueOf(venta.getDetalles().size()));
        TableColumn<Venta, String> colIngresos = Ui.columnaDerecha("Ingresos", 118,
                venta -> Formato.dinero(venta.calcularTotal()));
        TableColumn<Venta, String> colCosto = Ui.columnaDerecha("Costo", 118,
                venta -> Formato.dinero(venta.calcularCostoTotal()));
        TableColumn<Venta, String> colUtilidad = Ui.columnaDerecha("Utilidad", 118,
                venta -> Formato.dinero(
                        venta.calcularTotal() - venta.calcularCostoTotal()));

        tablaVentas.getColumns().setAll(List.of(colId, colFecha, colLineas,
                colIngresos, colCosto, colUtilidad));
        tablaVentas.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, actual) -> mostrarDetalleVenta(actual));

        TableColumn<DetalleVenta, String> colProducto = Ui.columna(
                "Producto", 220, DetalleVenta::getNombreProducto);
        TableColumn<DetalleVenta, String> colCantidad = Ui.columnaDerecha(
                "Cantidad", 80, detalle -> String.valueOf(detalle.getCantidad()));
        TableColumn<DetalleVenta, String> colPrecio = Ui.columnaDerecha(
                "Precio", 110,
                detalle -> Formato.dinero(detalle.getPrecioUnitario()));
        TableColumn<DetalleVenta, String> colCostoUnitario = Ui.columnaDerecha(
                "Costo", 110,
                detalle -> Formato.dinero(detalle.getCostoUnitario()));
        TableColumn<DetalleVenta, String> colSubtotal = Ui.columnaDerecha(
                "Subtotal", 118,
                detalle -> Formato.dinero(detalle.calcularSubtotal()));

        tablaDetalleVenta.getColumns().setAll(List.of(colProducto, colCantidad,
                colPrecio, colCostoUnitario, colSubtotal));
        tablaDetalleVenta.setMinHeight(130);
        VBox.setVgrow(tablaVentas, Priority.ALWAYS);

        VBox detalle = new VBox(8,
                Ui.etiqueta("Detalle de la venta seleccionada",
                        "tarjeta-subtitulo"),
                tablaDetalleVenta);
        VBox.setVgrow(detalle, Priority.ALWAYS);

        VBox contenido = new VBox(14, tablaVentas, detalle);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return contenido;
    }

    private VBox crearContenidoProductos() {
        TableColumn<RegistroProducto, String> colFecha = Ui.columna(
                "Fecha y hora", 132,
                registro -> Formato.fechaHora(registro.getFechaHora()));
        TableColumn<RegistroProducto, String> colEvento = Ui.columna(
                "Evento", 105,
                registro -> textoEvento(registro.getEvento()));
        colEvento.setCellFactory(lista -> new TableCell<RegistroProducto, String>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String variante = switch (item) {
                    case "Registro" -> "badge-exito";
                    case "Edición" -> "badge-aviso";
                    default -> "badge-peligro";
                };
                setText(null);
                setGraphic(Ui.badge(item, variante));
            }
        });
        TableColumn<RegistroProducto, String> colId = Ui.columna("ID", 52,
                registro -> String.valueOf(registro.getIdProducto()));
        TableColumn<RegistroProducto, String> colNombre = Ui.columna(
                "Producto", 150, RegistroProducto::getNombre);
        TableColumn<RegistroProducto, String> colCosto = Ui.columnaDerecha(
                "Costo", 92,
                registro -> Formato.dinero(registro.getCosto()));
        TableColumn<RegistroProducto, String> colPrecio = Ui.columnaDerecha(
                "Precio", 92,
                registro -> Formato.dinero(registro.getPrecioVenta()));
        TableColumn<RegistroProducto, String> colStock = Ui.columnaDerecha(
                "Stock", 62,
                registro -> String.valueOf(registro.getStockActual()));
        TableColumn<RegistroProducto, String> colDetalle = Ui.columna(
                "Detalle del cambio", 300, RegistroProducto::getDetalle);

        tablaProductos.getColumns().setAll(List.of(colFecha, colEvento, colId,
                colNombre, colCosto, colPrecio, colStock, colDetalle));
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);

        VBox contenido = new VBox(tablaProductos);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return contenido;
    }

    private VBox crearContenidoInventario() {
        TableColumn<MovimientoInventario, String> colFecha = Ui.columna(
                "Fecha y hora", 132,
                movimiento -> Formato.fechaHora(movimiento.getFechaHora()));
        TableColumn<MovimientoInventario, String> colTipo = Ui.columna(
                "Tipo", 90,
                movimiento -> textoMovimiento(movimiento.getTipo()));
        colTipo.setCellFactory(lista
                -> new TableCell<MovimientoInventario, String>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String variante = switch (item) {
                    case "Entrada" -> "badge-exito";
                    case "Salida" -> "badge-peligro";
                    default -> "badge-aviso";
                };
                setText(null);
                setGraphic(Ui.badge(item, variante));
            }
        });
        TableColumn<MovimientoInventario, String> colProducto = Ui.columna(
                "Producto", 180, MovimientoInventario::getNombreProducto);
        TableColumn<MovimientoInventario, String> colCantidad = Ui.columnaDerecha(
                "Cantidad", 76,
                movimiento -> String.valueOf(movimiento.getCantidad()));
        TableColumn<MovimientoInventario, String> colAnterior = Ui.columnaDerecha(
                "Stock anterior", 100,
                movimiento -> String.valueOf(movimiento.getStockAnterior()));
        TableColumn<MovimientoInventario, String> colNuevo = Ui.columnaDerecha(
                "Stock nuevo", 92,
                movimiento -> String.valueOf(movimiento.getStockNuevo()));
        TableColumn<MovimientoInventario, String> colMotivo = Ui.columna(
                "Motivo", 280, MovimientoInventario::getMotivo);

        tablaMovimientos.getColumns().setAll(List.of(colFecha, colTipo,
                colProducto, colCantidad, colAnterior, colNuevo, colMotivo));
        VBox.setVgrow(tablaMovimientos, Priority.ALWAYS);

        VBox contenido = new VBox(tablaMovimientos);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return contenido;
    }

    private VBox crearContenidoCostos() {
        TableColumn<Costo, String> colFecha = Ui.columna("Fecha", 100,
                costo -> Formato.fecha(costo.getFecha()));
        TableColumn<Costo, String> colTipo = Ui.columna("Tipo", 90,
                costo -> costo.getTipo() == Costo.Tipo.FIJO ? "Fijo" : "Variable");
        TableColumn<Costo, String> colDescripcion = Ui.columna(
                "Descripción", 300, Costo::getDescripcion);
        TableColumn<Costo, String> colMonto = Ui.columnaDerecha("Monto", 120,
                costo -> Formato.dinero(costo.getMonto()));

        tablaCostos.getColumns().setAll(List.of(colFecha, colTipo,
                colDescripcion, colMonto));
        VBox.setVgrow(tablaCostos, Priority.ALWAYS);

        tablaCostos.setRowFactory(vista -> {
            TableRow<Costo> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    tablaCostos.getSelectionModel().select(fila.getItem());
                    editarCostoSeleccionado();
                }
            });
            return fila;
        });

        Button btnEditar = Ui.boton("Editar costo", Icono.EDITAR,
                "button-secundario");
        btnEditar.disableProperty().bind(
                tablaCostos.getSelectionModel().selectedItemProperty().isNull());
        btnEditar.setOnAction(evento -> editarCostoSeleccionado());

        Button btnEliminar = Ui.boton("Eliminar costo", Icono.ELIMINAR,
                "button-peligro-suave");
        btnEliminar.disableProperty().bind(
                tablaCostos.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.setOnAction(evento -> eliminarCostoSeleccionado());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox acciones = new HBox(8,
                Ui.etiqueta("Selecciona un costo para editarlo o eliminarlo; "
                        + "también puedes hacer doble clic sobre la fila.",
                        "tarjeta-subtitulo"),
                espacio, btnEditar, btnEliminar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        VBox contenido = new VBox(12, acciones, tablaCostos);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return contenido;
    }

    private void editarCostoSeleccionado() {
        Costo seleccionado = tablaCostos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ventana.mostrarError("Selecciona un costo de la tabla para editarlo.");
            return;
        }

        var resultado = DialogoCosto.mostrar(ventana.getEscenario(), seleccionado);
        resultado.ifPresent(editado -> {
            try {
                if (!controladorFinanzas.editarCosto(editado)) {
                    throw new IllegalStateException("El costo ya no existe.");
                }
                ventana.mostrarMensaje("Costo #" + editado.getId()
                        + " actualizado correctamente.");
                ventana.actualizarVista();
            } catch (RuntimeException ex) {
                ventana.mostrarError(ex.getMessage());
            }
        });
    }

    private void eliminarCostoSeleccionado() {
        Costo seleccionado = tablaCostos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ventana.mostrarError("Selecciona un costo de la tabla para eliminarlo.");
            return;
        }

        boolean confirmado = ventana.confirmarPeligro(
                "¿Eliminar el costo #" + seleccionado.getId() + " \""
                + seleccionado.getDescripcion() + "\" por "
                + Formato.dinero(seleccionado.getMonto()) + "?\n\n"
                + "Los indicadores financieros se recalcularán sin este costo.");
        if (!confirmado) {
            return;
        }

        try {
            if (!controladorFinanzas.eliminarCosto(seleccionado.getId())) {
                throw new IllegalStateException("El costo ya no existe.");
            }
            ventana.mostrarMensaje("Costo #" + seleccionado.getId()
                    + " eliminado correctamente.");
            ventana.actualizarVista();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void abrirDialogoLimpiar() {
        int indice = pestanas.getSelectionModel().getSelectedIndex();
        if (indice < 0) {
            return;
        }
        if (indice == TAB_FINANCIERO) {
            ventana.mostrarError("El historial financiero es un resumen calculado "
                    + "a partir de las ventas y los costos. Para limpiarlo, elimina "
                    + "las ventas en su pestaña o los costos en la pestaña Costos.");
            return;
        }

        Object seleccionado = registroSeleccionado(indice);
        int total = totalRegistros(indice);
        String nombre = nombreHistorial(indice);
        String descripcion = descripcionSeleccion(indice, seleccionado);
        String nota = notaHistorial(indice);

        var opcion = DialogoLimpiarHistorial.mostrar(ventana.getEscenario(), nombre,
                descripcion, total, seleccionado != null, nota);
        if (opcion.isEmpty()) {
            return;
        }

        DialogoLimpiarHistorial.Modo modo = opcion.get();
        String confirmacion = modo == DialogoLimpiarHistorial.Modo.SELECCIONADO
                ? "¿Eliminar este registro del historial de " + nombre + "?\n\n"
                + descripcion + "\n\nEsta acción no se puede deshacer."
                : "¿Eliminar TODOS los registros del historial de " + nombre
                + " (" + total + ")?\n\nEsta acción es irreversible y no se "
                + "puede deshacer.\n\n" + nota;
        if (!ventana.confirmarPeligro(confirmacion)) {
            return;
        }

        try {
            int eliminados = modo == DialogoLimpiarHistorial.Modo.SELECCIONADO
                    ? eliminarSeleccionado(indice, seleccionado)
                    : eliminarTodo(indice);
            if (eliminados <= 0) {
                throw new IllegalStateException("No se pudo eliminar ningún registro.");
            }

            ventana.guardarDatos();
            ventana.actualizarVista();
            ventana.mostrarMensaje(eliminados == 1
                    ? "Se eliminó 1 registro del historial de " + nombre + "."
                    : "Se eliminaron " + eliminados + " registros del historial de "
                    + nombre + ".");
        } catch (RuntimeException ex) {
            ventana.actualizarVista();
            ventana.mostrarError("No se pudo limpiar el historial: "
                    + ex.getMessage());
        }
    }

    private Object registroSeleccionado(int indice) {
        return switch (indice) {
            case TAB_VENTAS -> tablaVentas.getSelectionModel().getSelectedItem();
            case TAB_PRODUCTOS -> tablaProductos.getSelectionModel().getSelectedItem();
            case TAB_INVENTARIO -> tablaMovimientos.getSelectionModel().getSelectedItem();
            case TAB_COSTOS -> tablaCostos.getSelectionModel().getSelectedItem();
            default -> null;
        };
    }

    private int totalRegistros(int indice) {
        return switch (indice) {
            case TAB_VENTAS -> controlador.listarVentas(null, null).size();
            case TAB_PRODUCTOS -> controlador.listarRegistrosProducto(null, null).size();
            case TAB_INVENTARIO -> controlador.listarMovimientos(null, null).size();
            case TAB_COSTOS -> controladorFinanzas.listarCostos().size();
            default -> 0;
        };
    }

    private int eliminarSeleccionado(int indice, Object registro) {
        if (registro == null) {
            throw new IllegalArgumentException(
                    "Selecciona un registro de la tabla para eliminarlo.");
        }

        boolean eliminado = switch (indice) {
            case TAB_VENTAS -> controladorVentas.eliminarVenta(
                    ((Venta) registro).getId());
            case TAB_PRODUCTOS -> controlador.eliminarRegistroProducto(
                    ((RegistroProducto) registro).getId());
            case TAB_INVENTARIO -> controlador.eliminarMovimiento(
                    ((MovimientoInventario) registro).getId());
            case TAB_COSTOS -> controladorFinanzas.eliminarCosto(
                    ((Costo) registro).getId());
            default -> false;
        };
        return eliminado ? 1 : 0;
    }

    private int eliminarTodo(int indice) {
        switch (indice) {
            case TAB_VENTAS:
                ArrayList<Venta> ventas = controlador.listarVentas(null, null);
                int eliminadas = 0;
                for (Venta venta : ventas) {
                    if (controladorVentas.eliminarVenta(venta.getId())) {
                        eliminadas++;
                    }
                }
                return eliminadas;
            case TAB_PRODUCTOS:
                int registros = controlador.listarRegistrosProducto(null, null).size();
                controlador.limpiarHistorialProductos();
                return registros;
            case TAB_INVENTARIO:
                int movimientos = controlador.listarMovimientos(null, null).size();
                controlador.limpiarMovimientos();
                return movimientos;
            case TAB_COSTOS:
                int costos = controladorFinanzas.listarCostos().size();
                controladorFinanzas.limpiarCostos();
                return costos;
            default:
                return 0;
        }
    }

    private static String nombreHistorial(int indice) {
        return switch (indice) {
            case TAB_VENTAS -> "ventas";
            case TAB_PRODUCTOS -> "productos";
            case TAB_INVENTARIO -> "inventario";
            case TAB_COSTOS -> "costos";
            default -> "financiero";
        };
    }

    private static String notaHistorial(int indice) {
        return switch (indice) {
            case TAB_VENTAS -> "Al eliminar ventas se repone el stock de los "
                    + "productos y se registran los movimientos de inventario "
                    + "correspondientes.";
            case TAB_PRODUCTOS -> "Los registros de cambios del catálogo se "
                    + "eliminarán permanentemente.";
            case TAB_INVENTARIO -> "Los movimientos de inventario se eliminarán "
                    + "permanentemente; el stock actual de los productos no cambiará.";
            case TAB_COSTOS -> "Los indicadores financieros, el margen y el punto "
                    + "de equilibrio se recalcularán sin los costos eliminados.";
            default -> "";
        };
    }

    private static String descripcionSeleccion(int indice, Object registro) {
        if (registro == null) {
            return null;
        }
        return switch (indice) {
            case TAB_VENTAS -> {
                Venta venta = (Venta) registro;
                yield "Venta #" + venta.getId() + " · "
                        + Formato.fecha(venta.getFecha()) + " · "
                        + Formato.dinero(venta.calcularTotal());
            }
            case TAB_PRODUCTOS -> {
                RegistroProducto producto = (RegistroProducto) registro;
                yield "Registro #" + producto.getId() + " · "
                        + Formato.fechaHora(producto.getFechaHora()) + " · "
                        + textoEvento(producto.getEvento()) + " · "
                        + producto.getNombre();
            }
            case TAB_INVENTARIO -> {
                MovimientoInventario movimiento = (MovimientoInventario) registro;
                yield "Movimiento #" + movimiento.getId() + " · "
                        + Formato.fechaHora(movimiento.getFechaHora()) + " · "
                        + textoMovimiento(movimiento.getTipo()) + " · "
                        + movimiento.getNombreProducto() + " · "
                        + movimiento.getCantidad() + " unidades";
            }
            case TAB_COSTOS -> {
                Costo costo = (Costo) registro;
                yield "Costo #" + costo.getId() + " · "
                        + costo.getDescripcion() + " · "
                        + Formato.dinero(costo.getMonto());
            }
            default -> null;
        };
    }

    private VBox crearContenidoFinanciero() {
        TableColumn<ResumenMensual, String> colMes = Ui.columna(
                "Mes", 130, resumen -> Formato.mes(resumen.mes()));
        TableColumn<ResumenMensual, String> colIngresos = Ui.columnaDerecha(
                "Ingresos", 112,
                resumen -> Formato.dinero(resumen.ingresos()));
        TableColumn<ResumenMensual, String> colCostoVentas = Ui.columnaDerecha(
                "Costo ventas", 108,
                resumen -> Formato.dinero(resumen.costoVentas()));
        TableColumn<ResumenMensual, String> colFijos = Ui.columnaDerecha(
                "Costos fijos", 100,
                resumen -> Formato.dinero(resumen.costosFijos()));
        TableColumn<ResumenMensual, String> colVariables = Ui.columnaDerecha(
                "Variables", 92,
                resumen -> Formato.dinero(resumen.costosVariables()));
        TableColumn<ResumenMensual, String> colUtilidad = Ui.columnaDerecha(
                "Utilidad neta", 112,
                resumen -> Formato.dinero(resumen.utilidadNeta()));
        TableColumn<ResumenMensual, String> colRentabilidad = Ui.columnaDerecha(
                "Rentab. %", 92,
                resumen -> Formato.porcentaje(resumen.rentabilidad()));

        tablaFinanciero.getColumns().setAll(List.of(colMes, colIngresos,
                colCostoVentas, colFijos, colVariables, colUtilidad,
                colRentabilidad));
        tablaFinanciero.setMinHeight(190);
        VBox.setVgrow(tablaFinanciero, Priority.ALWAYS);

        CategoryAxis ejeX = new CategoryAxis();
        NumberAxis ejeY = new NumberAxis();
        ejeY.setForceZeroInRange(true);
        ejeY.setMinorTickVisible(false);
        ejeY.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number valor) {
                return valor == null ? "" : abreviarDinero(valor.doubleValue());
            }

            @Override
            public Number fromString(String texto) {
                return 0;
            }
        });
        grafico = new BarChart<>(ejeX, ejeY);
        grafico.setLegendVisible(true);
        grafico.setAnimated(false);
        grafico.setVerticalGridLinesVisible(false);
        grafico.getStyleClass().add("grafico");
        grafico.setMinHeight(280);
        VBox.setVgrow(grafico, Priority.ALWAYS);

        lblSinDatosFinancieros.getStyleClass().add("tarjeta-subtitulo");

        Label nota = Ui.etiqueta(
                "Evolución de todos los meses con ventas o costos registrados.",
                "tarjeta-subtitulo");

        VBox contenido = new VBox(12, nota, tablaFinanciero, grafico,
                lblSinDatosFinancieros);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return contenido;
    }

    public void actualizar() {
        LocalDate[] periodo = periodo();
        ArrayList<Venta> ventas = controlador.listarVentas(periodo[0], periodo[1]);
        tablaVentas.setItems(FXCollections.observableArrayList(ventas));
        tablaDetalleVenta.setItems(FXCollections.observableArrayList());

        tablaProductos.setItems(FXCollections.observableArrayList(
                controlador.listarRegistrosProducto(periodo[0], periodo[1])));
        tablaMovimientos.setItems(FXCollections.observableArrayList(
                controlador.listarMovimientos(periodo[0], periodo[1])));
        tablaCostos.setItems(FXCollections.observableArrayList(
                controlador.listarCostos(periodo[0], periodo[1])));

        actualizarFinanciero();
    }

    private void actualizarFinanciero() {
        ArrayList<ResumenMensual> historial = controladorFinanzas.historialFinanciero();
        tablaFinanciero.setItems(FXCollections.observableArrayList(historial));

        XYChart.Series<String, Number> ingresos = new XYChart.Series<>();
        ingresos.setName("Ingresos");
        XYChart.Series<String, Number> costos = new XYChart.Series<>();
        costos.setName("Costos totales");
        XYChart.Series<String, Number> utilidad = new XYChart.Series<>();
        utilidad.setName("Utilidad neta");

        for (ResumenMensual resumen : historial) {
            String mes = Formato.mes(resumen.mes());
            ingresos.getData().add(new XYChart.Data<>(mes, resumen.ingresos()));
            costos.getData().add(new XYChart.Data<>(mes,
                    resumen.costoVentas() + resumen.costosFijos()
                    + resumen.costosVariables()));
            utilidad.getData().add(new XYChart.Data<>(mes, resumen.utilidadNeta()));
        }

        grafico.getData().setAll(List.of(ingresos, costos, utilidad));
        boolean vacio = historial.isEmpty();
        lblSinDatosFinancieros.setVisible(vacio);
        lblSinDatosFinancieros.setManaged(vacio);
        grafico.setVisible(!vacio);
        grafico.setManaged(!vacio);
    }

    private void mostrarDetalleVenta(Venta venta) {
        if (venta == null) {
            tablaDetalleVenta.setItems(FXCollections.observableArrayList());
            return;
        }
        tablaDetalleVenta.setItems(FXCollections.observableArrayList(
                venta.getDetalles()));
    }

    private LocalDate[] periodo() {
        LocalDate inicio = desde.getValue();
        LocalDate fin = hasta.getValue();
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            return new LocalDate[]{fin, inicio};
        }
        return new LocalDate[]{inicio, fin};
    }

    private static String textoEvento(RegistroProducto.Evento evento) {
        return switch (evento) {
            case REGISTRO -> "Registro";
            case EDICION -> "Edición";
            case ELIMINACION -> "Eliminación";
        };
    }

    private static String textoMovimiento(MovimientoInventario.Tipo tipo) {
        return switch (tipo) {
            case ENTRADA -> "Entrada";
            case SALIDA -> "Salida";
            case AJUSTE -> "Ajuste";
        };
    }

    private static String abreviarDinero(double valor) {
        if (Math.abs(valor) >= 1_000_000) {
            return compacto(valor / 1_000_000, "M");
        }
        if (Math.abs(valor) >= 1_000) {
            return compacto(valor / 1_000, "K");
        }
        return Formato.dinero(valor);
    }

    private static String compacto(double valor, String sufijo) {
        String texto = String.format(java.util.Locale.forLanguageTag("es-CO"),
                "%.1f", valor);
        if (texto.endsWith(",0")) {
            texto = texto.substring(0, texto.length() - 2);
        }
        return "$" + texto + " " + sufijo;
    }
}
