package vista;

import controlador.ControladorFinanzas;
import controlador.ControladorHistorial;
import controlador.ControladorProductos;
import controlador.ControladorReportes;
import controlador.ControladorVentas;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.Formato;
import vista.componentes.Dialogo;
import vista.componentes.Estilos;
import vista.componentes.Icono;
import vista.componentes.Ui;

/**
 * Ventana principal con navegación lateral tipo aplicación de escritorio
 * moderna. Las pantallas conservan los controladores existentes.
 */
public class VentanaPrincipal {

    private enum Seccion {
        INICIO("Panel", "Resumen general",
                "Una vista rápida del estado del negocio", Icono.PANEL),
        PRODUCTOS("Productos", "Productos",
                "Administra el catálogo y el inventario", Icono.CAJA),
        VENTAS("Ventas", "Ventas",
                "Registra y controla las ventas del día", Icono.CARRITO),
        REPORTES("Reportes", "Reportes",
                "Indicadores financieros del negocio", Icono.GRAFICO),
        FINANZAS("Finanzas", "Finanzas",
                "Costos, margen, rentabilidad y punto de equilibrio", Icono.DINERO),
        HISTORIAL("Historial", "Historial",
                "Consulta la evolución de ventas, productos, inventario y costos",
                Icono.RELOJ);

        private final String etiqueta;
        private final String titulo;
        private final String subtitulo;
        private final String icono;

        Seccion(String etiqueta, String titulo, String subtitulo, String icono) {
            this.etiqueta = etiqueta;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.icono = icono;
        }
    }

    private final Stage escenario;
    private final ControladorProductos controladorProductos;
    private final ControladorVentas controladorVentas;
    private final ControladorReportes controladorReportes;
    private final ControladorFinanzas controladorFinanzas;
    private final ControladorHistorial controladorHistorial;
    private final Runnable accionGuardar;

    private final Map<Seccion, ToggleButton> botonesNavegacion
            = new EnumMap<>(Seccion.class);
    private final Map<Seccion, Node> paginas = new EnumMap<>(Seccion.class);
    private final VBox capaNotificaciones = new VBox(10);

    private final Label tituloPagina = new Label();
    private final Label subtituloPagina = new Label();

    private PanelInicio panelInicio;
    private PanelProductos panelProductos;
    private PanelVentas panelVentas;
    private PanelReportes panelReportes;
    private PanelFinanzas panelFinanzas;
    private PanelHistorial panelHistorial;

    private Seccion seccionActual;
    private boolean primeraVista = true;

    public VentanaPrincipal(Stage escenario,
            ControladorProductos controladorProductos,
            ControladorVentas controladorVentas,
            ControladorReportes controladorReportes,
            ControladorFinanzas controladorFinanzas,
            ControladorHistorial controladorHistorial) {
        this(escenario, controladorProductos, controladorVentas,
                controladorReportes, controladorFinanzas, controladorHistorial,
                null);
    }

    public VentanaPrincipal(Stage escenario,
            ControladorProductos controladorProductos,
            ControladorVentas controladorVentas,
            ControladorReportes controladorReportes,
            ControladorFinanzas controladorFinanzas,
            ControladorHistorial controladorHistorial,
            Runnable accionGuardar) {
        this.escenario = escenario;
        this.controladorProductos = controladorProductos;
        this.controladorVentas = controladorVentas;
        this.controladorReportes = controladorReportes;
        this.controladorFinanzas = controladorFinanzas;
        this.controladorHistorial = controladorHistorial;
        this.accionGuardar = accionGuardar;
        construirInterfaz();
        configurarEscenario();
    }

