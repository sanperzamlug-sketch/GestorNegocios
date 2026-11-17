package gestor;

import java.time.LocalDate;
import java.util.ArrayList;
import modelo.RegistroProducto;

/**
 * Administra el historial de cambios del catálogo de productos.
 */
public class GestorHistorialProductos {

    private ArrayList<RegistroProducto> registros;
    private int ultimoId;

    public GestorHistorialProductos() {
        registros = new ArrayList<>();
        ultimoId = 0;
    }

    public void registrar(RegistroProducto registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo.");
        }

        if (registro.getId() <= 0) {
            ultimoId++;
            registro.setId(ultimoId);
        } else {
            if (buscar(registro.getId()) != null) {
                throw new IllegalArgumentException(
                        "Ya existe un registro con el ID " + registro.getId() + ".");
            }
            ultimoId = Math.max(ultimoId, registro.getId());
        }

        registros.add(registro);
    }

    public RegistroProducto buscar(int id) {
        for (RegistroProducto registro : registros) {
            if (registro.getId() == id) {
                return registro;
            }
        }
        return null;
    }

    public boolean eliminar(int id) {
        for (int i = 0; i < registros.size(); i++) {
            if (registros.get(i).getId() == id) {
                registros.remove(i);
                return true;
            }
        }
        return false;
    }

    public ArrayList<RegistroProducto> listar() {
        return new ArrayList<>(registros);
    }

    public ArrayList<RegistroProducto> listarPorPeriodo(LocalDate desde,
            LocalDate hasta) {
        ArrayList<RegistroProducto> resultado = new ArrayList<>();
        for (RegistroProducto registro : registros) {
            LocalDate fecha = registro.getFechaHora() == null
                    ? null : registro.getFechaHora().toLocalDate();
            if (GestorCostos.dentroDelPeriodo(fecha, desde, hasta)) {
                resultado.add(registro);
            }
        }
        return resultado;
    }

    public void limpiar() {
        registros.clear();
        ultimoId = 0;
    }

    public void asegurarUltimoId(int id) {
        if (id > ultimoId) {
            ultimoId = id;
        }
    }
}
