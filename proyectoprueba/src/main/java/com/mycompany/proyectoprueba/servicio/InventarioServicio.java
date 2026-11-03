package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.InventarioDAO;
import com.mycompany.proyectoprueba.dao.RemisionDAO;
import com.mycompany.proyectoprueba.dao.jdbc.RemisionDAOJdbc;
import com.mycompany.proyectoprueba.modelo.LineaRemision;
import com.mycompany.proyectoprueba.dao.jdbc.InventarioDAOJdbc;
import com.mycompany.proyectoprueba.inventario.MovimientoStock;
import com.mycompany.proyectoprueba.modelo.Ajuste;
import com.mycompany.proyectoprueba.modelo.Almacen;
import com.mycompany.proyectoprueba.modelo.FilaReporteInventario;
import com.mycompany.proyectoprueba.modelo.Insumo;
import com.mycompany.proyectoprueba.modelo.ItemInsumo;
import com.mycompany.proyectoprueba.modelo.LineaInventario;
import com.mycompany.proyectoprueba.modelo.LineasInventario;
import com.mycompany.proyectoprueba.modelo.MovimientoKardex;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.Remision;
import com.mycompany.proyectoprueba.modelo.Transferencia;
import com.mycompany.proyectoprueba.modelo.Transportista;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventario de insumos.
 *  - Salida de insumos: despacho de lo suministrado a una OT, con transportista; genera la
 *    remisión (REM-AAAA-000001).
 *  - Remisiones: listado y anulación con motivo.
 *  - Ajuste puntual (AJ-000001), transferencias entre almacenes (TR-000001) e inventario
 *    físico (el conteo genera un ajuste por las diferencias).
 *  - Kardex, reportes y alertas de stock mínimo.
 * Todo cambio de stock pasa por MovimientoStock (stock + Kardex en la misma transacción).
 */
public class InventarioServicio {

    /** Documento generado: número, id y (para transferencias) su remisión. */
    public static final class Documento {
        public final String numero;
        public final int id;
        public final String remision;
        public final int idRemision;
        public final Timestamp fecha;

        Documento(String numero, int id, String remision, int idRemision, Timestamp fecha) {
            this.numero = numero;
            this.id = id;
            this.remision = remision;
            this.idRemision = idRemision;
            this.fecha = fecha;
        }
    }

    /** Resultado de un conteo físico: sin diferencias (numero null) o el ajuste generado. */
    public static final class Conteo {
        public final String numero;
        public final int diferencias;
        public final Timestamp fecha;

        Conteo(String numero, int diferencias, Timestamp fecha) {
            this.numero = numero;
            this.diferencias = diferencias;
            this.fecha = fecha;
        }
    }

    private final Connection conn;
    private final InventarioDAO dao;
    private final RemisionDAO remisiones;
    private final Permisos permisos;
    private final int idUsuario;

    public InventarioServicio(Connection conn, InventarioDAO dao, RemisionDAO remisiones, Permisos permisos, int idUsuario) {
        this.conn = conn;
        this.dao = dao;
        this.remisiones = remisiones;
        this.permisos = permisos;
        this.idUsuario = idUsuario;
    }

    public static InventarioServicio con(Connection conn, Permisos permisos, int idUsuario) {
        return new InventarioServicio(conn, new InventarioDAOJdbc(conn), new RemisionDAOJdbc(conn), permisos, idUsuario);
    }

    // Consultas

    public List<Opcion> centros() throws SQLException {
        return dao.centrosActivos(permisos);
    }

    /** destino = true: almacén destino de una transferencia (de cualquier centro). */
    public List<Almacen> almacenes(int idCentro, boolean destino) throws SQLException {
        return dao.almacenesActivos(idCentro, destino, permisos);
    }

    public List<Insumo> insumos() throws SQLException {
        return dao.insumosActivos();
    }

    public List<ItemInsumo> stockDeAlmacen(int idAlmacen) throws SQLException {
        return dao.stockDeAlmacen(idAlmacen, permisos);
    }

    public List<OrdenTrabajo> otsConPendientes() throws SQLException {
        return dao.otsConPendientes(permisos);
    }

    /** OT de los centros del usuario con sus insumos pendientes de salida (en lineas no; ver pendientesDeSalida), o null. */
    public OrdenTrabajo otParaSalida(int idOT) throws SQLException {
        return dao.otDeSusCentros(idOT, permisos);
    }

    public List<ItemInsumo> pendientesDeSalida(int idOT) throws SQLException {
        return dao.pendientesDeSalida(idOT);
    }

