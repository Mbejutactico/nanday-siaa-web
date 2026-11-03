package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.SolicitudActivoDAO;
import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.LineaSolicitudActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.SolicitudActivo;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Comun;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SolicitudActivoDAOJdbc extends JdbcBase implements SolicitudActivoDAO {

    public SolicitudActivoDAOJdbc(Connection conn) {
        super(conn);
    }

    private static String t(ResultSet rs, String columna) throws SQLException {
        return Comun.txt(rs.getString(columna));
    }

    private static String t(ResultSet rs, int columna) throws SQLException {
        return Comun.txt(rs.getString(columna));
    }

    /** 0 = NULL. */
    private static Object id(int v) {
        return v == 0 ? Nulo.ENTERO : Integer.valueOf(v);
    }

    /** Texto sin espacios sobrantes; vacío o null = NULL. */
    private static Object recortado(String v) {
        return v == null || v.trim().isEmpty() ? Nulo.TEXTO : v.trim();
    }

    /** Solicitudes visibles: salen de sus centros o van hacia ellos (requiere alias s). */
    static String condSolicitud(Permisos p) {
        if (p.todosLosCentros()) {
            return "TRUE";
        }
        return "(" + p.sqlCentros("s.id_centro_destino") + " OR EXISTS (SELECT 1 FROM solicitudes_activos_detalle dx WHERE dx.id_solicitud = s.id_solicitud AND "
                + p.sqlCentros("dx.id_centro_origen") + "))";
    }

    /** Recepción: traslados hacia sus centros y retornos de reparación de sus centros (alias s y d). */
    static String condRecepcion(Permisos p) {
        if (p.todosLosCentros()) {
            return "TRUE";
        }
        return "((s.tipo = 'REPARACION' AND " + p.sqlCentros("d.id_centro_origen") + ") OR (s.tipo <> 'REPARACION' AND " + p.sqlCentros("s.id_centro_destino") + "))";
    }

    // Combos y selección

    @Override
    public List<Opcion> otsAbiertas() throws SQLException {
        return lista("SELECT o.id_ot, o.numero_ot, o.titulo, c.nombre, cc.codigo FROM ot o LEFT JOIN centros c ON c.id_centro = o.id_centro "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = o.id_centro_costo WHERE o.estado IN ('PENDIENTE', 'EN_PROCESO') ORDER BY o.fecha_solicitud, o.id_ot",
                rs -> new Opcion(rs.getInt(1), t(rs, 2) + " - " + t(rs, 3) + " (" + t(rs, 4) + " / " + t(rs, 5) + ")"));
    }

    @Override
    public List<Activo> activosParaSolicitud(Permisos p) throws SQLException {
        return lista("SELECT e.id_equipo, e.serial_activo, e.numero_serie, e.estado, i.codigo, i.descripcion, c.nombre AS centro, "
                + "       e.id_centro_actual, cc.codigo AS cc_codigo, cc.descripcion AS cc_desc, "
                + "       (SELECT s.numero FROM solicitudes_activos_detalle d JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud "
                + "         WHERE d.id_equipo = e.id_equipo AND d.estado_linea IN ('PENDIENTE', 'EN_CURSO') LIMIT 1) AS solicitud_abierta "
                + "FROM equipos e LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
                + "LEFT JOIN centros c ON c.id_centro = e.id_centro_actual "
                + "LEFT JOIN centros_costo cc ON cc.id_centro_costo = e.id_centro_costo_actual "
                + "WHERE e.estado NOT IN ('SOLICITADO', 'ANULADO', 'BAJA') AND " + p.sqlCentros("e.id_centro_actual") + " ORDER BY c.nombre, cc.codigo, e.serial_activo",
                rs -> {
                    Activo a = new Activo();
                    a.id = rs.getInt("id_equipo");
                    a.serial = rs.getString("serial_activo");
                    a.numeroSerie = t(rs, "numero_serie");
                    a.estado = rs.getString("estado");
                    a.codigo = t(rs, "codigo");
                    a.descripcion = t(rs, "descripcion");
                    a.centro = t(rs, "centro");
                    a.idCentro = rs.getInt("id_centro_actual");
                    a.centroCosto = Comun.nombreCC(rs.getString("cc_codigo"), rs.getString("cc_desc"));
                    a.solicitudAbierta = rs.getString("solicitud_abierta");
                    return a;
                });
    }

    // Crear solicitud

    @Override
    public boolean proveedorActivo(int idProveedor) throws SQLException {
        return existe("SELECT 1 FROM proveedores WHERE id_proveedor = ? AND activo = true", idProveedor);
    }

    @Override
    public boolean centroCostoActivoDelCentro(int idCentroCosto, int idCentro) throws SQLException {
        return existe("SELECT 1 FROM centros_costo WHERE id_centro_costo = ? AND id_centro = ? AND activo = true", idCentroCosto, idCentro);
    }

    @Override
    public int[] destinoDeOtAbierta(int idOT) throws SQLException {
        return uno("SELECT id_centro, id_centro_costo FROM ot WHERE id_ot = ? AND estado IN ('PENDIENTE', 'EN_PROCESO')",
                rs -> new int[]{rs.getInt(1), rs.getInt(2)}, idOT);
    }

    @Override
    public String siguienteNumero(boolean baja) throws SQLException {
        return uno(baja ? "SELECT 'BA-' || lpad(nextval('public.seq_baja_activo')::text, 6, '0')"
                : "SELECT 'SA-' || lpad(nextval('public.seq_solicitud_activo')::text, 6, '0')", rs -> rs.getString(1));
    }

    @Override
    public int insertar(String numero, String tipo, int idUsuario, int idProveedor, int idCentroDestino, int idCcDestino, int idOT,
            String motivoBaja, String observacion, String[] tr) throws SQLException {
        return insertarConId("INSERT INTO solicitudes_activos (numero, tipo, id_usuario_solicita, id_proveedor, id_centro_destino, id_centro_costo_destino, id_ot, "
                + "motivo_baja, observacion, transportista_nombre, transportista_apellido, transportista_ci, vehiculo_marca, vehiculo_modelo, vehiculo_chapa) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_solicitud",
                numero, tipo, idUsuario, id(idProveedor), id(idCentroDestino), id(idCcDestino), id(idOT),
                recortado(motivoBaja), recortado(observacion), recortado(tr[0]), recortado(tr[1]), recortado(tr[2]),
                recortado(tr[3]), recortado(tr[4]), recortado(tr[5]));
    }

    @Override
    public Activo bloquearActivo(int idEquipo) throws SQLException {
        return uno("SELECT estado, id_centro_actual, id_centro_costo_actual, serial_activo FROM equipos WHERE id_equipo = ? FOR UPDATE", rs -> {
            Activo a = new Activo();
            a.id = idEquipo;
            a.estado = rs.getString(1);
            a.idCentro = rs.getInt(2);
            int cc = rs.getInt(3);
            a.idCentroCosto = rs.wasNull() ? 0 : cc;
            a.serial = rs.getString(4);
            return a;
        }, idEquipo);
    }

    @Override
    public boolean enSolicitudAbierta(int idEquipo) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos_detalle WHERE id_equipo = ? AND estado_linea IN ('PENDIENTE', 'EN_CURSO')", idEquipo);
    }

    @Override
    public void insertarLinea(int idSolicitud, int idEquipo, int idCentroOrigen, int idCcOrigen, String estadoOrigen) throws SQLException {
        ejecutar("INSERT INTO solicitudes_activos_detalle (id_solicitud, id_equipo, id_centro_origen, id_centro_costo_origen, estado_origen) VALUES (?, ?, ?, ?, ?)",
                idSolicitud, idEquipo, id(idCentroOrigen), id(idCcOrigen), estadoOrigen);
    }

    // Consultas

    @Override
    public List<SolicitudActivo> listar(String estado, Permisos p) throws SQLException {
        return lista("SELECT s.id_solicitud, s.numero, s.tipo, s.estado, s.fecha, s.observacion, s.motivo_baja, s.motivo_rechazo, s.motivo_anulacion, "
                + "       s.fecha_resolucion, s.fecha_anulacion, s.id_remision, r.numero_remision, "
                + "       us.nombre || ' ' || us.apellido AS solicitante, ur.nombre || ' ' || ur.apellido AS resolvio, "
                + "       p.razon_social AS proveedor, cd.nombre AS centro_destino, ccd.codigo AS cc_destino, o.numero_ot, "
                + "       TRIM(COALESCE(s.transportista_nombre, '') || ' ' || COALESCE(s.transportista_apellido, '')) AS transportista, "
                + "       (SELECT COUNT(*) FROM solicitudes_activos_detalle d WHERE d.id_solicitud = s.id_solicitud) AS activos, "
                + "       (SELECT COUNT(*) FROM solicitudes_activos_detalle d WHERE d.id_solicitud = s.id_solicitud AND d.estado_linea IN ('RECIBIDO', 'RECHAZADO_DESTINO')) AS recibidos "
                + "FROM solicitudes_activos s LEFT JOIN remisiones r ON r.id_remision = s.id_remision "
                + "LEFT JOIN usuarios us ON us.id_usuario = s.id_usuario_solicita LEFT JOIN usuarios ur ON ur.id_usuario = s.id_usuario_resuelve "
                + "LEFT JOIN proveedores p ON p.id_proveedor = s.id_proveedor LEFT JOIN centros cd ON cd.id_centro = s.id_centro_destino "
                + "LEFT JOIN centros_costo ccd ON ccd.id_centro_costo = s.id_centro_costo_destino LEFT JOIN ot o ON o.id_ot = s.id_ot "
                + "WHERE (? = '' OR s.estado = ?) AND " + condSolicitud(p) + " ORDER BY s.fecha DESC, s.id_solicitud DESC", rs -> {
                    SolicitudActivo s = new SolicitudActivo();
                    s.id = rs.getInt("id_solicitud");
                    s.numero = t(rs, "numero");
                    s.tipo = rs.getString("tipo");
                    s.estado = t(rs, "estado");
                    s.fecha = rs.getTimestamp("fecha");
                    s.observacion = t(rs, "observacion");
                    s.motivoBaja = t(rs, "motivo_baja");
                    s.motivoRechazo = t(rs, "motivo_rechazo");
                    s.motivoAnulacion = t(rs, "motivo_anulacion");
                    s.fechaResolucion = rs.getTimestamp("fecha_resolucion");
                    s.fechaAnulacion = rs.getTimestamp("fecha_anulacion");
                    s.idRemision = rs.getInt("id_remision");
                    s.remision = t(rs, "numero_remision");
                    s.solicitante = t(rs, "solicitante");
                    s.resolvio = t(rs, "resolvio");
                    s.proveedor = t(rs, "proveedor");
                    s.centroDestino = t(rs, "centro_destino");
                    s.ccDestino = t(rs, "cc_destino");
                    s.numeroOt = rs.getString("numero_ot");
                    s.transportista = t(rs, "transportista");
                    s.activos = rs.getInt("activos");
                    s.recibidos = rs.getInt("recibidos");
                    return s;
                }, estado, estado);
    }

    @Override
    public List<LineaSolicitudActivo> lineasVisibles(int idSolicitud, Permisos p) throws SQLException {
        return lista("SELECT e.serial_activo, e.numero_serie, i.codigo, i.descripcion, co.nombre AS centro_origen, cco.codigo AS cc_origen, d.estado_origen, d.estado_linea, "
                + "       d.fecha_recepcion, d.observacion_recepcion "
                + "FROM solicitudes_activos_detalle d JOIN equipos e ON e.id_equipo = d.id_equipo LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base "
                + "LEFT JOIN centros co ON co.id_centro = d.id_centro_origen LEFT JOIN centros_costo cco ON cco.id_centro_costo = d.id_centro_costo_origen "
                + "WHERE d.id_solicitud = ? AND EXISTS (SELECT 1 FROM solicitudes_activos s WHERE s.id_solicitud = d.id_solicitud AND " + condSolicitud(p) + ") ORDER BY d.id_detalle",
                rs -> {
                    LineaSolicitudActivo l = new LineaSolicitudActivo();
                    l.numeroActivo = t(rs, 1);
                    l.numeroSerie = t(rs, 2);
                    l.codigo = t(rs, 3);
                    l.descripcion = t(rs, 4);
                    l.centroOrigen = t(rs, 5);
                    l.ccOrigen = rs.getString(6);
                    l.estadoOrigen = t(rs, 7);
                    l.estadoLinea = t(rs, 8);
                    l.fechaRecepcion = rs.getTimestamp(9);
                    l.observacionRecepcion = t(rs, 10);
                    return l;
                }, idSolicitud);
    }

    @Override
    public List<LineaSolicitudActivo> enRecepcion(Permisos p) throws SQLException {
        return lista("SELECT d.id_detalle, s.numero, s.tipo, s.fecha_resolucion, r.numero_remision, e.serial_activo, e.numero_serie, i.descripcion, "
                + "       co.nombre AS centro_origen, cco.codigo AS cc_origen, cd.nombre AS centro_destino, ccd.codigo AS cc_destino, p.razon_social AS proveedor, o.numero_ot "
                + "FROM solicitudes_activos_detalle d JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "LEFT JOIN insumos i ON i.id_insumo = e.id_insumo_base LEFT JOIN remisiones r ON r.id_remision = s.id_remision "
                + "LEFT JOIN centros co ON co.id_centro = d.id_centro_origen LEFT JOIN centros_costo cco ON cco.id_centro_costo = d.id_centro_costo_origen "
                + "LEFT JOIN centros cd ON cd.id_centro = s.id_centro_destino LEFT JOIN centros_costo ccd ON ccd.id_centro_costo = s.id_centro_costo_destino "
                + "LEFT JOIN proveedores p ON p.id_proveedor = s.id_proveedor LEFT JOIN ot o ON o.id_ot = s.id_ot "
                + "WHERE d.estado_linea = 'EN_CURSO' AND s.tipo <> 'BAJA' AND " + condRecepcion(p) + " ORDER BY s.fecha_resolucion, d.id_detalle", rs -> {
                    LineaSolicitudActivo l = new LineaSolicitudActivo();
                    l.id = rs.getInt("id_detalle");
                    l.solicitud = t(rs, "numero");
                    l.tipo = rs.getString("tipo");
                    l.fechaResolucion = rs.getTimestamp("fecha_resolucion");
                    l.remision = t(rs, "numero_remision");
                    l.numeroActivo = t(rs, "serial_activo");
                    l.numeroSerie = t(rs, "numero_serie");
                    l.descripcion = t(rs, "descripcion");
                    l.centroOrigen = t(rs, "centro_origen");
                    l.ccOrigen = rs.getString("cc_origen");
                    l.centroDestino = t(rs, "centro_destino");
                    l.ccDestino = t(rs, "cc_destino");
                    l.proveedor = t(rs, "proveedor");
                    l.numeroOt = rs.getString("numero_ot");
                    return l;
                });
    }

    @Override
    public boolean lineaRecibiblePor(int idDetalle, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos_detalle d JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud "
                + "WHERE d.id_detalle = ? AND " + condRecepcion(p), idDetalle);
    }

    @Override
    public boolean visible(int idSolicitud, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos s WHERE s.id_solicitud = ? AND " + condSolicitud(p), idSolicitud);
    }

    // Resolver

    @Override
    public boolean tieneActivosDeSusCentros(int idSolicitud, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos_detalle d WHERE d.id_solicitud = ? AND " + p.sqlCentros("d.id_centro_origen"), idSolicitud);
    }

    @Override
    public boolean tieneActivosFueraDeSusCentros(int idSolicitud, Permisos p) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos_detalle d WHERE d.id_solicitud = ? AND (d.id_centro_origen IS NULL OR NOT ("
                + p.sqlCentros("d.id_centro_origen") + "))", idSolicitud);
    }

    @Override
    public SolicitudActivo bloquear(int idSolicitud) throws SQLException {
        return uno("SELECT tipo, estado, numero, id_proveedor, id_centro_destino, id_centro_costo_destino, id_ot FROM solicitudes_activos WHERE id_solicitud = ? FOR UPDATE",
                rs -> {
                    SolicitudActivo s = new SolicitudActivo();
                    s.id = idSolicitud;
                    s.tipo = rs.getString(1);
                    s.estado = rs.getString(2);
                    s.numero = rs.getString(3);
                    s.idProveedor = enteroONull(rs, "id_proveedor");
                    s.idCentroDestino = enteroONull(rs, "id_centro_destino");
                    s.idCcDestino = enteroONull(rs, "id_centro_costo_destino");
                    s.idOt = enteroONull(rs, "id_ot");
                    return s;
                }, idSolicitud);
    }

    @Override
    public void rechazar(int idSolicitud, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE solicitudes_activos SET estado = 'RECHAZADA', fecha_resolucion = CURRENT_TIMESTAMP, id_usuario_resuelve = ?, motivo_rechazo = ? WHERE id_solicitud = ?",
                idUsuario, motivo, idSolicitud);
    }

    @Override
    public void anularLineas(int idSolicitud) throws SQLException {
        ejecutar("UPDATE solicitudes_activos_detalle SET estado_linea = 'ANULADO' WHERE id_solicitud = ?", idSolicitud);
    }

    @Override
    public SolicitudActivo datosDeSalida(int idSolicitud) throws SQLException {
        return uno("SELECT s.transportista_nombre, s.transportista_apellido, s.transportista_ci, s.vehiculo_marca, s.vehiculo_modelo, s.vehiculo_chapa, "
                + "       p.razon_social, s.observacion FROM solicitudes_activos s LEFT JOIN proveedores p ON p.id_proveedor = s.id_proveedor WHERE s.id_solicitud = ?",
                rs -> {
                    SolicitudActivo s = new SolicitudActivo();
                    s.datosTransporte = new String[6];
                    for (int i = 0; i < 6; i++) {
                        s.datosTransporte[i] = rs.getString(i + 1);
                    }
                    s.proveedor = t(rs, 7);
                    s.observacion = rs.getString(8);
                    return s;
                }, idSolicitud);
    }

    @Override
    public String siguienteRemision() throws SQLException {
        return uno("SELECT 'REM-' || to_char(CURRENT_DATE, 'YYYY') || '-' || lpad(nextval('public.seq_remision')::text, 6, '0')", rs -> rs.getString(1));
    }

    @Override
    public int insertarRemision(String numero, Integer idOT, int idSolicitud, int idUsuario, String[] tr, String observacion) throws SQLException {
        return insertarConId("INSERT INTO remisiones (numero_remision, fecha_emision, fecha_hora, tipo_remision, id_ot, id_solicitud_activo, id_usuario_emite, "
                + "transportista_nombre, transportista_apellido, transportista_ci, vehiculo_marca, vehiculo_modelo, vehiculo_chapa, observacion, anulada) "
                + "VALUES (?, CURRENT_DATE, CURRENT_TIMESTAMP, 'SALIDA_ACTIVOS', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false) RETURNING id_remision",
                numero, entero(idOT), idSolicitud, idUsuario, recortado(tr[0]), recortado(tr[1]), recortado(tr[2]),
                recortado(tr[3]), recortado(tr[4]), recortado(tr[5]), recortado(observacion));
    }

    @Override
    public List<LineaSolicitudActivo> lineasPendientes(int idSolicitud) throws SQLException {
        return lista("SELECT d.id_detalle, d.id_equipo, d.id_centro_origen, d.id_centro_costo_origen, e.estado FROM solicitudes_activos_detalle d "
                + "JOIN equipos e ON e.id_equipo = d.id_equipo WHERE d.id_solicitud = ? AND d.estado_linea = 'PENDIENTE' FOR UPDATE OF e", rs -> {
                    LineaSolicitudActivo l = new LineaSolicitudActivo();
                    l.id = rs.getInt(1);
                    l.idEquipo = rs.getInt(2);
                    l.idCentroOrigen = rs.getInt(3);
                    l.idCcOrigen = rs.getInt(4);
                    l.estadoActual = rs.getString(5);
                    return l;
                }, idSolicitud);
    }

    @Override
    public void actualizarActivo(int idEquipo, String estado, boolean activo) throws SQLException {
        ejecutar("UPDATE equipos SET estado = ?, activo = ? WHERE id_equipo = ?", estado, Boolean.valueOf(activo), idEquipo);
    }

    @Override
    public void estadoLinea(int idDetalle, String estado) throws SQLException {
        ejecutar("UPDATE solicitudes_activos_detalle SET estado_linea = ? WHERE id_detalle = ?", estado, idDetalle);
    }

    @Override
    public void marcarAprobada(int idSolicitud, int idUsuario, Integer idRemision) throws SQLException {
        ejecutar("UPDATE solicitudes_activos SET estado = 'APROBADA', fecha_resolucion = CURRENT_TIMESTAMP, id_usuario_resuelve = ?, id_remision = ? WHERE id_solicitud = ?",
                idUsuario, entero(idRemision), idSolicitud);
    }

    @Override
    public void eventoOT(int idOT, String tipo, int idUsuario, String detalle) throws SQLException {
        ejecutar("INSERT INTO ot_eventos (id_ot, tipo, id_usuario, detalle) VALUES (?, ?, ?, ?)", idOT, tipo, idUsuario, detalle);
    }

    @Override
    public LineaSolicitudActivo lineaParaRecibir(int idDetalle) throws SQLException {
        return uno("SELECT d.id_equipo, d.id_centro_origen, d.id_centro_costo_origen, d.estado_origen, d.estado_linea, s.tipo, s.numero, "
                + "       s.id_centro_destino, s.id_centro_costo_destino, s.id_remision, e.estado, e.serial_activo, e.id_centro_actual, e.id_centro_costo_actual "
                + "FROM solicitudes_activos_detalle d JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud JOIN equipos e ON e.id_equipo = d.id_equipo "
                + "WHERE d.id_detalle = ? FOR UPDATE OF d, e", rs -> {
                    LineaSolicitudActivo l = new LineaSolicitudActivo();
                    l.id = idDetalle;
                    l.idEquipo = rs.getInt(1);
                    l.idCentroOrigen = rs.getInt(2);
                    l.idCcOrigen = rs.getInt(3);
                    l.estadoOrigen = rs.getString(4);
                    l.estadoLinea = rs.getString(5);
                    l.tipo = rs.getString(6);
                    l.solicitud = rs.getString(7);
                    l.idCentroDestino = rs.getInt(8);
                    l.idCcDestino = rs.getInt(9);
                    l.idRemision = rs.getInt(10);
                    l.estadoActual = rs.getString(11);
                    l.numeroActivo = rs.getString(12);
                    l.idCentroActual = rs.getInt(13);
                    l.idCcActual = rs.getInt(14);
                    return l;
                }, idDetalle);
    }

    @Override
    public void ubicarActivo(int idEquipo, String estado, Integer idCentro, Integer idCentroCosto) throws SQLException {
        ejecutar("UPDATE equipos SET estado = ?, id_centro_actual = ?, id_centro_costo_actual = ? WHERE id_equipo = ?",
                estado, entero(idCentro), entero(idCentroCosto), idEquipo);
    }

    @Override
    public void registrarRecepcion(int idDetalle, String estadoLinea, int idUsuario, String observacion) throws SQLException {
        ejecutar("UPDATE solicitudes_activos_detalle SET estado_linea = ?, fecha_recepcion = CURRENT_TIMESTAMP, id_usuario_recibe = ?, observacion_recepcion = ? WHERE id_detalle = ?",
                estadoLinea, idUsuario, texto(observacion), idDetalle);
    }

    @Override
    public boolean tieneLineasResueltas(int idSolicitud) throws SQLException {
        return existe("SELECT 1 FROM solicitudes_activos_detalle WHERE id_solicitud = ? AND estado_linea <> 'EN_CURSO'", idSolicitud);
    }

    @Override
    public List<LineaSolicitudActivo> lineasParaAnular(int idSolicitud) throws SQLException {
        return lista("SELECT d.id_detalle, d.id_equipo, d.id_centro_origen, d.id_centro_costo_origen, d.estado_origen, e.estado, s.id_remision "
                + "FROM solicitudes_activos_detalle d JOIN equipos e ON e.id_equipo = d.id_equipo JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud "
                + "WHERE d.id_solicitud = ? FOR UPDATE OF e", rs -> {
                    LineaSolicitudActivo l = new LineaSolicitudActivo();
                    l.id = rs.getInt(1);
                    l.idEquipo = rs.getInt(2);
                    l.idCentroOrigen = rs.getInt(3);
                    l.idCcOrigen = rs.getInt(4);
                    l.estadoOrigen = rs.getString(5);
                    l.estadoActual = rs.getString(6);
                    l.idRemision = rs.getInt(7);
                    return l;
                }, idSolicitud);
    }

    @Override
    public void marcarAnulada(int idSolicitud, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE solicitudes_activos SET estado = 'ANULADA', fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? WHERE id_solicitud = ?",
                idUsuario, motivo, idSolicitud);
    }

    @Override
    public void anularRemision(int idRemision, int idUsuario, String motivo) throws SQLException {
        ejecutar("UPDATE remisiones SET anulada = true, fecha_anulacion = CURRENT_TIMESTAMP, id_usuario_anula = ?, motivo_anulacion = ? WHERE id_remision = ?",
                idUsuario, motivo, idRemision);
    }

    @Override
    public int remisionDe(int idSolicitud) throws SQLException {
        Integer v = uno("SELECT COALESCE(id_remision, 0) FROM solicitudes_activos WHERE id_solicitud = ?", rs -> Integer.valueOf(rs.getInt(1)), idSolicitud);
        return v == null ? 0 : v.intValue();
    }

    @Override
    public String ahora() throws SQLException {
        return uno("SELECT to_char(CURRENT_TIMESTAMP, 'DD/MM/YYYY HH24:MI')", rs -> rs.getString(1));
    }
}
