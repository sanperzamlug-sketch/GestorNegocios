package modelo;

import java.time.LocalDate;
import util.Formato;

/**
 * Costo fijo o variable registrado por el negocio.
 */
public class Costo extends Entidad implements Persistible {

    public enum Tipo {
        FIJO,
        VARIABLE
    }

    private LocalDate fecha;
    private Tipo tipo;
    private String descripcion;
    private double monto;

    public Costo() {
        this(0, LocalDate.now(), Tipo.FIJO, "", 0.0);
    }

    public Costo(int id, LocalDate fecha, Tipo tipo, String descripcion, double monto) {
        this.id = id;
        this.fecha = fecha == null ? LocalDate.now() : fecha;
        this.tipo = tipo == null ? Tipo.FIJO : tipo;
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha == null ? LocalDate.now() : fecha;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo == null ? Tipo.FIJO : tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @Override
    public String aLinea() {
        return String.join("\t",
                String.valueOf(getId()),
                String.valueOf(fecha),
                tipo.name(),
                Formato.codificar(descripcion),
                String.valueOf(monto));
    }

    @Override
    public String toString() {
        return descripcion + " · " + Formato.dinero(monto);
    }
}
