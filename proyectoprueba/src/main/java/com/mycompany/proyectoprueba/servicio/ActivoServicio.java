package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.ActivoDAO;
import com.mycompany.proyectoprueba.dao.jdbc.ActivoDAOJdbc;
import com.mycompany.proyectoprueba.inventario.Movimiento;
import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.MovimientoActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Activos fijos: listado, ficha con historial y movimientos dentro del centro.
 *   ASIGNACION   DISPONIBLE -> ASIGNADO    (responsable: usuario o persona externa)
 *   DEVOLUCION   ASIGNADO   -> DISPONIBLE
 *   REUBICACION  otro centro de costo del mismo centro (el estado no cambia)
 * Cada cambio queda en movimientos_activos (historial de solo inserción).
 */
public class ActivoServicio {

    /** Datos de un movimiento pedido desde la pantalla. */
    public static final class PedidoMovimiento {
        public String accion;
        public String observaciones;
        /** Texto tal como llegó (vacío o no numérico = persona externa). */
        public String usuarioReceptor;
        public String personaNombre;
        public String personaApellido;
        public String personaCi;
        /** Texto tal como llegó. */
        public String centroCosto;
    }

    private static final String[] ESTADOS_RESUMEN = {"DISPONIBLE", "ASIGNADO", "EN_TRANSITO", "EN_REPARACION", "BAJA", "SOLICITADO"};

    private final Connection conn;
    private final ActivoDAO dao;
    private final Permisos permisos;

    public ActivoServicio(Connection conn, ActivoDAO dao, Permisos permisos) {
        this.conn = conn;
        this.dao = dao;
        this.permisos = permisos;
    }

    public static ActivoServicio con(Connection conn, Permisos permisos) {
        return new ActivoServicio(conn, new ActivoDAOJdbc(conn), permisos);
    }

    /** Cantidad por estado, TOTAL (sin SOLICITADO ni BAJA) y CENTROS_COSTO (en orden). */
    public Map<String, Integer> resumen() throws SQLException {
        Map<String, Integer> r = new LinkedHashMap<String, Integer>();
        for (String e : ESTADOS_RESUMEN) {
            r.put(e, Integer.valueOf(0));
        }
        int total = 0;
        for (Map.Entry<String, Integer> e : dao.cantidadPorEstado(permisos).entrySet()) {
            r.put(e.getKey(), e.getValue());
            if (!"SOLICITADO".equals(e.getKey()) && !"BAJA".equals(e.getKey())) {
                total += e.getValue().intValue();
            }
        }
        r.put("TOTAL", Integer.valueOf(total));
        r.put("CENTROS_COSTO", Integer.valueOf(dao.centrosCostoConActivos(permisos)));
        return r;
    }

    public List<Opcion> centros() throws SQLException {
        return dao.centros(permisos);
    }

    /** filtro = true: todos (para filtrar); si no, solo activos (para reubicar). */
    public List<Opcion> centrosCosto(int idCentro, boolean filtro) throws SQLException {
        return dao.centrosCosto(idCentro, !filtro, permisos);
    }

    public List<Opcion> usuarios() throws SQLException {
        return dao.usuariosActivos();
    }

    public List<Activo> listar(String estado, int idCentro, int idCentroCosto, boolean pendientes) throws SQLException {
        return dao.listar(Activo.estadoValido(estado) ? estado : "", idCentro, idCentroCosto, pendientes, permisos);
    }

    public Activo ficha(int idEquipo) throws SQLException {
        return dao.ficha(idEquipo);
    }

    /** Lo ve si no tiene centro o es de uno de sus centros. */
    public boolean puedeVer(Activo a) {
        return a.idCentro == 0 || permisos.veCentro(a.idCentro);
    }

    public List<MovimientoActivo> historial(int idEquipo) throws SQLException {
        return dao.historial(idEquipo);
    }

