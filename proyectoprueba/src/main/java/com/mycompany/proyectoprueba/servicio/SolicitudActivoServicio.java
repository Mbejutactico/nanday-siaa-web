package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.SolicitudActivoDAO;
import com.mycompany.proyectoprueba.dao.jdbc.RemisionDAOJdbc;
import com.mycompany.proyectoprueba.dao.jdbc.SolicitudActivoDAOJdbc;
import com.mycompany.proyectoprueba.inventario.Movimiento;
import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.IdsEnviados;
import com.mycompany.proyectoprueba.modelo.LineaSolicitudActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.SolicitudActivo;
import com.mycompany.proyectoprueba.modelo.Transportista;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Salidas, aprobaciones, recepción y bajas de activos fijos.
 *   Solicitud (SA-000001 / BA-000001) -> Aprobaciones -> remisión + movimientos.
 *   REPARACION: quedan EN_REPARACION; vuelven con "retorno de reparación" -> DISPONIBLE.
 *   TRANSFERENCIA / ENTREGA_OT: quedan EN_TRANSITO; el destino los recibe (DISPONIBLE en destino)
 *     o los rechaza con justificación (vuelven al origen).
 *   BAJA: al aprobarse quedan en BAJA (definitivo).
 * Mientras la solicitud está abierta los activos quedan reservados. Una salida aprobada sin
 * recepciones se puede anular con motivo: la remisión queda anulada y los activos vuelven al origen.
 */
public class SolicitudActivoServicio {

    /** Datos de una solicitud nueva tal como llegan de la pantalla (ids en 0 = no indicado). */
    public static final class Pedido {
        public String tipo;
        public IdsEnviados activos;
        public String observacion;
        public String motivoBaja;
        public Transportista transportista;
        public int idProveedor;
        public int idCentroDestino;
        public int idCcDestino;
        public int idOT;
    }

    /** Resultado de aprobar o rechazar. */
    public static final class Resolucion {
        /** Número de la remisión generada ("" en bajas); null si se rechazó. */
        public final String remision;
        public final String fecha;
        public final int idRemision;

        Resolucion(String remision, String fecha, int idRemision) {
            this.remision = remision;
            this.fecha = fecha;
            this.idRemision = idRemision;
        }
    }

    private final Connection conn;
    private final SolicitudActivoDAO dao;
    private final Permisos permisos;
    private final int idUsuario;

    public SolicitudActivoServicio(Connection conn, SolicitudActivoDAO dao, Permisos permisos, int idUsuario) {
        this.conn = conn;
        this.dao = dao;
        this.permisos = permisos;
        this.idUsuario = idUsuario;
    }

    public static SolicitudActivoServicio con(Connection conn, Permisos permisos, int idUsuario) {
        return new SolicitudActivoServicio(conn, new SolicitudActivoDAOJdbc(conn), permisos, idUsuario);
    }

    // Consultas

    public List<Opcion> otsAbiertas() throws SQLException {
        return dao.otsAbiertas();
    }

    public List<Activo> activosParaSolicitud() throws SQLException {
        return dao.activosParaSolicitud(permisos);
    }

    /** soloPendientes: las que esperan aprobación; si no, el historial completo. */
    public List<SolicitudActivo> listar(boolean soloPendientes) throws SQLException {
        return dao.listar(soloPendientes ? "PENDIENTE" : "", permisos);
    }

    public List<LineaSolicitudActivo> lineas(int idSolicitud) throws SQLException {
        return dao.lineasVisibles(idSolicitud, permisos);
    }

    public List<LineaSolicitudActivo> enRecepcion() throws SQLException {
        return dao.enRecepcion(permisos);
    }

    public boolean visible(int idSolicitud) throws SQLException {
        return dao.visible(idSolicitud, permisos);
    }

    // Crear solicitud

