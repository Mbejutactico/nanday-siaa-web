package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.OrdenTrabajoDAO;
import com.mycompany.proyectoprueba.modelo.AlmacenStock;
import com.mycompany.proyectoprueba.modelo.ContextoUsuario;
import com.mycompany.proyectoprueba.modelo.Insumo;
import com.mycompany.proyectoprueba.modelo.InsumoDisponible;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.ResponsableOT;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Comun;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrdenTrabajoDAOJdbc extends JdbcBase implements OrdenTrabajoDAO {

    /** Usuarios que pueden ser responsables de una OT: activos y de un rol que trabaja OT. */
    private static final String RESPONSABLE_POSIBLE = "u.activo = true AND r.codigo IN ('ADMINISTRADOR', 'GERENTE', 'AUXILIAR')";

    /** El usuario trabaja en el centro (principal o adicional vigente). Parámetros: centro, centro. */
    private static final String TRABAJA_EN_CENTRO = "(u.centro_asignado = ? OR EXISTS (SELECT 1 FROM usuario_centros uc "
            + "  WHERE uc.id_usuario = u.id_usuario AND uc.id_centro = ? AND uc.fecha_desde <= CURRENT_DATE "
            + "    AND (uc.fecha_hasta IS NULL OR uc.fecha_hasta >= CURRENT_DATE)))";

    private static final String PENDIENTES_SALIDA =
            "SELECT 1 FROM detalle_ot d LEFT JOIN remisiones r ON r.id_remision = d.id_remision AND r.anulada = false "
            + "WHERE d.id_ot = ? AND r.id_remision IS NULL";

    public OrdenTrabajoDAOJdbc(Connection conn) {
        super(conn);
    }

    /** OT que el usuario puede ver: las de sus centros (centro destino), las que pidió y las que tiene a cargo. */
    private static String visible(Permisos p, int idUsuario) {
        return "(" + p.sqlCentros("o.id_centro") + " OR o.id_usuario_solic = " + idUsuario
                + " OR o.id_usuario_responsable = " + idUsuario + ")";
    }

    private static String t(ResultSet rs, String columna) throws SQLException {
        return Comun.txt(rs.getString(columna));
    }

    private static int enteroO0(ResultSet rs, String columna) throws SQLException {
        int v = rs.getInt(columna);
        return rs.wasNull() ? 0 : v;
    }

    private static String fecha(ResultSet rs, String columna) throws SQLException {
        java.sql.Date d = rs.getDate(columna);
        return d == null ? "" : d.toString();
    }

    // Consultas

    @Override
    public ContextoUsuario contextoUsuario(int idUsuario) throws SQLException {
        return uno("SELECT u.id_usuario, u.nombre, u.apellido, u.email, r.nombre AS rol, u.centro_asignado, c.nombre AS centro "
                + "FROM usuarios u LEFT JOIN roles r ON r.id_rol = u.id_rol "
                + "LEFT JOIN centros c ON c.id_centro = u.centro_asignado WHERE u.id_usuario = ?",
                rs -> new ContextoUsuario(rs.getInt("id_usuario"), t(rs, "nombre") + " " + t(rs, "apellido"), t(rs, "email"),
                        t(rs, "rol"), enteroO0(rs, "centro_asignado"), t(rs, "centro")), idUsuario);
    }

    @Override
    public List<ResponsableOT> responsables(int idCentro) throws SQLException {
        return lista("SELECT u.id_usuario, u.nombre || ' ' || u.apellido AS nombre, r.nombre AS rol, u.centro_asignado, c.nombre AS centro "
                + "FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol AND r.activo = true "
                + "LEFT JOIN centros c ON c.id_centro = u.centro_asignado "
                + "WHERE " + RESPONSABLE_POSIBLE + " AND (? = 0 OR " + TRABAJA_EN_CENTRO + ") "
                + "ORDER BY u.apellido, u.nombre",
                rs -> new ResponsableOT(rs.getInt("id_usuario"), t(rs, "nombre"), t(rs, "rol"), enteroO0(rs, "centro_asignado"), t(rs, "centro")),
                idCentro, idCentro, idCentro);
    }

    @Override
    public Map<String, Integer> resumenPorEstado(Permisos p, int idUsuario) throws SQLException {
        final Map<String, Integer> m = new LinkedHashMap<String, Integer>();
        lista("SELECT estado, COUNT(*) FROM ot o WHERE " + visible(p, idUsuario) + " GROUP BY estado", rs -> {
            m.put(rs.getString(1), Integer.valueOf(rs.getInt(2)));
            return null;
        });
        return m;
    }

    @Override
    public List<OrdenTrabajo> listar(String estado, String mias, Permisos p, int idUsuario) throws SQLException {
        String condMias = "SOLICITADAS".equals(mias) ? " AND o.id_usuario_solic = " + idUsuario
                : "A_CARGO".equals(mias) ? " AND o.id_usuario_responsable = " + idUsuario : "";
        return lista("SELECT o.id_ot, o.numero_ot, o.fecha_solicitud, o.fecha_creacion, o.titulo, o.estado, o.fecha_finalizacion, o.fecha_anulacion_fin, "
                + "       o.id_centro, c.nombre AS centro, cc.codigo AS cc_codigo, cc.descripcion AS cc_descripcion, "
                + "       u.nombre || ' ' || u.apellido AS solicitante, uc.nombre AS centro_solicitante, "
                + "       ur.nombre || ' ' || ur.apellido AS responsable, o.id_usuario_responsable, o.id_usuario_solic, o.id_usuario_finaliza, "
                + "       (SELECT COUNT(*) FROM detalle_ot d WHERE d.id_ot = o.id_ot) AS items, "
                + "       (SELECT COALESCE(SUM(d.cantidad), 0) FROM detalle_ot d WHERE d.id_ot = o.id_ot) AS unidades, "
                + "       (SELECT COUNT(*) FROM detalle_ot d LEFT JOIN remisiones r ON r.id_remision = d.id_remision AND r.anulada = false "
                + "         WHERE d.id_ot = o.id_ot AND r.id_remision IS NULL) AS sin_salida, "
                + "       (SELECT COUNT(*) FROM remisiones r WHERE r.id_ot = o.id_ot AND r.anulada = false "
                + "         AND r.tipo_remision IN ('SALIDA_INSUMOS', 'SALIDA_ACTIVOS')) AS salidas "
                + "FROM ot o "
                + "LEFT JOIN centros c ON c.id_centro = o.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo "
                + "LEFT JOIN usuarios u ON u.id_usuario = o.id_usuario_solic "
                + "LEFT JOIN centros uc ON uc.id_centro = u.centro_asignado "
                + "LEFT JOIN usuarios ur ON ur.id_usuario = o.id_usuario_responsable "
                + "WHERE (? = '' OR (? = 'ABIERTAS' AND o.estado IN ('PENDIENTE', 'EN_PROCESO')) OR o.estado = ?) AND " + visible(p, idUsuario) + condMias + " "
                + "ORDER BY o.fecha_solicitud DESC, o.id_ot DESC", rs -> {
                    OrdenTrabajo o = new OrdenTrabajo();
                    o.id = rs.getInt("id_ot");
                    o.numero = t(rs, "numero_ot");
                    o.fecha = fecha(rs, "fecha_solicitud");
                    o.titulo = t(rs, "titulo");
                    o.estado = t(rs, "estado");
                    o.idCentro = rs.getInt("id_centro");
                    o.centro = t(rs, "centro");
                    o.centroCosto = Comun.nombreCC(rs.getString("cc_codigo"), rs.getString("cc_descripcion"));
                    o.solicitante = t(rs, "solicitante");
                    o.centroSolicitante = t(rs, "centro_solicitante");
                    o.responsable = t(rs, "responsable");
                    o.idSolicitante = rs.getInt("id_usuario_solic");
                    o.idResponsable = rs.getInt("id_usuario_responsable");
                    o.idFinaliza = enteroO0(rs, "id_usuario_finaliza");
                    o.items = rs.getInt("items");
                    o.unidades = rs.getInt("unidades");
                    o.sinSalida = rs.getInt("sin_salida");
                    o.salidas = rs.getInt("salidas");
                    o.fechaCreacion = rs.getTimestamp("fecha_creacion");
                    o.fechaFinalizacion = rs.getTimestamp("fecha_finalizacion");
                    o.fechaAnulacionFin = rs.getTimestamp("fecha_anulacion_fin");
                    return o;
                }, estado, estado, estado);
    }

    @Override
    public OrdenTrabajo cabecera(int idOT) throws SQLException {
        return uno("SELECT o.*, c.nombre AS centro, cc.codigo AS cc_codigo, cc.descripcion AS cc_descripcion, "
                + "       u.nombre || ' ' || u.apellido AS solicitante, u.email AS solicitante_email, usc.nombre AS centro_solicitante, "
                + "       ur.nombre || ' ' || ur.apellido AS responsable, ur.email AS responsable_email, "
                + "       uf.nombre || ' ' || uf.apellido AS finalizado_por, "
                + "       ua.nombre || ' ' || ua.apellido AS anulado_por "
                + "FROM ot o "
                + "LEFT JOIN centros c ON c.id_centro = o.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo "
                + "LEFT JOIN usuarios u ON u.id_usuario = o.id_usuario_solic "
                + "LEFT JOIN centros usc ON usc.id_centro = u.centro_asignado "
                + "LEFT JOIN usuarios ur ON ur.id_usuario = o.id_usuario_responsable "
                + "LEFT JOIN usuarios uf ON uf.id_usuario = o.id_usuario_finaliza "
                + "LEFT JOIN usuarios ua ON ua.id_usuario = o.id_usuario_anula_fin "
                + "WHERE o.id_ot = ?", rs -> {
                    OrdenTrabajo o = new OrdenTrabajo();
                    o.id = rs.getInt("id_ot");
                    o.numero = t(rs, "numero_ot");
                    o.fecha = fecha(rs, "fecha_solicitud");
                    o.titulo = t(rs, "titulo");
                    o.descripcion = t(rs, "descripcion");
                    o.estado = t(rs, "estado");
                    o.idCentro = rs.getInt("id_centro");
                    o.idSolicitante = rs.getInt("id_usuario_solic");
                    o.idResponsable = enteroO0(rs, "id_usuario_responsable");
                    o.idFinaliza = enteroO0(rs, "id_usuario_finaliza");
                    o.responsable = t(rs, "responsable");
                    o.responsableEmail = t(rs, "responsable_email");
                    o.centroSolicitante = t(rs, "centro_solicitante");
                    o.idCentroCosto = rs.getInt("id_centro_costo");
                    o.centro = t(rs, "centro");
                    o.centroCosto = Comun.nombreCC(rs.getString("cc_codigo"), rs.getString("cc_descripcion"));
                    o.solicitante = t(rs, "solicitante");
                    o.solicitanteEmail = t(rs, "solicitante_email");
                    o.fechaCreacion = rs.getTimestamp("fecha_creacion");
                    o.fechaFinalizacion = rs.getTimestamp("fecha_finalizacion");
                    o.finalizadoPor = t(rs, "finalizado_por");
                    o.observacionFinalizacion = t(rs, "observacion_finalizacion");
                    o.fechaAnulacionFin = rs.getTimestamp("fecha_anulacion_fin");
                    o.anuladoPor = t(rs, "anulado_por");
                    o.motivoAnulacionFin = t(rs, "motivo_anulacion_fin");
                    return o;
                }, idOT);
    }

    @Override
    public List<OrdenTrabajo.Linea> lineas(int idOT) throws SQLException {
        return lista("SELECT d.id_detalle_ot, d.cantidad, d.precio_unitario, d.subtotal, d.fecha_suministro, "
                + "       i.codigo, i.descripcion, um.abreviatura, a.nombre AS almacen, ca.nombre AS centro_almacen, "
                + "       us.nombre || ' ' || us.apellido AS suministrado_por, r.numero_remision "
                + "FROM detalle_ot d "
                + "LEFT JOIN remisiones r ON r.id_remision = d.id_remision AND r.anulada = false "
                + "LEFT JOIN insumos i ON i.id_insumo = d.id_insumo "
                + "LEFT JOIN unidad_medida um ON um.id_unidad_medida = i.id_unidad_medida "
                + "LEFT JOIN almacenes a ON a.id_almacen = d.id_almacen "
                + "LEFT JOIN centros ca ON ca.id_centro = a.id_centro "
                + "LEFT JOIN usuarios us ON us.id_usuario = d.id_usuario_suministra "
                + "WHERE d.id_ot = ? ORDER BY d.id_detalle_ot", rs -> {
                    OrdenTrabajo.Linea l = new OrdenTrabajo.Linea();
                    Timestamp fs = rs.getTimestamp("fecha_suministro");
                    l.id = rs.getInt("id_detalle_ot");
                    l.codigo = t(rs, "codigo");
                    l.descripcion = t(rs, "descripcion");
                    l.unidad = t(rs, "abreviatura");
                    l.cantidad = rs.getInt("cantidad");
                    l.almacen = t(rs, "almacen");
                    l.centroAlmacen = t(rs, "centro_almacen");
                    l.fecha = fs == null ? "" : fs.toString().substring(0, 16);
                    l.suministradoPor = t(rs, "suministrado_por");
                    BigDecimal sub = rs.getBigDecimal("subtotal");
                    l.subtotal = sub == null ? "0" : sub.toPlainString();
                    l.remision = t(rs, "numero_remision");
                    return l;
                }, idOT);
    }

    @Override
    public List<OrdenTrabajo.Remision> remisiones(int idOT) throws SQLException {
        return lista("SELECT r.id_remision, r.numero_remision, r.tipo_remision, r.fecha_hora, r.anulada, r.motivo_anulacion, "
                + "       TRIM(COALESCE(r.transportista_nombre, '') || ' ' || COALESCE(r.transportista_apellido, '')) AS transportista "
                + "FROM remisiones r WHERE r.id_ot = ? ORDER BY r.fecha_hora DESC, r.id_remision DESC", rs -> {
                    OrdenTrabajo.Remision r = new OrdenTrabajo.Remision();
                    r.id = rs.getInt("id_remision");
                    r.numero = t(rs, "numero_remision");
                    r.tipo = t(rs, "tipo_remision");
                    r.fecha = rs.getTimestamp("fecha_hora");
                    r.anulada = rs.getBoolean("anulada");
                    r.motivoAnulacion = t(rs, "motivo_anulacion");
                    r.transportista = t(rs, "transportista");
                    return r;
                }, idOT);
    }

    @Override
    public List<OrdenTrabajo.Evento> eventos(int idOT) throws SQLException {
        return lista("SELECT e.fecha, e.tipo, e.detalle, u.nombre || ' ' || u.apellido AS usuario "
                + "FROM ot_eventos e LEFT JOIN usuarios u ON u.id_usuario = e.id_usuario "
                + "WHERE e.id_ot = ? ORDER BY e.fecha DESC, e.id_evento DESC", rs -> {
                    OrdenTrabajo.Evento e = new OrdenTrabajo.Evento();
                    e.fecha = rs.getTimestamp("fecha").toString().substring(0, 16);
                    e.tipo = t(rs, "tipo");
                    e.detalle = t(rs, "detalle");
                    e.usuario = t(rs, "usuario");
                    return e;
                }, idOT);
    }

    @Override
    public List<OrdenTrabajo> abiertas(Permisos p) throws SQLException {
        return lista("SELECT id_ot, numero_ot, titulo, estado FROM ot WHERE estado IN ('PENDIENTE', 'EN_PROCESO') AND "
                + p.sqlCentros("id_centro") + " ORDER BY fecha_solicitud, id_ot", rs -> {
                    OrdenTrabajo o = new OrdenTrabajo();
                    o.id = rs.getInt("id_ot");
                    o.numero = t(rs, "numero_ot");
                    o.titulo = t(rs, "titulo");
                    o.estado = rs.getString("estado");
                    return o;
                });
    }

    @Override
    public List<InsumoDisponible> insumosActivosConTotal() throws SQLException {
        return lista("SELECT i.id_insumo, i.codigo, i.descripcion, COALESCE(SUM(s.stock_actual), 0) AS total "
                + "FROM insumos i LEFT JOIN stock_almacen s ON s.id_insumo = i.id_insumo "
                + "WHERE i.activo = true GROUP BY i.id_insumo, i.codigo, i.descripcion ORDER BY i.descripcion",
                rs -> new InsumoDisponible(rs.getInt("id_insumo"), rs.getString("codigo"), rs.getString("descripcion"), rs.getLong("total")));
    }

    @Override
    public List<AlmacenStock> almacenesConStock(int idInsumo, Permisos p) throws SQLException {
        return lista("SELECT a.id_almacen, a.nombre AS almacen, c.nombre AS centro, s.stock_actual "
                + "FROM stock_almacen s INNER JOIN almacenes a ON a.id_almacen = s.id_almacen "
                + "LEFT JOIN centros c ON c.id_centro = a.id_centro "
                + "WHERE s.id_insumo = ? AND s.stock_actual > 0 AND a.activo = true AND " + p.sqlCentros("a.id_centro") + " ORDER BY c.nombre, a.nombre",
                rs -> new AlmacenStock(rs.getInt("id_almacen"), t(rs, "almacen"), t(rs, "centro"), rs.getInt("stock_actual")), idInsumo);
    }

    // Validaciones

    @Override
    public boolean centroActivo(int idCentro) throws SQLException {
        return existe("SELECT 1 FROM centros WHERE id_centro = ? AND activo = true", idCentro);
    }

    @Override
    public boolean centroCostoDelCentro(int idCentroCosto, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM centros_costo WHERE id_centro_costo = ? AND id_centro = ? AND activo = true", idCentroCosto, idCentro);
    }

    @Override
    public boolean responsableValido(int idUsuario, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol AND r.activo = true "
                + "WHERE u.id_usuario = ? AND " + RESPONSABLE_POSIBLE + " AND " + TRABAJA_EN_CENTRO, idUsuario, idCentro, idCentro);
    }

    @Override
    public boolean tieneLineas(int idOT) throws SQLException {
        return existe("SELECT 1 FROM detalle_ot WHERE id_ot = ?", idOT);
    }

    @Override
    public boolean tieneRemisiones(int idOT) throws SQLException {
        return existe("SELECT 1 FROM remisiones WHERE id_ot = ?", idOT);
    }

    @Override
    public boolean pendientesDeSalida(int idOT) throws SQLException {
        return existe(PENDIENTES_SALIDA, idOT);
    }

    @Override
    public boolean tieneSalida(int idOT) throws SQLException {
        return existe("SELECT 1 FROM remisiones WHERE id_ot = ? AND anulada = false AND tipo_remision IN ('SALIDA_INSUMOS', 'SALIDA_ACTIVOS')", idOT);
    }

    @Override
    public boolean almacenDeSusCentros(int idAlmacen, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM almacenes WHERE id_almacen = ? AND " + p.sqlCentros("id_centro"), idAlmacen);
    }

    @Override
    public Insumo insumoActivo(int idInsumo) throws SQLException {
        return uno("SELECT codigo, descripcion, COALESCE(precio_compra, 0) FROM insumos WHERE id_insumo = ? AND activo = true",
                rs -> new Insumo(idInsumo, rs.getString(1), rs.getString(2), rs.getBigDecimal(3), null, true), idInsumo);
    }

    // Nombres

    @Override
    public String nombreCentro(int idCentro) throws SQLException {
        String n = uno("SELECT nombre FROM centros WHERE id_centro = ?", rs -> String.valueOf(rs.getString(1)), idCentro);
        return n != null ? n : String.valueOf(idCentro);
    }

    @Override
    public String nombreUsuario(int idUsuario) throws SQLException {
        String n = uno("SELECT nombre || ' ' || apellido FROM usuarios WHERE id_usuario = ?", rs -> String.valueOf(rs.getString(1)), idUsuario);
        return n != null ? n : "usuario " + idUsuario;
    }

    @Override
    public String nombreAlmacen(int idAlmacen) throws SQLException {
        String n = uno("SELECT nombre FROM almacenes WHERE id_almacen = ?", rs -> String.valueOf(rs.getString(1)), idAlmacen);
        return n != null ? n : "";
    }

    @Override
    public String[] tituloYDescripcion(int idOT) throws SQLException {
        return uno("SELECT titulo, COALESCE(descripcion, '') FROM ot WHERE id_ot = ?",
                rs -> new String[]{rs.getString(1), rs.getString(2)}, idOT);
    }

    // Escritura

    @Override
    public String siguienteNumero() throws SQLException {
        return uno("INSERT INTO correlativo_ot (fecha, ultimo) VALUES (CURRENT_DATE, 1) "
                + "ON CONFLICT (fecha) DO UPDATE SET ultimo = correlativo_ot.ultimo + 1 RETURNING ultimo, fecha",
                rs -> "OT-" + new java.text.SimpleDateFormat("yyyyMMdd").format(rs.getDate("fecha")) + "-" + String.format("%04d", rs.getInt("ultimo")));
    }

    @Override
    public Alta insertar(String numero, int idCentro, int idCentroCosto, String titulo, String descripcion,
            int idSolicitante, Integer idResponsable) throws SQLException {
        return uno("INSERT INTO ot (numero_ot, fecha_solicitud, fecha_creacion, id_centro, id_centro_costo, titulo, descripcion, estado, id_usuario_solic, id_usuario_responsable) "
                + "VALUES (?, CURRENT_DATE, CURRENT_TIMESTAMP, ?, ?, ?, ?, 'PENDIENTE', ?, ?) RETURNING id_ot, fecha_creacion",
                rs -> new Alta(rs.getInt(1), rs.getTimestamp(2)),
                numero, idCentro, idCentroCosto, titulo, texto(descripcion), idSolicitante, entero(idResponsable));
    }

    @Override
    public OrdenTrabajo.Bloqueo bloquear(int idOT) throws SQLException {
        return uno("SELECT estado, id_centro, numero_ot, id_centro_costo, id_usuario_solic, id_usuario_responsable, id_usuario_finaliza FROM ot WHERE id_ot = ? FOR UPDATE",
                rs -> {
                    OrdenTrabajo.Bloqueo b = new OrdenTrabajo.Bloqueo();
                    b.estado = rs.getString(1);
                    b.idCentro = rs.getInt(2);
                    b.numero = rs.getString(3);
                    b.idCentroCosto = enteroONull(rs, "id_centro_costo");
                    b.idSolicitante = enteroONull(rs, "id_usuario_solic");
                    b.idResponsable = enteroONull(rs, "id_usuario_responsable");
                    b.idFinaliza = enteroONull(rs, "id_usuario_finaliza");
                    return b;
                }, idOT);
    }

    @Override
    public void actualizar(int idOT, String titulo, String descripcion, int idCentroCosto, int idCentro, Integer idResponsable) throws SQLException {
        ejecutar("UPDATE ot SET titulo = ?, descripcion = ?, id_centro_costo = ?, id_centro = ?, id_usuario_responsable = ? WHERE id_ot = ?",
                titulo, texto(descripcion), idCentroCosto, idCentro, entero(idResponsable), idOT);
    }

    @Override
    public void eliminar(int idOT) throws SQLException {
        ejecutar("DELETE FROM ot WHERE id_ot = ?", idOT);
    }

    @Override
    public void insertarLinea(int idOT, int idInsumo, int cantidad, int idAlmacen, BigDecimal precio, int idUsuario) throws SQLException {
        ejecutar("INSERT INTO detalle_ot (id_ot, id_insumo, cantidad, id_almacen, precio_unitario, fecha_suministro, id_usuario_suministra) "
                + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)", idOT, idInsumo, cantidad, idAlmacen, precio, idUsuario);
    }

    @Override
    public OrdenTrabajo.LineaSuministrada lineaSuministrada(int idDetalle) throws SQLException {
        return uno("SELECT d.id_ot, d.id_insumo, d.id_almacen, d.cantidad, d.precio_unitario, i.codigo, i.descripcion, r.numero_remision "
                + "FROM detalle_ot d LEFT JOIN insumos i ON i.id_insumo = d.id_insumo "
                + "LEFT JOIN remisiones r ON r.id_remision = d.id_remision AND r.anulada = false "
                + "WHERE d.id_detalle_ot = ?", rs -> {
                    OrdenTrabajo.LineaSuministrada l = new OrdenTrabajo.LineaSuministrada();
                    l.idOt = rs.getInt("id_ot");
                    l.idInsumo = rs.getInt("id_insumo");
                    l.idAlmacen = rs.getInt("id_almacen");
                    l.cantidad = rs.getInt("cantidad");
                    l.codigo = t(rs, "codigo");
                    l.descripcion = t(rs, "descripcion");
                    l.precio = rs.getBigDecimal("precio_unitario");
                    l.remision = rs.getString("numero_remision");
                    return l;
                }, idDetalle);
    }

    @Override
    public void eliminarLinea(int idDetalle) throws SQLException {
        ejecutar("DELETE FROM detalle_ot WHERE id_detalle_ot = ?", idDetalle);
    }

    @Override
    public void marcarEnProceso(int idOT) throws SQLException {
        ejecutar("UPDATE ot SET estado = 'EN_PROCESO' WHERE id_ot = ?", idOT);
    }

    @Override
    public void recalcularEstado(int idOT) throws SQLException {
        ejecutar("UPDATE ot SET estado = CASE WHEN EXISTS (SELECT 1 FROM detalle_ot WHERE id_ot = ?) THEN 'EN_PROCESO' ELSE 'PENDIENTE' END WHERE id_ot = ?",
                idOT, idOT);
    }

    @Override
    public void finalizar(int idOT, int idUsuario, String observacion) throws SQLException {
        ejecutar("UPDATE ot SET estado = 'FINALIZADA', fecha_finalizacion = CURRENT_TIMESTAMP, id_usuario_finaliza = ?, observacion_finalizacion = ? WHERE id_ot = ?",
                idUsuario, texto(observacion), idOT);
    }

    @Override
    public void reabrir(int idOT, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE ot SET estado = CASE WHEN EXISTS (SELECT 1 FROM detalle_ot WHERE id_ot = ?) THEN 'EN_PROCESO' ELSE 'PENDIENTE' END, "
                + "fecha_finalizacion = NULL, id_usuario_finaliza = NULL, observacion_finalizacion = NULL, "
                + "fecha_anulacion_fin = CURRENT_TIMESTAMP, id_usuario_anula_fin = ?, motivo_anulacion_fin = ? WHERE id_ot = ?",
                idOT, idUsuario, motivo, idOT);
    }

    @Override
    public void evento(int idOT, String tipo, int idUsuario, String detalle) throws SQLException {
        ejecutar("INSERT INTO ot_eventos (id_ot, tipo, id_usuario, detalle) VALUES (?, ?, ?, ?)",
                idOT, tipo, idUsuario, detalle != null && detalle.length() > 4000 ? detalle.substring(0, 4000) : detalle);
    }

    @Override
    public Timestamp ahora() throws SQLException {
        return uno("SELECT CURRENT_TIMESTAMP", rs -> rs.getTimestamp(1));
    }
}
