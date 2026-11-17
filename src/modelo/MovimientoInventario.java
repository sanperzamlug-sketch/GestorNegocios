package modelo;

import java.time.LocalDateTime;
import util.Formato;

/**
 * Entrada, salida o ajuste de existencias de un producto.
 */
public class MovimientoInventario extends Entidad implements Persistible {

    public enum Tipo {
        ENTRADA,
        SALIDA,
        AJUSTE
    }

    private LocalDateTime fechaHora;
    private int idProducto;
    private String nombreProducto;
    private Tipo tipo;
    private int cantidad;
    private int stockAnterior;
    private int stockNuevo;
    private String motivo;

    public MovimientoInventario() {
        this(0, LocalDateTime.now(), 0, "", Tipo.ENTRADA, 0, 0, 0, "");
    }

    public MovimientoInventario(int id, LocalDateTime fechaHora, int idProducto,
            String nombreProducto, Tipo tipo, int cantidad, int stockAnterior,
            int stockNuevo, String motivo) {
        this.id = id;
        this.fechaHora = fechaHora == null ? LocalDateTime.now() : fechaHora;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto == null ? "" : nombreProducto.trim();
        this.tipo = tipo == null ? Tipo.AJUSTE : tipo;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.motivo = motivo == null ? "" : motivo.trim();
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora == null ? LocalDateTime.now() : fechaHora;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto == null ? "" : nombreProducto.trim();
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo == null ? Tipo.AJUSTE : tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(int stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo == null ? "" : motivo.trim();
    }

    @Override
    public String aLinea() {
        return String.join("\t",
                String.valueOf(getId()),
                String.valueOf(fechaHora),
                String.valueOf(idProducto),
                Formato.codificar(nombreProducto),
                tipo.name(),
                String.valueOf(cantidad),
                String.valueOf(stockAnterior),
                String.valueOf(stockNuevo),
                Formato.codificar(motivo));
    }

    @Override
    public String toString() {
        return tipo + " · " + nombreProducto + " · " + cantidad;
    }
}
