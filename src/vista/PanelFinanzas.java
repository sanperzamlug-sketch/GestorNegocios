package vista;

import controlador.ControladorFinanzas;
import controlador.ControladorFinanzas.IndicadorProducto;
import controlador.ControladorFinanzas.ResumenFinanciero;
import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Costo;
import util.Formato;
import util.Validador;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Indicadores financieros, registro de costos y rentabilidad por producto.
 */
public class PanelFinanzas extends ScrollPane {

    private final ControladorFinanzas controlador;
    private final VentanaPrincipal ventana;

    private final DatePicker desde = new DatePicker();
    private final DatePicker hasta = new DatePicker();

    private final Ui.Kpi kpiFijos = Ui.kpi("Costos fijos", Icono.CAJA, "acento-ambar");
    private final Ui.Kpi kpiVariables = Ui.kpi("Costos variables", Icono.TENDENCIA,
            "acento-rosa");
    private final Ui.Kpi kpiOperativos = Ui.kpi("Costos operativos", Icono.DINERO,
            "acento-violeta");
    private final Ui.Kpi kpiUtilidad = Ui.kpi("Utilidad neta", Icono.GRAFICO,
            "acento-verde");
    private final Ui.Kpi kpiRentabilidad = Ui.kpi("Rentabilidad general", Icono.TENDENCIA,
            "acento-azul");
    private final Ui.Kpi kpiPunto = Ui.kpi("Punto de equilibrio", Icono.RECIBO,
            "acento-indigo");

    private final ComboBox<Costo.Tipo> tipoCosto = new ComboBox<>();
    private final DatePicker fechaCosto = new DatePicker(LocalDate.now());
    private final TextField txtDescripcion = Ui.campo("Ej. Arriendo del local");
    private final TextField txtMonto = Ui.campo("0,00");
    private final Button btnRegistrar = Ui.boton("Registrar costo", Icono.GUARDAR,
            "button-primario");
    private final Button btnCancelarEdicion = Ui.boton("Cancelar edición", null,
            "button-fantasma");
    private final Label lblModoEdicion = new Label();

    private Costo costoEnEdicion;

    private final TableView<Costo> tablaCostos = Ui.tabla(
            "Registra un costo fijo o variable para comenzar.");
    private final TableView<IndicadorProducto> tablaProductos = Ui.tabla(
            "No hay productos registrados para analizar.");

    private final Label lblPuntoTitulo = new Label("—");
    private final Label lblPuntoDetalle = new Label();

    public PanelFinanzas(ControladorFinanzas controlador, VentanaPrincipal ventana) {
        this.controlador = controlador;
        this.ventana = ventana;

        getStyleClass().add("scroll-pane");
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        construirInterfaz();
        actualizar();
    }

    private void construirInterfaz() {
        VBox contenido = new VBox(18);
        contenido.getStyleClass().add("page");

        contenido.getChildren().addAll(crearBarraPeriodo(), crearIndicadores(),
                crearSeccionCostos(), crearTarjetaProductos(),
                crearTarjetaPuntoEquilibrio());

        setContent(contenido);
    }

