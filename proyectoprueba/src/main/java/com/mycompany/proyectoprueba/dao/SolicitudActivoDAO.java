package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.LineaSolicitudActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.SolicitudActivo;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de las solicitudes de salida y baja de activos, su aprobación y recepción. */
public interface SolicitudActivoDAO {

    // Combos y selección
    /** OT pendientes o en proceso: "OT-... - Título (Centro / CC)". */
    List<Opcion> otsAbiertas() throws SQLException;

    /** Activos del inventario de los centros del usuario, con la solicitud abierta en la que estén. */
    List<Activo> activosParaSolicitud(Permisos p) throws SQLException;

    // Crear solicitud
    boolean proveedorActivo(int idProveedor) throws SQLException;

    boolean centroCostoActivoDelCentro(int idCentroCosto, int idCentro) throws SQLException;

    /** {centro, centro de costo} de una OT pendiente o en proceso, o null. */
    int[] destinoDeOtAbierta(int idOT) throws SQLException;

    /** SA-000001 o, para bajas, BA-000001. */
    String siguienteNumero(boolean baja) throws SQLException;

    /** Inserta la cabecera (ids en 0 y textos vacíos = NULL) y devuelve su id. */
    int insertar(String numero, String tipo, int idUsuario, int idProveedor, int idCentroDestino, int idCcDestino, int idOT,
            String motivoBaja, String observacion, String[] transporte) throws SQLException;

    /** Bloquea el activo: estado, centro, centro de costo y número (ids en 0 = sin dato), o null. */
    Activo bloquearActivo(int idEquipo) throws SQLException;

    boolean enSolicitudAbierta(int idEquipo) throws SQLException;

    void insertarLinea(int idSolicitud, int idEquipo, int idCentroOrigen, int idCcOrigen, String estadoOrigen) throws SQLException;

    // Consultas
    /** estado: PENDIENTE o "" (todas). */
    List<SolicitudActivo> listar(String estado, Permisos p) throws SQLException;

    List<LineaSolicitudActivo> lineasVisibles(int idSolicitud, Permisos p) throws SQLException;

    /** Activos en tránsito y en reparación que el usuario puede recibir. */
    List<LineaSolicitudActivo> enRecepcion(Permisos p) throws SQLException;

    boolean lineaRecibiblePor(int idDetalle, Permisos p) throws SQLException;

    boolean visible(int idSolicitud, Permisos p) throws SQLException;

    // Resolver
    boolean tieneActivosDeSusCentros(int idSolicitud, Permisos p) throws SQLException;

    boolean tieneActivosFueraDeSusCentros(int idSolicitud, Permisos p) throws SQLException;

    /** Bloquea la solicitud (tipo, estado, número, proveedor, destino y OT) o null. */
    SolicitudActivo bloquear(int idSolicitud) throws SQLException;

    void rechazar(int idSolicitud, int idUsuario, String motivo) throws SQLException;

    void anularLineas(int idSolicitud) throws SQLException;

    /** Transportista, nombre del proveedor ("" si no hay) y observación de la solicitud. */
    SolicitudActivo datosDeSalida(int idSolicitud) throws SQLException;

    String siguienteRemision() throws SQLException;

    int insertarRemision(String numero, Integer idOT, int idSolicitud, int idUsuario, String[] transporte, String observacion) throws SQLException;

    /** Líneas PENDIENTE con el estado actual del activo (bloquea los activos). */
    List<LineaSolicitudActivo> lineasPendientes(int idSolicitud) throws SQLException;

    void actualizarActivo(int idEquipo, String estado, boolean activo) throws SQLException;

    void estadoLinea(int idDetalle, String estado) throws SQLException;

    void marcarAprobada(int idSolicitud, int idUsuario, Integer idRemision) throws SQLException;

    void eventoOT(int idOT, String tipo, int idUsuario, String detalle) throws SQLException;

    /** Línea con su solicitud y el activo (bloquea ambos), o null. */
    LineaSolicitudActivo lineaParaRecibir(int idDetalle) throws SQLException;

    void ubicarActivo(int idEquipo, String estado, Integer idCentro, Integer idCentroCosto) throws SQLException;

    void registrarRecepcion(int idDetalle, String estadoLinea, int idUsuario, String observacion) throws SQLException;

    boolean tieneLineasResueltas(int idSolicitud) throws SQLException;

    /** Todas las líneas con el estado actual del activo y la remisión de la solicitud (bloquea los activos). */
    List<LineaSolicitudActivo> lineasParaAnular(int idSolicitud) throws SQLException;

    void marcarAnulada(int idSolicitud, int idUsuario, String motivo) throws SQLException;

    void anularRemision(int idRemision, int idUsuario, String motivo) throws SQLException;

    /** Id de la remisión de la solicitud (0 si no tiene). */
    int remisionDe(int idSolicitud) throws SQLException;

    /** dd/MM/yyyy HH:mm de la base. */
    String ahora() throws SQLException;
}
