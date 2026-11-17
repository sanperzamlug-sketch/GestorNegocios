package controlador;

import gestor.GestorMovimientos;
import gestor.GestorProductos;
import gestor.GestorVentas;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import modelo.DetalleVenta;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.Venta;

/**
 * Controlador del proceso de ventas. Mantiene la venta que se está armando y
 * coordina el descuento, la reposición de stock y el historial de inventario.
 */
public class ControladorVentas {

    private final GestorVentas gestorVentas;
    private final GestorProductos gestorProductos;
    private final GestorMovimientos gestorMovimientos;
    private Venta ventaActual;

    public ControladorVentas(GestorVentas gestorVentas, GestorProductos gestorProductos) {
        this(gestorVentas, gestorProductos, new GestorMovimientos());
    }

    public ControladorVentas(GestorVentas gestorVentas, GestorProductos gestorProductos,
            GestorMovimientos gestorMovimientos) {
        this.gestorVentas = gestorVentas;
        this.gestorProductos = gestorProductos;
        this.gestorMovimientos = gestorMovimientos;
        this.ventaActual = new Venta();
    }

    public void iniciarVenta() {
        ventaActual = new Venta();
    }

    public void agregarLinea(int idProducto, int cantidad) {
        if (ventaActual == null) {
            ventaActual = new Venta();
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        Producto producto = gestorProductos.buscar(idProducto);
        if (producto == null) {
            throw new IllegalArgumentException("El producto seleccionado ya no existe.");
        }

        int disponible = stockDisponibleParaAgregar(idProducto);
        if (cantidad > disponible) {
            throw new IllegalArgumentException(
                    "Stock insuficiente de '" + producto.getNombre() + "'. "
                    + "Disponible para esta venta: " + disponible + ".");
        }

        DetalleVenta existente = buscarDetalle(ventaActual, idProducto);
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            ventaActual.agregarDetalle(new DetalleVenta(
                    producto.getId(),
                    producto.getNombre(),
                    cantidad,
                    producto.getPrecioVenta(),
                    producto.getCosto()));
        }
    }

    public void quitarLinea(int indice) {
        if (ventaActual == null || !ventaActual.quitarDetalle(indice)) {
            throw new IllegalArgumentException("Debe seleccionar una línea de la venta actual.");
        }
    }

