package persistencia;

import gestor.GestorCostos;
import gestor.GestorHistorialProductos;
import gestor.GestorMovimientos;
import gestor.GestorProductos;
import gestor.GestorVentas;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Costo;
import modelo.DetalleVenta;
import modelo.MovimientoInventario;
import modelo.Producto;
import modelo.RegistroProducto;
import modelo.Venta;
import util.Formato;

/**
 * Guarda y recupera la información en archivos de texto dentro de la carpeta
 * "datos".
 */
public class GestorArchivo {

    private static final String ARCHIVO_PRODUCTOS = "productos.txt";
    private static final String ARCHIVO_VENTAS = "ventas.txt";
    private static final String ARCHIVO_COSTOS = "costos.txt";
    private static final String ARCHIVO_HISTORIAL_PRODUCTOS = "historial_productos.txt";
    private static final String ARCHIVO_MOVIMIENTOS = "movimientos.txt";

    private final GestorProductos gestorProductos;
    private final GestorVentas gestorVentas;
    private final GestorCostos gestorCostos;
    private final GestorHistorialProductos gestorHistorialProductos;
    private final GestorMovimientos gestorMovimientos;
    private final Path carpetaDatos;

    public GestorArchivo(GestorProductos gestorProductos, GestorVentas gestorVentas,
            GestorCostos gestorCostos, GestorHistorialProductos gestorHistorialProductos,
            GestorMovimientos gestorMovimientos) {
        this(gestorProductos, gestorVentas, gestorCostos, gestorHistorialProductos,
                gestorMovimientos, Path.of("datos"));
    }

    public GestorArchivo(GestorProductos gestorProductos, GestorVentas gestorVentas,
            GestorCostos gestorCostos, GestorHistorialProductos gestorHistorialProductos,
            GestorMovimientos gestorMovimientos, Path carpetaDatos) {
        this.gestorProductos = gestorProductos;
        this.gestorVentas = gestorVentas;
        this.gestorCostos = gestorCostos;
        this.gestorHistorialProductos = gestorHistorialProductos;
        this.gestorMovimientos = gestorMovimientos;
        this.carpetaDatos = carpetaDatos;
    }

    public void guardarDatos() {
        guardarProductos();
        guardarVentas();
        guardarCostos();
        guardarHistorialProductos();
        guardarMovimientos();
    }

