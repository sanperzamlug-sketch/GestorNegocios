package vista.componentes;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * Iconos vectoriales ligeros basados en trazos. Se mantienen en una sola clase
 * para no depender de librerías externas ni de archivos de imagen.
 */
public final class Icono {

    public static final String PANEL = "M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z";
    public static final String CAJA = "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z M3.27 6.96L12 12.01l8.73-5.05 M12 22.08V12";
    public static final String CARRITO = "M9 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M20 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6";
    public static final String GRAFICO = "M18 20V10 M12 20V4 M6 20v-6";
    public static final String TENDENCIA = "M23 6l-9.5 9.5-5-5L1 18 M17 6h6v6";
    public static final String MAS = "M12 5v14 M5 12h14";
    public static final String EDITAR = "M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7 M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z";
    public static final String ELIMINAR = "M3 6h18 M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6 M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2 M10 11v6 M14 11v6";
    public static final String GUARDAR = "M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z M17 21v-8H7v8 M7 3v5h8";
    public static final String ACTUALIZAR = "M23 4v6h-6 M1 20v-6h6 M3.51 9a9 9 0 0 1 14.85-3.36L23 10 M1 14l4.64 4.36A9 9 0 0 0 20.49 15";
    public static final String BUSCAR = "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14z M21 21l-4.35-4.35";
    public static final String ALERTA = "M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z M12 9v4 M12 17h.01";
    public static final String DINERO = "M12 1v22 M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6";
    public static final String CALENDARIO = "M8 2v4 M16 2v4 M3 10h18 M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z";
    public static final String CERRAR = "M18 6L6 18 M6 6l12 12";
    public static final String CHECK = "M22 11.08V12a10 10 0 1 1-5.93-9.14 M22 4L12 14.01l-3-3";
    public static final String INFO = "M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 16v-4 M12 8h.01";
    public static final String FLECHA = "M5 12h14 M12 5l7 7-7 7";
    public static final String RECIBO = "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8";
    public static final String USUARIO = "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z";
    public static final String RELOJ = "M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 6v6l4 2";

    private Icono() {
    }

    /**
     * Crea un icono de tamanio fijo. El color del trazo se define en la hoja
     * de estilos mediante la clase CSS "icono".
     */
    public static StackPane crear(String contenido, double tamanio) {
        SVGPath trazo = new SVGPath();
        trazo.setContent(contenido);
        trazo.getStyleClass().add("icono");

        double escala = tamanio / 24.0;
        trazo.setScaleX(escala);
        trazo.setScaleY(escala);

        StackPane contenedor = new StackPane(trazo);
        contenedor.setPrefSize(tamanio, tamanio);
        contenedor.setMinSize(tamanio, tamanio);
        contenedor.setMaxSize(tamanio, tamanio);
        contenedor.setMouseTransparent(true);
        return contenedor;
    }
}
