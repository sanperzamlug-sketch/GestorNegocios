package vista;

import controlador.ControladorReportes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.DetalleVenta;
import modelo.Venta;
import util.Formato;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Reportes financieros y comportamiento de ventas.
 */
public class PanelReportes extends VBox {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-CO");

    private final ControladorReportes controlador;
    private final VentanaPrincipal ventana;

    private final Ui.Kpi kpiIngresos = Ui.kpi("Ingresos totales", Icono.DINERO,
            "acento-verde");
    private final Ui.Kpi kpiCostos = Ui.kpi("Costos totales", Icono.CAJA,
            "acento-ambar");
    private final Ui.Kpi kpiUtilidad = Ui.kpi("Utilidad", Icono.TENDENCIA,
            "acento-indigo");
    private final Ui.Kpi kpiMargen = Ui.kpi("Margen de utilidad", Icono.GRAFICO,
            "acento-azul");

    private final BarChart<String, Number> grafico;
    private final Label lblSinVentas = new Label(
            "No hay ventas registradas en los últimos 7 días.");
    private final TableView<FilaTop> tablaTop = Ui.tabla(
            "Aún no hay productos vendidos.");
    private final Label lblActualizado = new Label();

    public PanelReportes(ControladorReportes controlador, VentanaPrincipal ventana) {
        this.controlador = controlador;
        this.ventana = ventana;

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
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setVerticalGridLinesVisible(false);
        grafico.setHorizontalGridLinesVisible(true);
        grafico.setCategoryGap(18);
        grafico.setBarGap(4);
        grafico.getStyleClass().add("grafico");

        getStyleClass().add("page");
        construirInterfaz();
        actualizar();
    }

    private void construirInterfaz() {
        HBox filaIndicadores = new HBox(16,
                kpiIngresos.raiz, kpiCostos.raiz, kpiUtilidad.raiz, kpiMargen.raiz);
        filaIndicadores.setAlignment(Pos.TOP_LEFT);

        configurarGrafico();
        configurarTablaTop();

        Button btnActualizar = Ui.botonIcono(Icono.ACTUALIZAR,
                "Actualizar reportes", "button-icono");
        btnActualizar.setOnAction(evento -> {
            actualizar();
            ventana.mostrarMensaje("Reportes actualizados correctamente.");
        });

        VBox tarjetaGrafico = Ui.tarjeta("Ingresos de los últimos 7 días",
                "Evolución diaria de las ventas registradas",
                new VBox(6, grafico, lblSinVentas), btnActualizar);
        tarjetaGrafico.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(tarjetaGrafico, Priority.ALWAYS);

        VBox tarjetaTop = Ui.tarjeta("Productos más vendidos",
                "Ranking por ingresos históricos", tablaTop);
        tarjetaTop.setPrefWidth(415);
        tarjetaTop.setMinWidth(395);
        tarjetaTop.setMaxWidth(440);
        tarjetaTop.setMaxHeight(Double.MAX_VALUE);

        HBox filaInferior = new HBox(18, tarjetaGrafico, tarjetaTop);
        filaInferior.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(filaInferior, Priority.ALWAYS);
        VBox.setVgrow(tarjetaGrafico, Priority.ALWAYS);

        lblActualizado.getStyleClass().add("tarjeta-subtitulo");
        Button btnFinanzas = Ui.boton("Ver finanzas", Icono.DINERO,
                "button-fantasma");
        btnFinanzas.setOnAction(evento -> ventana.irAFinanzas());

        javafx.scene.layout.Region espacioPie = new javafx.scene.layout.Region();
        HBox.setHgrow(espacioPie, Priority.ALWAYS);

        HBox pie = new HBox(10, lblActualizado, espacioPie, btnFinanzas);
        pie.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(filaIndicadores, filaInferior, pie);
    }

    private void configurarGrafico() {
        grafico.setPrefHeight(290);
        grafico.setMinHeight(180);
        VBox.setVgrow(grafico, Priority.ALWAYS);
        lblSinVentas.getStyleClass().add("tarjeta-subtitulo");
        lblSinVentas.setVisible(false);
        lblSinVentas.setManaged(false);
    }

    private void configurarTablaTop() {
        TableColumn<FilaTop, String> colProducto = Ui.columna("Producto", 150,
                FilaTop::nombre);
        TableColumn<FilaTop, String> colUnidades = Ui.columnaDerecha(
                "Unidades", 80, fila -> String.valueOf(fila.unidades()));
        TableColumn<FilaTop, String> colIngresos = Ui.columnaDerecha(
                "Ingresos", 120, fila -> Formato.dinero(fila.ingresos()));

        tablaTop.getColumns().setAll(
                List.of(colProducto, colUnidades, colIngresos));
        tablaTop.setItems(FXCollections.observableArrayList());
        VBox.setVgrow(tablaTop, Priority.ALWAYS);
    }

