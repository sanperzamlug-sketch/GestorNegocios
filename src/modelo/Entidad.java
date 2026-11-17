package modelo;

/**
 * Clase base para las entidades del sistema.
 */
public abstract class Entidad {

    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
