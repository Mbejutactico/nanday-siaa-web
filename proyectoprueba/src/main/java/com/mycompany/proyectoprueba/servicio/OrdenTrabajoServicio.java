package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.OrdenTrabajoDAO;
import com.mycompany.proyectoprueba.dao.jdbc.OrdenTrabajoDAOJdbc;
import com.mycompany.proyectoprueba.inventario.MovimientoStock;
import com.mycompany.proyectoprueba.modelo.AlmacenStock;
import com.mycompany.proyectoprueba.modelo.ContextoUsuario;
import com.mycompany.proyectoprueba.modelo.Insumo;
import com.mycompany.proyectoprueba.modelo.InsumoDisponible;
import com.mycompany.proyectoprueba.modelo.LineaSuministro;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.ResponsableOT;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Órdenes de trabajo (OT).
 *  - Crear: el solicitante es el usuario de la sesión; elige el centro destino (el que la
 *    resuelve), su centro de costo, el responsable (opcional, de ese centro), título y descripción.
 *  - Quién la ve: el centro destino, quien la pidió y su responsable.
 *  - Modificar: el centro destino o quien la pidió.
 *  - Eliminar: solo quien la creó o el administrador.
 *  - Suministrar / quitar insumos / finalizar: el centro destino.
 *  - Anular la finalización: solo quien la finalizó o el administrador.
 *  - Toda acción queda en ot_eventos (historial de solo inserción).
 * Las operaciones de escritura son transaccionales: si una regla falla no queda nada a medias.
 */
public class OrdenTrabajoServicio {

    /** Resultado del alta de una OT. */
    public static final class Creada {
        public final int id;
        public final String numero;
        public final Timestamp fechaCreacion;

        Creada(int id, String numero, Timestamp fechaCreacion) {
            this.id = id;
            this.numero = numero;
            this.fechaCreacion = fechaCreacion;
        }
    }

    private final Connection conn;
    private final OrdenTrabajoDAO dao;
    private final Permisos permisos;
    private final int idUsuario;

    public OrdenTrabajoServicio(Connection conn, OrdenTrabajoDAO dao, Permisos permisos, int idUsuario) {
        this.conn = conn;
        this.dao = dao;
        this.permisos = permisos;
        this.idUsuario = idUsuario;
    }

    public static OrdenTrabajoServicio con(Connection conn, Permisos permisos, int idUsuario) {
        return new OrdenTrabajoServicio(conn, new OrdenTrabajoDAOJdbc(conn), permisos, idUsuario);
    }

    // Consultas

    public ContextoUsuario contextoUsuario() throws SQLException {
        return dao.contextoUsuario(idUsuario);
    }

    public List<ResponsableOT> responsables(int idCentro) throws SQLException {
        return dao.responsables(idCentro);
    }

    public Map<String, Integer> resumen() throws SQLException {
        return dao.resumenPorEstado(permisos, idUsuario);
    }

    /** estado: ABIERTAS, PENDIENTE, EN_PROCESO, FINALIZADA, ANULADA o cualquier otro = todos. */
    public List<OrdenTrabajo> listar(String estado, String mias) throws SQLException {
        if (!"ABIERTAS".equals(estado) && !"PENDIENTE".equals(estado) && !"EN_PROCESO".equals(estado)
                && !"FINALIZADA".equals(estado) && !"ANULADA".equals(estado)) {
            estado = "";
        }
        return dao.listar(estado, mias, permisos, idUsuario);
    }

    public boolean veCentro(int idCentro) {
        return permisos.veCentro(idCentro);
    }

    /** Cabecera de la ficha (sin comprobar acceso) o null si no existe. */
    public OrdenTrabajo cabecera(int idOT) throws SQLException {
        return dao.cabecera(idOT);
    }

    /** Puede verla: es de su centro, la pidió o la tiene a cargo. */
    public boolean puedeVer(OrdenTrabajo o) {
        return permisos.veCentro(o.idCentro) || o.idSolicitante == idUsuario || o.idResponsable == idUsuario;
    }

    /** Completa insumos, remisiones e historial de la ficha. */
    public void completarFicha(OrdenTrabajo o) throws SQLException {
        o.lineas.addAll(dao.lineas(o.id));
        o.remisiones.addAll(dao.remisiones(o.id));
        o.eventos.addAll(dao.eventos(o.id));
    }

    public boolean pendientesDeSalida(int idOT) throws SQLException {
        return dao.pendientesDeSalida(idOT);
    }

    public List<OrdenTrabajo> abiertas() throws SQLException {
        return dao.abiertas(permisos);
    }

    public List<InsumoDisponible> insumosParaSuministrar() throws SQLException {
        return dao.insumosActivosConTotal();
    }

