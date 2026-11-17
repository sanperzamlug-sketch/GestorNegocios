package gestor;

import java.util.ArrayList;
import modelo.Costo;
import modelo.Producto;
import modelo.Venta;

/**
 * Calcula los indicadores financieros del negocio.
 */
public class CalculadoraFinanciera {

    public double totalIngresos(ArrayList<Venta> ventas) {
        double total = 0.0;
        if (ventas != null) {
            for (Venta venta : ventas) {
                if (venta != null) {
                    total += venta.calcularTotal();
                }
            }
        }
        return total;
    }

    public double totalCostos(ArrayList<Venta> ventas) {
        double total = 0.0;
        if (ventas != null) {
            for (Venta venta : ventas) {
                if (venta != null) {
                    total += venta.calcularCostoTotal();
                }
            }
        }
        return total;
    }

    /**
     * Utilidad bruta: ingresos menos el costo de la mercancía vendida.
     */
    public double utilidad(ArrayList<Venta> ventas) {
        return totalIngresos(ventas) - totalCostos(ventas);
    }

    public double totalCostosFijos(ArrayList<Costo> costos) {
        return totalCostosPorTipo(costos, Costo.Tipo.FIJO);
    }

    public double totalCostosVariables(ArrayList<Costo> costos) {
        return totalCostosPorTipo(costos, Costo.Tipo.VARIABLE);
    }

    public double totalCostosOperativos(ArrayList<Costo> costos) {
        return totalCostosFijos(costos) + totalCostosVariables(costos);
    }

    public double utilidadNeta(ArrayList<Venta> ventas, ArrayList<Costo> costos) {
        return utilidad(ventas) - totalCostosOperativos(costos);
    }

    public double rentabilidadGeneral(ArrayList<Venta> ventas,
            ArrayList<Costo> costos) {
        double ingresos = totalIngresos(ventas);
        if (ingresos <= 0) {
            return 0.0;
        }
        return (utilidadNeta(ventas, costos) / ingresos) * 100.0;
    }

    /**
     * Porcentaje de cada peso vendido que queda disponible para cubrir los
     * costos fijos después de descontar el costo de ventas y los costos
     * variables.
     */
    public double margenContribucion(ArrayList<Venta> ventas,
            ArrayList<Costo> costos) {
        double ingresos = totalIngresos(ventas);
        if (ingresos <= 0) {
            return 0.0;
        }
        double margen = ingresos - totalCostos(ventas) - totalCostosVariables(costos);
        return margen / ingresos;
    }

    public boolean puntoEquilibrioCalculable(ArrayList<Venta> ventas,
            ArrayList<Costo> costos) {
        return totalIngresos(ventas) > 0 && margenContribucion(ventas, costos) > 0;
    }

    /**
     * Ventas necesarias para cubrir los costos fijos, expresadas en dinero.
     */
    public double puntoEquilibrio(ArrayList<Venta> ventas,
            ArrayList<Costo> costos) {
        if (!puntoEquilibrioCalculable(ventas, costos)) {
            return 0.0;
        }
        return totalCostosFijos(costos) / margenContribucion(ventas, costos);
    }

    public double margenProducto(Producto producto) {
        return producto == null ? 0.0 : producto.calcularMargen();
    }

    /**
     * Rentabilidad sobre el costo de adquisición del producto.
     */
    public double rentabilidadProducto(Producto producto) {
        if (producto == null || producto.getCosto() <= 0) {
            return 0.0;
        }
        return ((producto.getPrecioVenta() - producto.getCosto())
                / producto.getCosto()) * 100.0;
    }

    private double totalCostosPorTipo(ArrayList<Costo> costos, Costo.Tipo tipo) {
        double total = 0.0;
        if (costos != null) {
            for (Costo costo : costos) {
                if (costo != null && costo.getTipo() == tipo) {
                    total += costo.getMonto();
                }
            }
        }
        return total;
    }
}