    public List<Remision> remisiones(String tipo, String estado) throws SQLException {
        if (!Remision.SALIDA_INSUMOS.equals(tipo) && !Remision.TRANSFERENCIA.equals(tipo) && !Remision.SALIDA_ACTIVOS.equals(tipo) && !"INSUMOS".equals(tipo)) {
            tipo = "";
        }
        if (!"VIGENTE".equals(estado) && !"ANULADA".equals(estado)) {
            estado = "";
        }
        return dao.listarRemisiones(tipo, estado, permisos);
    }

    public boolean remisionVisible(int idRemision) throws SQLException {
        return dao.remisionVisible(idRemision, permisos);
    }

    /** Detalle de una remisión (remisiones_detalle), solo si es de los centros del usuario. */
    public List<LineaRemision> detalleRemision(int idRemision) throws SQLException {
        if (!dao.remisionVisible(idRemision, permisos)) {
            throw new ReglaNegocioException("Remisión no encontrada o de otro centro");
        }
        return remisiones.detalle(idRemision);
    }

    public List<Ajuste> ajustes() throws SQLException {
        return dao.listarAjustes(permisos);
    }

    public List<ItemInsumo> lineasAjuste(int idAjuste) throws SQLException {
        return dao.lineasAjusteVisible(idAjuste, permisos);
    }

    public List<Transferencia> transferencias() throws SQLException {
        return dao.listarTransferencias(permisos);
    }

    public List<ItemInsumo> lineasTransferencia(int idTransferencia) throws SQLException {
        return dao.lineasTransferenciaVisible(idTransferencia, permisos);
    }

    public List<MovimientoKardex> kardex(int idInsumo, int idAlmacen, String desde, String hasta) throws SQLException {
        return dao.kardex(idInsumo, idAlmacen, desde, hasta, permisos);
    }

    public List<FilaReporteInventario> consumoPorCentroCosto(String desde, String hasta) throws SQLException {
        return dao.consumoPorCentroCosto(desde, hasta, permisos);
    }

    public List<FilaReporteInventario> masUsados(String desde, String hasta) throws SQLException {
        return dao.masUsados(desde, hasta, permisos);
    }

    public List<FilaReporteInventario> valorizacion() throws SQLException {
        return dao.valorizacion(permisos);
    }

    public List<FilaReporteInventario> alertasMinimo() throws SQLException {
        return dao.alertasMinimo(permisos);
    }

    // Salida de insumos de una OT y remisiones

