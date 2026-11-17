package gestor;

import java.time.LocalDate;
import java.util.ArrayList;
import modelo.MovimientoInventario;

/**
 * Administra el historial de entradas, salidas y ajustes de inventario.
 */
public class GestorMovimientos {

    private ArrayList<MovimientoInventario> movimientos;
    private int ultimoId;

    public GestorMovimientos() {
        movimientos = new ArrayList<>();
        ultimoId = 0;
    }

    public void registrar(MovimientoInventario movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo.");
        }

        if (movimiento.getId() <= 0) {
            ultimoId++;
            movimiento.setId(ultimoId);
        } else {
            if (buscar(movimiento.getId()) != null) {
                throw new IllegalArgumentException(
                        "Ya existe un movimiento con el ID " + movimiento.getId() + ".");
            }
            ultimoId = Math.max(ultimoId, movimiento.getId());
        }

        movimientos.add(movimiento);
    }

    public MovimientoInventario buscar(int id) {
        for (MovimientoInventario movimiento : movimientos) {
            if (movimiento.getId() == id) {
                return movimiento;
            }
        }
        return null;
    }

    public boolean eliminar(int id) {
        for (int i = 0; i < movimientos.size(); i++) {
            if (movimientos.get(i).getId() == id) {
                movimientos.remove(i);
                return true;
            }
        }
        return false;
    }

    public ArrayList<MovimientoInventario> listar() {
        return new ArrayList<>(movimientos);
    }

    public ArrayList<MovimientoInventario> listarPorPeriodo(LocalDate desde,
            LocalDate hasta) {
        ArrayList<MovimientoInventario> resultado = new ArrayList<>();
        for (MovimientoInventario movimiento : movimientos) {
            LocalDate fecha = movimiento.getFechaHora() == null
                    ? null : movimiento.getFechaHora().toLocalDate();
            if (GestorCostos.dentroDelPeriodo(fecha, desde, hasta)) {
                resultado.add(movimiento);
            }
        }
        return resultado;
    }

    public ArrayList<MovimientoInventario> listarPorProducto(int idProducto) {
        ArrayList<MovimientoInventario> resultado = new ArrayList<>();
        for (MovimientoInventario movimiento : movimientos) {
            if (movimiento.getIdProducto() == idProducto) {
                resultado.add(movimiento);
            }
        }
        return resultado;
    }

    public void limpiar() {
        movimientos.clear();
        ultimoId = 0;
    }

    public void asegurarUltimoId(int id) {
        if (id > ultimoId) {
            ultimoId = id;
        }
    }
}
