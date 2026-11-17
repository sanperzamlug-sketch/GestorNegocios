package principal;

import controlador.ControladorFinanzas;
import controlador.ControladorHistorial;
import controlador.ControladorProductos;
import controlador.ControladorReportes;
import controlador.ControladorVentas;
import gestor.CalculadoraFinanciera;
import gestor.GestorCostos;
import gestor.GestorHistorialProductos;
import gestor.GestorMovimientos;
import gestor.GestorProductos;
import gestor.GestorVentas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import persistencia.GestorArchivo;
import vista.VentanaPrincipal;
import vista.componentes.Dialogo;

/**
 * Punto de entrada de GestorNegocios.
 */
public class Main extends Application {

    @Override
    public void start(Stage escenario) {
        GestorProductos gestorProductos = new GestorProductos();
        GestorVentas gestorVentas = new GestorVentas();
        GestorCostos gestorCostos = new GestorCostos();
        GestorHistorialProductos gestorHistorialProductos = new GestorHistorialProductos();
        GestorMovimientos gestorMovimientos = new GestorMovimientos();
        GestorArchivo gestorArchivo = new GestorArchivo(gestorProductos,
                gestorVentas, gestorCostos, gestorHistorialProductos, gestorMovimientos);

        String errorCarga = null;
        try {
            gestorArchivo.cargarDatos();
        } catch (RuntimeException ex) {
            errorCarga = ex.getMessage();
        }

        CalculadoraFinanciera calculadora = new CalculadoraFinanciera();
        ControladorProductos controladorProductos
                = new ControladorProductos(gestorProductos, gestorHistorialProductos,
                        gestorMovimientos);
        ControladorVentas controladorVentas
                = new ControladorVentas(gestorVentas, gestorProductos,
                        gestorMovimientos);
        ControladorReportes controladorReportes
                = new ControladorReportes(calculadora, gestorVentas);
        ControladorFinanzas controladorFinanzas
                = new ControladorFinanzas(gestorVentas, gestorProductos,
                        gestorCostos, calculadora);
        ControladorHistorial controladorHistorial
                = new ControladorHistorial(gestorVentas, gestorHistorialProductos,
                        gestorMovimientos, gestorCostos);

        VentanaPrincipal ventana = new VentanaPrincipal(
                escenario, controladorProductos, controladorVentas,
                controladorReportes, controladorFinanzas, controladorHistorial,
                gestorArchivo::guardarDatos);

        escenario.setOnCloseRequest(evento -> {
            try {
                gestorArchivo.guardarDatos();
            } catch (RuntimeException ex) {
                boolean cerrar = Dialogo.confirmarPeligro(escenario,
                        "No se pudieron guardar los datos",
                        ex.getMessage() + "\n\nSi cierras ahora, los últimos cambios "
                        + "podrían perderse.",
                        "Cerrar sin guardar");
                if (!cerrar) {
                    evento.consume();
                }
            }
        });

        ventana.mostrar();

        if (errorCarga != null) {
            String detalle = errorCarga;
            Platform.runLater(() -> ventana.mostrarError(
                    "No se pudieron cargar los datos guardados:\n" + detalle));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