    /** Crea la solicitud de salida o baja y reserva los activos. Devuelve {número, fecha}. */
    public String[] crear(Pedido p) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String tipo = p.tipo;
            if (!SolicitudActivo.tipoValido(tipo)) {
                throw new ReglaNegocioException("Seleccione el tipo de salida");
            }
            int cantidad = p.activos.cantidad();
            if (cantidad == 0) {
                throw new ReglaNegocioException("Seleccione al menos un activo");
            }
            Transportista t = p.transportista;
            String[] tr = {t.nombre, t.apellido, t.ci, t.marca, t.modelo, t.chapa};
            int idProveedor = 0, idCentroDest = 0, idCcDest = 0, idOT = 0;
            String motivoBaja = null;
            boolean baja = SolicitudActivo.BAJA.equals(tipo);
            if (baja) {
                motivoBaja = p.motivoBaja;
                if (!SolicitudActivo.motivoBajaValido(motivoBaja)) {
                    throw new ReglaNegocioException("Seleccione el motivo de la baja");
                }
                if (p.observacion.isEmpty()) {
                    throw new ReglaNegocioException("Describa el motivo de la baja en la observación");
                }
                tr = new String[]{"", "", "", "", "", ""};
            } else {
                if (tr[0].isEmpty() || tr[1].isEmpty() || tr[2].isEmpty()) {
                    throw new ReglaNegocioException("Complete nombre, apellido y cédula de identidad del transportista");
                }
                if (tr[0].length() > 100 || tr[1].length() > 100 || tr[2].length() > 20 || tr[3].length() > 50 || tr[4].length() > 50 || tr[5].length() > 20) {
                    throw new ReglaNegocioException("Algún dato del transportista o del vehículo es demasiado largo");
                }
                if (SolicitudActivo.REPARACION.equals(tipo)) {
                    idProveedor = p.idProveedor;
                    if (idProveedor == 0 || !dao.proveedorActivo(idProveedor)) {
                        throw new ReglaNegocioException("Seleccione el proveedor que realizará la reparación");
                    }
                } else if (SolicitudActivo.TRANSFERENCIA.equals(tipo)) {
                    idCentroDest = p.idCentroDestino;
                    idCcDest = p.idCcDestino;
                    if (idCentroDest == 0 || idCcDest == 0) {
                        throw new ReglaNegocioException("Seleccione el centro y el centro de costo de destino");
                    }
                    if (!dao.centroCostoActivoDelCentro(idCcDest, idCentroDest)) {
                        throw new ReglaNegocioException("El centro de costo no pertenece al centro de destino");
                    }
                } else {
                    idOT = p.idOT;
                    int[] destino = dao.destinoDeOtAbierta(idOT);
                    if (destino == null) {
                        throw new ReglaNegocioException("Seleccione una orden de trabajo pendiente o en proceso");
                    }
                    idCentroDest = destino[0];
                    idCcDest = destino[1];
                }
            }
            String numero = dao.siguienteNumero(baja);
            int idSol = dao.insertar(numero, tipo, idUsuario, idProveedor, idCentroDest, idCcDest, idOT, motivoBaja, p.observacion, tr);
            for (int i = 0; i < cantidad; i++) {
                int idEquipo = p.activos.id(i);
                Activo a = dao.bloquearActivo(idEquipo);
                if (a == null) {
                    throw new ReglaNegocioException("Uno de los activos no existe");
                }
                permisos.exigirCentro(a.idCentro);
                if (!Activo.DISPONIBLE.equals(a.estado) && !Activo.ASIGNADO.equals(a.estado)) {
                    throw new ReglaNegocioException("El activo " + a.serial + " no está disponible (estado: " + a.estado + ")");
                }
                if (dao.enSolicitudAbierta(idEquipo)) {
                    throw new ReglaNegocioException("El activo " + a.serial + " ya está en otra solicitud abierta");
                }
                if (SolicitudActivo.TRANSFERENCIA.equals(tipo) && a.idCentro == idCentroDest) {
                    throw new ReglaNegocioException("El activo " + a.serial + " ya está en el centro de destino; use \"Cambiar centro de costo\" en Listar Activos Fijos");
                }
                dao.insertarLinea(idSol, idEquipo, a.idCentro, a.idCentroCosto, a.estado);
            }
            conn.commit();
            return new String[]{numero, dao.ahora()};
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Aprobar / rechazar

    public Resolucion aprobar(int idSolicitud) throws SQLException {
        return resolver(idSolicitud, true, null);
    }

    public Resolucion rechazar(int idSolicitud, String motivo) throws SQLException {
        return resolver(idSolicitud, false, motivo);
    }