    /** Registra una asignación, devolución o reubicación. */
    public void registrarMovimiento(int idEquipo, PedidoMovimiento p, int idUsuario) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String accion = p.accion;
            if (!Movimiento.ASIGNACION.equals(accion) && !Movimiento.DEVOLUCION.equals(accion) && !Movimiento.REUBICACION.equals(accion)) {
                throw new ReglaNegocioException("Movimiento no permitido");
            }
            // Bloquea el activo: dos usuarios no pueden moverlo a la vez
            Activo a = dao.bloquear(idEquipo);
            if (a == null) {
                throw new ReglaNegocioException("Activo no encontrado");
            }
            String estado = a.estado;
            Integer centroActual = a.idCentro == 0 ? null : Integer.valueOf(a.idCentro);
            Integer ccActual = a.idCentroCosto == 0 ? null : Integer.valueOf(a.idCentroCosto);
            if (centroActual != null) {
                permisos.exigirCentro(centroActual.intValue());
            }
            if (dao.enSolicitudAbierta(idEquipo)) {
                throw new ReglaNegocioException("El activo está en una solicitud de salida o baja abierta; espere a que se resuelva en Aprobaciones");
            }
            Movimiento m = new Movimiento(accion, idEquipo, idUsuario);
            m.estadoAnterior = estado;
            m.observaciones = p.observaciones;
            m.centroOrigen = centroActual;
            m.centroCostoOrigen = ccActual;
            String nuevoEstado = estado;
            Integer nuevoCC = ccActual;

            if (Movimiento.ASIGNACION.equals(accion)) {
                exigirEstado(estado, Activo.DISPONIBLE, "asignar");
                boolean interno = p.usuarioReceptor != null && p.usuarioReceptor.trim().matches("[0-9]{1,9}");
                if (interno) {
                    int receptor = Integer.parseInt(p.usuarioReceptor.trim());
                    if (!dao.usuarioActivo(receptor)) {
                        throw new ReglaNegocioException("El usuario responsable no existe o está inactivo");
                    }
                    m.usuarioReceptor = Integer.valueOf(receptor);
                } else {
                    m.receptorNombre = p.personaNombre;
                    m.receptorApellido = p.personaApellido;
                    m.receptorCi = p.personaCi;
                    if (Comun.vacio(m.receptorNombre) || Comun.vacio(m.receptorApellido) || Comun.vacio(m.receptorCi)) {
                        throw new ReglaNegocioException("Elija un usuario o cargue nombre, apellido y CI del responsable");
                    }
                }
                m.usuarioEmisor = Integer.valueOf(idUsuario);
                m.centroDestino = centroActual;
                m.centroCostoDestino = ccActual;
                nuevoEstado = Activo.ASIGNADO;
            } else if (Movimiento.DEVOLUCION.equals(accion)) {
                exigirEstado(estado, Activo.ASIGNADO, "registrar la devolución de");
                m.centroDestino = centroActual;
                m.centroCostoDestino = ccActual;
                m.usuarioReceptor = Integer.valueOf(idUsuario);
                nuevoEstado = Activo.DISPONIBLE;
            } else { // REUBICACION: otro centro de costo del mismo centro
                if (!Activo.DISPONIBLE.equals(estado) && !Activo.ASIGNADO.equals(estado)) {
                    throw new ReglaNegocioException("No se puede cambiar el centro de costo: el estado actual es " + estado);
                }
                if (p.centroCosto == null || !p.centroCosto.trim().matches("[0-9]{1,9}")) {
                    throw new ReglaNegocioException("Seleccione el nuevo centro de costo");
                }
                int cc = Integer.parseInt(p.centroCosto.trim());
                if (ccActual != null && cc == ccActual.intValue()) {
                    throw new ReglaNegocioException("El activo ya está en ese centro de costo");
                }
                if (centroActual == null || !dao.centroCostoActivoDelCentro(cc, centroActual.intValue())) {
                    throw new ReglaNegocioException("El centro de costo debe pertenecer al centro del activo y estar activo");
                }
                m.centroDestino = centroActual;
                m.centroCostoDestino = Integer.valueOf(cc);
                nuevoCC = Integer.valueOf(cc);
            }
            m.estadoNuevo = nuevoEstado;
            m.registrar(conn);
            dao.actualizarEstadoYCentroCosto(idEquipo, nuevoEstado, nuevoCC);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    private static void exigirEstado(String actual, String requerido, String verbo) {
        if (!requerido.equals(actual)) {
            throw new ReglaNegocioException("No se puede " + verbo + " el activo: su estado actual es " + actual);
        }
    }
}
