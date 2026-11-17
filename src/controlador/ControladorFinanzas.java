package controlador;

import gestor.CalculadoraFinanciera;
import gestor.GestorCostos;
import gestor.GestorProductos;
import gestor.GestorVentas;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import modelo.Costo;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Venta;
import util.Validador;

/**
 * Controlador de indicadores financieros, costos y rentabilidad por producto.
 */
public class ControladorFinanzas {

    private final GestorVentas gestorVentas;
    private final GestorProductos gestorProductos;
    private final GestorCostos gestorCostos;
    private final CalculadoraFinanciera calculadora;

    public ControladorFinanzas(GestorVentas gestorVentas,
            GestorProductos gestorProductos, GestorCostos gestorCostos,
            CalculadoraFinanciera calculadora) {
        this.gestorVentas = gestorVentas;
        this.gestorProductos = gestorProductos;
        this.gestorCostos = gestorCostos;
        this.calculadora = calculadora;
    }

    public void registrarCosto(Costo costo) {
        validarCosto(costo);
        gestorCostos.registrar(costo);
    }

    public boolean editarCosto(Costo costo) {
        validarCosto(costo);
        if (costo.getId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un costo para editar.");
        }
        if (gestorCostos.buscar(costo.getId()) == null) {
            throw new IllegalArgumentException("El costo que intenta editar ya no existe.");
        }
        return gestorCostos.editar(costo);
    }