    private Resolucion resolver(int idSolicitud, boolean aprobar, String motivo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            exigirOrigen(idSolicitud);
            SolicitudActivo s = bloquear(idSolicitud);
            if (!"PENDIENTE".equals(s.estado)) {
                throw new ReglaNegocioException("La solicitud ya fue resuelta (estado: " + s.estado + ")");
            }
            String remision = null;
            if (!aprobar) {
                if (motivo.isEmpty()) {
                    throw new ReglaNegocioException("Indique el motivo del rechazo");
                }
                dao.rechazar(idSolicitud, idUsuario, motivo);
                dao.anularLineas(idSolicitud);
            } else {
                remision = registrarSalida(idSolicitud);
            }
            conn.commit();
            return new Resolucion(remision, dao.ahora(), dao.remisionDe(idSolicitud));
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Genera la remisión (salvo baja) y mueve cada activo. Devuelve el número de remisión ("" en bajas). */
    private String registrarSalida(int idSolicitud) throws SQLException {
        SolicitudActivo s = bloquear(idSolicitud);
        String tipo = s.tipo;
        boolean baja = SolicitudActivo.BAJA.equals(tipo);
        boolean reparacion = SolicitudActivo.REPARACION.equals(tipo);
        SolicitudActivo datos = dao.datosDeSalida(idSolicitud);
        String[] tr = datos.datosTransporte;
        String observacion = datos.observacion;

        Integer idRem = null;
        String numeroRem = "";
        if (!baja) {
            numeroRem = dao.siguienteRemision();
            idRem = Integer.valueOf(dao.insertarRemision(numeroRem, s.idOt, idSolicitud, idUsuario, tr, observacion));
        }
        List<LineaSolicitudActivo> lineas = dao.lineasPendientes(idSolicitud);
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("La solicitud no tiene activos pendientes");
        }
        for (LineaSolicitudActivo l : lineas) {
            String estadoAct = l.estadoActual;
            if (!Activo.DISPONIBLE.equals(estadoAct) && !Activo.ASIGNADO.equals(estadoAct)) {
                throw new ReglaNegocioException("Un activo cambió de estado (" + estadoAct + ") desde que se hizo la solicitud; rechácela y cree una nueva");
            }
            String tipoMov = baja ? Movimiento.BAJA : (reparacion ? Movimiento.REPARACION : Movimiento.ENVIO);
            String nuevoEstado = baja ? "BAJA" : (reparacion ? "EN_REPARACION" : "EN_TRANSITO");
            Movimiento m = new Movimiento(tipoMov, l.idEquipo, idUsuario);
            m.estadoAnterior = estadoAct;
            m.estadoNuevo = nuevoEstado;
            m.centroOrigen = nulo(l.idCentroOrigen);
            m.centroCostoOrigen = nulo(l.idCcOrigen);
            m.usuarioEmisor = Integer.valueOf(idUsuario);
            m.idRemision = idRem;
            if (!baja) {
                m.receptorNombre = tr[0];
                m.receptorApellido = tr[1];
                m.receptorCi = tr[2];
                m.vehiculoMarca = tr[3];
                m.vehiculoModelo = tr[4];
                m.vehiculoChapa = tr[5];
            }
            if (reparacion) {
                m.observaciones = s.numero + " - Reparación en " + datos.proveedor + (observacion == null ? "" : ". " + observacion);
            } else if (baja) {
                m.observaciones = s.numero + " - Baja: " + (observacion == null ? "" : observacion);
            } else {
                m.centroDestino = s.idCentroDestino;
                m.centroCostoDestino = s.idCcDestino;
                m.observaciones = s.numero + (SolicitudActivo.ENTREGA_OT.equals(tipo) ? " - Entrega por OT" : " - Transferencia entre sucursales")
                        + (observacion == null ? "" : ". " + observacion);
            }
            m.registrar(conn);
            dao.actualizarActivo(l.idEquipo, nuevoEstado, !baja);
            dao.estadoLinea(l.id, baja ? "FINALIZADO" : "EN_CURSO");
        }
        dao.marcarAprobada(idSolicitud, idUsuario, idRem);
        if (idRem != null) {
            new RemisionDAOJdbc(conn).copiarDetalle(idRem.intValue());   // detalle propio de la remisión
        }
        if (s.idOt != null) {
            dao.eventoOT(s.idOt.intValue(), "SALIDA", idUsuario, "Salida de " + lineas.size() + " activo(s) con la remisión " + numeroRem + " (" + s.numero + ")");
        }
        return numeroRem;
    }

