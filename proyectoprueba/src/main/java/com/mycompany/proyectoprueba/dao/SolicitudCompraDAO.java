package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.BorradorCompra;
import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.InsumoPrecio;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de la solicitud de compra en curso (BORRADOR) de cada usuario. */
public interface SolicitudCompraDAO {

    /** Insumos activos ordenados por descripción, con su precio de compra. */
    List<InsumoPrecio> insumosActivos() throws SQLException;

    /** Precio de un insumo activo o null si no existe o está inactivo. */
    BigDecimal precioInsumoActivo(int idInsumo) throws SQLException;

    boolean proveedorActivo(int idProveedor) throws SQLException;

    /** Cabecera del borrador del usuario o null. */
    BorradorCompra borrador(int idUsuario) throws SQLException;

    /** id del borrador del usuario o 0. */
    int idBorrador(int idUsuario) throws SQLException;

    /** Bloquea el borrador (FOR UPDATE) y devuelve su id y fecha, o null. */
    BorradorCompra bloquearBorrador(int idUsuario) throws SQLException;

    /** Crea el borrador con la fecha de hoy y devuelve su id. */
    int crearBorrador(int idUsuario, int idProveedor, int idCentroDestino, String observaciones) throws SQLException;

    List<DetalleCompra> detalle(int idCompra) throws SQLException;

    int contarLineas(int idCompra) throws SQLException;

    /** Suma cantidad a la línea del insumo si ya existe; devuelve las filas actualizadas. */
    int sumarCantidadInsumo(int idCompra, int idInsumo, int cantidad) throws SQLException;

    void insertarLineaInsumo(int idCompra, int idInsumo, int cantidad, BigDecimal precio) throws SQLException;

    /** Crea el equipo pendiente (SOLICITADO, número provisorio) y devuelve su id. */
    int crearEquipoSolicitado(String serialTemporal, int idInsumoBase) throws SQLException;

    void insertarLineaEquipo(int idCompra, int idEquipo, BigDecimal precio) throws SQLException;

    /** Línea del borrador del usuario (tipo e id de equipo) o null si no es suya. */
    DetalleCompra lineaDelBorrador(int idDetalle, int idUsuario) throws SQLException;

    void eliminarLinea(int idDetalle) throws SQLException;

    /** Borra el equipo solo si sigue SOLICITADO (aún no recibido). */
    void eliminarEquipoSolicitado(int idEquipo) throws SQLException;

    void actualizarCabecera(int idCompra, int idProveedor, int idCentroDestino, String observaciones) throws SQLException;

    /** Siguiente correlativo del día para el número de OC (atómico). */
    int siguienteCorrelativo(Date fecha) throws SQLException;

    void emitir(int idCompra, String numeroOc) throws SQLException;

    /** Equipos pendientes de las líneas del borrador del usuario. */
    List<Integer> equiposDelBorrador(int idUsuario) throws SQLException;

    /** Borra el borrador (el detalle se borra en cascada); devuelve las filas borradas. */
    int eliminarBorrador(int idUsuario) throws SQLException;
}