    public void cargarDatos() {
        cargarProductos();
        cargarVentas();
        cargarCostos();
        cargarHistorialProductos();
        cargarMovimientos();

        int maxIdProductoHistorico = 0;
        for (Venta venta : gestorVentas.listar()) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                maxIdProductoHistorico = Math.max(
                        maxIdProductoHistorico, detalle.getIdProducto());
            }
        }
        gestorProductos.asegurarUltimoId(maxIdProductoHistorico);
    }

    public void guardarProductos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_PRODUCTOS);
        try {
            Files.createDirectories(carpetaDatos);
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    archivo, StandardCharsets.UTF_8)) {
                for (Producto producto : gestorProductos.listar()) {
                    escritor.write(producto.aLinea());
                    escritor.newLine();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo guardar el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void guardarVentas() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_VENTAS);
        try {
            Files.createDirectories(carpetaDatos);
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    archivo, StandardCharsets.UTF_8)) {
                for (Venta venta : gestorVentas.listar()) {
                    escritor.write(venta.aLinea());
                    escritor.newLine();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo guardar el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void guardarCostos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_COSTOS);
        try {
            Files.createDirectories(carpetaDatos);
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    archivo, StandardCharsets.UTF_8)) {
                for (Costo costo : gestorCostos.listar()) {
                    escritor.write(costo.aLinea());
                    escritor.newLine();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo guardar el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void guardarHistorialProductos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_HISTORIAL_PRODUCTOS);
        try {
            Files.createDirectories(carpetaDatos);
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    archivo, StandardCharsets.UTF_8)) {
                for (RegistroProducto registro : gestorHistorialProductos.listar()) {
                    escritor.write(registro.aLinea());
                    escritor.newLine();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo guardar el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void guardarMovimientos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_MOVIMIENTOS);
        try {
            Files.createDirectories(carpetaDatos);
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    archivo, StandardCharsets.UTF_8)) {
                for (MovimientoInventario movimiento : gestorMovimientos.listar()) {
                    escritor.write(movimiento.aLinea());
                    escritor.newLine();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo guardar el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void cargarProductos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_PRODUCTOS);
        if (!Files.exists(archivo)) {
            gestorProductos.limpiar();
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            ArrayList<Producto> productos = new ArrayList<>();
            int numeroLinea = 0;

            for (String linea : lineas) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    String[] campos = linea.split("\t", -1);
                    if (campos.length != 6) {
                        throw new IllegalArgumentException(
                                "se esperaban 6 campos y se encontraron " + campos.length);
                    }

                    Producto producto = new Producto(
                            Integer.parseInt(campos[0]),
                            Formato.decodificar(campos[1]),
                            Double.parseDouble(campos[2]),
                            Double.parseDouble(campos[3]),
                            Integer.parseInt(campos[4]),
                            Integer.parseInt(campos[5]));
                    productos.add(producto);
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Error en " + ARCHIVO_PRODUCTOS + ", línea " + numeroLinea
                            + ": " + ex.getMessage(), ex);
                }
            }

            gestorProductos.limpiar();
            for (Producto producto : productos) {
                gestorProductos.agregar(producto);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void cargarVentas() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_VENTAS);
        if (!Files.exists(archivo)) {
            gestorVentas.limpiar();
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            ArrayList<Venta> ventas = new ArrayList<>();
            int numeroLinea = 0;

            for (String linea : lineas) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    ventas.add(leerVenta(linea));
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Error en " + ARCHIVO_VENTAS + ", línea " + numeroLinea
                            + ": " + ex.getMessage(), ex);
                }
            }

            gestorVentas.limpiar();
            for (Venta venta : ventas) {
                gestorVentas.registrar(venta);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void cargarCostos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_COSTOS);
        if (!Files.exists(archivo)) {
            gestorCostos.limpiar();
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            ArrayList<Costo> costos = new ArrayList<>();
            int numeroLinea = 0;

            for (String linea : lineas) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    costos.add(leerCosto(linea));
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Error en " + ARCHIVO_COSTOS + ", línea " + numeroLinea
                            + ": " + ex.getMessage(), ex);
                }
            }

            gestorCostos.limpiar();
            for (Costo costo : costos) {
                gestorCostos.registrar(costo);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void cargarHistorialProductos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_HISTORIAL_PRODUCTOS);
        if (!Files.exists(archivo)) {
            gestorHistorialProductos.limpiar();
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            ArrayList<RegistroProducto> registros = new ArrayList<>();
            int numeroLinea = 0;

            for (String linea : lineas) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    registros.add(leerRegistroProducto(linea));
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Error en " + ARCHIVO_HISTORIAL_PRODUCTOS + ", línea "
                            + numeroLinea + ": " + ex.getMessage(), ex);
                }
            }

            gestorHistorialProductos.limpiar();
            for (RegistroProducto registro : registros) {
                gestorHistorialProductos.registrar(registro);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    public void cargarMovimientos() {
        Path archivo = carpetaDatos.resolve(ARCHIVO_MOVIMIENTOS);
        if (!Files.exists(archivo)) {
            gestorMovimientos.limpiar();
            return;
        }

        try {
            List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
            ArrayList<MovimientoInventario> movimientos = new ArrayList<>();
            int numeroLinea = 0;

            for (String linea : lineas) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    movimientos.add(leerMovimiento(linea));
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Error en " + ARCHIVO_MOVIMIENTOS + ", línea " + numeroLinea
                            + ": " + ex.getMessage(), ex);
                }
            }

            gestorMovimientos.limpiar();
            for (MovimientoInventario movimiento : movimientos) {
                gestorMovimientos.registrar(movimiento);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer el archivo " + archivo.toAbsolutePath() + ".", ex);
        }
    }

    private Venta leerVenta(String linea) {
        String[] campos = linea.split("\t", -1);
        if (campos.length < 3) {
            throw new IllegalArgumentException("faltan campos en la venta");
        }

        int id = Integer.parseInt(campos[0]);
        LocalDate fecha = LocalDate.parse(campos[1]);
        int cantidadDetalles = Integer.parseInt(campos[2]);

        if (cantidadDetalles < 0) {
            throw new IllegalArgumentException("la cantidad de detalles no puede ser negativa");
        }

        int camposEsperados = 3 + cantidadDetalles * 5;
        if (campos.length != camposEsperados) {
            throw new IllegalArgumentException(
                    "se esperaban " + camposEsperados + " campos y se encontraron "
                    + campos.length);
        }

        Venta venta = new Venta(id, fecha);
        int posicion = 3;

        for (int i = 0; i < cantidadDetalles; i++) {
            DetalleVenta detalle = new DetalleVenta(
                    Integer.parseInt(campos[posicion]),
                    Formato.decodificar(campos[posicion + 1]),
                    Integer.parseInt(campos[posicion + 2]),
                    Double.parseDouble(campos[posicion + 3]),
                    Double.parseDouble(campos[posicion + 4]));
            venta.agregarDetalle(detalle);
            posicion += 5;
        }

        return venta;
    }

    private Costo leerCosto(String linea) {
        String[] campos = linea.split("\t", -1);
        if (campos.length != 5) {
            throw new IllegalArgumentException(
                    "se esperaban 5 campos y se encontraron " + campos.length);
        }

        return new Costo(
                Integer.parseInt(campos[0]),
                LocalDate.parse(campos[1]),
                Costo.Tipo.valueOf(campos[2]),
                Formato.decodificar(campos[3]),
                Double.parseDouble(campos[4]));
    }

    private RegistroProducto leerRegistroProducto(String linea) {
        String[] campos = linea.split("\t", -1);
        if (campos.length != 9) {
            throw new IllegalArgumentException(
                    "se esperaban 9 campos y se encontraron " + campos.length);
        }

        return new RegistroProducto(
                Integer.parseInt(campos[0]),
                LocalDateTime.parse(campos[1]),
                RegistroProducto.Evento.valueOf(campos[2]),
                Integer.parseInt(campos[3]),
                Formato.decodificar(campos[4]),
                Double.parseDouble(campos[5]),
                Double.parseDouble(campos[6]),
                Integer.parseInt(campos[7]),
                Formato.decodificar(campos[8]));
    }

    private MovimientoInventario leerMovimiento(String linea) {
        String[] campos = linea.split("\t", -1);
        if (campos.length != 9) {
            throw new IllegalArgumentException(
                    "se esperaban 9 campos y se encontraron " + campos.length);
        }

        return new MovimientoInventario(
                Integer.parseInt(campos[0]),
                LocalDateTime.parse(campos[1]),
                Integer.parseInt(campos[2]),
                Formato.decodificar(campos[3]),
                MovimientoInventario.Tipo.valueOf(campos[4]),
                Integer.parseInt(campos[5]),
                Integer.parseInt(campos[6]),
                Integer.parseInt(campos[7]),
                Formato.decodificar(campos[8]));
    }
}
