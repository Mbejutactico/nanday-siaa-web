package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.NotificacionDAO;
import com.mycompany.proyectoprueba.modelo.Notificacion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class NotificacionDAOJdbc extends JdbcBase implements NotificacionDAO {

    public NotificacionDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public int stockBajoMinimo(Permisos p) throws SQLException {
        return entero("SELECT COUNT(*) FROM stock_almacen s JOIN insumos i ON i.id_insumo = s.id_insumo "
                + "JOIN almacenes a ON a.id_almacen = s.id_almacen "
                + "WHERE s.stock_minimo > 0 AND s.stock_actual <= s.stock_minimo AND i.activo = true AND " + p.sqlCentros("a.id_centro"));
    }

    /** Solo las que puede resolver: todos los activos salen de sus centros (misma regla que al aprobar). */
    @Override
    public int solicitudesPorAprobar(Permisos p) throws SQLException {
        String cond = p.todosLosCentros() ? "TRUE"
                : "EXISTS (SELECT 1 FROM solicitudes_activos_detalle d WHERE d.id_solicitud = s.id_solicitud AND " + p.sqlCentros("d.id_centro_origen") + ") "
                + "AND NOT EXISTS (SELECT 1 FROM solicitudes_activos_detalle d WHERE d.id_solicitud = s.id_solicitud "
                + "AND NOT (" + p.sqlCentros("COALESCE(d.id_centro_origen, 0)") + "))";
        return entero("SELECT COUNT(*) FROM solicitudes_activos s WHERE s.estado = 'PENDIENTE' AND " + cond);
    }

    /** Centros propios del usuario (principal y adicionales vigentes), aunque su rol vea todos. */
    private static String misCentros(Permisos p, String columna) {
        StringBuilder sb = new StringBuilder();
        for (Integer c : p.getCentros()) {
            sb.append(sb.length() == 0 ? "" : ",").append(c.intValue());
        }
        return sb.length() == 0 ? "FALSE" : columna + " IN (" + sb + ")";
    }

    @Override
    public int activosPorRecibir(Permisos p) throws SQLException {
        return entero("SELECT COUNT(*) FROM solicitudes_activos_detalle d JOIN solicitudes_activos s ON s.id_solicitud = d.id_solicitud "
                + "WHERE d.estado_linea = 'EN_CURSO' AND s.tipo <> 'BAJA' AND " + SolicitudActivoDAOJdbc.condRecepcion(p));
    }

    @Override
    public List<Notificacion> otAsignadas(int idUsuario, Permisos p) throws SQLException {
        return lista("SELECT * FROM ("
                + "  SELECT DISTINCT ON (o.id_ot) o.id_ot, o.numero_ot, o.titulo, c.nombre AS centro, e.tipo, e.fecha, e.id_evento "
                + "  FROM ot_eventos e JOIN ot o ON o.id_ot = e.id_ot LEFT JOIN centros c ON c.id_centro = o.id_centro "
                + "  WHERE e.fecha >= CURRENT_TIMESTAMP - INTERVAL '7 days' "
                + "    AND (e.id_usuario IS NULL OR e.id_usuario <> ?) "
                + "    AND o.estado IN ('PENDIENTE', 'EN_PROCESO') "
                + "    AND ((e.tipo IN ('CREACION', 'CAMBIO_CENTRO') AND " + misCentros(p, "o.id_centro") + ") "
                + "         OR (e.tipo = 'RESPONSABLE' AND o.id_usuario_responsable = ?)) "
                + "  ORDER BY o.id_ot, e.fecha DESC, e.id_evento DESC"
                + ") x ORDER BY x.fecha DESC LIMIT 10", rs -> {
                    Notificacion n = new Notificacion();
                    n.clase = "NOVEDAD";
                    n.tipo = "OT";
                    String numero = rs.getString("numero_ot");
                    String evento = rs.getString("tipo");
                    if ("RESPONSABLE".equals(evento)) {
                        n.texto = "Usted es responsable de la " + numero;
                    } else if ("CAMBIO_CENTRO".equals(evento)) {
                        n.texto = numero + " pasó a su centro";
                    } else {
                        n.texto = "Nueva " + numero + " para su centro";
                    }
                    String titulo = rs.getString("titulo");
                    String centro = rs.getString("centro");
                    n.detalle = (titulo == null ? "" : titulo) + (centro == null ? "" : " · " + centro);
                    n.url = "otListado.jsp?ot=" + rs.getInt("id_ot");
                    n.fecha = rs.getTimestamp("fecha");
                    n.clave = "OT-" + rs.getInt("id_evento");
                    return n;
                }, idUsuario, idUsuario);
    }

    @Override
    public List<Notificacion> solicitudesResueltas(int idUsuario) throws SQLException {
        return lista("SELECT s.id_solicitud, s.numero, s.tipo, s.estado, s.fecha_resolucion, s.motivo_rechazo, s.id_remision, r.numero_remision "
                + "FROM solicitudes_activos s LEFT JOIN remisiones r ON r.id_remision = s.id_remision "
                + "WHERE s.id_usuario_solicita = ? AND s.estado IN ('APROBADA', 'RECHAZADA') "
                + "  AND s.fecha_resolucion >= CURRENT_TIMESTAMP - INTERVAL '7 days' "
                + "  AND (s.id_usuario_resuelve IS NULL OR s.id_usuario_resuelve <> ?) "
                + "ORDER BY s.fecha_resolucion DESC LIMIT 10", rs -> {
                    Notificacion n = new Notificacion();
                    n.clase = "NOVEDAD";
                    n.tipo = "SOLICITUD";
                    boolean aprobada = "APROBADA".equals(rs.getString("estado"));
                    boolean baja = "BAJA".equals(rs.getString("tipo"));
                    n.texto = "Su solicitud " + rs.getString("numero") + (aprobada ? " fue aprobada" : " fue rechazada");
                    if (!aprobada) {
                        String motivo = rs.getString("motivo_rechazo");
                        n.detalle = motivo == null ? "" : "Motivo: " + motivo;
                        n.url = "activosFijosVista.jsp";
                    } else if (baja) {
                        n.detalle = "Baja aprobada · imprimir acta de baja";
                        n.url = "SolicitudActivoServlet?tipo=imprimirActaBaja&id=" + rs.getInt("id_solicitud");
                        n.ventanaNueva = true;
                    } else if (rs.getInt("id_remision") > 0) {
                        n.detalle = "Remisión " + rs.getString("numero_remision") + " · imprimir";
                        n.url = "InventarioServlet?tipo=imprimirRemision&id=" + rs.getInt("id_remision");
                        n.ventanaNueva = true;
                    } else {
                        n.detalle = "";
                        n.url = "activosFijosVista.jsp";
                    }
                    n.fecha = rs.getTimestamp("fecha_resolucion");
                    n.clave = "SA-" + rs.getInt("id_solicitud") + "-" + rs.getString("estado");
                    return n;
                }, idUsuario, idUsuario);
    }

    @Override
    public int loginsFallidosHoy() throws SQLException {
        if (!existe("SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'bitacora'")) {
            return 0;
        }
        return entero("SELECT COUNT(*) FROM bitacora WHERE accion = 'LOGIN_FALLIDO' AND fecha >= CURRENT_DATE");
    }

    @Override
    public Timestamp vistasHasta(int idUsuario) throws SQLException {
        return uno("SELECT notif_vistas_hasta FROM usuarios WHERE id_usuario = ?", rs -> rs.getTimestamp(1), idUsuario);
    }

    @Override
    public void marcarVistas(int idUsuario) throws SQLException {
        ejecutar("UPDATE usuarios SET notif_vistas_hasta = CURRENT_TIMESTAMP WHERE id_usuario = ?", idUsuario);
    }
}