    public List<AlmacenStock> almacenesConStock(int idInsumo) throws SQLException {
        return dao.almacenesConStock(idInsumo, permisos);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    // Alta, modificación y baja

    public Creada crear(String titulo, String descripcion, int idCentroCosto, int idCentro, int idResponsable) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (titulo.isEmpty()) {
                throw new ReglaNegocioException("Ingrese un título para la orden de trabajo");
            }
            if (titulo.length() > 200) {
                throw new ReglaNegocioException("El título no puede superar 200 caracteres");
            }
            if (descripcion.length() > 2000) {
                throw new ReglaNegocioException("La descripción no puede superar 2000 caracteres");
            }
            if (idCentroCosto == 0) {
                throw new ReglaNegocioException("Seleccione el centro de costo");
            }
            if (idCentro == 0 || !dao.centroActivo(idCentro)) {
                throw new ReglaNegocioException("Seleccione el centro destino (el centro que va a resolver la orden de trabajo)");
            }
            if (!dao.centroCostoDelCentro(idCentroCosto, idCentro)) {
                throw new ReglaNegocioException("El centro de costo no pertenece al centro destino");
            }
            if (idResponsable != 0 && !dao.responsableValido(idResponsable, idCentro)) {
                throw new ReglaNegocioException("El responsable elegido no trabaja en el centro destino (o no está activo)");
            }
            String numero = dao.siguienteNumero();
            OrdenTrabajoDAO.Alta alta = dao.insertar(numero, idCentro, idCentroCosto, titulo, vacioANull(descripcion),
                    idUsuario, idResponsable == 0 ? null : Integer.valueOf(idResponsable));
            dao.evento(alta.id, "CREACION", idUsuario, "OT creada para el centro " + dao.nombreCentro(idCentro)
                    + ": " + titulo + (idResponsable == 0 ? "" : ". Responsable: " + dao.nombreUsuario(idResponsable)));
            conn.commit();
            return new Creada(alta.id, numero, alta.fechaCreacion);
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Título, descripción, centro de costo, centro destino (sin insumos entregados) y responsable. */
    public void modificar(int idOT, String titulo, String descripcion, int idCentroCosto, int idCentroPedido, int idResponsable) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (titulo.isEmpty()) {
                throw new ReglaNegocioException("Ingrese un título para la orden de trabajo");
            }
            if (titulo.length() > 200 || descripcion.length() > 2000) {
                throw new ReglaNegocioException("El título admite hasta 200 caracteres y la descripción hasta 2000");
            }
            OrdenTrabajo.Bloqueo ot = bloquear(idOT);
            exigirGestion(ot);
            exigirAbierta(ot.estado);
            int centroActual = ot.idCentro;
            int idCentro = idCentroPedido == 0 ? centroActual : idCentroPedido;
            boolean cambiaCentro = idCentro != centroActual;
            if (cambiaCentro) {
                if (dao.tieneLineas(idOT)) {
                    throw new ReglaNegocioException("La OT ya tiene insumos entregados: no se puede cambiar el centro destino");
                }
                if (!dao.centroActivo(idCentro)) {
                    throw new ReglaNegocioException("Seleccione un centro destino activo");
                }
            }
            if (idCentroCosto == 0 || !dao.centroCostoDelCentro(idCentroCosto, idCentro)) {
                throw new ReglaNegocioException("Seleccione un centro de costo del centro destino");
            }
            int responsableActual = ot.idResponsable == null ? 0 : ot.idResponsable.intValue();
            if (idResponsable != 0 && idResponsable != responsableActual && !dao.responsableValido(idResponsable, idCentro)) {
                throw new ReglaNegocioException("El responsable elegido no trabaja en el centro destino (o no está activo)");
            }
            if (idResponsable != 0 && cambiaCentro && !dao.responsableValido(idResponsable, idCentro)) {
                throw new ReglaNegocioException("El responsable no trabaja en el nuevo centro destino: elija otro o déjelo vacío");
            }
            // Solo se registra en el historial lo que cambió
            String[] actual = dao.tituloYDescripcion(idOT);
            boolean cambianDatos = !titulo.equals(actual[0]) || !descripcion.equals(actual[1])
                    || ot.idCentroCosto == null || idCentroCosto != ot.idCentroCosto.intValue();
            dao.actualizar(idOT, titulo, vacioANull(descripcion), idCentroCosto, idCentro,
                    idResponsable == 0 ? null : Integer.valueOf(idResponsable));
            if (cambianDatos) {
                dao.evento(idOT, "MODIFICACION", idUsuario, "Datos modificados. Título: " + titulo);
            }
            if (cambiaCentro) {
                dao.evento(idOT, "CAMBIO_CENTRO", idUsuario, "Centro destino: " + dao.nombreCentro(centroActual)
                        + " -> " + dao.nombreCentro(idCentro));
            }
            if (idResponsable != responsableActual) {
                dao.evento(idOT, "RESPONSABLE", idUsuario, "Responsable: "
                        + (responsableActual == 0 ? "(sin responsable)" : dao.nombreUsuario(responsableActual)) + " -> "
                        + (idResponsable == 0 ? "(sin responsable)" : dao.nombreUsuario(idResponsable)));
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Solo si no está finalizada, no tiene remisiones ni insumos suministrados. */
    public void eliminar(int idOT) throws SQLException {
        conn.setAutoCommit(false);
        try {
            OrdenTrabajo.Bloqueo ot = bloquear(idOT);
            if (!puedeEliminar(ot.idSolicitante == null ? 0 : ot.idSolicitante.intValue())) {
                throw new ReglaNegocioException("Solo quien creó la orden de trabajo o el administrador pueden eliminarla");
            }
            if (OrdenTrabajo.FINALIZADA.equals(ot.estado)) {
                throw new ReglaNegocioException("No se puede eliminar una OT finalizada");
            }
            if (dao.tieneRemisiones(idOT)) {
                throw new ReglaNegocioException("La OT tiene remisiones registradas y no se puede eliminar (se conserva por trazabilidad).");
            }
            if (dao.tieneLineas(idOT)) {
                throw new ReglaNegocioException("La OT tiene insumos suministrados. Quítelos primero en \"Suministrar OT\" (el stock vuelve al almacén) y luego elimínela.");
            }
            dao.eliminar(idOT);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Suministro de insumos

    /** Entrega insumos a la OT: descuenta el stock (Kardex) y la deja EN_PROCESO. */
    public void suministrar(int idOT, List<LineaSuministro> lineas) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (lineas.isEmpty()) {
                throw new ReglaNegocioException("Agregue al menos un insumo");
            }
            OrdenTrabajo.Bloqueo ot = bloquear(idOT);
            permisos.exigirCentro(ot.idCentro);
            exigirAbierta(ot.estado);
            StringBuilder resumen = new StringBuilder();
            for (LineaSuministro l : lineas) {
                if (l.errorLectura != null) {
                    throw l.errorLectura;
                }
                if (l.idInsumo <= 0 || l.idAlmacen <= 0) {
                    throw new ReglaNegocioException("Cada línea debe tener insumo y almacén");
                }
                if (l.cantidad <= 0) {
                    throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
                }
                if (!dao.almacenDeSusCentros(l.idAlmacen, permisos)) {
                    throw new ReglaNegocioException("El almacén de salida no pertenece a sus centros");
                }
                Insumo insumo = dao.insumoActivo(l.idInsumo);
                if (insumo == null) {
                    throw new ReglaNegocioException("Uno de los insumos no existe o está inactivo");
                }
                BigDecimal precio = insumo.getPrecioCompra();

                // Salida de stock + Kardex (bloquea el stock y no permite dejarlo negativo)
                MovimientoStock salida = new MovimientoStock(MovimientoStock.SALIDA_OT, l.idInsumo, l.idAlmacen, -l.cantidad, idUsuario);
                salida.documento = ot.numero;
                salida.idOt = Integer.valueOf(idOT);
                salida.idCentroCosto = ot.idCentroCosto;
                salida.costoUnitario = precio;
                salida.observacion = "Suministro a la OT";
                salida.registrar(conn);
                String almacen = dao.nombreAlmacen(l.idAlmacen);

                dao.insertarLinea(idOT, l.idInsumo, l.cantidad, l.idAlmacen, precio, idUsuario);
                resumen.append(resumen.length() == 0 ? "" : "; ").append(l.cantidad).append(" x ").append(insumo.getCodigo())
                        .append(" - ").append(insumo.getDescripcion()).append(" (").append(almacen).append(")");
            }
            dao.marcarEnProceso(idOT);
            dao.evento(idOT, "SUMINISTRO", idUsuario, "Insumos entregados: " + resumen);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Quita una línea sin salida registrada: el stock vuelve al almacén. */
    public void quitarSuministro(int idDetalle) throws SQLException {
        conn.setAutoCommit(false);
        try {
            OrdenTrabajo.LineaSuministrada l = dao.lineaSuministrada(idDetalle);
            if (l == null) {
                throw new ReglaNegocioException("La línea ya no existe");
            }
            String texto = l.cantidad + " x " + l.codigo + " - " + l.descripcion;
            OrdenTrabajo.Bloqueo ot = bloquear(l.idOt);
            permisos.exigirCentro(ot.idCentro);
            exigirAbierta(ot.estado);
            if (l.remision != null) {
                throw new ReglaNegocioException("Este insumo ya salió con la remisión " + l.remision
                        + ". Para quitarlo, primero anule la remisión en \"Salida de insumos\".");
            }
            // El stock vuelve al almacén (queda en el Kardex como devolución)
            MovimientoStock devolucion = new MovimientoStock(MovimientoStock.DEVOLUCION_OT, l.idInsumo, l.idAlmacen, l.cantidad, idUsuario);
            devolucion.documento = ot.numero;
            devolucion.idOt = Integer.valueOf(l.idOt);
            devolucion.idCentroCosto = ot.idCentroCosto;
            devolucion.costoUnitario = l.precio;
            devolucion.observacion = "Insumo quitado de la OT";
            devolucion.registrar(conn);

            dao.eliminarLinea(idDetalle);
            dao.recalcularEstado(l.idOt);   // sin líneas vuelve a PENDIENTE
            dao.evento(l.idOt, "QUITAR_SUMINISTRO", idUsuario, "Se quitó " + texto + " (el stock volvió al almacén)");
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Finalización

    /** Cierra la OT (todo lo entregado salió y hay al menos una salida). Devuelve la fecha y hora. */
    public Timestamp finalizar(int idOT, String observacion) throws SQLException {
        conn.setAutoCommit(false);
        try {
            OrdenTrabajo.Bloqueo ot = bloquear(idOT);
            permisos.exigirCentro(ot.idCentro);
            exigirAbierta(ot.estado);
            if (dao.pendientesDeSalida(idOT)) {
                throw new ReglaNegocioException("La OT tiene insumos suministrados que todavía no salieron. Registre la salida en \"Salida de insumos\" antes de finalizar.");
            }
            if (!dao.tieneSalida(idOT)) {
                throw new ReglaNegocioException("La OT no tiene ninguna salida registrada (de insumos o de activos). Registre la salida antes de finalizar.");
            }
            dao.finalizar(idOT, idUsuario, vacioANull(observacion));
            dao.evento(idOT, "FINALIZACION", idUsuario, observacion.isEmpty() ? "OT finalizada" : "OT finalizada: " + observacion);
            conn.commit();
            return dao.ahora();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Anula la finalización (motivo obligatorio): vuelve a PENDIENTE o EN_PROCESO. */
    public Timestamp reabrir(int idOT, String motivo) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (motivo.isEmpty()) {
                throw new ReglaNegocioException("Indique el motivo para anular la finalización");
            }
            OrdenTrabajo.Bloqueo ot = bloquear(idOT);
            permisos.exigirCentro(ot.idCentro);
            if (!OrdenTrabajo.FINALIZADA.equals(ot.estado)) {
                throw new ReglaNegocioException("La OT no está finalizada");
            }
            if (!puedeReabrir(ot.idFinaliza == null ? 0 : ot.idFinaliza.intValue())) {
                throw new ReglaNegocioException("Solo quien finalizó la orden de trabajo o el administrador pueden anular la finalización");
            }
            dao.reabrir(idOT, idUsuario, motivo);
            dao.evento(idOT, "REAPERTURA", idUsuario, "Finalización anulada: " + motivo);
            conn.commit();
            return dao.ahora();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Reglas comunes

    private OrdenTrabajo.Bloqueo bloquear(int idOT) throws SQLException {
        OrdenTrabajo.Bloqueo ot = dao.bloquear(idOT);
        if (ot == null) {
            throw new ReglaNegocioException("Orden de trabajo no encontrada");
        }
        return ot;
    }

    /** Eliminar: solo quien creó la OT o el administrador. */
    public boolean puedeEliminar(int idSolicitante) {
        return permisos.esAdministrador() || (idSolicitante > 0 && idSolicitante == idUsuario);
    }

    /** Anular la finalización: solo quien la finalizó o el administrador. */
    public boolean puedeReabrir(int idFinaliza) {
        return permisos.esAdministrador() || (idFinaliza > 0 && idFinaliza == idUsuario);
    }

    /** Modificar: el centro destino o quien pidió la OT. */
    private void exigirGestion(OrdenTrabajo.Bloqueo ot) {
        boolean laPidio = ot.idSolicitante != null && ot.idSolicitante.intValue() == idUsuario;
        if (!laPidio && !permisos.veCentro(ot.idCentro)) {
            throw new ReglaNegocioException("Solo el centro destino o quien pidió la orden de trabajo pueden modificarla");
        }
    }

    private static void exigirAbierta(String estado) {
        if (!OrdenTrabajo.abierta(estado)) {
            throw new ReglaNegocioException("La OT está " + estado + " y no se puede modificar. "
                    + (OrdenTrabajo.FINALIZADA.equals(estado) ? "Anule la finalización en \"Finalizar OT\" para volver a usarla." : ""));
        }
    }

    private static String vacioANull(String s) {
        return s.isEmpty() ? null : s;
    }
}
