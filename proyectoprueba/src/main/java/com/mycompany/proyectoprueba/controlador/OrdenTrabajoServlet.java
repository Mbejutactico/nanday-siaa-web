package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.AlmacenStock;
import com.mycompany.proyectoprueba.modelo.Centro;
import com.mycompany.proyectoprueba.modelo.ContextoUsuario;
import com.mycompany.proyectoprueba.modelo.LineaSuministro;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.ResponsableOT;
import com.mycompany.proyectoprueba.servicio.CentroCostoServicio;
import com.mycompany.proyectoprueba.servicio.CentroServicio;
import com.mycompany.proyectoprueba.servicio.OrdenTrabajoServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.util.Comun;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Órdenes de trabajo. URL: OrdenTrabajoServlet
 * Operaciones (tipo): contextoUsuario, ahora, listarCentrosCosto, listarResponsables, crear,
 * resumen, listar, detalle, modificar, eliminar, listarAbiertas, listarInsumos,
 * almacenesConStock, suministrar, quitarSuministro, finalizar, reabrir.
 * Responde JSON ({ok, error} en las operaciones de escritura) y combos HTML.
 */
public class OrdenTrabajoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Connection conn = conexion(req);
        int idUsuario = idUsuario(req).intValue();
        OrdenTrabajoServicio servicio = OrdenTrabajoServicio.con(conn, permisos(req), idUsuario);

        if ("contextoUsuario".equals(tipo)) {
            contextoUsuario(servicio, conn, resp);

        } else if ("ahora".equals(tipo)) {
            JSONObject a = new JSONObject();
            a.put("fecha", ahora());
            json(resp, a);

        } else if ("listarCentrosCosto".equals(tipo)) {
            // Cualquier centro activo: la OT puede ir dirigida a otro centro
            opciones(req, resp, "-- Seleccionar --", CentroCostoServicio.con(conn).activosDelCentro(numero(req.getParameter("id_centro"))));

        } else if ("listarResponsables".equals(tipo)) {
            // id_centro > 0: usuarios de ese centro; 0: todos, con su centro principal
            JSONArray lista = new JSONArray();
            for (ResponsableOT r : servicio.responsables(numero(req.getParameter("id_centro")))) {
                JSONObject u = new JSONObject();
                u.put("id", r.id);
                u.put("nombre", r.nombre);
                u.put("rol", r.rol);
                u.put("id_centro", r.idCentro);
                u.put("centro", r.centro);
                lista.put(u);
            }
            json(resp, lista);

        } else if ("crear".equals(tipo)) {
            crear(servicio, req, resp);

        } else if ("resumen".equals(tipo)) {
            JSONObject r = new JSONObject();
            r.put("PENDIENTE", 0);
            r.put("EN_PROCESO", 0);
            r.put("FINALIZADA", 0);
            int total = 0;
            for (Map.Entry<String, Integer> e : servicio.resumen().entrySet()) {
                r.put(e.getKey(), e.getValue().intValue());
                total += e.getValue().intValue();
            }
            r.put("TOTAL", total);
            json(resp, r);

        } else if ("listar".equals(tipo)) {
            listar(servicio, req, resp);

        } else if ("detalle".equals(tipo)) {
            detalle(servicio, numero(req.getParameter("id")), resp);

        } else if ("modificar".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                servicio.modificar(numero(req.getParameter("id")), Comun.limpio(req.getParameter("titulo")),
                        Comun.limpio(req.getParameter("descripcion")), numero(req.getParameter("id_centro_costo")),
                        numero(req.getParameter("id_centro")), numero(req.getParameter("id_responsable")));
                r.put("ok", true);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("eliminar".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                servicio.eliminar(numero(req.getParameter("id")));
                r.put("ok", true);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("listarAbiertas".equals(tipo)) {
            List<Opcion> lista = new ArrayList<Opcion>();
            for (OrdenTrabajo o : servicio.abiertas()) {
                lista.add(new Opcion(o.id, o.numero + " - " + o.titulo + (OrdenTrabajo.EN_PROCESO.equals(o.estado) ? " (en proceso)" : "")));
            }
            opciones(req, resp, "-- Seleccionar orden de trabajo --", lista);

        } else if ("listarInsumos".equals(tipo)) {
            req.setAttribute("insumos", servicio.insumosParaSuministrar());
            vista(req, resp, "ot/insumos.jsp");

        } else if ("almacenesConStock".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (AlmacenStock a : servicio.almacenesConStock(numero(req.getParameter("id_insumo")))) {
                JSONObject o = new JSONObject();
                o.put("id", a.id);
                o.put("almacen", a.almacen);
                o.put("centro", a.centro);
                o.put("stock", a.stock);
                lista.put(o);
            }
            json(resp, lista);

        } else if ("suministrar".equals(tipo)) {
            suministrar(servicio, req, resp);

        } else if ("quitarSuministro".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                servicio.quitarSuministro(numero(req.getParameter("id_detalle")));
                r.put("ok", true);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("finalizar".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                Timestamp fecha = servicio.finalizar(numero(req.getParameter("id")), Comun.limpio(req.getParameter("observacion")));
                r.put("ok", true);
                r.put("fecha", Comun.fechaHora(fecha));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("reabrir".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                Timestamp fecha = servicio.reabrir(numero(req.getParameter("id")), Comun.limpio(req.getParameter("motivo")));
                r.put("ok", true);
                r.put("fecha", Comun.fechaHora(fecha));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else {
            escribir(resp, "error: Operación no reconocida");
        }
    }

    /** Error no previsto: {ok:false, error:"Error: ..."}. */
    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        JSONObject err = new JSONObject();
        rechazo(err, "Error: " + mensaje(e));
        json(resp, err);
    }

    private static void rechazo(JSONObject r, String mensaje) {
        r.put("ok", false);
        r.put("error", mensaje);
    }

    private static String ahora() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
    }

    /** Datos del usuario (pestaña "Datos del usuario") y los centros destino posibles. */
    private void contextoUsuario(OrdenTrabajoServicio servicio, Connection conn, HttpServletResponse resp) throws Exception {
        JSONObject u = new JSONObject();
        ContextoUsuario c = servicio.contextoUsuario();
        if (c != null) {
            u.put("id_usuario", c.id);
            u.put("nombre", c.nombre);
            u.put("email", c.email);
            u.put("rol", c.rol);
            u.put("id_centro", c.idCentro);
            u.put("centro", c.centro);
            u.put("fecha", ahora());
            // Centros destino posibles: todos los activos (la OT se pide al centro que la resuelve)
            JSONArray centros = new JSONArray();
            for (Centro ce : CentroServicio.con(conn).listarActivos()) {
                JSONObject o = new JSONObject();
                o.put("id", ce.getId());
                o.put("nombre", Comun.txt(ce.getNombre()));
                centros.put(o);
            }
            u.put("centros", centros);
        }
        json(resp, u);
    }

    private void crear(OrdenTrabajoServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONObject r = new JSONObject();
        try {
            OrdenTrabajoServicio.Creada ot = servicio.crear(Comun.limpio(req.getParameter("titulo")), Comun.limpio(req.getParameter("descripcion")),
                    numero(req.getParameter("id_centro_costo")), numero(req.getParameter("id_centro")), numero(req.getParameter("id_responsable")));
            r.put("ok", true);
            r.put("id_ot", ot.id);
            r.put("numero_ot", ot.numero);
            r.put("fecha", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ot.fechaCreacion));
        } catch (IllegalArgumentException e) {
            rechazo(r, mensaje(e));
        } catch (Exception e) {
            rechazo(r, "Error al crear la OT: " + mensaje(e));
        }
        json(resp, r);
    }

    /** estado: '' | PENDIENTE | EN_PROCESO | FINALIZADA | ABIERTAS; mias: '' | SOLICITADAS | A_CARGO. */
    private void listar(OrdenTrabajoServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int idUsuario = servicio.getIdUsuario();
        JSONArray lista = new JSONArray();
        for (OrdenTrabajo o : servicio.listar(req.getParameter("estado"), req.getParameter("mias"))) {
            JSONObject j = new JSONObject();
            j.put("id", o.id);
            j.put("numero", o.numero);
            j.put("fecha", o.fecha);
            j.put("titulo", o.titulo);
            j.put("estado", o.estado);
            j.put("centro", o.centro);
            j.put("centro_costo", o.centroCosto);
            j.put("solicitante", o.solicitante);
            j.put("centro_solicitante", o.centroSolicitante);
            j.put("responsable", o.responsable);
            j.put("es_mia", o.idSolicitante == idUsuario);
            j.put("a_mi_cargo", o.idResponsable == idUsuario);
            j.put("puede_gestionar", servicio.veCentro(o.idCentro) || o.idSolicitante == idUsuario);
            j.put("puede_eliminar", servicio.puedeEliminar(o.idSolicitante));
            j.put("puede_reabrir", OrdenTrabajo.FINALIZADA.equals(o.estado) && servicio.puedeReabrir(o.idFinaliza));
            j.put("items", o.items);
            j.put("unidades", o.unidades);
            j.put("sin_salida", o.sinSalida);
            j.put("salidas", o.salidas);
            j.put("finalizable", o.finalizable());
            j.put("fecha_creacion", Comun.fechaHora(o.fechaCreacion));
            j.put("fecha_finalizacion", Comun.fechaHora(o.fechaFinalizacion));
            j.put("fecha_anulacion_fin", Comun.fechaHora(o.fechaAnulacionFin));
            lista.put(j);
        }
        json(resp, lista);
    }

    /** Ficha de la OT: cabecera, insumos, remisiones e historial. */
    private void detalle(OrdenTrabajoServicio servicio, int idOT, HttpServletResponse resp) throws Exception {
        JSONObject f = new JSONObject();
        OrdenTrabajo o = servicio.cabecera(idOT);
        if (o == null) {
            f.put("error", "Orden de trabajo no encontrada");
            json(resp, f);
            return;
        }
        if (!servicio.puedeVer(o)) {
            f.put("error", "La orden de trabajo es de un centro al que no tiene acceso");
            json(resp, f);
            return;
        }
        int idUsuario = servicio.getIdUsuario();
        boolean deSuCentro = servicio.veCentro(o.idCentro);
        boolean laPidio = o.idSolicitante == idUsuario;
        f.put("id", o.id);
        f.put("numero", o.numero);
        f.put("fecha", o.fecha);
        f.put("titulo", o.titulo);
        f.put("descripcion", o.descripcion);
        f.put("estado", o.estado);
        f.put("id_centro", o.idCentro);
        // gestionar = modificar (centro destino o quien la pidió); eliminar = quien la creó o el administrador;
        // operar = suministrar / finalizar (centro destino); reabrir = quien la finalizó o el administrador
        f.put("puede_gestionar", deSuCentro || laPidio);
        f.put("puede_eliminar", servicio.puedeEliminar(o.idSolicitante));
        f.put("puede_reabrir", OrdenTrabajo.FINALIZADA.equals(o.estado) && servicio.puedeReabrir(o.idFinaliza));
        f.put("puede_operar", deSuCentro);
        f.put("es_mia", laPidio);
        f.put("a_mi_cargo", o.idResponsable == idUsuario);
        f.put("id_responsable", o.idResponsable);
        f.put("responsable", o.responsable);
        f.put("responsable_email", o.responsableEmail);
        f.put("centro_solicitante", o.centroSolicitante);
        f.put("id_centro_costo", o.idCentroCosto);
        f.put("centro", o.centro);
        f.put("centro_costo", o.centroCosto);
        f.put("solicitante", o.solicitante);
        f.put("solicitante_email", o.solicitanteEmail);
        f.put("fecha_creacion", Comun.fechaHora(o.fechaCreacion));
        f.put("fecha_finalizacion", Comun.fechaHora(o.fechaFinalizacion));
        f.put("finalizado_por", o.finalizadoPor);
        f.put("observacion_finalizacion", o.observacionFinalizacion);
        f.put("fecha_anulacion_fin", Comun.fechaHora(o.fechaAnulacionFin));
        f.put("anulado_por", o.anuladoPor);
        f.put("motivo_anulacion_fin", o.motivoAnulacionFin);
        servicio.completarFicha(o);

        JSONArray items = new JSONArray();
        for (OrdenTrabajo.Linea l : o.lineas) {
            JSONObject it = new JSONObject();
            it.put("id", l.id);
            it.put("codigo", l.codigo);
            it.put("descripcion", l.descripcion);
            it.put("unidad", l.unidad);
            it.put("cantidad", l.cantidad);
            it.put("almacen", l.almacen);
            it.put("centro_almacen", l.centroAlmacen);
            it.put("fecha", l.fecha);
            it.put("suministrado_por", l.suministradoPor);
            it.put("subtotal", l.subtotal);
            it.put("remision", l.remision);
            items.put(it);
        }
        f.put("items", items);

        JSONArray remisiones = new JSONArray();
        boolean haySalida = false;
        for (OrdenTrabajo.Remision r : o.remisiones) {
            JSONObject rm = new JSONObject();
            rm.put("id", r.id);
            rm.put("numero", r.numero);
            rm.put("tipo", r.tipo);
            rm.put("fecha", Comun.fechaHora(r.fecha));
            rm.put("anulada", r.anulada);
            rm.put("motivo_anulacion", r.motivoAnulacion);
            rm.put("transportista", r.transportista);
            haySalida |= !r.anulada;
            remisiones.put(rm);
        }
        f.put("remisiones", remisiones);
        boolean pendientes = servicio.pendientesDeSalida(idOT);
        f.put("pendientes_salida", pendientes);
        f.put("finalizable", haySalida && !pendientes && OrdenTrabajo.abierta(o.estado));

        JSONArray eventos = new JSONArray();
        for (OrdenTrabajo.Evento e : o.eventos) {
            JSONObject ev = new JSONObject();
            ev.put("fecha", e.fecha);
            ev.put("tipo", e.tipo);
            ev.put("detalle", e.detalle);
            ev.put("usuario", e.usuario);
            eventos.put(ev);
        }
        f.put("eventos", eventos);
        json(resp, f);
    }

    /** items = [{id_insumo, id_almacen, cantidad}] */
    private void suministrar(OrdenTrabajoServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONObject r = new JSONObject();
        try {
            int idOT = idObligatorio(req.getParameter("id_ot"), "Indique la orden de trabajo a la que se suministra");
            JSONArray items = listaObligatoria(req.getParameter("items"), "Agregue al menos un insumo para suministrar");
            List<LineaSuministro> lineas = new ArrayList<LineaSuministro>();
            for (int i = 0; i < items.length(); i++) {
                try {
                    JSONObject it = items.getJSONObject(i);
                    lineas.add(new LineaSuministro(it.optInt("id_insumo", 0), it.optInt("id_almacen", 0), it.optInt("cantidad", 0)));
                } catch (RuntimeException e) {
                    // se informa al llegar a esa línea, como antes, pero con un mensaje claro
                    lineas.add(new LineaSuministro(new ReglaNegocioException("La línea " + (i + 1) + " de insumos no es válida")));
                }
            }
            servicio.suministrar(idOT, lineas);
            r.put("ok", true);
        } catch (IllegalArgumentException e) {
            rechazo(r, mensaje(e));
        } catch (Exception e) {
            rechazo(r, "Error al suministrar: " + mensaje(e));
        }
        json(resp, r);
    }
}
