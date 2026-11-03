package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.EquipoEstado;
import com.mycompany.proyectoprueba.modelo.LineaOC;
import com.mycompany.proyectoprueba.modelo.LineaRecepcion;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenCompra;
import com.mycompany.proyectoprueba.modelo.RecepcionOC;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/** Acceso a datos de las órdenes de compra y sus recepciones. */
public interface OrdenCompraDAO {

    // Consultas

    /** OC emitidas (con número) de los centros del usuario, la más reciente primero, con su total. */
    List<OrdenCompra> listar(Permisos p) throws SQLException;

    /** Cabecera (proveedor, centro, observaciones, estado) si la OC es de los centros del usuario, o null. */
    OrdenCompra cabecera(int idCompra, Permisos p) throws SQLException;

    boolean visible(int idCompra, Permisos p) throws SQLException;

    /** Ficha completa (proveedor, centro, solicitante, comprador) o null. */
    OrdenCompra ficha(int idCompra) throws SQLException;

    /** Líneas para la ficha (con número de activo y recibido). */
    List<DetalleCompra> lineasFicha(int idCompra) throws SQLException;

    /** Líneas para la edición. */
    List<DetalleCompra> lineasEditables(int idCompra) throws SQLException;

    /** Recepciones de la OC (vigentes y anuladas), en orden de fecha. */
    List<RecepcionOC> recepciones(int idCompra) throws SQLException;

    /** Líneas de todas las recepciones de la OC. */
    List<LineaRecepcion> lineasDeRecepciones(int idCompra) throws SQLException;

    /** Todos los almacenes del centro (activos primero). */
    List<Opcion> almacenesDelCentro(int idCentro) throws SQLException;

    /** Almacenes activos del centro. */
    List<Opcion> almacenesActivos(int idCentro) throws SQLException;

    /** Centros de costo activos del centro ("CODIGO - descripción"). */
    List<Opcion> centrosCostoActivos(int idCentro) throws SQLException;

    /** OC por recibir (SOLICITADO o PARCIAL) de los centros del usuario. */
    List<Opcion> pendientesDeRecibir(Permisos p) throws SQLException;

    /** Número, estado y centro de la OC, o null. */
    OrdenCompra paraRecibir(int idCompra) throws SQLException;

    /** Líneas con unidades pendientes de recibir (activos primero). */
    List<DetalleCompra> lineasPendientes(int idCompra) throws SQLException;

    String estado(int idCompra) throws SQLException;

    // Edición

    void actualizarCabecera(int idCompra, int idProveedor, int idCentro, String observaciones) throws SQLException;

    /** Línea con el estado de la OC y la cantidad de líneas, o null. */
    LineaOC linea(int idDetalle) throws SQLException;

    void actualizarLinea(int idDetalle, int cantidad, BigDecimal precio) throws SQLException;

    void eliminarLinea(int idDetalle) throws SQLException;

    void eliminarEquipoSolicitado(int idEquipo) throws SQLException;

    boolean tieneRecepciones(int idDetalle) throws SQLException;

    void agregarLineaInsumo(int idCompra, int idInsumo, int cantidad, BigDecimal precio) throws SQLException;

    void anular(int idCompra) throws SQLException;

    /** Los equipos pedidos que nunca llegaron quedan ANULADO e inactivos. */
    void anularEquiposPendientes(int idCompra) throws SQLException;

    /**
     * Recalcula el estado según TODAS las líneas: todo recibido = RECIBIDO, algo = PARCIAL,
     * nada = SOLICITADO. No toca BORRADOR ni ANULADO. Devuelve el estado calculado.
     */
    String recalcularEstado(int idCompra) throws SQLException;

    // Recepción

    /** Bloquea la OC (FOR UPDATE) y devuelve estado, centro y número, o null. */
    OrdenCompra bloquear(int idCompra) throws SQLException;

    int contarRecepciones(int idCompra) throws SQLException;

    int crearRecepcion(int idCompra, String numero, int idUsuario) throws SQLException;

    /** Línea de ESTA OC bloqueada (FOR UPDATE), o null. */
    LineaOC bloquearLinea(int idDetalle, int idCompra) throws SQLException;

    boolean centroCostoDelCentro(int idCentroCosto, int idCentro) throws SQLException;

    boolean almacenActivoDelCentro(int idAlmacen, int idCentro) throws SQLException;

    boolean almacenDelCentro(int idAlmacen, int idCentro) throws SQLException;

    /** true si otro activo ya tiene ese número de serie (sin distinguir mayúsculas). */
    boolean serieDuplicada(String serie, int idEquipoExcluido) throws SQLException;

    /** Número de activo nuevo (AF-000001...). */
    String siguienteNumeroActivo() throws SQLException;

    void ingresarEquipo(int idEquipo, String numeroActivo, String serie, int idCentro, int idCentroCosto) throws SQLException;

    void recibirLineaEquipo(int idDetalle, int cantidad, int idCentro, int idCentroCosto, int idEquipo) throws SQLException;

    void recibirLineaInsumo(int idDetalle, int cantidad, int idAlmacen) throws SQLException;

    void registrarLineaEquipo(int idRecepcion, int idDetalle, int cantidad, int idEquipo, int idCentroCosto, String numeroActivo) throws SQLException;

    void registrarLineaInsumo(int idRecepcion, int idDetalle, int cantidad, int idAlmacen) throws SQLException;

    // Anulación de una recepción

    /** Recepción con los datos de su OC, bloqueadas (FOR UPDATE), o null. */
    RecepcionOC bloquearRecepcion(int idRecepcion) throws SQLException;

    /** Líneas de la recepción con sus líneas de OC bloqueadas. */
    List<LineaRecepcion> lineasParaAnular(int idRecepcion) throws SQLException;

    EquipoEstado bloquearEquipo(int idEquipo) throws SQLException;

    /** Movimientos del activo posteriores a la fecha + solicitudes pendientes que lo incluyen. */
    int movimientosPosteriores(int idEquipo, Timestamp desde) throws SQLException;

    /** El activo vuelve a ser un pedido pendiente (sin número ni ubicación). */
    void devolverEquipoASolicitado(int idEquipo, String serialTemporal) throws SQLException;

    void restarRecibidoEquipo(int idDetalle, int cantidad) throws SQLException;

    void restarRecibidoInsumo(int idDetalle, int cantidad) throws SQLException;

    void asignarAlmacenALinea(int idLineaRecepcion, int idAlmacen) throws SQLException;

    void marcarRecepcionAnulada(int idRecepcion, int idUsuario, String motivo) throws SQLException;
}
