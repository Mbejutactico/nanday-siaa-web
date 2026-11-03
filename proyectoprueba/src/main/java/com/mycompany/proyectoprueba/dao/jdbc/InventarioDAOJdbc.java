package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.InventarioDAO;
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
import com.mycompany.proyectoprueba.util.Comun;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class InventarioDAOJdbc extends JdbcBase implements InventarioDAO {

    /** Rango de fechas opcional ('' = sin límite). Usa 2 parámetros: desde, hasta (yyyy-MM-dd). */
    private static final String RANGO_FECHAS =
            "{c} >= COALESCE(CAST(NULLIF(?, '') AS date), '-infinity'::date) "
            + "AND {c} < COALESCE(CAST(NULLIF(?, '') AS date) + 1, 'infinity'::date)";

    /** Consumos: insumos entregados en OT (a su centro de costo) + ajustes de consumo no anulados. */
    private static final String CONSUMOS =
            "SELECT d.fecha_suministro AS fecha, o.id_centro_costo AS id_cc, d.id_insumo, d.cantidad, d.cantidad * d.precio_unitario AS valor, o.numero_ot AS doc "
            + "  FROM detalle_ot d JOIN ot o ON o.id_ot = d.id_ot "
            + "UNION ALL "
            + "SELECT a.fecha, a.id_centro_costo, x.id_insumo, -x.cantidad, -x.cantidad * COALESCE(x.costo_unitario, 0), a.numero "
            + "  FROM ajustes a JOIN ajustes_detalle x ON x.id_ajuste = a.id_ajuste "
            + " WHERE a.tipo = 'CONSUMO' AND a.anulado = false AND a.id_centro_costo IS NOT NULL";

    private static final String SIN_REMISION_VIGENTE =
            "LEFT JOIN remisiones r ON r.id_remision = d.id_remision AND r.anulada = false ";

    public InventarioDAOJdbc(Connection conn) {
        super(conn);
    }

    private static String t(ResultSet rs, String columna) throws SQLException {
        return Comun.txt(rs.getString(columna));
    }

    private static String t(ResultSet rs, int columna) throws SQLException {
        return Comun.txt(rs.getString(columna));
    }

    private static String rango(String columna) {
        return RANGO_FECHAS.replace("{c}", columna);
    }

    /** Remisiones visibles para el usuario (requiere alias r, o, ao, ad). */
    private static String condRemision(Permisos p) {
        if (p.todosLosCentros()) {
            return "TRUE";
        }
        return "(" + p.sqlCentros("o.id_centro") + " OR " + p.sqlCentros("ao.id_centro") + " OR " + p.sqlCentros("ad.id_centro")
                + " OR EXISTS (SELECT 1 FROM solicitudes_activos sa WHERE sa.id_solicitud = r.id_solicitud_activo AND " + p.sqlCentros("sa.id_centro_destino") + ")"
                + " OR EXISTS (SELECT 1 FROM solicitudes_activos_detalle sd WHERE sd.id_solicitud = r.id_solicitud_activo AND " + p.sqlCentros("sd.id_centro_origen") + "))";
    }

    // Combos

    @Override
    public List<Opcion> centrosActivos(Permisos p) throws SQLException {
        return lista("SELECT id_centro, nombre FROM centros WHERE activo = true AND " + p.sqlCentros("id_centro") + " ORDER BY nombre",
                rs -> new Opcion(rs.getInt(1), rs.getString(2)));
    }

    @Override
    public List<Almacen> almacenesActivos(int idCentro, boolean deCualquierCentro, Permisos p) throws SQLException {
        return lista("SELECT a.id_almacen, a.nombre, c.nombre FROM almacenes a LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "WHERE a.activo = true AND (? = 0 OR a.id_centro = ?) AND "
                + (deCualquierCentro ? "TRUE" : p.sqlCentros("a.id_centro")) + " ORDER BY c.nombre, a.nombre", rs -> {
                    Almacen a = new Almacen();
                    a.setId(rs.getInt(1));
                    a.setNombre(rs.getString(2));
                    a.setCentroNombre(rs.getString(3));
                    return a;
                }, idCentro, idCentro);
    }

    @Override
    public List<Insumo> insumosActivos() throws SQLException {
        return lista("SELECT id_insumo, codigo, descripcion FROM insumos WHERE activo = true ORDER BY descripcion",
                rs -> new Insumo(rs.getInt(1), rs.getString(2), rs.getString(3), null, null, true));
    }

    @Override
    public List<ItemInsumo> stockDeAlmacen(int idAlmacen, Permisos p) throws SQLException {
        return lista("SELECT i.id_insumo, i.codigo, i.descripcion, u.abreviatura, s.stock_actual "
                + "FROM stock_almacen s JOIN insumos i ON i.id_insumo = s.id_insumo "
                + "LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida "
                + "WHERE s.id_almacen = ? AND EXISTS (SELECT 1 FROM almacenes ax WHERE ax.id_almacen = s.id_almacen AND " + p.sqlCentros("ax.id_centro") + ") ORDER BY i.descripcion",
                rs -> {
                    ItemInsumo i = new ItemInsumo();
                    i.idInsumo = rs.getInt(1);
                    i.codigo = t(rs, 2);
                    i.nombre = t(rs, 3);
                    i.unidad = t(rs, 4);
                    i.cantidad = rs.getInt(5);
                    return i;
                }, idAlmacen);
    }

    // Salida de insumos

    @Override
    public List<OrdenTrabajo> otsConPendientes(Permisos p) throws SQLException {
        return lista("SELECT o.id_ot, o.numero_ot, o.titulo, COUNT(*) AS lineas FROM ot o "
                + "JOIN detalle_ot d ON d.id_ot = o.id_ot "
                + SIN_REMISION_VIGENTE
                + "WHERE o.estado IN ('PENDIENTE', 'EN_PROCESO') AND r.id_remision IS NULL AND " + p.sqlCentros("o.id_centro") + " "
                + "GROUP BY o.id_ot, o.numero_ot, o.titulo, o.fecha_solicitud ORDER BY o.fecha_solicitud, o.id_ot", rs -> {
                    OrdenTrabajo o = new OrdenTrabajo();
                    o.id = rs.getInt(1);
                    o.numero = t(rs, 2);
                    o.titulo = t(rs, 3);
                    o.items = rs.getInt(4);
                    return o;
                });
    }

    @Override
    public OrdenTrabajo otDeSusCentros(int idOT, Permisos p) throws SQLException {
        return uno("SELECT o.numero_ot, o.titulo, o.estado, c.nombre AS centro, cc.codigo, cc.descripcion AS cc_desc, "
                + "       u.nombre || ' ' || u.apellido AS solicitante "
                + "FROM ot o LEFT JOIN centros c ON c.id_centro = o.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo "
                + "LEFT JOIN usuarios u ON u.id_usuario = o.id_usuario_solic WHERE o.id_ot = ? AND " + p.sqlCentros("o.id_centro"), rs -> {
                    OrdenTrabajo o = new OrdenTrabajo();
                    o.id = idOT;
                    o.numero = t(rs, "numero_ot");
                    o.titulo = t(rs, "titulo");
                    o.estado = t(rs, "estado");
                    o.centro = t(rs, "centro");
                    o.centroCosto = Comun.nombreCC(rs.getString("codigo"), rs.getString("cc_desc"));
                    o.solicitante = t(rs, "solicitante");
                    return o;
                }, idOT);
    }

    @Override
    public List<ItemInsumo> pendientesDeSalida(int idOT) throws SQLException {
        return lista("SELECT i.codigo, i.descripcion, u.abreviatura, d.cantidad, a.nombre AS almacen "
                + "FROM detalle_ot d JOIN insumos i ON i.id_insumo = d.id_insumo "
                + "LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida "
                + "LEFT JOIN almacenes a ON a.id_almacen = d.id_almacen "
                + SIN_REMISION_VIGENTE
                + "WHERE d.id_ot = ? AND r.id_remision IS NULL ORDER BY d.id_detalle_ot", rs -> {
                    ItemInsumo i = new ItemInsumo();
                    i.codigo = t(rs, 1);
                    i.nombre = t(rs, 2);
                    i.unidad = t(rs, 3);
                    i.cantidad = rs.getInt(4);
                    i.almacen = t(rs, 5);
                    return i;
                }, idOT);
    }

    @Override
    public boolean hayPendientesDeSalida(int idOT) throws SQLException {
        return existe("SELECT 1 FROM detalle_ot d " + SIN_REMISION_VIGENTE + "WHERE d.id_ot = ? AND r.id_remision IS NULL", idOT);
    }

    @Override
    public String[] bloquearOT(int idOT, Permisos p) throws SQLException {
        return uno("SELECT estado, numero_ot FROM ot WHERE id_ot = ? AND " + p.sqlCentros("id_centro") + " FOR UPDATE",
                rs -> new String[]{rs.getString(1), rs.getString(2)}, idOT);
    }

    @Override
    public String estadoOTBloqueada(int idOT) throws SQLException {
        String[] e = uno("SELECT estado FROM ot WHERE id_ot = ? FOR UPDATE", rs -> new String[]{rs.getString(1)}, idOT);
        return e == null ? "" : e[0];
    }

    @Override
    public boolean otEsDeSusCentros(int idOT, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM ot WHERE id_ot = ? AND " + p.sqlCentros("id_centro"), idOT);
    }

    @Override
    public int asignarRemision(int idRemision, int idOT) throws SQLException {
        return ejecutar("UPDATE detalle_ot d SET id_remision = ? WHERE d.id_ot = ? AND NOT EXISTS "
                + "(SELECT 1 FROM remisiones r WHERE r.id_remision = d.id_remision AND r.anulada = false)", idRemision, idOT);
    }

    @Override
    public void liberarRemision(int idRemision) throws SQLException {
        ejecutar("UPDATE detalle_ot SET id_remision = NULL WHERE id_remision = ?", idRemision);
    }

    @Override
    public void eventoOT(int idOT, String tipo, int idUsuario, String detalle) throws SQLException {
        ejecutar("INSERT INTO ot_eventos (id_ot, tipo, id_usuario, detalle) VALUES (?, ?, ?, ?)", idOT, tipo, idUsuario, detalle);
    }

    // Remisiones

    @Override
    public String siguienteRemision() throws SQLException {
        return uno("SELECT 'REM-' || to_char(CURRENT_DATE, 'YYYY') || '-' || lpad(nextval('public.seq_remision')::text, 6, '0')", rs -> rs.getString(1));
    }

    @Override
    public int insertarRemision(String numero, String tipo, Integer idOT, Integer idTransferencia, int idUsuario,
            Transportista t, String observacion) throws SQLException {
        return insertarConId("INSERT INTO remisiones (numero_remision, fecha_emision, fecha_hora, tipo_remision, id_ot, id_transferencia, id_usuario_emite, "
                + "transportista_nombre, transportista_apellido, transportista_ci, vehiculo_marca, vehiculo_modelo, vehiculo_chapa, observacion, anulada) "
                + "VALUES (?, CURRENT_DATE, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false) RETURNING id_remision",
                numero, tipo, entero(idOT), entero(idTransferencia), idUsuario,
                vacioNull(t.nombre), vacioNull(t.apellido), vacioNull(t.ci), vacioNull(t.marca), vacioNull(t.modelo), vacioNull(t.chapa), vacioNull(observacion));
    }

    private static Object vacioNull(String v) {
        return v == null || v.isEmpty() ? Nulo.TEXTO : v;
    }

    @Override
    public List<Remision> listarRemisiones(String tipo, String estado, Permisos p) throws SQLException {
        return lista("SELECT r.id_remision, r.numero_remision, r.tipo_remision, r.fecha_hora, r.anulada, r.motivo_anulacion, r.fecha_anulacion, "
                + "       TRIM(COALESCE(r.transportista_nombre, '') || ' ' || COALESCE(r.transportista_apellido, '')) AS transportista, r.transportista_ci, "
                + "       o.numero_ot, o.titulo, t.numero AS numero_tr, ao.nombre AS origen, ad.nombre AS destino, "
                + "       ue.nombre || ' ' || ue.apellido AS emitio, sa.numero AS numero_sa, sa.tipo AS tipo_sa, "
                + "       (SELECT COUNT(*) FROM remisiones_detalle x WHERE x.id_remision = r.id_remision) AS items "
                + "FROM remisiones r LEFT JOIN ot o ON o.id_ot = r.id_ot "
                + "LEFT JOIN transferencias t ON t.id_transferencia = r.id_transferencia "
                + "LEFT JOIN almacenes ao ON ao.id_almacen = t.id_almacen_origen LEFT JOIN almacenes ad ON ad.id_almacen = t.id_almacen_destino "
                + "LEFT JOIN usuarios ue ON ue.id_usuario = r.id_usuario_emite "
                + "LEFT JOIN solicitudes_activos sa ON sa.id_solicitud = r.id_solicitud_activo "
                + "WHERE (? = '' OR r.tipo_remision = ? OR (? = 'INSUMOS' AND r.tipo_remision IN ('SALIDA_INSUMOS', 'TRANSFERENCIA'))) "
                + "AND (? = '' OR (? = 'VIGENTE' AND r.anulada = false) OR (? = 'ANULADA' AND r.anulada = true)) "
                + "AND " + condRemision(p) + " ORDER BY r.fecha_hora DESC NULLS LAST, r.id_remision DESC", rs -> {
                    Remision r = new Remision();
                    r.id = rs.getInt("id_remision");
                    r.numero = t(rs, "numero_remision");
                    r.tipo = rs.getString("tipo_remision");
                    r.fecha = rs.getTimestamp("fecha_hora");
                    r.anulada = rs.getBoolean("anulada");
                    r.motivoAnulacion = t(rs, "motivo_anulacion");
                    r.fechaAnulacion = rs.getTimestamp("fecha_anulacion");
                    r.transportista = t(rs, "transportista");
                    r.ci = t(rs, "transportista_ci");
                    r.numeroOt = t(rs, "numero_ot");
                    r.tituloOt = rs.getString("titulo");
                    r.numeroTransferencia = t(rs, "numero_tr");
                    r.almacenOrigen = t(rs, "origen");
                    r.almacenDestino = t(rs, "destino");
                    r.emitio = t(rs, "emitio");
                    r.numeroSolicitud = t(rs, "numero_sa");
                    r.tipoSolicitud = rs.getString("tipo_sa");
                    r.items = rs.getInt("items");
                    return r;
                }, tipo, tipo, tipo, estado, estado, estado);
    }

    @Override
    public Remision bloquearRemision(int idRemision) throws SQLException {
        return uno("SELECT tipo_remision, anulada, id_ot, id_transferencia, numero_remision FROM remisiones WHERE id_remision = ? FOR UPDATE", rs -> {
            Remision r = new Remision();
            r.id = idRemision;
            r.tipo = rs.getString(1);
            r.anulada = rs.getBoolean(2);
            r.idOt = rs.getInt(3);
            r.idTransferencia = rs.getInt(4);
            r.numero = rs.getString(5);
            return r;
        }, idRemision);
    }

    @Override
    public void marcarRemisionAnulada(int idRemision, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE remisiones SET anulada = true, fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? WHERE id_remision = ?",
                idUsuario, motivo, idRemision);
    }

    @Override
    public boolean remisionVisible(int idRemision, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM remisiones r LEFT JOIN ot o ON o.id_ot = r.id_ot LEFT JOIN transferencias t ON t.id_transferencia = r.id_transferencia "
                + "LEFT JOIN almacenes ao ON ao.id_almacen = t.id_almacen_origen LEFT JOIN almacenes ad ON ad.id_almacen = t.id_almacen_destino "
                + "WHERE r.id_remision = ? AND " + condRemision(p), idRemision);
    }

    // Ajustes

    @Override
    public List<Ajuste> listarAjustes(Permisos p) throws SQLException {
        return lista("SELECT a.id_ajuste, a.numero, a.fecha, a.tipo, a.origen, a.motivo, a.anulado, a.fecha_anulacion, a.motivo_anulacion, "
                + "       c.nombre AS centro, cc.codigo, cc.descripcion AS cc_desc, al.nombre AS almacen, u.nombre || ' ' || u.apellido AS usuario, "
                + "       (SELECT COUNT(*) FROM ajustes_detalle d WHERE d.id_ajuste = a.id_ajuste) AS items, "
                + "       (SELECT COALESCE(SUM(d.cantidad * COALESCE(d.costo_unitario, 0)), 0) FROM ajustes_detalle d WHERE d.id_ajuste = a.id_ajuste) AS valor "
                + "FROM ajustes a LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = a.id_centro_costo "
                + "LEFT JOIN almacenes al ON al.id_almacen = a.id_almacen LEFT JOIN usuarios u ON u.id_usuario = a.id_usuario "
                + "WHERE " + p.sqlCentros("a.id_centro") + " ORDER BY a.fecha DESC, a.id_ajuste DESC", rs -> {
                    Ajuste a = new Ajuste();
                    a.id = rs.getInt("id_ajuste");
                    a.numero = t(rs, "numero");
                    a.fecha = rs.getTimestamp("fecha");
                    a.tipo = t(rs, "tipo");
                    a.origen = t(rs, "origen");
                    a.motivo = t(rs, "motivo");
                    a.anulado = rs.getBoolean("anulado");
                    a.fechaAnulacion = rs.getTimestamp("fecha_anulacion");
                    a.motivoAnulacion = t(rs, "motivo_anulacion");
                    a.centro = t(rs, "centro");
                    a.centroCosto = Comun.nombreCC(rs.getString("codigo"), rs.getString("cc_desc"));
                    a.almacen = t(rs, "almacen");
                    a.usuario = t(rs, "usuario");
                    a.items = rs.getInt("items");
                    a.valor = rs.getBigDecimal("valor");
                    return a;
                });
    }

    @Override
    public List<ItemInsumo> lineasAjusteVisible(int idAjuste, Permisos p) throws SQLException {
        return lista("SELECT i.codigo, i.descripcion, u.abreviatura, d.cantidad, d.costo_unitario FROM ajustes_detalle d "
                + "JOIN insumos i ON i.id_insumo = d.id_insumo LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida "
                + "WHERE d.id_ajuste = ? AND EXISTS (SELECT 1 FROM ajustes a WHERE a.id_ajuste = d.id_ajuste AND " + p.sqlCentros("a.id_centro") + ") "
                + "ORDER BY d.id_detalle", rs -> {
                    ItemInsumo i = new ItemInsumo();
                    i.codigo = t(rs, 1);
                    i.nombre = t(rs, 2);
                    i.unidad = t(rs, 3);
                    i.cantidad = rs.getInt(4);
                    i.costo = rs.getBigDecimal(5);
                    return i;
                }, idAjuste);
    }

    @Override
    public boolean centroCostoDelCentro(int idCentroCosto, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM centros_costo WHERE id_centro_costo = ? AND id_centro = ?", idCentroCosto, idCentro);
    }

    @Override
    public boolean almacenActivoDelCentro(int idAlmacen, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM almacenes WHERE id_almacen = ? AND id_centro = ? AND activo = true", idAlmacen, idCentro);
    }

    @Override
    public String siguienteAjuste() throws SQLException {
        return uno("SELECT 'AJ-' || lpad(nextval('public.seq_ajuste')::text, 6, '0')", rs -> rs.getString(1));
    }

    @Override
    public int insertarAjuste(String numero, String tipo, String origen, int idCentro, Integer idCentroCosto, int idAlmacen,
            String motivo, int idUsuario) throws SQLException {
        return insertarConId("INSERT INTO ajustes (numero, tipo, origen, id_centro, id_centro_costo, id_almacen, motivo, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_ajuste",
                numero, tipo, origen, idCentro, entero(idCentroCosto), idAlmacen, motivo, idUsuario);
    }

    @Override
    public void insertarLineaAjuste(int idAjuste, int idInsumo, int cantidad, BigDecimal costo) throws SQLException {
        ejecutar("INSERT INTO ajustes_detalle (id_ajuste, id_insumo, cantidad, costo_unitario) VALUES (?, ?, ?, ?)",
                idAjuste, idInsumo, cantidad, costo == null ? Nulo.NUMERICO : costo);
    }

    @Override
    public BigDecimal precioInsumoActivo(int idInsumo) throws SQLException {
        return uno("SELECT COALESCE(precio_compra, 0) FROM insumos WHERE id_insumo = ? AND activo = true", rs -> rs.getBigDecimal(1), idInsumo);
    }

    @Override
    public Ajuste bloquearAjuste(int idAjuste) throws SQLException {
        return uno("SELECT numero, anulado, id_almacen, id_centro_costo FROM ajustes WHERE id_ajuste = ? FOR UPDATE", rs -> {
            Ajuste a = new Ajuste();
            a.id = idAjuste;
            a.numero = rs.getString(1);
            a.anulado = rs.getBoolean(2);
            a.idAlmacen = rs.getInt(3);
            a.idCentroCosto = enteroONull(rs, "id_centro_costo");
            return a;
        }, idAjuste);
    }

    @Override
    public boolean almacenDeSusCentros(int idAlmacen, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM almacenes WHERE id_almacen = ? AND " + p.sqlCentros("id_centro"), idAlmacen);
    }

    @Override
    public List<ItemInsumo> lineasAjuste(int idAjuste) throws SQLException {
        return lista("SELECT id_insumo, cantidad, costo_unitario FROM ajustes_detalle WHERE id_ajuste = ?", rs -> {
            ItemInsumo i = new ItemInsumo();
            i.idInsumo = rs.getInt(1);
            i.cantidad = rs.getInt(2);
            i.costo = rs.getBigDecimal(3);
            return i;
        }, idAjuste);
    }

    @Override
    public void marcarAjusteAnulado(int idAjuste, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE ajustes SET anulado = true, fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? WHERE id_ajuste = ?",
                idUsuario, motivo, idAjuste);
    }

    // Transferencias

    @Override
    public List<Transferencia> listarTransferencias(Permisos p) throws SQLException {
        return lista("SELECT t.id_transferencia, t.numero, t.fecha, t.observacion, t.anulada, t.fecha_anulacion, t.motivo_anulacion, "
                + "       ao.nombre AS origen, co.nombre AS centro_origen, ad.nombre AS destino, cd.nombre AS centro_destino, "
                + "       u.nombre || ' ' || u.apellido AS usuario, r.id_remision, r.numero_remision, "
                + "       (SELECT COUNT(*) FROM transferencias_detalle d WHERE d.id_transferencia = t.id_transferencia) AS items "
                + "FROM transferencias t JOIN almacenes ao ON ao.id_almacen = t.id_almacen_origen LEFT JOIN centros co ON co.id_centro = ao.id_centro "
                + "JOIN almacenes ad ON ad.id_almacen = t.id_almacen_destino LEFT JOIN centros cd ON cd.id_centro = ad.id_centro "
                + "LEFT JOIN usuarios u ON u.id_usuario = t.id_usuario LEFT JOIN remisiones r ON r.id_transferencia = t.id_transferencia "
                + "WHERE (" + p.sqlCentros("ao.id_centro") + " OR " + p.sqlCentros("ad.id_centro") + ") "
                + "ORDER BY t.fecha DESC, t.id_transferencia DESC", rs -> {
                    Transferencia tr = new Transferencia();
                    tr.id = rs.getInt("id_transferencia");
                    tr.numero = t(rs, "numero");
                    tr.fecha = rs.getTimestamp("fecha");
                    tr.observacion = t(rs, "observacion");
                    tr.anulada = rs.getBoolean("anulada");
                    tr.fechaAnulacion = rs.getTimestamp("fecha_anulacion");
                    tr.motivoAnulacion = t(rs, "motivo_anulacion");
                    tr.origen = t(rs, "origen");
                    tr.centroOrigen = t(rs, "centro_origen");
                    tr.destino = t(rs, "destino");
                    tr.centroDestino = t(rs, "centro_destino");
                    tr.usuario = t(rs, "usuario");
                    tr.idRemision = rs.getInt("id_remision");
                    tr.remision = t(rs, "numero_remision");
                    tr.items = rs.getInt("items");
                    return tr;
                });
    }

    @Override
    public List<ItemInsumo> lineasTransferenciaVisible(int idTransferencia, Permisos p) throws SQLException {
        return lista("SELECT i.codigo, i.descripcion, u.abreviatura, d.cantidad FROM transferencias_detalle d JOIN insumos i ON i.id_insumo = d.id_insumo "
                + "LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida WHERE d.id_transferencia = ? "
                + "AND EXISTS (SELECT 1 FROM transferencias t JOIN almacenes ao ON ao.id_almacen = t.id_almacen_origen "
                + "JOIN almacenes ad ON ad.id_almacen = t.id_almacen_destino WHERE t.id_transferencia = d.id_transferencia "
                + "AND (" + p.sqlCentros("ao.id_centro") + " OR " + p.sqlCentros("ad.id_centro") + ")) ORDER BY d.id_detalle", rs -> {
                    ItemInsumo i = new ItemInsumo();
                    i.codigo = t(rs, 1);
                    i.nombre = t(rs, 2);
                    i.unidad = t(rs, 3);
                    i.cantidad = rs.getInt(4);
                    return i;
                }, idTransferencia);
    }

    @Override
    public boolean almacenActivo(int idAlmacen) throws SQLException {
        return existe("SELECT 1 FROM almacenes WHERE id_almacen = ? AND activo = true", idAlmacen);
    }

    @Override
    public String siguienteTransferencia() throws SQLException {
        return uno("SELECT 'TR-' || lpad(nextval('public.seq_transferencia')::text, 6, '0')", rs -> rs.getString(1));
    }

    @Override
    public int insertarTransferencia(String numero, int origen, int destino, String observacion, int idUsuario) throws SQLException {
        return insertarConId("INSERT INTO transferencias (numero, id_almacen_origen, id_almacen_destino, observacion, id_usuario) VALUES (?, ?, ?, ?, ?) RETURNING id_transferencia",
                numero, origen, destino, vacioNull(observacion), idUsuario);
    }

    @Override
    public void insertarLineaTransferencia(int idTransferencia, int idInsumo, int cantidad) throws SQLException {
        ejecutar("INSERT INTO transferencias_detalle (id_transferencia, id_insumo, cantidad) VALUES (?, ?, ?)", idTransferencia, idInsumo, cantidad);
    }

    @Override
    public boolean transferenciaSaleDeSusCentros(int idTransferencia, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM transferencias t JOIN almacenes a ON a.id_almacen = t.id_almacen_origen WHERE t.id_transferencia = ? AND "
                + p.sqlCentros("a.id_centro"), idTransferencia);
    }

    @Override
    public Transferencia bloquearTransferencia(int idTransferencia) throws SQLException {
        return uno("SELECT numero, anulada, id_almacen_origen, id_almacen_destino FROM transferencias WHERE id_transferencia = ? FOR UPDATE", rs -> {
            Transferencia tr = new Transferencia();
            tr.id = idTransferencia;
            tr.numero = rs.getString(1);
            tr.anulada = rs.getBoolean(2);
            tr.idAlmacenOrigen = rs.getInt(3);
            tr.idAlmacenDestino = rs.getInt(4);
            return tr;
        }, idTransferencia);
    }

    @Override
    public List<ItemInsumo> lineasTransferencia(int idTransferencia) throws SQLException {
        return lista("SELECT id_insumo, cantidad FROM transferencias_detalle WHERE id_transferencia = ?", rs -> {
            ItemInsumo i = new ItemInsumo();
            i.idInsumo = rs.getInt(1);
            i.cantidad = rs.getInt(2);
            return i;
        }, idTransferencia);
    }

    @Override
    public void marcarTransferenciaAnulada(int idTransferencia, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE transferencias SET anulada = true, fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? WHERE id_transferencia = ?",
                idUsuario, motivo, idTransferencia);
    }

    @Override
    public void anularRemisionesDeTransferencia(int idTransferencia, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE remisiones SET anulada = true, fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? "
                + "WHERE id_transferencia = ? AND anulada = false", idUsuario, motivo, idTransferencia);
    }

    // Inventario físico

    @Override
    public Integer centroDeAlmacenActivo(int idAlmacen) throws SQLException {
        return uno("SELECT id_centro FROM almacenes WHERE id_almacen = ? AND activo = true", rs -> Integer.valueOf(rs.getInt(1)), idAlmacen);
    }

    @Override
    public int stockBloqueado(int idInsumo, int idAlmacen) throws SQLException {
        Integer s = uno("SELECT stock_actual FROM stock_almacen WHERE id_insumo = ? AND id_almacen = ? FOR UPDATE",
                rs -> Integer.valueOf(rs.getInt(1)), idInsumo, idAlmacen);
        return s == null ? 0 : s.intValue();
    }

    // Kardex y reportes

    @Override
    public List<MovimientoKardex> kardex(int idInsumo, int idAlmacen, String desde, String hasta, Permisos p) throws SQLException {
        return lista("SELECT m.fecha, m.tipo, m.documento, m.cantidad, m.saldo, m.costo_unitario, m.observacion, a.nombre AS almacen, "
                + "       u.nombre || ' ' || u.apellido AS usuario, cc.codigo AS cc "
                + "FROM movimientos_stock m JOIN almacenes a ON a.id_almacen = m.id_almacen "
                + "LEFT JOIN usuarios u ON u.id_usuario = m.id_usuario LEFT JOIN centros_costo cc ON cc.id_centro_costo = m.id_centro_costo "
                + "WHERE m.id_insumo = ? AND (? = 0 OR m.id_almacen = ?) AND " + p.sqlCentros("a.id_centro") + " AND " + rango("m.fecha")
                + " ORDER BY m.fecha, m.id_mov", rs -> {
                    MovimientoKardex m = new MovimientoKardex();
                    m.fecha = rs.getTimestamp("fecha");
                    m.tipo = t(rs, "tipo");
                    m.documento = t(rs, "documento");
                    m.cantidad = rs.getInt("cantidad");
                    m.saldo = rs.getInt("saldo");
                    m.costo = rs.getBigDecimal("costo_unitario");
                    m.observacion = t(rs, "observacion");
                    m.almacen = t(rs, "almacen");
                    m.usuario = t(rs, "usuario");
                    m.centroCosto = t(rs, "cc");
                    return m;
                }, idInsumo, idAlmacen, idAlmacen, desde, hasta);
    }

    @Override
    public List<FilaReporteInventario> consumoPorCentroCosto(String desde, String hasta, Permisos p) throws SQLException {
        return lista("SELECT c.nombre AS centro, cc.codigo, cc.descripcion, SUM(x.cantidad) AS unidades, SUM(x.valor) AS valor, COUNT(DISTINCT x.doc) AS documentos "
                + "FROM (" + CONSUMOS + ") x JOIN centros_costo cc ON cc.id_centro_costo = x.id_cc LEFT JOIN centros c ON c.id_centro = cc.id_centro "
                + "WHERE " + rango("x.fecha")
                + " AND " + p.sqlCentros("cc.id_centro") + " GROUP BY c.nombre, cc.codigo, cc.descripcion ORDER BY SUM(x.valor) DESC", rs -> {
                    FilaReporteInventario f = new FilaReporteInventario();
                    f.centro = t(rs, "centro");
                    f.centroCosto = Comun.nombreCC(rs.getString("codigo"), rs.getString("descripcion"));
                    f.unidades = rs.getLong("unidades");
                    f.valor = rs.getBigDecimal("valor");
                    f.documentos = rs.getInt("documentos");
                    return f;
                }, desde, hasta);
    }

    @Override
    public List<FilaReporteInventario> masUsados(String desde, String hasta, Permisos p) throws SQLException {
        return lista("SELECT i.codigo, i.descripcion, u.abreviatura, SUM(x.cantidad) AS unidades, SUM(x.valor) AS valor "
                + "FROM (" + CONSUMOS + ") x JOIN insumos i ON i.id_insumo = x.id_insumo LEFT JOIN unidad_medida u ON u.id_unidad_medida = i.id_unidad_medida "
                + "LEFT JOIN centros_costo ccx ON ccx.id_centro_costo = x.id_cc "
                + "WHERE " + rango("x.fecha")
                + " AND " + p.sqlCentros("ccx.id_centro") + " GROUP BY i.codigo, i.descripcion, u.abreviatura ORDER BY SUM(x.cantidad) DESC LIMIT 20", rs -> {
                    FilaReporteInventario f = new FilaReporteInventario();
                    f.codigo = t(rs, 1);
                    f.nombre = t(rs, 2);
                    f.unidad = t(rs, 3);
                    f.unidades = rs.getLong(4);
                    f.valor = rs.getBigDecimal(5);
                    return f;
                }, desde, hasta);
    }

    @Override
    public List<FilaReporteInventario> valorizacion(Permisos p) throws SQLException {
        return lista("SELECT c.nombre AS centro, a.nombre AS almacen, COUNT(*) FILTER (WHERE s.stock_actual > 0) AS insumos, "
                + "       COALESCE(SUM(s.stock_actual), 0) AS unidades, COALESCE(SUM(s.stock_actual * COALESCE(i.precio_compra, 0)), 0) AS valor "
                + "FROM stock_almacen s JOIN almacenes a ON a.id_almacen = s.id_almacen JOIN insumos i ON i.id_insumo = s.id_insumo "
                + "LEFT JOIN centros c ON c.id_centro = a.id_centro WHERE " + p.sqlCentros("a.id_centro") + " GROUP BY c.nombre, a.nombre ORDER BY c.nombre, a.nombre", rs -> {
                    FilaReporteInventario f = new FilaReporteInventario();
                    f.centro = t(rs, 1);
                    f.almacen = t(rs, 2);
                    f.insumos = rs.getInt(3);
                    f.unidades = rs.getLong(4);
                    f.valor = rs.getBigDecimal(5);
                    return f;
                });
    }

    @Override
    public List<FilaReporteInventario> alertasMinimo(Permisos p) throws SQLException {
        return lista("SELECT i.codigo, i.descripcion, a.nombre AS almacen, s.stock_actual, s.stock_minimo "
                + "FROM stock_almacen s JOIN insumos i ON i.id_insumo = s.id_insumo JOIN almacenes a ON a.id_almacen = s.id_almacen "
                + "WHERE s.stock_minimo > 0 AND s.stock_actual <= s.stock_minimo AND i.activo = true AND " + p.sqlCentros("a.id_centro") + " "
                + "ORDER BY (s.stock_actual::numeric / s.stock_minimo), i.descripcion LIMIT 50", rs -> {
                    FilaReporteInventario f = new FilaReporteInventario();
                    f.codigo = t(rs, 1);
                    f.nombre = t(rs, 2);
                    f.almacen = t(rs, 3);
                    f.stock = rs.getInt(4);
                    f.minimo = rs.getInt(5);
                    return f;
                });
    }

    @Override
    public Timestamp ahora() throws SQLException {
        return uno("SELECT CURRENT_TIMESTAMP", rs -> rs.getTimestamp(1));
    }
}
