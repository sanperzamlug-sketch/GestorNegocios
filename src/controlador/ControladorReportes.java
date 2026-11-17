package controlador;

import gestor.CalculadoraFinanciera;
import gestor.GestorVentas;
import java.util.ArrayList;
import modelo.Venta;

/**
 * Controlador de los reportes financieros.
 */
public class ControladorReportes {

    private final CalculadoraFinanciera calculadora;
    private final GestorVentas gestorVentas;

    public ControladorReportes(CalculadoraFinanciera calculadora, GestorVentas gestorVentas) {
        this.calculadora = calculadora;
        this.gestorVentas = gestorVentas;
    }

    public double totalIngresos() {
        return calculadora.totalIngresos(ventas());
    }

    public double totalCostos() {
        return calculadora.totalCostos(ventas());
    }

    public double utilidad() {
        return calculadora.utilidad(ventas());
    }

    /**
     * Ventas históricas completas. La vista de reportes la usa para construir
     * el gráfico por día y el ranking de productos más vendidos.
     */
    public ArrayList<Venta> listarVentas() {
        return ventas();
    }

    private ArrayList<Venta> ventas() {
        return gestorVentas.listar();
    }
}