    private HBox crearBarraPeriodo() {
        Ui.configurarFecha(desde);
        Ui.configurarFecha(hasta);

        LocalDate hoy = LocalDate.now();
        desde.setValue(hoy.withDayOfMonth(1));
        hasta.setValue(hoy);
        desde.setPrefWidth(138);
        hasta.setPrefWidth(138);

        Button btnMes = Ui.boton("Este mes", null, "button-secundario");
        btnMes.setOnAction(evento -> {
            desde.setValue(hoy.withDayOfMonth(1));
            hasta.setValue(hoy);
            actualizar();
        });

        Button btnTreinta = Ui.boton("Últimos 30 días", null, "button-secundario");
        btnTreinta.setOnAction(evento -> {
            desde.setValue(hoy.minusDays(29));
            hasta.setValue(hoy);
            actualizar();
        });

        Button btnTodo = Ui.boton("Todo", null, "button-fantasma");
        btnTodo.setOnAction(evento -> {
            desde.setValue(null);
            hasta.setValue(null);
            actualizar();
        });

        Button btnActualizar = Ui.botonIcono(Icono.ACTUALIZAR,
                "Actualizar indicadores", "button-icono");
        btnActualizar.setOnAction(evento -> {
            actualizar();
            ventana.mostrarMensaje("Indicadores financieros actualizados.");
        });

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(9,
                Ui.etiqueta("Periodo", "etiqueta-campo"),
                desde,
                Ui.etiqueta("a", "tarjeta-subtitulo"),
                hasta,
                btnMes, btnTreinta, btnTodo,
                espacio, btnActualizar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-filtros");
        return barra;
    }

    private VBox crearIndicadores() {
        HBox primeraFila = new HBox(16,
                kpiFijos.raiz, kpiVariables.raiz, kpiOperativos.raiz);
        primeraFila.setAlignment(Pos.TOP_LEFT);

        HBox segundaFila = new HBox(16,
                kpiUtilidad.raiz, kpiRentabilidad.raiz, kpiPunto.raiz);
        segundaFila.setAlignment(Pos.TOP_LEFT);

        return new VBox(16, primeraFila, segundaFila);
    }

    private HBox crearSeccionCostos() {
        VBox tarjetaFormulario = crearFormularioCosto();
        VBox tarjetaCostos = crearTarjetaCostos();

        HBox fila = new HBox(18, tarjetaFormulario, tarjetaCostos);
        fila.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(tarjetaCostos, Priority.ALWAYS);
        return fila;
    }

    private VBox crearFormularioCosto() {
        tipoCosto.getItems().setAll(Costo.Tipo.values());
        tipoCosto.setConverter(new StringConverter<Costo.Tipo>() {
            @Override
            public String toString(Costo.Tipo tipo) {
                if (tipo == null) {
                    return "";
                }
                return tipo == Costo.Tipo.FIJO ? "Fijo" : "Variable";
            }

            @Override
            public Costo.Tipo fromString(String texto) {
                return null;
            }
        });
        tipoCosto.getSelectionModel().selectFirst();
        tipoCosto.setMaxWidth(Double.MAX_VALUE);

        Ui.configurarFecha(fechaCosto);
        fechaCosto.setValue(LocalDate.now());
        fechaCosto.setMaxWidth(Double.MAX_VALUE);

        limitarDecimales(txtMonto);
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setOnAction(evento -> guardarCosto());

        btnCancelarEdicion.setMaxWidth(Double.MAX_VALUE);
        btnCancelarEdicion.setOnAction(evento -> limpiarCosto());
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.setManaged(false);

        lblModoEdicion.getStyleClass().add("tarjeta-subtitulo");
        lblModoEdicion.setWrapText(true);
        lblModoEdicion.setVisible(false);
        lblModoEdicion.setManaged(false);

        HBox filaTipoFecha = new HBox(12,
                Ui.campoEtiquetado("Tipo", tipoCosto),
                Ui.campoEtiquetado("Fecha", fechaCosto));
        HBox.setHgrow(filaTipoFecha.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(filaTipoFecha.getChildren().get(1), Priority.ALWAYS);

        VBox formulario = new VBox(12,
                filaTipoFecha,
                Ui.campoEtiquetado("Descripción", txtDescripcion),
                Ui.campoEtiquetado("Monto", txtMonto),
                lblModoEdicion,
                btnRegistrar,
                btnCancelarEdicion);

        VBox tarjeta = Ui.tarjeta("Registro de costos",
                "Registra un costo nuevo o selecciona uno de la tabla para editarlo",
                formulario);
        tarjeta.setPrefWidth(330);
        tarjeta.setMinWidth(310);
        tarjeta.setMaxWidth(370);
        return tarjeta;
    }

    private VBox crearTarjetaCostos() {
        TableColumn<Costo, String> colFecha = Ui.columna("Fecha", 94,
                costo -> Formato.fecha(costo.getFecha()));
        TableColumn<Costo, String> colTipo = Ui.columna("Tipo", 82,
                costo -> costo.getTipo() == Costo.Tipo.FIJO ? "Fijo" : "Variable");
        TableColumn<Costo, String> colDescripcion = Ui.columna("Descripción", 200,
                Costo::getDescripcion);
        TableColumn<Costo, String> colMonto = Ui.columnaDerecha("Monto", 110,
                costo -> Formato.dinero(costo.getMonto()));

        tablaCostos.getColumns().setAll(
                List.of(colFecha, colTipo, colDescripcion, colMonto));
        tablaCostos.setMinHeight(190);
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

        Button btnEditar = Ui.boton("Editar", Icono.EDITAR, "button-secundario");
        btnEditar.disableProperty().bind(
                tablaCostos.getSelectionModel().selectedItemProperty().isNull());
        btnEditar.setOnAction(evento -> editarCostoSeleccionado());

        Button btnEliminar = Ui.boton("Eliminar", Icono.ELIMINAR,
                "button-peligro-suave");
        btnEliminar.disableProperty().bind(
                tablaCostos.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.setOnAction(evento -> eliminarCostoSeleccionado());

        VBox tarjeta = Ui.tarjeta("Costos del periodo",
                "Selecciona un costo para editarlo o eliminarlo",
                tablaCostos, btnEditar, btnEliminar);
        tarjeta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return tarjeta;
    }

    private VBox crearTarjetaProductos() {
        TableColumn<IndicadorProducto, String> colProducto = Ui.columna(
                "Producto", 150, IndicadorProducto::nombre);
        TableColumn<IndicadorProducto, String> colPrecio = Ui.columnaDerecha(
                "Precio", 82,
                indicador -> Formato.dinero(indicador.precioUnitario()));
        TableColumn<IndicadorProducto, String> colCosto = Ui.columnaDerecha(
                "Costo", 82,
                indicador -> Formato.dinero(indicador.costoUnitario()));
        TableColumn<IndicadorProducto, String> colMargenValor = Ui.columnaDerecha(
                "Margen $", 82,
                indicador -> Formato.dinero(indicador.margenUnitario()));
        TableColumn<IndicadorProducto, String> colMargen = Ui.columnaDerecha(
                "Margen %", 78,
                indicador -> Formato.porcentaje(indicador.margenPorcentaje()));
        TableColumn<IndicadorProducto, String> colRentabilidad = Ui.columnaDerecha(
                "Rentab. %", 82,
                indicador -> Formato.porcentaje(indicador.rentabilidadPorcentaje()));
        TableColumn<IndicadorProducto, String> colUnidades = Ui.columnaDerecha(
                "Unidades", 72,
                indicador -> String.valueOf(indicador.unidadesVendidas()));
        TableColumn<IndicadorProducto, String> colIngresos = Ui.columnaDerecha(
                "Ingresos", 104,
                indicador -> Formato.dinero(indicador.ingresos()));
        TableColumn<IndicadorProducto, String> colUtilidad = Ui.columnaDerecha(
                "Utilidad", 104,
                indicador -> Formato.dinero(indicador.utilidadBruta()));

        tablaProductos.getColumns().setAll(List.of(colProducto, colPrecio,
                colCosto, colMargenValor, colMargen, colRentabilidad, colUnidades,
                colIngresos, colUtilidad));
        tablaProductos.setMinHeight(230);
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);

        VBox tarjeta = Ui.tarjeta("Margen y rentabilidad por producto",
                "Margen sobre el precio de venta y rentabilidad sobre el costo; "
                + "las ventas corresponden al periodo seleccionado",
                tablaProductos);
        tarjeta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return tarjeta;
    }

    private VBox crearTarjetaPuntoEquilibrio() {
        lblPuntoTitulo.getStyleClass().add("total-actual");
        lblPuntoDetalle.getStyleClass().add("tarjeta-subtitulo");
        lblPuntoDetalle.setWrapText(true);

        VBox textos = new VBox(4, lblPuntoTitulo, lblPuntoDetalle);
        VBox cuerpo = new VBox(10, textos,
                Ui.etiqueta("El punto de equilibrio se calcula como costos fijos "
                        + "divididos entre el margen de contribución del periodo.",
                        "tarjeta-subtitulo"));
        return Ui.tarjeta("Punto de equilibrio", "Ventas necesarias para cubrir "
                + "todos los costos", cuerpo);
    }

    public void actualizar() {
        if (costoEnEdicion != null && controlador.listarCostos().stream()
                .noneMatch(costo -> costo.getId() == costoEnEdicion.getId())) {
            limpiarCosto();
        }

        LocalDate[] periodo = periodo();
        ResumenFinanciero resumen = controlador.resumen(periodo[0], periodo[1]);

        kpiFijos.valor.setText(Formato.dinero(resumen.costosFijos()));
        kpiFijos.pista.setText("Gastos permanentes del periodo");

        kpiVariables.valor.setText(Formato.dinero(resumen.costosVariables()));
        kpiVariables.pista.setText("Gastos asociados a la operación");

        kpiOperativos.valor.setText(Formato.dinero(resumen.costosOperativos()));
        kpiOperativos.pista.setText("Fijos + variables registrados");

        kpiUtilidad.valor.setText(Formato.dinero(resumen.utilidadNeta()));
        kpiUtilidad.pista.setText("Ingresos menos todos los costos");
        aplicarNegativo(kpiUtilidad, resumen.utilidadNeta() < 0);

        kpiRentabilidad.valor.setText(
                Formato.porcentaje(resumen.rentabilidadGeneral()));
        kpiRentabilidad.pista.setText("Utilidad neta sobre los ingresos");
        aplicarNegativo(kpiRentabilidad, resumen.rentabilidadGeneral() < 0);

        if (resumen.puntoEquilibrioCalculable()) {
            kpiPunto.valor.setText(Formato.dinero(resumen.puntoEquilibrio()));
            kpiPunto.pista.setText("Ventas necesarias para no perder dinero");
            aplicarNegativo(kpiPunto, false);
            lblPuntoTitulo.setText(Formato.dinero(resumen.puntoEquilibrio()));
            lblPuntoDetalle.setText(
                    "Margen de contribución del periodo: "
                    + Formato.porcentaje(resumen.margenContribucion() * 100.0)
                    + ". Con ventas por encima de este valor el negocio comienza "
                    + "a generar utilidad.");
        } else {
            kpiPunto.valor.setText("—");
            kpiPunto.pista.setText("No calculable con los datos actuales");
            lblPuntoTitulo.setText("No calculable");
            lblPuntoDetalle.setText("Registra ventas y costos variables para "
                    + "obtener un margen de contribución positivo. Sin margen no "
                    + "es posible cubrir los costos fijos.");
        }

        tablaCostos.setItems(FXCollections.observableArrayList(
                controlador.listarCostos(periodo[0], periodo[1])));
        tablaProductos.setItems(FXCollections.observableArrayList(
                controlador.indicadoresPorProducto(periodo[0], periodo[1])));
    }

    private void guardarCosto() {
        try {
            if (!Validador.noVacio(txtDescripcion.getText())) {
                txtDescripcion.requestFocus();
                throw new IllegalArgumentException(
                        "La descripción del costo es obligatoria.");
            }
            if (fechaCosto.getValue() == null) {
                fechaCosto.requestFocus();
                throw new IllegalArgumentException("La fecha del costo es obligatoria.");
            }
            if (!Validador.esNumero(txtMonto.getText())) {
                txtMonto.requestFocus();
                throw new IllegalArgumentException("El monto debe ser un número válido.");
            }

            boolean editando = costoEnEdicion != null;
            int id = editando ? costoEnEdicion.getId() : 0;
            Costo costo = new Costo(id, fechaCosto.getValue(),
                    tipoCosto.getValue(), txtDescripcion.getText(),
                    Validador.aDouble(txtMonto.getText()));

            if (editando) {
                if (!controlador.editarCosto(costo)) {
                    throw new IllegalStateException("El costo ya no existe.");
                }
                ventana.mostrarMensaje("Costo #" + id + " actualizado correctamente.");
            } else {
                controlador.registrarCosto(costo);
                ventana.mostrarMensaje("Costo registrado con el ID " + costo.getId() + ".");
            }

            limpiarCosto();
            ventana.actualizarVista();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void editarCostoSeleccionado() {
        Costo seleccionado = tablaCostos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            ventana.mostrarError("Selecciona un costo de la tabla para editarlo.");
            return;
        }

        costoEnEdicion = seleccionado;
        tipoCosto.setValue(seleccionado.getTipo());
        fechaCosto.setValue(seleccionado.getFecha());
        txtDescripcion.setText(seleccionado.getDescripcion());
        txtMonto.setText(numeroPlano(seleccionado.getMonto()));

        btnRegistrar.setText("Guardar cambios");
        btnCancelarEdicion.setVisible(true);
        btnCancelarEdicion.setManaged(true);
        lblModoEdicion.setText("Editando el costo #" + seleccionado.getId()
                + ". Modifica los datos y pulsa Guardar cambios.");
        lblModoEdicion.setVisible(true);
        lblModoEdicion.setManaged(true);
        txtDescripcion.requestFocus();
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
            if (!controlador.eliminarCosto(seleccionado.getId())) {
                throw new IllegalStateException("El costo ya no existe.");
            }
            if (costoEnEdicion != null
                    && costoEnEdicion.getId() == seleccionado.getId()) {
                limpiarCosto();
            }
            ventana.mostrarMensaje("Costo #" + seleccionado.getId()
                    + " eliminado correctamente.");
            ventana.actualizarVista();
        } catch (RuntimeException ex) {
            ventana.mostrarError(ex.getMessage());
        }
    }

    private void limpiarCosto() {
        costoEnEdicion = null;
        tipoCosto.getSelectionModel().selectFirst();
        fechaCosto.setValue(LocalDate.now());
        txtDescripcion.clear();
        txtMonto.clear();
        tablaCostos.getSelectionModel().clearSelection();

        btnRegistrar.setText("Registrar costo");
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.setManaged(false);
        lblModoEdicion.setVisible(false);
        lblModoEdicion.setManaged(false);
        txtDescripcion.requestFocus();
    }

    private static String numeroPlano(double valor) {
        return java.math.BigDecimal.valueOf(valor)
                .stripTrailingZeros()
                .toPlainString();
    }

    private LocalDate[] periodo() {
        LocalDate inicio = desde.getValue();
        LocalDate fin = hasta.getValue();
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            return new LocalDate[]{fin, inicio};
        }
        return new LocalDate[]{inicio, fin};
    }

    private static void aplicarNegativo(Ui.Kpi kpi, boolean negativo) {
        kpi.valor.getStyleClass().remove("kpi-valor-negativo");
        if (negativo) {
            kpi.valor.getStyleClass().add("kpi-valor-negativo");
        }
    }

    private static void limitarDecimales(TextField campo) {
        UnaryOperator<TextFormatter.Change> filtro = cambio
                -> cambio.getControlNewText().matches("\\d{0,12}([.,]\\d{0,4})?")
                ? cambio : null;
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }
}
