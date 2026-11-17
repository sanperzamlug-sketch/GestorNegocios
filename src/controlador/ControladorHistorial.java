package controlador;

import gestor.GestorCostos;
import gestor.GestorHistorialProductos;
import gestor.GestorMovimientos;
import gestor.GestorVentas;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import modelo.Costo;
import modelo.MovimientoInventario;
import modelo.RegistroProducto;
import modelo.Venta;

/**
 * Controlador de consulta y limpieza de los historiales del sistema.
 */
public class ControladorHistorial {

    private final GestorVentas gestorVentas;
    private final GestorHistorialProductos gestorHistorialProductos;
    private final GestorMovimientos gestorMovimientos;
    private final GestorCostos gestorCostos;

    public ControladorHistorial(GestorVentas gestorVentas,
            GestorHistorialProductos gestorHistorialProductos,
            GestorMovimientos gestorMovimientos, GestorCostos gestorCostos) {
        this.gestorVentas = gestorVentas;
        this.gestorHistorialProductos = gestorHistorialProductos;
        this.gestorMovimientos = gestorMovimientos;
        this.gestorCostos = gestorCostos;
    }

    public ArrayList<Venta> listarVentas(LocalDate desde, LocalDate hasta) {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta venta : gestorVentas.listar()) {
            if (GestorCostos.dentroDelPeriodo(venta.getFecha(), desde, hasta)) {
                resultado.add(venta);
            }
        }
        resultado.sort(Comparator.comparing(Venta::getFecha).reversed()
                .thenComparing(Venta::getId, Comparator.reverseOrder()));
        return resultado;
    }

    public ArrayList<RegistroProducto> listarRegistrosProducto(LocalDate desde,
            LocalDate hasta) {
        ArrayList<RegistroProducto> registros
                = gestorHistorialProductos.listarPorPeriodo(desde, hasta);
        registros.sort(Comparator.comparing(RegistroProducto::getFechaHora).reversed()
                .thenComparing(RegistroProducto::getId, Comparator.reverseOrder()));
        return registros;
    }

    public ArrayList<MovimientoInventario> listarMovimientos(LocalDate desde,
            LocalDate hasta) {
        ArrayList<MovimientoInventario> movimientos
                = gestorMovimientos.listarPorPeriodo(desde, hasta);
        movimientos.sort(Comparator.comparing(MovimientoInventario::getFechaHora)
                .reversed().thenComparing(MovimientoInventario::getId,
                        Comparator.reverseOrder()));
        return movimientos;
    }

    public ArrayList<Costo> listarCostos(LocalDate desde, LocalDate hasta) {
        ArrayList<Costo> costos = gestorCostos.listarPorPeriodo(desde, hasta);
        costos.sort(Comparator.comparing(Costo::getFecha).reversed()
                .thenComparing(Costo::getId, Comparator.reverseOrder()));
        return costos;
    }

    public boolean eliminarRegistroProducto(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un registro del historial de productos.");
        }
        return gestorHistorialProductos.eliminar(id);
    }

    public void limpiarHistorialProductos() {
        gestorHistorialProductos.limpiar();
    }

    public boolean eliminarMovimiento(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un movimiento de inventario.");
        }
        return gestorMovimientos.eliminar(id);
    }

    public void limpiarMovimientos() {
        gestorMovimientos.limpiar();
    }
}
