package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.MovimientoActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Acceso a datos de los activos fijos (equipos) y su historial. */
public interface ActivoDAO {

    /** Cantidad por estado (sin los pedidos que siguen en un borrador de solicitud de compra). */
    Map<String, Integer> cantidadPorEstado(Permisos p) throws SQLException;

    /** Centros de costo distintos con activos disponibles o asignados. */
    int centrosCostoConActivos(Permisos p) throws SQLException;

    List<Opcion> centros(Permisos p) throws SQLException;

    /** Centros de costo de un centro (0 = todos); soloActivos para reubicar. */
    List<Opcion> centrosCosto(int idCentro, boolean soloActivos, Permisos p) throws SQLException;

    /** Usuarios activos ("Apellido, Nombre"). */
    List<Opcion> usuariosActivos() throws SQLException;

    /** estado "" = todos; pendientes = incluir los SOLICITADO y ANULADO. */
    List<Activo> listar(String estado, int idCentro, int idCentroCosto, boolean pendientes, Permisos p) throws SQLException;

    /** Ficha con datos de la compra, o null. */
    Activo ficha(int idEquipo) throws SQLException;

    List<MovimientoActivo> historial(int idEquipo) throws SQLException;

    /** Bloquea el activo: estado, centro y centro de costo actuales (idCentro/idCentroCosto 0 = sin dato), o null. */
    Activo bloquear(int idEquipo) throws SQLException;

    boolean enSolicitudAbierta(int idEquipo) throws SQLException;

    boolean usuarioActivo(int idUsuario) throws SQLException;

    boolean centroCostoActivoDelCentro(int idCentroCosto, int idCentro) throws SQLException;

    void actualizarEstadoYCentroCosto(int idEquipo, String estado, Integer idCentroCosto) throws SQLException;
}
