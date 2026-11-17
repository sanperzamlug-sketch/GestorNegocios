package modelo;

import util.Formato;

/**
 * Producto disponible en el inventario.
 */
public class Producto extends Entidad implements Persistible {

    private String nombre;
    private double costo;
    private double precioVenta;
    private int stockActual;
    private int stockMinimo;

    public Producto() {
        this(0, "", 0.0, 0.0, 0, 0);
    }

    public Producto(int id, String nombre, double costo, double precioVenta,
            int stockActual, int stockMinimo) {
        this.id = id;
        this.nombre = nombre == null ? "" : nombre.trim();
        this.costo = costo;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
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

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    /**
     * Margen comercial sobre el precio de venta, expresado en porcentaje.
     */
    public double calcularMargen() {
        if (precioVenta <= 0) {
            return 0.0;
        }
        return ((precioVenta - costo) / precioVenta) * 100.0;
    }

    public boolean tieneStockBajo() {
        return stockActual <= stockMinimo;
    }

    public void descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero.");
        }
        if (cantidad > stockActual) {
            throw new IllegalArgumentException(
                    "Stock insuficiente de '" + nombre + "'. Disponible: " + stockActual
                    + ", solicitado: " + cantidad + ".");
        }
        stockActual -= cantidad;
    }

    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor que cero.");
        }
        stockActual += cantidad;
    }

    @Override
    public String aLinea() {
        return String.join("\t",
                String.valueOf(getId()),
                Formato.codificar(nombre),
                String.valueOf(costo),
                String.valueOf(precioVenta),
                String.valueOf(stockActual),
                String.valueOf(stockMinimo));
    }

    @Override
    public String toString() {
        return getId() + " - " + nombre;
    }
}