    /**
     * Carga una copia de la venta indicada para poder editarla sin modificar
     * el histórico hasta que se guarde.
     */
    public void cargarVentaParaEditar(Venta venta) {
        if (venta == null || venta.getId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una venta para editar.");
        }

        Venta copia = new Venta(venta.getId(), venta.getFecha());
        for (DetalleVenta detalle : venta.getDetalles()) {
            copia.agregarDetalle(new DetalleVenta(
                    detalle.getIdProducto(),
                    detalle.getNombreProducto(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getCostoUnitario()));
        }
        ventaActual = copia;
    }

    public void guardarVenta() {
        if (ventaActual == null || ventaActual.estaVacia()) {
            throw new IllegalArgumentException("La venta debe tener al menos una línea.");
        }

        if (ventaActual.getId() <= 0) {
            guardarNuevaVenta();
        } else {
            actualizarVentaExistente();
        }
    }

    private void guardarNuevaVenta() {
        validarStock(ventaActual.getDetalles());
        aplicarDescuentos(ventaActual.getDetalles());
        try {
            gestorVentas.registrar(ventaActual);
        } catch (RuntimeException ex) {
            restaurarStock(ventaActual.getDetalles());
            throw ex;
        }
        registrarSalidas(ventaActual, "Venta #" + ventaActual.getId());
    }

    private void actualizarVentaExistente() {
        Venta original = gestorVentas.buscar(ventaActual.getId());
        if (original == null) {
            throw new IllegalArgumentException("La venta que intenta editar ya no existe.");
        }

        restaurarStock(original.getDetalles());
        try {
            validarStock(ventaActual.getDetalles());
        } catch (RuntimeException ex) {
            aplicarDescuentos(original.getDetalles());
            throw ex;
        }

        aplicarDescuentos(ventaActual.getDetalles());
        if (!gestorVentas.editar(ventaActual)) {
            restaurarStock(ventaActual.getDetalles());
            aplicarDescuentos(original.getDetalles());
            throw new IllegalStateException("No se pudo actualizar la venta.");
        }
        registrarMovimientosEdicion(original, ventaActual);
    }

    public boolean editarVenta(Venta venta) {
        if (venta == null) {
            return false;
        }

        Venta anterior = ventaActual;
        try {
            cargarVentaParaEditar(venta);
            guardarVenta();
            return true;
        } catch (RuntimeException ex) {
            ventaActual = anterior;
            throw ex;
        }
    }

    public boolean eliminarVenta(int id) {
        Venta venta = gestorVentas.buscar(id);
        if (venta == null) {
            return false;
        }

        restaurarStock(venta.getDetalles());
        if (!gestorVentas.eliminar(id)) {
            aplicarDescuentos(venta.getDetalles());
            return false;
        }

        registrarEntradas(venta, "Eliminación de la venta #" + id);
        if (ventaActual != null && ventaActual.getId() == id) {
            iniciarVenta();
        }
        return true;
    }

    public ArrayList<Venta> ventasDelDia() {
        return gestorVentas.ventasDelDia();
    }

    public double totalDelDia() {
        double total = 0.0;
        for (Venta venta : ventasDelDia()) {
            total += venta.calcularTotal();
        }
        return total;
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public ArrayList<Producto> listarProductos() {
        return gestorProductos.listar();
    }

    /**
     * Cantidad que todavía se puede agregar al detalle actual de la venta,
     * considerando que al editar una venta su stock se repone al guardar.
     */
    public int stockDisponibleParaAgregar(int idProducto) {
        Producto producto = gestorProductos.buscar(idProducto);
        if (producto == null) {
            return 0;
        }

        int enVenta = cantidadEnVenta(ventaActual, idProducto);
        int enVentaOriginal = 0;

        if (ventaActual != null && ventaActual.getId() > 0) {
            Venta original = gestorVentas.buscar(ventaActual.getId());
            if (original != null) {
                enVentaOriginal = cantidadEnVenta(original, idProducto);
            }
        }

        return Math.max(0, producto.getStockActual() + enVentaOriginal - enVenta);
    }

    private DetalleVenta buscarDetalle(Venta venta, int idProducto) {
        if (venta == null) {
            return null;
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getIdProducto() == idProducto) {
                return detalle;
            }
        }
        return null;
    }

    private int cantidadEnVenta(Venta venta, int idProducto) {
        if (venta == null) {
            return 0;
        }
        int cantidad = 0;
        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getIdProducto() == idProducto) {
                cantidad += detalle.getCantidad();
            }
        }
        return cantidad;
    }

    /**
     * Agrupa las cantidades por producto y verifica que haya stock suficiente
     * para todas las líneas.
     */
    private void validarStock(ArrayList<DetalleVenta> detalles) {
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos una línea.");
        }

        HashMap<Integer, Integer> cantidades = new HashMap<>();
        HashMap<Integer, Producto> productos = new HashMap<>();

        for (DetalleVenta detalle : detalles) {
            if (detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad de todas las líneas debe ser mayor que cero.");
            }

            Producto producto = gestorProductos.buscar(detalle.getIdProducto());
            if (producto == null) {
                throw new IllegalArgumentException(
                        "El producto '" + detalle.getNombreProducto() + "' ya no existe.");
            }

            cantidades.merge(detalle.getIdProducto(), detalle.getCantidad(), Integer::sum);
            productos.put(detalle.getIdProducto(), producto);
        }

        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.get(entrada.getKey());
            int solicitado = entrada.getValue();
            if (solicitado > producto.getStockActual()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente de '" + producto.getNombre() + "'. "
                        + "Disponible: " + producto.getStockActual()
                        + ", solicitado: " + solicitado + ".");
            }
        }
    }

    private void aplicarDescuentos(ArrayList<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            Producto producto = gestorProductos.buscar(detalle.getIdProducto());
            if (producto != null) {
                producto.descontarStock(detalle.getCantidad());
            }
        }
    }

    private void restaurarStock(ArrayList<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            Producto producto = gestorProductos.buscar(detalle.getIdProducto());
            if (producto != null) {
                producto.aumentarStock(detalle.getCantidad());
            }
        }
    }

    private void registrarSalidas(Venta venta, String motivo) {
        Map<Integer, Integer> cantidades = agruparCantidades(venta);
        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            Producto producto = gestorProductos.buscar(entrada.getKey());
            if (producto == null || entrada.getValue() <= 0) {
                continue;
            }
            int stockNuevo = producto.getStockActual();
            registrarMovimiento(MovimientoInventario.Tipo.SALIDA, producto,
                    entrada.getValue(), stockNuevo + entrada.getValue(), stockNuevo,
                    motivo);
        }
    }

    private void registrarEntradas(Venta venta, String motivo) {
        Map<Integer, Integer> cantidades = agruparCantidades(venta);
        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            Producto producto = gestorProductos.buscar(entrada.getKey());
            if (producto == null || entrada.getValue() <= 0) {
                continue;
            }
            int stockNuevo = producto.getStockActual();
            registrarMovimiento(MovimientoInventario.Tipo.ENTRADA, producto,
                    entrada.getValue(), stockNuevo - entrada.getValue(), stockNuevo,
                    motivo);
        }
    }

    /**
     * Reconstruye el orden lógico de una edición: primero se repone el stock
     * de la venta anterior y después se descuenta el de la venta nueva.
     */
    private void registrarMovimientosEdicion(Venta original, Venta actual) {
        Map<Integer, Integer> cantidadesOriginal = agruparCantidades(original);
        Map<Integer, Integer> cantidadesActual = agruparCantidades(actual);
        Map<Integer, Integer> ids = new TreeMap<>(cantidadesOriginal);
        for (Map.Entry<Integer, Integer> entrada : cantidadesActual.entrySet()) {
            ids.merge(entrada.getKey(), entrada.getValue(), Integer::sum);
        }

        for (Integer idProducto : ids.keySet()) {
            Producto producto = gestorProductos.buscar(idProducto);
            if (producto == null) {
                continue;
            }

            int originalQty = cantidadesOriginal.getOrDefault(idProducto, 0);
            int actualQty = cantidadesActual.getOrDefault(idProducto, 0);
            int stockFinal = producto.getStockActual();

            if (originalQty > 0) {
                int stockAntes = stockFinal + actualQty - originalQty;
                int stockDespues = stockFinal + actualQty;
                registrarMovimiento(MovimientoInventario.Tipo.ENTRADA, producto,
                        originalQty, stockAntes, stockDespues,
                        "Edición de la venta #" + actual.getId()
                        + ": reposición de la venta anterior");
            }
            if (actualQty > 0) {
                int stockAntes = stockFinal + actualQty;
                registrarMovimiento(MovimientoInventario.Tipo.SALIDA, producto,
                        actualQty, stockAntes, stockFinal,
                        "Edición de la venta #" + actual.getId()
                        + ": consumo de la venta actualizada");
            }
        }
    }

    private Map<Integer, Integer> agruparCantidades(Venta venta) {
        Map<Integer, Integer> cantidades = new TreeMap<>();
        if (venta == null) {
            return cantidades;
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            cantidades.merge(detalle.getIdProducto(), detalle.getCantidad(),
                    Integer::sum);
        }
        return cantidades;
    }

    private void registrarMovimiento(MovimientoInventario.Tipo tipo,
            Producto producto, int cantidad, int stockAnterior, int stockNuevo,
            String motivo) {
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
}
