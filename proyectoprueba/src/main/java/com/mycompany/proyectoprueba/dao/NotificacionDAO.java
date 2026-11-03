package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Notificacion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/** Consultas de la campana de notificaciones. */
public interface NotificacionDAO {

    /** Registros de stock en o bajo el mínimo (insumos activos) de los centros del usuario. */
    int stockBajoMinimo(Permisos p) throws SQLException;

    /** Solicitudes de activos pendientes de aprobación que el usuario puede ver. */
    int solicitudesPorAprobar(Permisos p) throws SQLException;

    /** Activos en camino hacia los centros del usuario (o que vuelven de reparación). */
    int activosPorRecibir(Permisos p) throws SQLException;

    /**
     * OT abiertas de los últimos 7 días que llegaron a los centros propios del usuario (creación o cambio
     * de centro) o en las que lo designaron responsable, hechas por otro usuario. Una por OT.
     */
    List<Notificacion> otAsignadas(int idUsuario, Permisos p) throws SQLException;

    /** Solicitudes de activos del usuario aprobadas o rechazadas por otro en los últimos 7 días. */
    List<Notificacion> solicitudesResueltas(int idUsuario) throws SQLException;

    /** Inicios de sesión fallidos de hoy (bitácora). 0 si la bitácora todavía no existe. */
    int loginsFallidosHoy() throws SQLException;

    /** Hasta cuándo vio las novedades (null = nunca abrió la campana). */
    Timestamp vistasHasta(int idUsuario) throws SQLException;

    /** Marca como vistas las novedades hasta este momento. */
    void marcarVistas(int idUsuario) throws SQLException;
}
