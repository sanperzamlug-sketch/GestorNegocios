package controlador;

import gestor.GestorHistorialProductos;
import gestor.GestorMovimientos;
import gestor.GestorProductos;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.RegistroProducto;
import util.Validador;

/**
 * Controlador de productos. Valida los datos, delega en el gestor y registra
 * los cambios en los historiales.
 */
public class ControladorProductos {

    private final GestorProductos gestorProductos;
    private final GestorHistorialProductos gestorHistorialProductos;
    private final GestorMovimientos gestorMovimientos;

    public ControladorProductos(GestorProductos gestorProductos) {
        this(gestorProductos, new GestorHistorialProductos(), new GestorMovimientos());
    }

    public ControladorProductos(GestorProductos gestorProductos,
            GestorHistorialProductos gestorHistorialProductos,
            GestorMovimientos gestorMovimientos) {
        this.gestorProductos = gestorProductos;
        this.gestorHistorialProductos = gestorHistorialProductos;
        this.gestorMovimientos = gestorMovimientos;
    }

    public void registrar(Producto producto) {
        validar(producto);
        if (gestorProductos.existeNombre(producto.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el nombre '" + producto.getNombre() + "'.");
        }
        gestorProductos.agregar(producto);

        registrarEvento(RegistroProducto.Evento.REGISTRO, producto,
                "Producto registrado en el catálogo.");
        if (producto.getStockActual() > 0) {
            registrarMovimiento(MovimientoInventario.Tipo.ENTRADA,
                    producto, producto.getStockActual(), 0,
                    producto.getStockActual(), "Stock inicial del producto");
        }
    }

    public boolean editar(Producto producto) {
        if (producto == null || producto.getId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto para editar.");
        }
        validar(producto);
        if (gestorProductos.existeNombre(producto.getNombre(), producto.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe otro producto con el nombre '" + producto.getNombre() + "'.");
        }

        Producto anterior = gestorProductos.buscar(producto.getId());
        if (anterior == null) {
            return false;
        }

        String detalle = describirCambios(anterior, producto);
        boolean actualizado = gestorProductos.editar(producto);
        if (actualizado) {
            registrarEvento(RegistroProducto.Evento.EDICION, producto, detalle);
            registrarCambioStock(anterior, producto);
        }
        return actualizado;
    }

    public boolean eliminar(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto para eliminar.");
        }

        Producto producto = gestorProductos.buscar(id);
        boolean eliminado = gestorProductos.eliminar(id);
        if (eliminado && producto != null) {
            registrarEvento(RegistroProducto.Evento.ELIMINACION, producto,
                    "Producto eliminado del catálogo.");
            if (producto.getStockActual() > 0) {
                registrarMovimiento(MovimientoInventario.Tipo.AJUSTE, producto,
                        producto.getStockActual(), producto.getStockActual(), 0,
                        "Producto eliminado del catálogo");
            }
        }
        return eliminado;
    }

    public Producto buscar(int id) {
        return gestorProductos.buscar(id);
    }

    public ArrayList<Producto> listar() {
        return gestorProductos.listar();
    }

    public ArrayList<Producto> alertasStockBajo() {
        return gestorProductos.stockBajo();
    }

    private void validar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (!Validador.noVacio(producto.getNombre())) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (!Validador.noNegativo(producto.getCosto())) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        if (!Validador.noNegativo(producto.getPrecioVenta())) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo.");
        }
        if (producto.getStockActual() < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo.");
        }
        if (producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
    }

    private void registrarEvento(RegistroProducto.Evento evento, Producto producto,
            String detalle) {
        gestorHistorialProductos.registrar(new RegistroProducto(
                0,
                LocalDateTime.now(),
                evento,
                producto.getId(),
                producto.getNombre(),
                producto.getCosto(),
                producto.getPrecioVenta(),
                producto.getStockActual(),
                detalle));
    }

    private void registrarCambioStock(Producto anterior, Producto actual) {
        int diferencia = actual.getStockActual() - anterior.getStockActual();
        if (diferencia == 0) {
            return;
        }

        MovimientoInventario.Tipo tipo = diferencia > 0
                ? MovimientoInventario.Tipo.ENTRADA
                : MovimientoInventario.Tipo.SALIDA;
        registrarMovimiento(tipo, actual, Math.abs(diferencia),
                anterior.getStockActual(), actual.getStockActual(),
                "Ajuste manual desde la edición del producto");
    }

    private void registrarMovimiento(MovimientoInventario.Tipo tipo, Producto producto,
            int cantidad, int stockAnterior, int stockNuevo, String motivo) {
        gestorMovimientos.registrar(new MovimientoInventario(
                0,
                LocalDateTime.now(),
                producto.getId(),
                producto.getNombre(),
                tipo,
                cantidad,
                stockAnterior,
                stockNuevo,
                motivo));
    }

    private String describirCambios(Producto anterior, Producto actual) {
        List<String> cambios = new ArrayList<>();
        if (!anterior.getNombre().equals(actual.getNombre())) {
            cambios.add("nombre: '" + anterior.getNombre() + "' → '"
                    + actual.getNombre() + "'");
        }
        if (Double.compare(anterior.getCosto(), actual.getCosto()) != 0) {
            cambios.add("costo: " + numero(anterior.getCosto()) + " → "
                    + numero(actual.getCosto()));
        }
        if (Double.compare(anterior.getPrecioVenta(), actual.getPrecioVenta()) != 0) {
            cambios.add("precio de venta: " + numero(anterior.getPrecioVenta())
                    + " → " + numero(actual.getPrecioVenta()));
        }
        if (anterior.getStockActual() != actual.getStockActual()) {
            cambios.add("stock: " + anterior.getStockActual() + " → "
                    + actual.getStockActual());
        }
        if (anterior.getStockMinimo() != actual.getStockMinimo()) {
            cambios.add("stock mínimo: " + anterior.getStockMinimo() + " → "
                    + actual.getStockMinimo());
        }
        return cambios.isEmpty()
                ? "Sin cambios en los datos del producto."
                : String.join("; ", cambios) + ".";
    }

    private static String numero(double valor) {
        return String.valueOf(valor);
    }
}