    public void actualizar() {
        double ingresos = controlador.totalIngresos();
        double costos = controlador.totalCostos();
        double utilidad = controlador.utilidad();
        int cantidadVentas = controlador.listarVentas().size();

        kpiIngresos.valor.setText(Formato.dinero(ingresos));
        kpiIngresos.pista.setText(cantidadVentas + (cantidadVentas == 1
                ? " venta registrada" : " ventas registradas"));

        kpiCostos.valor.setText(Formato.dinero(costos));
        kpiCostos.pista.setText("Costo de la mercancía vendida");

        kpiUtilidad.valor.setText(Formato.dinero(utilidad));
        kpiUtilidad.pista.setText("Ganancia neta acumulada");
        kpiUtilidad.valor.getStyleClass().remove("kpi-valor-negativo");
        if (utilidad < 0) {
            kpiUtilidad.valor.getStyleClass().add("kpi-valor-negativo");
        }

        double margen = ingresos <= 0 ? 0 : (utilidad / ingresos) * 100;
        kpiMargen.valor.setText(Formato.porcentaje(margen));
        kpiMargen.pista.setText("Rentabilidad sobre los ingresos");

        actualizarGrafico();
        actualizarTop();
        String fecha = Formato.fechaLarga(LocalDate.now());
        if (!fecha.isEmpty()) {
            fecha = Character.toLowerCase(fecha.charAt(0)) + fecha.substring(1);
        }
        lblActualizado.setText("Información actualizada el " + fecha + ".");
    }

    private void actualizarGrafico() {
        ArrayList<Venta> ventas = controlador.listarVentas();
        Map<LocalDate, Double> ingresosPorDia = new HashMap<>();
        for (Venta venta : ventas) {
            ingresosPorDia.merge(venta.getFecha(), venta.calcularTotal(),
                    Double::sum);
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        LocalDate hoy = LocalDate.now();
        double totalSemana = 0;
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            double total = ingresosPorDia.getOrDefault(dia, 0.0);
            totalSemana += total;
            serie.getData().add(new XYChart.Data<>(etiquetaDia(dia), total));
        }

        grafico.getData().setAll(List.of(serie));
        boolean sinVentas = totalSemana <= 0;
        lblSinVentas.setVisible(sinVentas);
        lblSinVentas.setManaged(sinVentas);
    }

    private void actualizarTop() {
        Map<Integer, double[]> acumulados = new HashMap<>();
        Map<Integer, Integer> unidades = new HashMap<>();
        Map<Integer, String> nombres = new HashMap<>();

        for (Venta venta : controlador.listarVentas()) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                int id = detalle.getIdProducto();
                acumulados.merge(id, new double[]{detalle.calcularSubtotal()},
                        (actual, nuevo) -> {
                            actual[0] += nuevo[0];
                            return actual;
                        });
                unidades.merge(id, detalle.getCantidad(), Integer::sum);
                nombres.put(id, detalle.getNombreProducto());
            }
        }

        List<FilaTop> filas = new ArrayList<>();
        for (Map.Entry<Integer, double[]> entrada : acumulados.entrySet()) {
            int id = entrada.getKey();
            filas.add(new FilaTop(nombres.getOrDefault(id, "Producto " + id),
                    unidades.getOrDefault(id, 0), entrada.getValue()[0]));
        }
        filas.sort(Comparator.comparingDouble(FilaTop::ingresos).reversed());

        if (filas.size() > 8) {
            filas = new ArrayList<>(filas.subList(0, 8));
        }
        tablaTop.setItems(FXCollections.observableArrayList(filas));
    }

    private static String etiquetaDia(LocalDate fecha) {
        String texto = Formato.fecha(fecha);
        return texto.length() >= 5 ? texto.substring(0, 5) : texto;
    }

    private static String abreviarDinero(double valor) {
        if (Math.abs(valor) >= 1_000_000) {
            return compacto(valor / 1_000_000, "M");
        }
        if (Math.abs(valor) >= 1_000) {
            return compacto(valor / 1_000, "K");
        }
        return String.format(LOCALE_ES, "$%,.0f", valor);
    }

    private static String compacto(double valor, String sufijo) {
        String texto = String.format(LOCALE_ES, "%.1f", valor);
        if (texto.endsWith(",0")) {
            texto = texto.substring(0, texto.length() - 2);
        }
        return "$" + texto + " " + sufijo;
    }

    private record FilaTop(String nombre, int unidades, double ingresos) {

    }
}
