package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import util.Formato;

/**
 * Venta compuesta por uno o varios detalles.
 */
public class Venta extends Entidad implements Persistible {

    private LocalDate fecha;
    private ArrayList<DetalleVenta> detalles;

    public Venta() {
        this(0, LocalDate.now());
    }

    public Venta(int id, LocalDate fecha) {
        this.id = id;
        this.fecha = fecha == null ? LocalDate.now() : fecha;
        this.detalles = new ArrayList<>();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha == null ? LocalDate.now() : fecha;
    }

    public void agregarDetalle(DetalleVenta detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de la venta no puede ser nulo.");
        }
        if (detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad del detalle debe ser mayor que cero.");
        }
        detalles.add(detalle);
    }

    public boolean quitarDetalle(int indice) {
        if (indice < 0 || indice >= detalles.size()) {
            return false;
        }
        detalles.remove(indice);
        return true;
    }

    public ArrayList<DetalleVenta> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public boolean estaVacia() {
        return detalles.isEmpty();
    }

    public double calcularTotal() {
        double total = 0.0;
        for (DetalleVenta detalle : detalles) {
            total += detalle.calcularSubtotal();
        }
        return total;
    }

    public double calcularCostoTotal() {
        double total = 0.0;
        for (DetalleVenta detalle : detalles) {
            total += detalle.calcularCostoTotal();
        }
        return total;
    }

    @Override
    public String aLinea() {
        StringBuilder linea = new StringBuilder();
        linea.append(getId()).append('\t')
                .append(fecha).append('\t')
                .append(detalles.size());

        for (DetalleVenta detalle : detalles) {
            linea.append('\t').append(detalle.getIdProducto())
                    .append('\t').append(Formato.codificar(detalle.getNombreProducto()))
                    .append('\t').append(detalle.getCantidad())
                    .append('\t').append(detalle.getPrecioUnitario())
                    .append('\t').append(detalle.getCostoUnitario());
        }
        return linea.toString();
    }

    @Override
    public String toString() {
        return "Venta #" + getId() + " - " + Formato.fecha(fecha)
                + " - " + Formato.dinero(calcularTotal());
    }
}
