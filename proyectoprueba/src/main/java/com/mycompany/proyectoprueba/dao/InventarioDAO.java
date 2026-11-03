package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Ajuste;
import com.mycompany.proyectoprueba.modelo.Almacen;
import com.mycompany.proyectoprueba.modelo.FilaReporteInventario;
import com.mycompany.proyectoprueba.modelo.Insumo;
import com.mycompany.proyectoprueba.modelo.ItemInsumo;
import com.mycompany.proyectoprueba.modelo.MovimientoKardex;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.Remision;
import com.mycompany.proyectoprueba.modelo.Transferencia;
import com.mycompany.proyectoprueba.modelo.Transportista;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Acceso a datos del inventario de insumos: remisiones, ajustes, transferencias, conteos,
 * Kardex y reportes. El stock se mueve siempre con MovimientoStock (en el servicio).
 * Fechas de filtro: "yyyy-MM-dd" o "" (sin límite).
 */
public interface InventarioDAO {

    // Combos
    List<Opcion> centrosActivos(Permisos p) throws SQLException;

    /** Almacenes activos de un centro (0 = todos); deCualquierCentro = sin filtrar por los centros del usuario. */
    List<Almacen> almacenesActivos(int idCentro, boolean deCualquierCentro, Permisos p) throws SQLException;

    List<Insumo> insumosActivos() throws SQLException;

    /** Stock de un almacén de los centros del usuario (cantidad = stock). */
    List<ItemInsumo> stockDeAlmacen(int idAlmacen, Permisos p) throws SQLException;

    // Salida de insumos de una OT
    /** OT abiertas con insumos sin salida (items = cantidad de líneas pendientes). */
    List<OrdenTrabajo> otsConPendientes(Permisos p) throws SQLException;

    /** Cabecera de la OT (de los centros del usuario) o null. */
    OrdenTrabajo otDeSusCentros(int idOT, Permisos p) throws SQLException;

    List<ItemInsumo> pendientesDeSalida(int idOT) throws SQLException;

    boolean hayPendientesDeSalida(int idOT) throws SQLException;

    /** Bloquea la OT (de los centros del usuario): {estado, numero} o null. */
    String[] bloquearOT(int idOT, Permisos p) throws SQLException;

    /** Estado de la OT con bloqueo ("" si no existe). */
    String estadoOTBloqueada(int idOT) throws SQLException;

    boolean otEsDeSusCentros(int idOT, Permisos p) throws SQLException;

    /** Asigna la remisión a las líneas de la OT sin remisión vigente; devuelve cuántas. */
    int asignarRemision(int idRemision, int idOT) throws SQLException;

    /** Las líneas de la remisión vuelven a quedar pendientes de salida. */
    void liberarRemision(int idRemision) throws SQLException;

    void eventoOT(int idOT, String tipo, int idUsuario, String detalle) throws SQLException;

    // Remisiones
    String siguienteRemision() throws SQLException;

    int insertarRemision(String numero, String tipo, Integer idOT, Integer idTransferencia, int idUsuario,
            Transportista t, String observacion) throws SQLException;

    /** tipo: "" | INSUMOS | SALIDA_INSUMOS | TRANSFERENCIA | SALIDA_ACTIVOS; estado: "" | VIGENTE | ANULADA. */
    List<Remision> listarRemisiones(String tipo, String estado, Permisos p) throws SQLException;

    /** Bloquea la remisión: tipo, anulada, idOt, idTransferencia y número; o null. */
    Remision bloquearRemision(int idRemision) throws SQLException;

    void marcarRemisionAnulada(int idRemision, int idUsuario, String motivo) throws SQLException;

    boolean remisionVisible(int idRemision, Permisos p) throws SQLException;

    // Ajustes
    List<Ajuste> listarAjustes(Permisos p) throws SQLException;

    /** Líneas (código, nombre, unidad, cantidad, costo) de un ajuste de los centros del usuario. */
    List<ItemInsumo> lineasAjusteVisible(int idAjuste, Permisos p) throws SQLException;

    boolean centroCostoDelCentro(int idCentroCosto, int idCentro) throws SQLException;

    boolean almacenActivoDelCentro(int idAlmacen, int idCentro) throws SQLException;

    String siguienteAjuste() throws SQLException;

    int insertarAjuste(String numero, String tipo, String origen, int idCentro, Integer idCentroCosto, int idAlmacen,
            String motivo, int idUsuario) throws SQLException;

    void insertarLineaAjuste(int idAjuste, int idInsumo, int cantidad, BigDecimal costo) throws SQLException;

    /** Precio de compra (0 si no tiene) de un insumo activo, o null si no existe o está inactivo. */
    BigDecimal precioInsumoActivo(int idInsumo) throws SQLException;

    /** Bloquea el ajuste: número, anulado, almacén y centro de costo; o null. */
    Ajuste bloquearAjuste(int idAjuste) throws SQLException;

    boolean almacenDeSusCentros(int idAlmacen, Permisos p) throws SQLException;

    /** Líneas (idInsumo, cantidad con signo, costo) de un ajuste. */
    List<ItemInsumo> lineasAjuste(int idAjuste) throws SQLException;

    void marcarAjusteAnulado(int idAjuste, int idUsuario, String motivo) throws SQLException;

    // Transferencias
    List<Transferencia> listarTransferencias(Permisos p) throws SQLException;

    List<ItemInsumo> lineasTransferenciaVisible(int idTransferencia, Permisos p) throws SQLException;

    boolean almacenActivo(int idAlmacen) throws SQLException;

    String siguienteTransferencia() throws SQLException;

    int insertarTransferencia(String numero, int origen, int destino, String observacion, int idUsuario) throws SQLException;

    void insertarLineaTransferencia(int idTransferencia, int idInsumo, int cantidad) throws SQLException;

    boolean transferenciaSaleDeSusCentros(int idTransferencia, Permisos p) throws SQLException;

    /** Bloquea la transferencia: número, anulada, origen y destino; o null. */
    Transferencia bloquearTransferencia(int idTransferencia) throws SQLException;

    List<ItemInsumo> lineasTransferencia(int idTransferencia) throws SQLException;

    void marcarTransferenciaAnulada(int idTransferencia, int idUsuario, String motivo) throws SQLException;

    void anularRemisionesDeTransferencia(int idTransferencia, int idUsuario, String motivo) throws SQLException;

    // Inventario físico
    /** Centro de un almacén activo o null. */
    Integer centroDeAlmacenActivo(int idAlmacen) throws SQLException;

    /** Stock actual con bloqueo (0 si no hay registro). */
    int stockBloqueado(int idInsumo, int idAlmacen) throws SQLException;

    // Kardex y reportes
    List<MovimientoKardex> kardex(int idInsumo, int idAlmacen, String desde, String hasta, Permisos p) throws SQLException;

    List<FilaReporteInventario> consumoPorCentroCosto(String desde, String hasta, Permisos p) throws SQLException;

    List<FilaReporteInventario> masUsados(String desde, String hasta, Permisos p) throws SQLException;

    List<FilaReporteInventario> valorizacion(Permisos p) throws SQLException;

    List<FilaReporteInventario> alertasMinimo(Permisos p) throws SQLException;

    Timestamp ahora() throws SQLException;
}
