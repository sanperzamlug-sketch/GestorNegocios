package modelo;

import java.time.LocalDateTime;
import util.Formato;

/**
 * Evento del historial de productos. Guarda una foto de los datos del
 * producto en el momento en que ocurrió el cambio.
 */
public class RegistroProducto extends Entidad implements Persistible {

    public enum Evento {
        REGISTRO,
        EDICION,
        ELIMINACION
    }

    private LocalDateTime fechaHora;
    private Evento evento;
    private int idProducto;
    private String nombre;
    private double costo;
    private double precioVenta;
    private int stockActual;
    private String detalle;

    public RegistroProducto() {
        this(0, LocalDateTime.now(), Evento.REGISTRO, 0, "", 0.0, 0.0, 0, "");
    }

    public RegistroProducto(int id, LocalDateTime fechaHora, Evento evento,
            int idProducto, String nombre, double costo, double precioVenta,
            int stockActual, String detalle) {
        this.id = id;
        this.fechaHora = fechaHora == null ? LocalDateTime.now() : fechaHora;
        this.evento = evento == null ? Evento.REGISTRO : evento;
        this.idProducto = idProducto;
        this.nombre = nombre == null ? "" : nombre.trim();
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.detalle = detalle == null ? "" : detalle.trim();
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora == null ? LocalDateTime.now() : fechaHora;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento == null ? Evento.REGISTRO : evento;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? "" : nombre.trim();
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle == null ? "" : detalle.trim();
    }

    @Override
    public String aLinea() {
        return String.join("\t",
                String.valueOf(getId()),
                String.valueOf(fechaHora),
                evento.name(),
                String.valueOf(idProducto),
                Formato.codificar(nombre),
                String.valueOf(costo),
                String.valueOf(precioVenta),
                String.valueOf(stockActual),
                Formato.codificar(detalle));
    }

    @Override
    public String toString() {
        return evento + " · " + nombre;
    }
}
