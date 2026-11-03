package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.StockDAO;
import com.mycompany.proyectoprueba.dao.jdbc.StockDAOJdbc;
import com.mycompany.proyectoprueba.modelo.IngresoStock;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.ResumenStock;
import com.mycompany.proyectoprueba.modelo.Stock;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Stock por almacén: consulta y mantenimiento del registro (alta con su mínimo, modificación
 * del stock mínimo y baja de un registro vacío). La cantidad en stock NUNCA se edita a mano:
 * solo cambia con recepciones de OC, salidas, ajustes y transferencias (inventario/MovimientoStock).
 */
public class StockServicio {

    private final StockDAO dao;
    private final Permisos permisos;

    public StockServicio(StockDAO dao, Permisos permisos) {
        this.dao = dao;
        this.permisos = permisos;
    }

    public static StockServicio con(Connection conn, Permisos permisos) {
        return new StockServicio(new StockDAOJdbc(conn), permisos);
    }

    public ResumenStock resumen() throws SQLException {
        return dao.resumen(permisos);
    }

    public List<Opcion> centros() throws SQLException {
        return dao.centros(permisos);
    }

    public List<Opcion> almacenes(int idCentro) throws SQLException {
        return dao.almacenes(idCentro, permisos);
    }

    /** situacion: SIN_STOCK, BAJO_MINIMO, NORMAL; cualquier otro valor = todas. */
    public List<Stock> listar(int idCentro, int idAlmacen, String situacion) throws SQLException {
        if (!"SIN_STOCK".equals(situacion) && !"BAJO_MINIMO".equals(situacion) && !"NORMAL".equals(situacion)) {
            situacion = "";
        }
        return dao.listar(idCentro, idAlmacen, situacion, permisos);
    }

    public Stock buscar(int idStock) throws SQLException {
        return dao.buscar(idStock, permisos);
    }

    public List<IngresoStock> ingresos(Stock s) throws SQLException {
        return dao.ingresos(s.getIdInsumo(), s.getIdAlmacen());
    }

    public List<Stock> otrosAlmacenes(Stock s) throws SQLException {
        return dao.otrosAlmacenes(s.getIdInsumo(), s.getId(), permisos);
    }

    // Mantenimiento

    public List<Opcion> insumosParaAlta() throws SQLException {
        return dao.insumosActivos();
    }

    public List<Opcion> almacenesParaAlta() throws SQLException {
        return dao.almacenesActivos(permisos);
    }

    /** Alta: el registro nace con stock 0 y el mínimo indicado. Devuelve el id creado. */
    public int crear(int idInsumo, int idAlmacen, String minimoTexto) throws SQLException {
        int minimo = minimo(minimoTexto);
        if (idInsumo <= 0) {
            throw new ReglaNegocioException("Seleccione un insumo");
        }
        if (idAlmacen <= 0) {
            throw new ReglaNegocioException("Seleccione un almacén");
        }
        if (!dao.insumoActivo(idInsumo)) {
            throw new ReglaNegocioException("El insumo no existe o está inactivo");
        }
        Integer centro = dao.centroDeAlmacen(idAlmacen);
        if (centro == null) {
            throw new ReglaNegocioException("El almacén no existe o está inactivo");
        }
        permisos.exigirCentro(centro.intValue());
        if (dao.existe(idInsumo, idAlmacen)) {
            throw new ReglaNegocioException("Ese insumo ya tiene un registro de stock en ese almacén. Use Modificar mínimo.");
        }
        try {
            return dao.insertar(idInsumo, idAlmacen, minimo);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // otro usuario lo creó al mismo tiempo
                throw new ReglaNegocioException("Ese insumo ya tiene un registro de stock en ese almacén.");
            }
            throw e;
        }
    }

    public void modificarMinimo(int idStock, String minimoTexto) throws SQLException {
        int minimo = minimo(minimoTexto);
        visible(idStock);
        if (dao.actualizarMinimo(idStock, minimo) == 0) {
            throw new ReglaNegocioException("Registro de stock no encontrado");
        }
    }

    /** Baja: solo si el registro está en 0 y nunca tuvo movimientos (no se pierde historia). */
    public void eliminar(int idStock) throws SQLException {
        Stock s = visible(idStock);
        if (s.getActual() != 0) {
            throw new ReglaNegocioException("No se puede eliminar: el registro tiene " + s.getActual()
                    + " unidades en stock. Primero debe quedar en 0 con una salida, transferencia o ajuste.");
        }
        if (dao.tieneMovimientos(s.getIdInsumo(), s.getIdAlmacen())) {
            throw new ReglaNegocioException("No se puede eliminar: el insumo tiene movimientos en este almacén (Kardex). "
                    + "Puede dejar el mínimo en 0 si ya no se usa.");
        }
        if (dao.eliminarVacio(idStock) == 0) {
            throw new ReglaNegocioException("No se pudo eliminar: el registro cambió mientras tanto. Actualice la pantalla.");
        }
    }

    private Stock visible(int idStock) throws SQLException {
        Stock s = idStock > 0 ? dao.buscar(idStock, permisos) : null;
        if (s == null) {
            throw new ReglaNegocioException("Registro de stock no encontrado o de un centro que no tiene asignado");
        }
        return s;
    }

    private static int minimo(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (!t.matches("[0-9]{1,7}")) {
            throw new ReglaNegocioException("El stock mínimo debe ser un número entero mayor o igual a 0");
        }
        return Integer.parseInt(t);
    }
}
