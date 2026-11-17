package gestor;

import java.util.ArrayList;
import modelo.Producto;

/**
 * Administra la colección de productos.
 */
public class GestorProductos {

    private ArrayList<Producto> productos;
    private int ultimoId;

    public GestorProductos() {
        productos = new ArrayList<>();
        ultimoId = 0;
    }

    public void agregar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }

        if (producto.getId() <= 0) {
            ultimoId++;
            producto.setId(ultimoId);
        } else {
            if (buscar(producto.getId()) != null) {
                throw new IllegalArgumentException(
                        "Ya existe un producto con el ID " + producto.getId() + ".");
            }
            ultimoId = Math.max(ultimoId, producto.getId());
        }

        productos.add(producto);
    }

    public boolean editar(Producto producto) {
        if (producto == null || producto.getId() <= 0) {
            return false;
        }

        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == producto.getId()) {
                productos.set(i, producto);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == id) {
                productos.remove(i);
                return true;
            }
        }
        return false;
    }

    public Producto buscar(int id) {
        for (Producto producto : productos) {
            if (producto.getId() == id) {
                return producto;
            }
        }
        return null;
    }

    public ArrayList<Producto> listar() {
        return new ArrayList<>(productos);
    }

    public ArrayList<Producto> stockBajo() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.tieneStockBajo()) {
                resultado.add(producto);
            }
        }
        return resultado;
    }

    public boolean existeNombre(String nombre) {
        return existeNombre(nombre, 0);
    }

    /**
     * Verifica si existe otro producto con el mismo nombre, excluyendo el ID
     * indicado. Se usa al editar para permitir conservar el mismo nombre.
     */
    public boolean existeNombre(String nombre, int idExcluir) {
        if (nombre == null) {
            return false;
        }
        String buscado = nombre.trim();
        for (Producto producto : productos) {
            if (producto.getId() != idExcluir
                    && producto.getNombre().equalsIgnoreCase(buscado)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina todos los productos y reinicia el consecutivo. Se usa al cargar
     * datos desde el archivo.
     */
    public void limpiar() {
        productos.clear();
        ultimoId = 0;
    }

    /**
     * Garantiza que los productos nuevos no reutilicen un ID que ya aparece
     * en el histórico de ventas.
     */
    public void asegurarUltimoId(int id) {
        if (id > ultimoId) {
            ultimoId = id;
        }
    }
}