    // Recepción

    /**
     * Recibe (o rechaza en destino) activos en curso. En tránsito -> DISPONIBLE en destino;
     * reparación -> DISPONIBLE en su ubicación; rechazo (solo tránsito, con justificación) -> vuelve al origen.
     * Devuelve {cantidad, fecha}.
     */
    public String[] recibir(IdsEnviados ids, String observacion, boolean rechazo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int cantidad = ids.cantidad();
            for (int i = 0; i < cantidad; i++) {
                if (!dao.lineaRecibiblePor(ids.id(i), permisos)) {
                    throw new ReglaNegocioException("Uno de los activos no corresponde a sus centros");
                }
            }
            if (cantidad == 0) {
                throw new ReglaNegocioException("Seleccione al menos un activo");
            }
            if (rechazo && observacion.isEmpty()) {
                throw new ReglaNegocioException("La justificación del rechazo es obligatoria");
            }
            for (int i = 0; i < cantidad; i++) {
                recibirLinea(ids.id(i), rechazo, observacion);
            }
            conn.commit();
            return new String[]{String.valueOf(cantidad), dao.ahora()};
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    private void recibirLinea(int idDetalle, boolean rechazo, String obs) throws SQLException {
        LineaSolicitudActivo l = dao.lineaParaRecibir(idDetalle);
        if (l == null) {
            throw new ReglaNegocioException("Uno de los activos ya no está pendiente de recepción");
        }
        if (!"EN_CURSO".equals(l.estadoLinea)) {
            throw new ReglaNegocioException("El activo " + l.numeroActivo + " ya fue recibido o su salida fue anulada");
        }
        boolean reparacion = SolicitudActivo.REPARACION.equals(l.tipo);
        if (rechazo && reparacion) {
            throw new ReglaNegocioException("Un activo que vuelve de reparación se recibe; no se rechaza (" + l.numeroActivo + ")");
        }
        Movimiento m;
        String nuevoEstado;
        Integer nuevoCentro, nuevoCC;
        if (reparacion) {
            m = new Movimiento(Movimiento.RETORNO_REPARACION, l.idEquipo, idUsuario);
            nuevoEstado = Activo.DISPONIBLE;
            nuevoCentro = nulo(l.idCentroActual);
            nuevoCC = nulo(l.idCcActual);
            m.centroDestino = nuevoCentro;
            m.centroCostoDestino = nuevoCC;
            m.observaciones = l.solicitud + " - Retorno de reparación" + (obs.isEmpty() ? "" : ": " + obs);
        } else if (!rechazo) {
            m = new Movimiento(Movimiento.RECEPCION, l.idEquipo, idUsuario);
            nuevoEstado = Activo.DISPONIBLE;
            nuevoCentro = Integer.valueOf(l.idCentroDestino);
            nuevoCC = Integer.valueOf(l.idCcDestino);
            m.centroOrigen = nulo(l.idCentroOrigen);
            m.centroCostoOrigen = nulo(l.idCcOrigen);
            m.centroDestino = nuevoCentro;
            m.centroCostoDestino = nuevoCC;
            m.observaciones = l.solicitud + " - Recepción en destino" + (obs.isEmpty() ? "" : ": " + obs);
        } else {
            m = new Movimiento(Movimiento.RECHAZO, l.idEquipo, idUsuario);
            nuevoEstado = l.estadoOrigen == null ? Activo.DISPONIBLE : l.estadoOrigen;
            nuevoCentro = nulo(l.idCentroOrigen);
            nuevoCC = nulo(l.idCcOrigen);
            m.centroOrigen = Integer.valueOf(l.idCentroDestino);
            m.centroCostoOrigen = Integer.valueOf(l.idCcDestino);
            m.centroDestino = nuevoCentro;
            m.centroCostoDestino = nuevoCC;
            m.justificacionRechazo = obs;
            m.observaciones = l.solicitud + " - Rechazado en destino; vuelve al origen";
        }
        m.estadoAnterior = l.estadoActual;
        m.estadoNuevo = nuevoEstado;
        m.usuarioReceptor = Integer.valueOf(idUsuario);
        m.idRemision = nulo(l.idRemision);
        m.registrar(conn);
        dao.ubicarActivo(l.idEquipo, nuevoEstado, nuevoCentro, nuevoCC);
        dao.registrarRecepcion(idDetalle, rechazo ? "RECHAZADO_DESTINO" : "RECIBIDO", idUsuario, obs.isEmpty() ? null : obs);
    }

    // Anular una salida aprobada

    /** Anula una salida aprobada sin recepciones: remisión anulada y activos a su origen. Devuelve la fecha. */
    public String anularSalida(int idSolicitud, String motivo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            exigirOrigen(idSolicitud);
            if (motivo.isEmpty()) {
                throw new ReglaNegocioException("Indique el motivo de la anulación");
            }
            SolicitudActivo s = bloquear(idSolicitud);
            if (!"APROBADA".equals(s.estado) || SolicitudActivo.BAJA.equals(s.tipo)) {
                throw new ReglaNegocioException("Solo se puede anular una salida aprobada (las bajas son definitivas)");
            }
            if (dao.tieneLineasResueltas(idSolicitud)) {
                throw new ReglaNegocioException("Algún activo de esta salida ya fue recibido o rechazado en destino; no se puede anular la remisión");
            }
            List<LineaSolicitudActivo> lineas = dao.lineasParaAnular(idSolicitud);
            int idRem = lineas.isEmpty() ? 0 : lineas.get(lineas.size() - 1).idRemision;
            for (LineaSolicitudActivo l : lineas) {
                String estadoOri = l.estadoOrigen == null ? Activo.DISPONIBLE : l.estadoOrigen;
                Movimiento m = new Movimiento(Movimiento.ANULACION_SALIDA, l.idEquipo, idUsuario);
                m.estadoAnterior = l.estadoActual;
                m.estadoNuevo = estadoOri;
                m.centroDestino = nulo(l.idCentroOrigen);
                m.centroCostoDestino = nulo(l.idCcOrigen);
                m.idRemision = nulo(idRem);
                m.observaciones = s.numero + " - Salida anulada: " + motivo;
                m.registrar(conn);
                dao.ubicarActivo(l.idEquipo, estadoOri, m.centroDestino, m.centroCostoDestino);
                dao.estadoLinea(l.id, "ANULADO");
            }
            dao.marcarAnulada(idSolicitud, idUsuario, motivo);
            if (idRem != 0) {
                dao.anularRemision(idRem, idUsuario, motivo);
            }
            if (s.idOt != null) {
                dao.eventoOT(s.idOt.intValue(), "ANULACION_REMISION", idUsuario, "Salida de activos " + s.numero + " anulada: " + motivo);
            }
            conn.commit();
            return dao.ahora();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Reglas comunes

    /**
     * Aprobar, rechazar o anular: solo quien opera el centro de origen de TODOS los activos
     * de la solicitud (o el administrador).
     */
    private void exigirOrigen(int idSolicitud) throws SQLException {
        if (permisos.todosLosCentros()) {
            return;
        }
        if (!dao.tieneActivosDeSusCentros(idSolicitud, permisos)) {
            throw new ReglaNegocioException("La solicitud es de un centro al que no tiene acceso");
        }
        if (dao.tieneActivosFueraDeSusCentros(idSolicitud, permisos)) {
            throw new ReglaNegocioException("La solicitud incluye activos de un centro al que no tiene acceso; "
                    + "debe resolverla un usuario con acceso a todos esos centros o el administrador");
        }
    }

    private SolicitudActivo bloquear(int idSolicitud) throws SQLException {
        SolicitudActivo s = dao.bloquear(idSolicitud);
        if (s == null) {
            throw new ReglaNegocioException("Solicitud no encontrada");
        }
        return s;
    }

    /** 0 = sin dato (null). */
    private static Integer nulo(int v) {
        return v == 0 ? null : Integer.valueOf(v);
    }
}