    /** Registra la salida de todo lo pendiente de la OT con una remisión nueva. */
    public Documento emitirSalida(int idOT, Transportista tr, String observacion) throws SQLException {
        conn.setAutoCommit(false);
        try {
            validarTransportista(tr, true);
            String[] ot = dao.bloquearOT(idOT, permisos);
            if (ot == null) {
                throw new ReglaNegocioException("Orden de trabajo no encontrada");
            }
            String estado = ot[0];
            if (!OrdenTrabajo.abierta(estado)) {
                throw new ReglaNegocioException("La OT está " + estado + "; no se puede registrar la salida");
            }
            if (!dao.hayPendientesDeSalida(idOT)) {
                throw new ReglaNegocioException("La OT no tiene insumos pendientes de salida");
            }
            String numero = dao.siguienteRemision();
            int idRem = dao.insertarRemision(numero, Remision.SALIDA_INSUMOS, Integer.valueOf(idOT), null, idUsuario, tr, observacion);
            int lineas = dao.asignarRemision(idRem, idOT);
            remisiones.copiarDetalle(idRem);   // detalle propio de la remisión (copia fija de lo que sale)
            dao.eventoOT(idOT, "SALIDA", idUsuario, "Salida de " + lineas + " insumo(s) con la remisión " + numero
                    + ". Transporta: " + tr.nombre + " " + tr.apellido + " (CI " + tr.ci + ")");
            conn.commit();
            return new Documento(numero, idRem, null, idRem, dao.ahora());
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /**
     * Anula una remisión: la de una OT deja sus insumos pendientes de salida otra vez; la de
     * una transferencia revierte la transferencia. Las de activos se anulan en Activos.
     */
    public Timestamp anularRemision(int idRemision, String motivo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (motivo.isEmpty()) {
                throw new ReglaNegocioException("Indique el motivo de la anulación");
            }
            Remision r = dao.bloquearRemision(idRemision);
            if (r == null) {
                throw new ReglaNegocioException("Remisión no encontrada");
            }
            if (r.anulada) {
                throw new ReglaNegocioException("La remisión ya está anulada");
            }
            if (Remision.SALIDA_INSUMOS.equals(r.tipo)) {
                if (!dao.otEsDeSusCentros(r.idOt, permisos)) {
                    throw new ReglaNegocioException("La remisión es de una OT de un centro al que no tiene acceso");
                }
                if (OrdenTrabajo.FINALIZADA.equals(dao.estadoOTBloqueada(r.idOt))) {
                    throw new ReglaNegocioException("La OT está finalizada. Anule primero la finalización en \"Finalizar OT\" y luego la remisión.");
                }
                dao.marcarRemisionAnulada(idRemision, idUsuario, motivo);
                // Los insumos vuelven a quedar pendientes de salida (el stock no cambia: ya se descontó al suministrar)
                dao.liberarRemision(idRemision);
                dao.eventoOT(r.idOt, "ANULACION_REMISION", idUsuario, "Remisión " + r.numero + " anulada: " + motivo);
            } else if (Remision.TRANSFERENCIA.equals(r.tipo)) {
                exigirOrigenTransferencia(r.idTransferencia);
                revertirTransferencia(r.idTransferencia, motivo);
            } else {
                throw new ReglaNegocioException("Las remisiones de salida de activos se anulan en Activos Fijos -> Aprobaciones (pestaña Historial)");
            }
            conn.commit();
            return dao.ahora();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Ajustes

    public Documento crearAjuste(String tipo, int idCentro, int idCentroCosto, int idAlmacen, String motivo, LineasInventario items) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (!Ajuste.tipoValido(tipo)) {
                throw new ReglaNegocioException("Seleccione el tipo de ajuste");
            }
            if (idCentro == 0) {
                throw new ReglaNegocioException("Seleccione el centro");
            }
            permisos.exigirCentro(idCentro);
            if (idCentroCosto == 0 && !Ajuste.CORRECCION_CONTEO.equals(tipo)) {
                throw new ReglaNegocioException("Seleccione el centro de costo");
            }
            if (idAlmacen == 0) {
                throw new ReglaNegocioException("Seleccione el almacén");
            }
            if (motivo.isEmpty()) {
                throw new ReglaNegocioException("Indique el motivo del ajuste");
            }
            if (idCentroCosto != 0 && !dao.centroCostoDelCentro(idCentroCosto, idCentro)) {
                throw new ReglaNegocioException("El centro de costo no pertenece al centro seleccionado");
            }
            if (!dao.almacenActivoDelCentro(idAlmacen, idCentro)) {
                throw new ReglaNegocioException("El almacén no pertenece al centro seleccionado");
            }
            List<LineaInventario> lineas = items.obtener();
            if (lineas.isEmpty()) {
                throw new ReglaNegocioException("Agregue al menos un insumo");
            }
            String numero = dao.siguienteAjuste();
            int id = registrarAjuste(numero, tipo, "MANUAL", idCentro, idCentroCosto, idAlmacen, motivo, lineas);
            conn.commit();
            return new Documento(numero, id, null, 0, dao.ahora());
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Revierte el stock del ajuste (queda en el Kardex) y lo marca anulado. */
    public Timestamp anularAjuste(int idAjuste, String motivo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (motivo.isEmpty()) {
                throw new ReglaNegocioException("Indique el motivo de la anulación");
            }
            Ajuste a = dao.bloquearAjuste(idAjuste);
            if (a == null) {
                throw new ReglaNegocioException("Ajuste no encontrado");
            }
            if (!dao.almacenDeSusCentros(a.idAlmacen, permisos)) {
                throw new ReglaNegocioException("El ajuste es de un centro al que no tiene acceso");
            }
            if (a.anulado) {
                throw new ReglaNegocioException("El ajuste ya está anulado");
            }
            for (ItemInsumo l : dao.lineasAjuste(idAjuste)) {
                MovimientoStock m = new MovimientoStock(MovimientoStock.ANULACION_AJUSTE, l.idInsumo, a.idAlmacen, -l.cantidad, idUsuario);
                m.documento = a.numero;
                m.idAjuste = Integer.valueOf(idAjuste);
                m.idCentroCosto = a.idCentroCosto;
                m.costoUnitario = l.costo;
                m.observacion = "Anulación: " + motivo;
                m.registrar(conn);
            }
            dao.marcarAjusteAnulado(idAjuste, idUsuario, motivo);
            conn.commit();
            return dao.ahora();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /**
     * Crea el ajuste con sus líneas y mueve el stock.
     * CORRECCION_CONTEO: la cantidad lleva signo (+ entra, - sale); el resto siempre sale del stock.
     */
    private int registrarAjuste(String numero, String tipo, String origen, int idCentro, int idCentroCosto, int idAlmacen,
            String motivo, List<LineaInventario> lineas) throws SQLException {
        Integer cc = idCentroCosto == 0 ? null : Integer.valueOf(idCentroCosto);
        int idAjuste = dao.insertarAjuste(numero, tipo, origen, idCentro, cc, idAlmacen, motivo, idUsuario);
        boolean conSigno = Ajuste.CORRECCION_CONTEO.equals(tipo);
        for (LineaInventario item : lineas) {
            LineaInventario l = item.leer();
            if (l.idInsumo <= 0 || l.cantidad == 0 || (!conSigno && l.cantidad < 0)) {
                throw new ReglaNegocioException(conSigno ? "Cada línea debe tener insumo y una cantidad distinta de 0"
                        : "Cada línea debe tener insumo y una cantidad mayor a 0");
            }
            int movimiento = conSigno ? l.cantidad : -l.cantidad;
            BigDecimal costo = dao.precioInsumoActivo(l.idInsumo);
            if (costo == null) {
                throw new ReglaNegocioException("Uno de los insumos no existe o está inactivo");
            }
            MovimientoStock m = new MovimientoStock(MovimientoStock.AJUSTE, l.idInsumo, idAlmacen, movimiento, idUsuario);
            m.documento = numero;
            m.idAjuste = Integer.valueOf(idAjuste);
            m.idCentroCosto = cc;
            m.costoUnitario = costo;
            m.observacion = tipo + ": " + motivo;
            m.registrar(conn);
            dao.insertarLineaAjuste(idAjuste, l.idInsumo, movimiento, costo);
        }
        return idAjuste;
    }

    // Transferencias

    public Documento crearTransferencia(int origen, int destino, Transportista tr, String observacion, LineasInventario items) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (origen == 0 || destino == 0) {
                throw new ReglaNegocioException("Seleccione el almacén de origen y el de destino");
            }
            if (!dao.almacenDeSusCentros(origen, permisos)) {
                throw new ReglaNegocioException("El almacén de origen no pertenece a sus centros");
            }
            if (origen == destino) {
                throw new ReglaNegocioException("El almacén de destino debe ser distinto al de origen");
            }
            if (!dao.almacenActivo(destino)) {
                throw new ReglaNegocioException("El almacén de destino no existe o está inactivo");
            }
            validarTransportista(tr, false);
            List<LineaInventario> lineas = items.obtener();
            if (lineas.isEmpty()) {
                throw new ReglaNegocioException("Agregue al menos un insumo");
            }
            String numero = dao.siguienteTransferencia();
            int idTr = dao.insertarTransferencia(numero, origen, destino, observacion, idUsuario);
            for (LineaInventario item : lineas) {
                LineaInventario l = item.leer();
                if (l.idInsumo <= 0 || l.cantidad <= 0) {
                    throw new ReglaNegocioException("Cada línea debe tener insumo y una cantidad mayor a 0");
                }
                MovimientoStock sale = new MovimientoStock(MovimientoStock.TRANSFERENCIA_SALIDA, l.idInsumo, origen, -l.cantidad, idUsuario);
                sale.documento = numero;
                sale.idTransferencia = Integer.valueOf(idTr);
                sale.observacion = "Transferencia a otro almacén";
                sale.registrar(conn);
                MovimientoStock entra = new MovimientoStock(MovimientoStock.TRANSFERENCIA_ENTRADA, l.idInsumo, destino, l.cantidad, idUsuario);
                entra.documento = numero;
                entra.idTransferencia = Integer.valueOf(idTr);
                entra.observacion = "Transferencia desde otro almacén";
                entra.registrar(conn);
                dao.insertarLineaTransferencia(idTr, l.idInsumo, l.cantidad);
            }
            String remision = dao.siguienteRemision();
            int idRem = dao.insertarRemision(remision, Remision.TRANSFERENCIA, null, Integer.valueOf(idTr), idUsuario, tr, observacion);
            remisiones.copiarDetalle(idRem);   // detalle propio de la remisión
            conn.commit();
            return new Documento(numero, idTr, remision, idRem, dao.ahora());
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    public Timestamp anularTransferencia(int idTransferencia, String motivo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (motivo.isEmpty()) {
                throw new ReglaNegocioException("Indique el motivo de la anulación");
            }
            exigirOrigenTransferencia(idTransferencia);
            revertirTransferencia(idTransferencia, motivo);
            conn.commit();
            return dao.ahora();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** La transferencia solo la anula quien opera el centro del almacén de origen. */
    private void exigirOrigenTransferencia(int idTransferencia) throws SQLException {
        if (!dao.transferenciaSaleDeSusCentros(idTransferencia, permisos)) {
            throw new ReglaNegocioException("La transferencia sale de un centro al que no tiene acceso");
        }
    }

    /** El stock vuelve al origen y se anulan la transferencia y su remisión. */
    private void revertirTransferencia(int idTransferencia, String motivo) throws SQLException {
        Transferencia t = dao.bloquearTransferencia(idTransferencia);
        if (t == null) {
            throw new ReglaNegocioException("Transferencia no encontrada");
        }
        if (t.anulada) {
            throw new ReglaNegocioException("La transferencia ya está anulada");
        }
        for (ItemInsumo l : dao.lineasTransferencia(idTransferencia)) {
            MovimientoStock sale = new MovimientoStock(MovimientoStock.ANULACION_TRANSFERENCIA, l.idInsumo, t.idAlmacenDestino, -l.cantidad, idUsuario);
            sale.documento = t.numero;
            sale.idTransferencia = Integer.valueOf(idTransferencia);
            sale.observacion = "Anulación: " + motivo;
            sale.registrar(conn);
            MovimientoStock vuelve = new MovimientoStock(MovimientoStock.ANULACION_TRANSFERENCIA, l.idInsumo, t.idAlmacenOrigen, l.cantidad, idUsuario);
            vuelve.documento = t.numero;
            vuelve.idTransferencia = Integer.valueOf(idTransferencia);
            vuelve.observacion = "Anulación: " + motivo;
            vuelve.registrar(conn);
        }
        dao.marcarTransferenciaAnulada(idTransferencia, idUsuario, motivo);
        dao.anularRemisionesDeTransferencia(idTransferencia, idUsuario, motivo);
    }

    // Inventario físico

    /** Compara lo contado con el sistema y, si hay diferencias, genera el ajuste CORRECCION_CONTEO. */
    public Conteo registrarConteo(int idAlmacen, String observacion, LineasInventario contado) throws SQLException {
        conn.setAutoCommit(false);
        try {
            Integer idCentro = dao.centroDeAlmacenActivo(idAlmacen);
            if (idCentro == null) {
                throw new ReglaNegocioException("Seleccione un almacén activo");
            }
            permisos.exigirCentro(idCentro.intValue());
            List<LineaInventario> diferencias = new ArrayList<LineaInventario>();
            for (LineaInventario item : contado.obtener()) {
                LineaInventario c = item.leer();
                if (c.idInsumo <= 0 || c.cantidad < 0) {
                    throw new ReglaNegocioException("Las cantidades contadas deben ser números enteros mayores o iguales a 0");
                }
                int sistema = dao.stockBloqueado(c.idInsumo, idAlmacen);
                if (c.cantidad != sistema) {
                    diferencias.add(new LineaInventario(c.idInsumo, c.cantidad - sistema));
                }
            }
            if (diferencias.isEmpty()) {
                conn.rollback();
                return new Conteo(null, 0, null);
            }
            String numero = dao.siguienteAjuste();
            registrarAjuste(numero, Ajuste.CORRECCION_CONTEO, "INVENTARIO_FISICO", idCentro.intValue(), 0, idAlmacen,
                    "Inventario físico" + (observacion.isEmpty() ? "" : ": " + observacion), diferencias);
            conn.commit();
            return new Conteo(numero, diferencias.size(), dao.ahora());
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Reglas comunes

    /** Nombre, apellido y CI obligatorios si obligatorio=true; largos máximos siempre. */
    private static void validarTransportista(Transportista t, boolean obligatorio) {
        if (obligatorio && (t.nombre.isEmpty() || t.apellido.isEmpty() || t.ci.isEmpty())) {
            throw new ReglaNegocioException("Complete nombre, apellido y cédula de identidad de quien transporta");
        }
        if (t.nombre.length() > 100 || t.apellido.length() > 100 || t.ci.length() > 20
                || t.marca.length() > 50 || t.modelo.length() > 50 || t.chapa.length() > 20) {
            throw new ReglaNegocioException("Algún dato del transportista o del vehículo es demasiado largo");
        }
    }
}