    public boolean eliminarCosto(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un costo para eliminar.");
        }
        return gestorCostos.eliminar(id);
    }

    public void limpiarCostos() {
        gestorCostos.limpiar();
    }

    public ArrayList<Costo> listarCostos(LocalDate desde, LocalDate hasta) {
        ArrayList<Costo> costos = gestorCostos.listarPorPeriodo(desde, hasta);
        costos.sort(Comparator.comparing(Costo::getFecha).reversed()
                .thenComparing(Costo::getId, Comparator.reverseOrder()));
        return costos;
    }

    public ArrayList<Costo> listarCostos() {
        ArrayList<Costo> costos = gestorCostos.listar();
        costos.sort(Comparator.comparing(Costo::getFecha).reversed()
                .thenComparing(Costo::getId, Comparator.reverseOrder()));
        return costos;
    }

    public ArrayList<Venta> listarVentas(LocalDate desde, LocalDate hasta) {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta venta : gestorVentas.listar()) {
            if (GestorCostos.dentroDelPeriodo(venta.getFecha(), desde, hasta)) {
                resultado.add(venta);
            }
        }
        return resultado;
    }

    public ResumenFinanciero resumen(LocalDate desde, LocalDate hasta) {
        ArrayList<Venta> ventas = listarVentas(desde, hasta);
        ArrayList<Costo> costos = gestorCostos.listarPorPeriodo(desde, hasta);

        double ingresos = calculadora.totalIngresos(ventas);
        double costoVentas = calculadora.totalCostos(ventas);
        double costosFijos = calculadora.totalCostosFijos(costos);
        double costosVariables = calculadora.totalCostosVariables(costos);
        double costosOperativos = costosFijos + costosVariables;
        double utilidadBruta = ingresos - costoVentas;
        double utilidadNeta = utilidadBruta - costosOperativos;
        double margenContribucion = calculadora.margenContribucion(ventas, costos);
        boolean puntoCalculable = calculadora.puntoEquilibrioCalculable(ventas, costos);
        double puntoEquilibrio = calculadora.puntoEquilibrio(ventas, costos);
        double rentabilidad = ingresos <= 0 ? 0.0 : (utilidadNeta / ingresos) * 100.0;

        return new ResumenFinanciero(ingresos, costoVentas, costosFijos,
                costosVariables, costosOperativos, utilidadBruta, utilidadNeta,
                margenContribucion, puntoEquilibrio, puntoCalculable,
                rentabilidad, ventas.size(), costos.size());
    }

    /**
     * Margen y rentabilidad de cada producto, junto con su participación en
     * las ventas del periodo. Los productos que ya no existen en el catálogo
     * se conservan usando los datos históricos de la última venta.
     */
    public ArrayList<IndicadorProducto> indicadoresPorProducto(LocalDate desde,
            LocalDate hasta) {
        Map<Integer, Acumulado> acumulados = new HashMap<>();

        for (Venta venta : listarVentas(desde, hasta)) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Acumulado acumulado = acumulados.computeIfAbsent(
                        detalle.getIdProducto(), clave -> new Acumulado());
                acumulado.nombre = detalle.getNombreProducto();
                acumulado.unidades += detalle.getCantidad();
                acumulado.ingresos += detalle.calcularSubtotal();
                acumulado.costoVentas += detalle.calcularCostoTotal();
                acumulado.precioUnitario = detalle.getPrecioUnitario();
                acumulado.costoUnitario = detalle.getCostoUnitario();
            }
        }

        ArrayList<IndicadorProducto> resultado = new ArrayList<>();
        java.util.HashSet<Integer> incluidos = new java.util.HashSet<>();

        for (Producto producto : gestorProductos.listar()) {
            Acumulado acumulado = acumulados.get(producto.getId());
            incluidos.add(producto.getId());
            resultado.add(crearIndicador(producto.getId(), producto.getNombre(),
                    producto.getPrecioVenta(), producto.getCosto(), acumulado));
        }

        for (Map.Entry<Integer, Acumulado> entrada : acumulados.entrySet()) {
            if (incluidos.contains(entrada.getKey())) {
                continue;
            }
            Acumulado acumulado = entrada.getValue();
            String nombre = acumulado.nombre == null || acumulado.nombre.isBlank()
                    ? "Producto " + entrada.getKey()
                    : acumulado.nombre;
            resultado.add(crearIndicador(entrada.getKey(), nombre + " (eliminado)",
                    acumulado.precioUnitario, acumulado.costoUnitario, acumulado));
        }

        resultado.sort(Comparator.comparing(IndicadorProducto::ingresos).reversed()
                .thenComparing(IndicadorProducto::nombre,
                        String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    public ArrayList<ResumenMensual> historialFinanciero() {
        Map<YearMonth, double[]> datos = new TreeMap<>();

        for (Venta venta : gestorVentas.listar()) {
            YearMonth mes = YearMonth.from(venta.getFecha());
            double[] valores = datos.computeIfAbsent(mes, clave -> new double[4]);
            valores[0] += venta.calcularTotal();
            valores[1] += venta.calcularCostoTotal();
        }

        for (Costo costo : gestorCostos.listar()) {
            YearMonth mes = YearMonth.from(costo.getFecha());
            double[] valores = datos.computeIfAbsent(mes, clave -> new double[4]);
            if (costo.getTipo() == Costo.Tipo.FIJO) {
                valores[2] += costo.getMonto();
            } else {
                valores[3] += costo.getMonto();
            }
        }

        ArrayList<ResumenMensual> resultado = new ArrayList<>();
        for (Map.Entry<YearMonth, double[]> entrada : datos.entrySet()) {
            double[] valores = entrada.getValue();
            double utilidad = valores[0] - valores[1] - valores[2] - valores[3];
            double rentabilidad = valores[0] <= 0 ? 0.0 : (utilidad / valores[0]) * 100.0;
            resultado.add(new ResumenMensual(entrada.getKey(), valores[0], valores[1],
                    valores[2], valores[3], utilidad, rentabilidad));
        }
        return resultado;
    }

    private void validarCosto(Costo costo) {
        if (costo == null) {
            throw new IllegalArgumentException("El costo no puede ser nulo.");
        }
        if (costo.getTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de costo.");
        }
        if (!Validador.noVacio(costo.getDescripcion())) {
            throw new IllegalArgumentException("La descripción del costo es obligatoria.");
        }
        if (!Validador.noNegativo(costo.getMonto()) || costo.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto del costo debe ser mayor que cero.");
        }
    }

    private IndicadorProducto crearIndicador(int id, String nombre,
            double precio, double costo, Acumulado acumulado) {
        double margenUnitario = precio - costo;
        double margen = precio > 0 ? (margenUnitario / precio) * 100.0 : 0.0;
        double rentabilidad = costo > 0 ? (margenUnitario / costo) * 100.0 : 0.0;
        int unidades = acumulado == null ? 0 : acumulado.unidades;
        double ingresos = acumulado == null ? 0.0 : acumulado.ingresos;
        double costoVentas = acumulado == null ? 0.0 : acumulado.costoVentas;
        return new IndicadorProducto(id, nombre, precio, costo, margenUnitario,
                margen, rentabilidad, unidades, ingresos, ingresos - costoVentas);
    }

    private static final class Acumulado {

        private String nombre;
        private int unidades;
        private double ingresos;
        private double costoVentas;
        private double precioUnitario;
        private double costoUnitario;
    }

    public record ResumenFinanciero(double ingresos, double costoVentas,
            double costosFijos, double costosVariables, double costosOperativos,
            double utilidadBruta, double utilidadNeta, double margenContribucion,
            double puntoEquilibrio, boolean puntoEquilibrioCalculable,
            double rentabilidadGeneral, int cantidadVentas, int cantidadCostos) {
    }

    public record IndicadorProducto(int idProducto, String nombre,
            double precioUnitario, double costoUnitario, double margenUnitario,
            double margenPorcentaje, double rentabilidadPorcentaje,
            int unidadesVendidas, double ingresos, double utilidadBruta) {
    }

    public record ResumenMensual(YearMonth mes, double ingresos, double costoVentas,
            double costosFijos, double costosVariables, double utilidadNeta,
            double rentabilidad) {
    }
}