    private void construirInterfaz() {
        panelInicio = new PanelInicio(controladorProductos, controladorVentas,
                controladorReportes, this);
        panelProductos = new PanelProductos(controladorProductos, this);
        panelVentas = new PanelVentas(controladorVentas, this);
        panelReportes = new PanelReportes(controladorReportes, this);
        panelFinanzas = new PanelFinanzas(controladorFinanzas, this);
        panelHistorial = new PanelHistorial(controladorHistorial,
                controladorFinanzas, controladorVentas, this);

        paginas.put(Seccion.INICIO, panelInicio);
        paginas.put(Seccion.PRODUCTOS, panelProductos);
        paginas.put(Seccion.VENTAS, panelVentas);
        paginas.put(Seccion.REPORTES, panelReportes);
        paginas.put(Seccion.FINANZAS, panelFinanzas);
        paginas.put(Seccion.HISTORIAL, panelHistorial);

        StackPane areaPaginas = new StackPane();
        areaPaginas.getStyleClass().add("content-area");
        for (Map.Entry<Seccion, Node> entrada : paginas.entrySet()) {
            Node pagina = entrada.getValue();
            if (pagina instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            pagina.setVisible(false);
            pagina.setManaged(false);
            areaPaginas.getChildren().add(pagina);
        }

        BorderPane contenedor = new BorderPane();
        contenedor.setTop(crearBarraSuperior());
        contenedor.setCenter(areaPaginas);

        BorderPane shell = new BorderPane();
        shell.setLeft(crearBarraLateral());
        shell.setCenter(contenedor);

        StackPane raiz = new StackPane(shell);

        capaNotificaciones.setAlignment(Pos.BOTTOM_RIGHT);
        capaNotificaciones.setPadding(new Insets(0, 26, 24, 0));
        capaNotificaciones.setMouseTransparent(true);
        capaNotificaciones.setPickOnBounds(false);
        StackPane.setAlignment(capaNotificaciones, Pos.BOTTOM_RIGHT);
        raiz.getChildren().add(capaNotificaciones);

        Scene escena = new Scene(raiz);
        Estilos.aplicar(escena);
        configurarAtajos(escena);
        escenario.setScene(escena);

        mostrarSeccion(Seccion.INICIO);
        botonesNavegacion.get(Seccion.INICIO).setSelected(true);
    }

    private Region crearBarraLateral() {
        VBox barra = new VBox(6);
        barra.getStyleClass().add("sidebar");

        StackPane logo = new StackPane(new Label("GN"));
        logo.getStyleClass().add("logo");
        ((Label) logo.getChildren().get(0)).getStyleClass().add("logo-texto");

        VBox marcaTextos = new VBox(1,
                Ui.etiqueta("GestorNegocios", "marca-nombre"),
                Ui.etiqueta("Gestión comercial", "marca-detalle"));

        HBox marca = new HBox(11, logo, marcaTextos);
        marca.setAlignment(Pos.CENTER_LEFT);
        marca.getStyleClass().add("marca");

        Label seccion = Ui.etiqueta("MENÚ PRINCIPAL", "nav-seccion");

        ToggleGroup grupo = new ToggleGroup();
        VBox navegacion = new VBox(5);
        int indice = 1;
        for (Seccion valor : Seccion.values()) {
            ToggleButton boton = new ToggleButton(valor.etiqueta,
                    Icono.crear(valor.icono, 18));
            boton.setToggleGroup(grupo);
            boton.setUserData(valor);
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.CENTER_LEFT);
            boton.getStyleClass().add("nav-button");
            boton.setTooltip(new Tooltip(valor.etiqueta
                    + "  (Ctrl+" + indice + ")"));
            botonesNavegacion.put(valor, boton);
            navegacion.getChildren().add(boton);
            indice++;
        }

        grupo.selectedToggleProperty().addListener((observable, anterior, actual) -> {
            if (actual == null) {
                if (anterior != null) {
                    anterior.setSelected(true);
                }
                return;
            }
            irA((Seccion) actual.getUserData());
        });

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);

        HBox estadoTitulo = new HBox(8,
                crearPuntoEstado(),
                Ui.etiqueta("Datos locales", "sidebar-tarjeta-titulo"));
        estadoTitulo.setAlignment(Pos.CENTER_LEFT);

        Label notaGuardado = Ui.etiqueta(
                "Guardado automático al cerrar.", "sidebar-tarjeta-texto");
        notaGuardado.setWrapText(true);

