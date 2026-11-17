package gestor;

import java.time.LocalDate;
import java.util.ArrayList;
import modelo.Costo;

/**
 * Administra los costos fijos y variables del negocio.
 */
public class GestorCostos {

    private ArrayList<Costo> costos;
    private int ultimoId;

    public GestorCostos() {
        costos = new ArrayList<>();
        ultimoId = 0;
    }

    public void registrar(Costo costo) {
        if (costo == null) {
            throw new IllegalArgumentException("El costo no puede ser nulo.");
        }

        if (costo.getId() <= 0) {
            ultimoId++;
            costo.setId(ultimoId);
        } else {
            if (buscar(costo.getId()) != null) {
                throw new IllegalArgumentException(
                        "Ya existe un costo con el ID " + costo.getId() + ".");
            }
            ultimoId = Math.max(ultimoId, costo.getId());
        }

        costos.add(costo);
    }

    public boolean editar(Costo costo) {
        if (costo == null || costo.getId() <= 0) {
            return false;
        }

        for (int i = 0; i < costos.size(); i++) {
            if (costos.get(i).getId() == costo.getId()) {
                costos.set(i, costo);
                return true;
            }
        }
        return false;
    }

    public boolean eliminar(int id) {
        for (int i = 0; i < costos.size(); i++) {
            if (costos.get(i).getId() == id) {
                costos.remove(i);
                return true;
            }
        }
        return false;
    }

    public Costo buscar(int id) {
        for (Costo costo : costos) {
            if (costo.getId() == id) {
                return costo;
            }
        }
        return null;
    }

    public ArrayList<Costo> listar() {
        return new ArrayList<>(costos);
    }

    public ArrayList<Costo> listarPorPeriodo(LocalDate desde, LocalDate hasta) {
        ArrayList<Costo> resultado = new ArrayList<>();
        for (Costo costo : costos) {
            if (dentroDelPeriodo(costo.getFecha(), desde, hasta)) {
                resultado.add(costo);
            }
        }
        return resultado;
    }

    public double totalPorTipo(Costo.Tipo tipo, LocalDate desde, LocalDate hasta) {
        double total = 0.0;
        for (Costo costo : costos) {
            if (costo.getTipo() == tipo
                    && dentroDelPeriodo(costo.getFecha(), desde, hasta)) {
                total += costo.getMonto();
            }
        }
        return total;
    }

    public double totalPorTipo(Costo.Tipo tipo) {
        return totalPorTipo(tipo, null, null);
    }

    public double total(LocalDate desde, LocalDate hasta) {
        double total = 0.0;
        for (Costo costo : costos) {
            if (dentroDelPeriodo(costo.getFecha(), desde, hasta)) {
                total += costo.getMonto();
            }
        }
        return total;
    }

    /**
     * Indica si una fecha está dentro del rango. Un límite nulo significa que
     * no se aplica esa restricción.
     */
    public static boolean dentroDelPeriodo(LocalDate fecha, LocalDate desde,
            LocalDate hasta) {
        if (fecha == null) {
            return false;
        }
        if (desde != null && fecha.isBefore(desde)) {
            return false;
        }
        return hasta == null || !fecha.isAfter(hasta);
    }

    public void limpiar() {
        costos.clear();
        ultimoId = 0;
    }

    public void asegurarUltimoId(int id) {
        if (id > ultimoId) {
            ultimoId = id;
        }
    }
}
