package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.IngresoStock;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.ResumenStock;
import com.mycompany.proyectoprueba.modelo.Stock;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;
import java.util.List;

/**
 * Consultas de stock (solo lectura: el stock cambia únicamente con inventario/MovimientoStock).
 * Todas se limitan a los centros que el usuario puede ver (Permisos).
 */
public interface StockDAO {

    ResumenStock resumen(Permisos p) throws SQLException;

    List<Opcion> centros(Permisos p) throws SQLException;

    /** Almacenes del usuario; idCentro 0 = todos. */
    List<Opcion> almacenes(int idCentro, Permisos p) throws SQLException;

    /** Listado con filtros (0 / "" = sin filtro), primero sin stock y bajo mínimo. */
    List<Stock> listar(int idCentro, int idAlmacen, String situacion, Permisos p) throws SQLException;

    /** Registro de stock o null si no existe o no es de los centros del usuario. */
    Stock buscar(int idStock, Permisos p) throws SQLException;

    /** Compras recibidas de ese insumo en ese almacén, la más reciente primero. */
    List<IngresoStock> ingresos(int idInsumo, int idAlmacen) throws SQLException;

    /** El mismo insumo en los demás almacenes del usuario. */
    List<Stock> otrosAlmacenes(int idInsumo, int idStockExcluido, Permisos p) throws SQLException;

    // Mantenimiento (alta, modificación del mínimo y baja)

    /** Insumos activos, para el alta de un registro de stock. */
    List<Opcion> insumosActivos() throws SQLException;

    /** Almacenes activos de los centros del usuario, para el alta. */
    List<Opcion> almacenesActivos(Permisos p) throws SQLException;

    /** Centro del almacén activo, o null si no existe o está inactivo. */
    Integer centroDeAlmacen(int idAlmacen) throws SQLException;

    boolean insumoActivo(int idInsumo) throws SQLException;

    boolean existe(int idInsumo, int idAlmacen) throws SQLException;

    /** Alta de un registro de stock en 0 con su mínimo. Devuelve el id. */
    int insertar(int idInsumo, int idAlmacen, int minimo) throws SQLException;

    int actualizarMinimo(int idStock, int minimo) throws SQLException;

    /** true si el insumo tuvo movimientos (Kardex) en ese almacén. */
    boolean tieneMovimientos(int idInsumo, int idAlmacen) throws SQLException;

    /** Elimina el registro solo si está en 0 y sin movimientos. Devuelve las filas eliminadas. */
    int eliminarVacio(int idStock) throws SQLException;
}
