package gestor;

import java.time.LocalDate;
import java.util.ArrayList;
import modelo.Venta;

/**
 * Administra la colección de ventas.
 */
public class GestorVentas {

    private ArrayList<Venta> ventas;
    private int ultimoId;

    public GestorVentas() {
        ventas = new ArrayList<>();
        ultimoId = 0;
    }

    public void registrar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }

        if (venta.getId() <= 0) {
            ultimoId++;
            venta.setId(ultimoId);
        } else {
            if (buscar(venta.getId()) != null) {
                throw new IllegalArgumentException(
                        "Ya existe una venta con el ID " + venta.getId() + ".");
            }
            ultimoId = Math.max(ultimoId, venta.getId());
        }

        ventas.add(venta);
    }

    public boolean editar(Venta venta) {
        if (venta == null || venta.getId() <= 0) {
            return false;
        }

        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getId() == venta.getId()) {
                ventas.set(i, venta);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getId() == id) {
                ventas.remove(i);
                return true;
            }
        }
        return false;
    }

    public Venta buscar(int id) {
        for (Venta venta : ventas) {
            if (venta.getId() == id) {
                return venta;
            }
        }
        return null;
    }

    public ArrayList<Venta> listar() {
        return new ArrayList<>(ventas);
    }

    public ArrayList<Venta> ventasDelDia() {
        LocalDate hoy = LocalDate.now();
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta venta : ventas) {
            if (hoy.equals(venta.getFecha())) {
                resultado.add(venta);
            }
        }
        return resultado;
    }

    /**
     * Elimina todas las ventas y reinicia el consecutivo. Se usa al cargar
     * datos desde el archivo.
     */
    public void limpiar() {
        ventas.clear();
        ultimoId = 0;
    }
}
