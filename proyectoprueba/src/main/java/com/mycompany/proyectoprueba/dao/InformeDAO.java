package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.FiltrosInforme;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Consultas de los informes. Los registros vuelven como mapas columna -> valor de la base
 * (Integer, Long, String, BigDecimal, Timestamp, Date o Boolean; los NULL como null, salvo
 * los enteros que se leen como 0), limitados a Informe.LIMITE + 1 filas. Los filtros con
 * lista de valores (estado, tipo...) llegan ya validados ("" = sin filtro).
 */
public interface InformeDAO {

    // Combos
    List<Opcion> centros(Permisos p) throws SQLException;

    /** "Apellido, Nombre" de todos los usuarios. */
    List<Opcion> usuarios() throws SQLException;

    List<Opcion> proveedores() throws SQLException;

    /** "CODIGO - Descripción" de todos los insumos. */
    List<Opcion> insumos() throws SQLException;

    /** "Almacén (Centro)" de los centros del usuario. */
    List<Opcion> almacenes(Permisos p) throws SQLException;

    /** Centros de costo de un centro ("CODIGO - Descripción"). */
    List<Opcion> centrosCosto(int idCentro) throws SQLException;

    // Informes
    List<Map<String, Object>> compras(FiltrosInforme f, String estado, String tipoItem, Permisos p) throws SQLException;

    List<Map<String, Object>> ordenesTrabajo(FiltrosInforme f, String estado, Permisos p) throws SQLException;

    List<Map<String, Object>> movimientosStock(FiltrosInforme f, String tipoMovimiento, Permisos p) throws SQLException;

    List<Map<String, Object>> movimientosActivos(FiltrosInforme f, String tipoMovimiento, String estadoActivo, Permisos p) throws SQLException;

    // Nombres de los filtros elegidos ("" si no existe)
    String nombreCentro(int id) throws SQLException;

    String nombreCentroCosto(int id) throws SQLException;

    String nombreUsuario(int id) throws SQLException;

    String nombreProveedor(int id) throws SQLException;

    String nombreInsumo(int id) throws SQLException;

    String nombreAlmacen(int id) throws SQLException;

    // Fichas (null si no existe)
    Map<String, Object> compra(int id) throws SQLException;

    List<Map<String, Object>> lineasCompra(int id) throws SQLException;

    Map<String, Object> ordenTrabajo(int id) throws SQLException;

    /** Tablas de la ficha de la OT, en texto: insumos suministrados, remisiones e historial. */
    List<String[]> insumosOT(int id) throws SQLException;

    List<String[]> remisionesOT(int id) throws SQLException;

    List<String[]> historialOT(int id) throws SQLException;

    Map<String, Object> movimientoStock(int id) throws SQLException;

    Map<String, Object> movimientoActivo(int id) throws SQLException;
}
