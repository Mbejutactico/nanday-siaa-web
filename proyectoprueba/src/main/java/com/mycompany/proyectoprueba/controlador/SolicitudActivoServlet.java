package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.Centro;
import com.mycompany.proyectoprueba.modelo.IdsEnviados;
import com.mycompany.proyectoprueba.modelo.LineaSolicitudActivo;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.Proveedor;
import com.mycompany.proyectoprueba.modelo.SolicitudActivo;
import com.mycompany.proyectoprueba.modelo.Transportista;
import com.mycompany.proyectoprueba.servicio.CentroCostoServicio;
import com.mycompany.proyectoprueba.servicio.CentroServicio;
import com.mycompany.proyectoprueba.servicio.ProveedorServicio;
import com.mycompany.proyectoprueba.servicio.ReglaNegocioException;
import com.mycompany.proyectoprueba.servicio.SolicitudActivoServicio;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Activos fijos: salidas, aprobaciones, recepción y bajas. URL: SolicitudActivoServlet
 * Operaciones (tipo): listarProveedores, listarCentros, listarCentrosCosto, listarOTAbiertas, activos,
 * crearSolicitud, listarSolicitudes, detalleSolicitud, aprobar, rechazar, enRecepcion, recibir,
 * rechazarRecepcion, anularSalida, imprimirActaBaja.
 */
