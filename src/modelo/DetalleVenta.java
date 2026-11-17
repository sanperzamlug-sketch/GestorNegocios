package modelo;

/**
 * Línea o detalle de una venta. Guarda una copia de los datos del producto
 * para conservar el histórico aunque el producto cambie o se elimine.
 */
public class DetalleVenta {

    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double costoUnitario;

    public DetalleVenta() {
        this(0, "", 0, 0.0, 0.0);
    }

    public DetalleVenta(int idProducto, String nombreProducto, int cantidad,
            double precioUnitario, double costoUnitario) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto == null ? "" : nombreProducto.trim();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.costoUnitario = costoUnitario;
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

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public double calcularCostoTotal() {
        return cantidad * costoUnitario;
    }

    @Override
    public String toString() {
        return nombreProducto + " x " + cantidad;
    }
}