        VBox tarjetaEstado = new VBox(5, estadoTitulo, notaGuardado);
        tarjetaEstado.getStyleClass().add("sidebar-tarjeta");

        Label version = Ui.etiqueta("Versión 2.0 · JavaFX", "sidebar-version");

        barra.getChildren().addAll(marca, seccion, navegacion, espacio,
                tarjetaEstado, version);
        return barra;
    }

    private Region crearPuntoEstado() {
        Region punto = new Region();
        punto.getStyleClass().add("estado-dot");
        return punto;
    }

    private Region crearBarraSuperior() {
        tituloPagina.getStyleClass().add("page-title");
        subtituloPagina.getStyleClass().add("page-subtitle");
        VBox textos = new VBox(3, tituloPagina, subtituloPagina);

        HBox fecha = new HBox(8,
                Icono.crear(Icono.CALENDARIO, 15),
                Ui.etiqueta(Formato.fechaLarga(LocalDate.now()), "fecha-texto"));
        fecha.setAlignment(Pos.CENTER);
        fecha.getStyleClass().add("date-chip");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(12, textos, espacio, fecha);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("topbar");
        return barra;
    }

    private void configurarEscenario() {
        escenario.setTitle("GestorNegocios · Gestión comercial");
        escenario.setMinWidth(1150);
        escenario.setMinHeight(680);

        Rectangle2D limites = Screen.getPrimary().getVisualBounds();
        escenario.setWidth(Math.min(1320, Math.max(1150, limites.getWidth() * 0.92)));
        escenario.setHeight(Math.min(850, Math.max(680, limites.getHeight() * 0.92)));
        escenario.centerOnScreen();

        Image icono = crearIconoAplicacion();
        if (icono != null) {
            escenario.getIcons().add(icono);
        }
    }

    private Image crearIconoAplicacion() {
        Canvas lienzo = new Canvas(64, 64);
        GraphicsContext grafico = lienzo.getGraphicsContext2D();
        LinearGradient degradado = new LinearGradient(0, 0, 1, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#818cf8")),
                new Stop(1, Color.web("#4338ca")));
        grafico.setFill(degradado);
        grafico.fillRoundRect(0, 0, 64, 64, 18, 18);
        grafico.setFill(Color.WHITE);
        grafico.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        grafico.fillText("S", 21, 43);

        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return lienzo.snapshot(parametros, null);
    }

    private void configurarAtajos(Scene escena) {
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN),
                () -> irA(Seccion.INICIO));
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN),
                () -> irA(Seccion.PRODUCTOS));
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN),
                () -> irA(Seccion.VENTAS));
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN),
                () -> irA(Seccion.REPORTES));
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN),
                () -> irA(Seccion.FINANZAS));
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.CONTROL_DOWN),
                () -> irA(Seccion.HISTORIAL));
        escena.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F5),
                () -> {
                    actualizarVista();
                    mostrarMensaje("La información se actualizó correctamente.");
                });
    }

    public void mostrar() {
        escenario.show();
    }

    public Stage getEscenario() {
        return escenario;
    }

    public void irAInicio() {
        irA(Seccion.INICIO);
    }

    public void irAProductos() {
        irA(Seccion.PRODUCTOS);
    }

    public void irAVentas() {
        irA(Seccion.VENTAS);
    }

    public void irAReportes() {
        irA(Seccion.REPORTES);
    }

    public void irAFinanzas() {
        irA(Seccion.FINANZAS);
    }

    public void irAHistorial() {
        irA(Seccion.HISTORIAL);
    }

    private void irA(Seccion seccion) {
        ToggleButton boton = botonesNavegacion.get(seccion);
        if (boton != null && !boton.isSelected()) {
            boton.setSelected(true);
            return;
        }
        mostrarSeccion(seccion);
    }

    private void mostrarSeccion(Seccion seccion) {
        if (seccion == null) {
            return;
        }

        boolean cambio = seccionActual != seccion;
        seccionActual = seccion;

        for (Map.Entry<Seccion, Node> entrada : paginas.entrySet()) {
            boolean activa = entrada.getKey() == seccion;
            Node pagina = entrada.getValue();
            pagina.setVisible(activa);
            pagina.setManaged(activa);
        }

        tituloPagina.setText(seccion.titulo);
        subtituloPagina.setText(seccion.subtitulo);

        Node pagina = paginas.get(seccion);
        if (pagina != null && cambio) {
            actualizarSeccion(seccion);
            if (!primeraVista) {
                pagina.setOpacity(0);
                FadeTransition fundido = new FadeTransition(Duration.millis(170), pagina);
                fundido.setFromValue(0);
                fundido.setToValue(1);
                fundido.play();
            }
            primeraVista = false;
        }
    }

    private void actualizarSeccion(Seccion seccion) {
        switch (seccion) {
            case INICIO -> panelInicio.actualizar();
            case PRODUCTOS -> panelProductos.actualizar();
            case VENTAS -> panelVentas.actualizar();
            case REPORTES -> panelReportes.actualizar();
            case FINANZAS -> panelFinanzas.actualizar();
            case HISTORIAL -> panelHistorial.actualizar();
        }
    }

    /**
     * Refresca todas las pantallas despues de una operacion sobre los datos.
     */
    public void actualizarVista() {
        panelInicio.actualizar();
        panelProductos.actualizar();
        panelVentas.actualizar();
        panelReportes.actualizar();
        panelFinanzas.actualizar();
        panelHistorial.actualizar();
    }

    public void mostrarMensaje(String mensaje) {
        Platform.runLater(() -> mostrarNotificacion(mensaje));
    }

    public void mostrarError(String mensaje) {
        Dialogo.error(escenario, "No se pudo completar la acción", mensaje);
    }

    /**
     * Guarda los cambios en disco cuando la ventana tiene configurada una
     * acción de persistencia.
     */
    public void guardarDatos() {
        if (accionGuardar != null) {
            accionGuardar.run();
        }
    }

    public boolean confirmar(String mensaje) {
        return Dialogo.confirmar(escenario, "Confirmar acción", mensaje,
                "Sí, continuar");
    }

    public boolean confirmarPeligro(String mensaje) {
        return Dialogo.confirmarPeligro(escenario, "Confirmar eliminación", mensaje,
                "Sí, eliminar");
    }

    public void nuevaVentaRapida() {
        irAVentas();
        panelVentas.nuevaVenta();
    }

    public void nuevoProductoRapido() {
        irAProductos();
        panelProductos.nuevoProducto();
    }

    private void mostrarNotificacion(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return;
        }

        Label texto = new Label(mensaje);
        texto.setWrapText(true);
        texto.setMaxWidth(370);

        StackPane icono = new StackPane(Icono.crear(Icono.CHECK, 15));
        icono.getStyleClass().add("toast-icono");

        HBox notificacion = new HBox(10, icono, texto);
        notificacion.setAlignment(Pos.CENTER_LEFT);
        notificacion.getStyleClass().add("toast");
        notificacion.setOpacity(0);
        notificacion.setTranslateY(14);

        while (capaNotificaciones.getChildren().size() >= 3) {
            capaNotificaciones.getChildren().remove(0);
        }
        capaNotificaciones.getChildren().add(notificacion);

        FadeTransition aparecer = new FadeTransition(Duration.millis(180), notificacion);
        aparecer.setFromValue(0);
        aparecer.setToValue(1);

        TranslateTransition subir = new TranslateTransition(
                Duration.millis(220), notificacion);
        subir.setFromY(14);
        subir.setToY(0);

        ParallelTransition entrada = new ParallelTransition(aparecer, subir);
        entrada.play();

        PauseTransition espera = new PauseTransition(Duration.seconds(3.5));
        espera.setOnFinished(evento -> {
            FadeTransition desaparecer = new FadeTransition(
                    Duration.millis(220), notificacion);
            desaparecer.setFromValue(1);
            desaparecer.setToValue(0);
            desaparecer.setOnFinished(fin -> capaNotificaciones.getChildren()
                    .remove(notificacion));
            desaparecer.play();
        });
        espera.play();
    }
}
