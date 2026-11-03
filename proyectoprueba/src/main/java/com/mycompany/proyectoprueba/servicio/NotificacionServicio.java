package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.NotificacionDAO;
import com.mycompany.proyectoprueba.dao.jdbc.NotificacionDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Notificacion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Campana de notificaciones. Cada aviso depende del permiso del rol:
 *   Stock bajo el mínimo      INVENTARIO o SOLICITUD_COMPRA
 *   Solicitudes por aprobar   ACTIVOS_APROBAR
 *   Activos por recibir       ACTIVOS_RECEPCION
 *   OT asignadas              OT_GESTION, OT_SUMINISTRAR u OT_FINALIZAR
 *   Resultado de mis pedidos  ACTIVOS
 *   Inicios de sesión fallidos de hoy (bitácora)   solo ADMINISTRADOR
 */
public class NotificacionServicio {

    private final NotificacionDAO dao;
    private final Permisos permisos;

    public NotificacionServicio(NotificacionDAO dao, Permisos permisos) {
        this.dao = dao;
        this.permisos = permisos;
    }

    public static NotificacionServicio con(Connection conn, Permisos permisos) {
        return new NotificacionServicio(new NotificacionDAOJdbc(conn), permisos);
    }

    public static boolean veStock(Permisos p) {
        return p.tieneAlguno(Permisos.INVENTARIO, Permisos.SOLICITUD_COMPRA);
    }

    public static boolean veOt(Permisos p) {
        return p.tieneAlguno(Permisos.OT_GESTION, Permisos.OT_SUMINISTRAR, Permisos.OT_FINALIZAR);
    }

    /** true si el rol tiene algún tipo de aviso (si no, la campana no se muestra). */
    public static boolean tieneAvisos(Permisos p) {
        return veStock(p) || veOt(p) || p.tieneAlguno(Permisos.ACTIVOS, Permisos.ACTIVOS_APROBAR, Permisos.ACTIVOS_RECEPCION);
    }

    /** Pendientes (cosas por hacer). */
    public List<Notificacion> pendientes() throws SQLException {
        List<Notificacion> l = new ArrayList<Notificacion>();
        if (veStock(permisos)) {
            int n = dao.stockBajoMinimo(permisos);
            String url = permisos.puedeVer("/stockVista.jsp") ? "stockVista.jsp"
                    : (permisos.puedeVer("/solicitudCompra.jsp") ? "solicitudCompra.jsp" : "dashboard.jsp");
            agregar(l, "STOCK", n, n == 1 ? "1 insumo en o bajo el stock mínimo" : n + " insumos en o bajo el stock mínimo", url);
        }
        if (permisos.tiene(Permisos.ACTIVOS_APROBAR)) {
            int n = dao.solicitudesPorAprobar(permisos);
            agregar(l, "APROBAR", n, n == 1 ? "1 solicitud de activos por aprobar" : n + " solicitudes de activos por aprobar",
                    "activosAprobaciones.jsp");
        }
        if (permisos.tiene(Permisos.ACTIVOS_RECEPCION)) {
            int n = dao.activosPorRecibir(permisos);
            agregar(l, "RECIBIR", n, n == 1 ? "1 activo por recibir" : n + " activos por recibir", "activosAprobaciones.jsp#tabRec");
        }
        if (permisos.esAdministrador()) {
            int n = dao.loginsFallidosHoy();
            agregar(l, "SEGURIDAD", n, n == 1 ? "1 inicio de sesión fallido hoy" : n + " inicios de sesión fallidos hoy",
                    "bitacora.jsp?accion=LOGIN_FALLIDO&hoy=1");
        }
        return l;
    }

    private static void agregar(List<Notificacion> l, String tipo, int cantidad, String texto, String url) {
        if (cantidad <= 0) {
            return;
        }
        Notificacion n = new Notificacion();
        n.clase = "PENDIENTE";
        n.tipo = tipo;
        n.cantidad = cantidad;
        n.texto = texto;
        n.url = url;
        l.add(n);
    }

    /** Novedades de los últimos 7 días, de la más reciente a la más antigua, marcando las no vistas. */
    public List<Notificacion> novedades(int idUsuario) throws SQLException {
        List<Notificacion> l = new ArrayList<Notificacion>();
        if (veOt(permisos)) {
            l.addAll(dao.otAsignadas(idUsuario, permisos));
        }
        if (permisos.tiene(Permisos.ACTIVOS)) {
            l.addAll(dao.solicitudesResueltas(idUsuario));
        }
        Collections.sort(l, (a, b) -> b.fecha.compareTo(a.fecha));
        Timestamp vistas = dao.vistasHasta(idUsuario);
        for (Notificacion n : l) {
            n.nueva = vistas == null || n.fecha.after(vistas);
        }
        return l.size() > 10 ? new ArrayList<Notificacion>(l.subList(0, 10)) : l;
    }

    public void marcarVistas(int idUsuario) throws SQLException {
        dao.marcarVistas(idUsuario);
    }
}