public class SolicitudActivoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Connection conn = conexion(req);
        SolicitudActivoServicio servicio = SolicitudActivoServicio.con(conn, permisos(req), idUsuario(req).intValue());

        if ("listarProveedores".equals(tipo)) {
            List<Opcion> lista = new ArrayList<Opcion>();
            for (Proveedor p : ProveedorServicio.con(conn).listarActivos()) {
                lista.add(new Opcion(p.getId(), p.getRazonSocial()));
            }
            opciones(req, resp, "-- Seleccionar proveedor --", lista);

        } else if ("listarCentros".equals(tipo)) {
            List<Opcion> lista = new ArrayList<Opcion>();
            for (Centro c : CentroServicio.con(conn).listarActivos()) {
                lista.add(new Opcion(c.getId(), c.getNombre()));
            }
            opciones(req, resp, "-- Seleccionar centro --", lista);

        } else if ("listarCentrosCosto".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar --", CentroCostoServicio.con(conn).activosDelCentro(numero(req.getParameter("id_centro"))));

        } else if ("listarOTAbiertas".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar orden de trabajo --", servicio.otsAbiertas());

        } else if ("activos".equals(tipo)) {
            // Todos los activos del inventario; "seleccionable" indica si pueden entrar en una solicitud nueva
            JSONArray lista = new JSONArray();
            for (Activo a : servicio.activosParaSolicitud()) {
                JSONObject j = new JSONObject();
                j.put("id", a.id);
                j.put("numero_activo", Comun.txt(a.serial));
                j.put("numero_serie", a.numeroSerie);
                j.put("estado", Comun.txt(a.estado));
                j.put("codigo", a.codigo);
                j.put("descripcion", a.descripcion);
                j.put("centro", a.centro);
                j.put("id_centro", a.idCentro);
                j.put("centro_costo", a.centroCosto);
                j.put("seleccionable", a.seleccionable());
                j.put("motivo", a.motivoNoSeleccionable());
                lista.put(j);
            }
            json(resp, lista);

        } else if ("crearSolicitud".equals(tipo)) {
            crearSolicitud(servicio, req, resp);

        } else if ("listarSolicitudes".equals(tipo)) {
            // estado: PENDIENTE (aprobaciones) o '' (historial)
            JSONArray lista = new JSONArray();
            for (SolicitudActivo s : servicio.listar("PENDIENTE".equals(req.getParameter("estado")))) {
                JSONObject j = new JSONObject();
                j.put("id", s.id);
                j.put("numero", s.numero);
                j.put("tipo", Comun.txt(s.tipo));
                j.put("estado", s.estado);
                j.put("fecha", Comun.fechaHora(s.fecha));
                j.put("observacion", s.observacion);
                j.put("motivo_baja", s.motivoBaja);
                j.put("motivo_rechazo", s.motivoRechazo);
                j.put("motivo_anulacion", s.motivoAnulacion);
                j.put("fecha_resolucion", Comun.fechaHora(s.fechaResolucion));
                j.put("fecha_anulacion", Comun.fechaHora(s.fechaAnulacion));
                j.put("id_remision", s.idRemision);
                j.put("remision", s.remision);
                j.put("solicitante", s.solicitante);
                j.put("resolvio", s.resolvio);
                j.put("destino", s.destino());
                j.put("transportista", s.transportista);
                j.put("activos", s.activos);
                j.put("recibidos", s.recibidos);
                lista.put(j);
            }
            json(resp, lista);

        } else if ("detalleSolicitud".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (LineaSolicitudActivo l : servicio.lineas(numero(req.getParameter("id")))) {
                JSONObject j = new JSONObject();
                j.put("numero_activo", l.numeroActivo);
                j.put("numero_serie", l.numeroSerie);
                j.put("codigo", l.codigo);
                j.put("descripcion", l.descripcion);
                j.put("origen", l.origen());
                j.put("estado_origen", l.estadoOrigen);
                j.put("estado_linea", l.estadoLinea);
                j.put("fecha_recepcion", Comun.fechaHora(l.fechaRecepcion));
                j.put("observacion_recepcion", l.observacionRecepcion);
                lista.put(j);
            }
            json(resp, lista);

        } else if ("aprobar".equals(tipo) || "rechazar".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                int idSol = numero(req.getParameter("id"));
                SolicitudActivoServicio.Resolucion res = "rechazar".equals(tipo)
                        ? servicio.rechazar(idSol, Comun.limpio(req.getParameter("motivo"))) : servicio.aprobar(idSol);
                if (res.remision != null) {
                    r.put("remision", res.remision);
                }
                r.put("ok", true);
                r.put("fecha", res.fecha);
                r.put("id_remision", res.idRemision);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("enRecepcion".equals(tipo)) {
            // Activos en tránsito (transferencia / entrega por OT) y en reparación
            JSONArray lista = new JSONArray();
            for (LineaSolicitudActivo l : servicio.enRecepcion()) {
                JSONObject j = new JSONObject();
                j.put("id", l.id);
                j.put("solicitud", l.solicitud);
                j.put("tipo", Comun.txt(l.tipo));
                j.put("fecha", Comun.fechaHora(l.fechaResolucion));
                j.put("remision", l.remision);
                j.put("numero_activo", l.numeroActivo);
                j.put("numero_serie", l.numeroSerie);
                j.put("descripcion", l.descripcion);
                j.put("origen", l.origen());
                j.put("destino", l.destino());
                lista.put(j);
            }
            json(resp, lista);

        } else if ("recibir".equals(tipo) || "rechazarRecepcion".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                boolean rechazo = "rechazarRecepcion".equals(tipo);
                IdsEnviados seleccion = ids(listaObligatoria(req.getParameter("ids"),
                        rechazo ? "Seleccione al menos un activo para rechazar" : "Seleccione al menos un activo para recibir"));
                String[] res = servicio.recibir(seleccion, Comun.limpio(req.getParameter("observacion")), rechazo);
                r.put("ok", true);
                r.put("cantidad", Integer.parseInt(res[0]));
                r.put("fecha", res[1]);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("anularSalida".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                String fecha = servicio.anularSalida(numero(req.getParameter("id")), Comun.limpio(req.getParameter("motivo")));
                r.put("ok", true);
                r.put("fecha", fecha);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("imprimirActaBaja".equals(tipo)) {
            imprimirActaBaja(servicio, conn, req, resp);

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

    /** Ids en JSON ([1, 2, ...]); los errores de lectura los informa el servicio al usarlos. */
    private static IdsEnviados ids(String json) {
        JSONArray arreglo;
        try {
            arreglo = new JSONArray(json);
        } catch (RuntimeException e) {
            return new IdsEnviados(e);
        }
        return ids(arreglo);
    }

    /** Igual, a partir de una lista ya leída; un elemento que no es número da un mensaje claro. */
    private static IdsEnviados ids(JSONArray arreglo) {
        List<Object> lista = new ArrayList<Object>();
        for (int i = 0; i < arreglo.length(); i++) {
            try {
                lista.add(Integer.valueOf(arreglo.getInt(i)));
            } catch (RuntimeException e) {
                lista.add(new ReglaNegocioException("Hay un activo no válido en la selección. Recargue la página e intente de nuevo."));
            }
        }
        return new IdsEnviados(lista);
    }

    private void crearSolicitud(SolicitudActivoServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject r = new JSONObject();
        try {
            SolicitudActivoServicio.Pedido p = new SolicitudActivoServicio.Pedido();
            p.tipo = req.getParameter("tipo_solicitud");
            p.activos = ids(req.getParameter("activos"));
            p.observacion = Comun.limpio(req.getParameter("observacion"));
            p.motivoBaja = req.getParameter("motivo_baja");
            p.transportista = new Transportista(Comun.limpio(req.getParameter("transportista_nombre")), Comun.limpio(req.getParameter("transportista_apellido")),
                    Comun.limpio(req.getParameter("transportista_ci")), Comun.limpio(req.getParameter("vehiculo_marca")),
                    Comun.limpio(req.getParameter("vehiculo_modelo")), Comun.limpio(req.getParameter("vehiculo_chapa")));
            p.idProveedor = numero(req.getParameter("id_proveedor"));
            p.idCentroDestino = numero(req.getParameter("id_centro_destino"));
            p.idCcDestino = numero(req.getParameter("id_centro_costo_destino"));
            p.idOT = numero(req.getParameter("id_ot"));
            String[] creada = servicio.crear(p);
            r.put("ok", true);
            r.put("numero", creada[0]);
            r.put("fecha", creada[1]);
        } catch (IllegalArgumentException e) {
            rechazo(r, mensaje(e));
        }
        json(resp, r);
    }

    /** Acta de baja en PDF (reportes/actaBaja.jrxml). */
    private void imprimirActaBaja(SolicitudActivoServicio servicio, Connection conn, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String usuarioSesion = (String) req.getSession().getAttribute("nombre_completo");
            int idSol = numero(req.getParameter("id"));
            if (!servicio.visible(idSol)) {
                escribir(resp, "<div style='font-family:sans-serif;padding:20px'>No tiene acceso a esa solicitud.</div>");
                return;
            }
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("ID_SOLICITUD", Integer.valueOf(idSol));
            parametros.put("USUARIO", usuarioSesion == null ? "Sistema" : usuarioSesion);
            parametros.put("FECHA", new java.util.Date());
            byte[] pdf = JasperRunManager.runReportToPdf(Reportes.cargar(getServletContext(), "actaBaja"), parametros, conn);
            ReportePdf.enviar(resp, pdf, "acta_baja.pdf");
        } catch (JRException e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div style='font-family:sans-serif;padding:20px'><h3>No se pudo generar el acta de baja</h3><p>" + Html.esc(mensaje(e)) + "</p></div>");
        }
    }
}
