package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Activo;
import com.mycompany.proyectoprueba.modelo.MovimientoActivo;
import com.mycompany.proyectoprueba.servicio.ActivoServicio;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Activos fijos: listado, ficha e historial, asignación / devolución / reubicación.
 * URL: ActivoServlet
 * Operaciones (tipo): resumen, listarCentros, listarCentrosCosto, listarUsuarios, listar, detalle,
 * registrarMovimiento, imprimir.
 */
public class ActivoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Connection conn = conexion(req);
        ActivoServicio servicio = ActivoServicio.con(conn, permisos(req));

        if ("resumen".equals(tipo)) {
            JSONObject res = new JSONObject();
            for (Map.Entry<String, Integer> e : servicio.resumen().entrySet()) {
                res.put(e.getKey(), e.getValue().intValue());
            }
            json(resp, res);

        } else if ("listarCentros".equals(tipo)) {
            opciones(req, resp, "Todos los centros", servicio.centros());

        } else if ("listarCentrosCosto".equals(tipo)) {
            // filtro=true: todos (para filtrar); si no, solo activos (para reubicar)
            boolean filtro = "true".equals(req.getParameter("filtro"));
            opciones(req, resp, filtro ? "Todos los centros de costo" : "-- Seleccionar --",
                    servicio.centrosCosto(numero(req.getParameter("id_centro")), filtro));

        } else if ("listarUsuarios".equals(tipo)) {
            opciones(req, resp, "-- Persona externa (cargar datos abajo) --", servicio.usuarios());

        } else if ("listar".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (Activo a : servicio.listar(req.getParameter("estado"), numero(req.getParameter("id_centro")),
                    numero(req.getParameter("id_centro_costo")), "true".equals(req.getParameter("pendientes")))) {
                JSONObject j = new JSONObject();
                j.put("id", a.id);
                j.put("numero_activo", a.numeroActivo());
                j.put("numero_serie", a.numeroSerie);
                j.put("codigo", a.codigo);
                j.put("descripcion", a.descripcion);
                j.put("estado", Comun.txt(a.estado));
                j.put("centro", a.centro);
                j.put("centro_costo", a.centroCosto);
                j.put("responsable", a.responsableActual());
                j.put("fecha_ingreso", a.fechaIngreso);
                j.put("numero_oc", a.numeroOc);
                j.put("situacion", Comun.txt(a.situacion));
                lista.put(j);
            }
            json(resp, lista);

        } else if ("detalle".equals(tipo)) {
            detalle(servicio, Integer.parseInt(req.getParameter("id")), resp);

        } else if ("registrarMovimiento".equals(tipo)) {
            registrarMovimiento(servicio, req, resp);

        } else if ("imprimir".equals(tipo)) {
            imprimir(req, resp, conn);

        } else {
            escribir(resp, "error: Operación no reconocida");
        }
    }

    /** "error: ..." en texto (como la pantalla anterior). */
    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof NumberFormatException) {
            escribir(resp, "error: Dato numérico inválido");
        } else {
            escribir(resp, "error: " + Html.esc(mensaje(e)));
        }
    }

    /** Ficha + historial completo (del más reciente al más antiguo). */
    private void detalle(ActivoServicio servicio, int idEquipo, HttpServletResponse resp) throws Exception {
        JSONObject f = new JSONObject();
        Activo a = servicio.ficha(idEquipo);
        if (a == null) {
            f.put("error", "Activo no encontrado");
            json(resp, f);
            return;
        }
        if (!servicio.puedeVer(a)) {
            f.put("error", "El activo es de un centro al que no tiene acceso");
            json(resp, f);
            return;
        }
        f.put("id", a.id);
        f.put("numero_activo", a.numeroActivo());
        f.put("numero_serie", a.numeroSerie);
        f.put("codigo", a.codigo);
        f.put("descripcion", a.descripcion);
        f.put("estado", Comun.txt(a.estado));
        f.put("fecha_ingreso", a.fechaIngreso);
        f.put("id_centro", a.idCentro);
        f.put("id_centro_costo", a.idCentroCosto);
        f.put("centro", a.centro);
        f.put("centro_costo", a.centroCosto);
        f.put("responsable", a.responsableActual());
        f.put("situacion", Comun.txt(a.situacion));
        f.put("numero_oc", a.numeroOc);
        f.put("fecha_compra", a.fechaCompra);
        f.put("estado_oc", a.estadoOc);
        f.put("proveedor", a.proveedor);
        f.put("ruc", a.ruc);
        f.put("solicitante", a.solicitante);
        f.put("valor", a.valor == null ? "" : a.valor.toPlainString());

        JSONArray movs = new JSONArray();
        for (MovimientoActivo m : servicio.historial(idEquipo)) {
            JSONObject j = new JSONObject();
            j.put("id", m.id);
            j.put("fecha", m.fecha);
            j.put("tipo", m.tipo);
            j.put("numero", m.numero);
            j.put("estado_anterior", m.estadoAnterior);
            j.put("estado_nuevo", m.estadoNuevo);
            j.put("observaciones", m.observaciones);
            j.put("justificacion", m.justificacion);
            j.put("persona_nombre", m.personaNombre);
            j.put("persona_apellido", m.personaApellido);
            j.put("persona_ci", m.personaCi);
            j.put("vehiculo_chapa", m.vehiculoChapa);
            j.put("vehiculo_marca", m.vehiculoMarca);
            j.put("vehiculo_modelo", m.vehiculoModelo);
            j.put("centro_origen", m.centroOrigen);
            j.put("centro_destino", m.centroDestino);
            j.put("sub_origen", m.subOrigen);
            j.put("sub_destino", m.subDestino);
            j.put("registrado_por", m.registradoPor);
            j.put("emisor", m.emisor);
            j.put("receptor", m.receptor);
            j.put("remision", m.remision);
            movs.put(j);
        }
        f.put("movimientos", movs);
        json(resp, f);
    }

    /** Respuesta JSON: {ok:true, mensaje} o {ok:false, error}. */
    private void registrarMovimiento(ActivoServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        int idUsuario = idUsuario(req).intValue();
        int idEquipo = Integer.parseInt(req.getParameter("id_equipo"));
        ActivoServicio.PedidoMovimiento p = new ActivoServicio.PedidoMovimiento();
        p.accion = req.getParameter("accion");
        p.observaciones = req.getParameter("observaciones");
        p.usuarioReceptor = req.getParameter("id_usuario_receptor");
        p.personaNombre = req.getParameter("persona_nombre");
        p.personaApellido = req.getParameter("persona_apellido");
        p.personaCi = req.getParameter("persona_ci");
        p.centroCosto = req.getParameter("id_centro_costo");
        JSONObject r = new JSONObject();
        try {
            servicio.registrarMovimiento(idEquipo, p, idUsuario);
            r.put("ok", true);
            r.put("mensaje", "Movimiento registrado correctamente");
        } catch (IllegalArgumentException e) {
            r.put("ok", false);
            r.put("error", mensaje(e));
        } catch (Exception e) {
            r.put("ok", false);
            r.put("error", "Error al registrar el movimiento: " + mensaje(e));
        }
        escribir(resp, r.toString());
    }

    /** Listado en PDF (reportes/listadoActivos.jrxml) con los filtros de la pantalla. */
    private void imprimir(HttpServletRequest req, HttpServletResponse resp, Connection conn) throws Exception {
        try {
            String estado = req.getParameter("estado");
            JasperReport reporte = Reportes.cargar(getServletContext(), "listadoActivos");
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("TITULO", "Listado de Activos Fijos");
            parametros.put("FECHA", new java.util.Date());
            parametros.put("USUARIO", usuarioReporte(req));
            parametros.put("ID_CENTRO", Integer.valueOf(numero(req.getParameter("id_centro"))));
            parametros.put("ID_CENTRO_COSTO", Integer.valueOf(numero(req.getParameter("id_centro_costo"))));
            parametros.put("ESTADO", Activo.estadoValido(estado) ? estado : "");
            parametros.put("PENDIENTES", Boolean.valueOf("true".equals(req.getParameter("pendientes"))));
            parametros.put("CENTROS", permisos(req).centrosCsv());
            byte[] pdf = JasperRunManager.runReportToPdf(reporte, parametros, conn);
            ReportePdf.enviar(resp, pdf, "activos_" + ReportePdf.sello() + ".pdf");
        } catch (JRException e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div style='font-family:sans-serif;padding:20px'><h3>No se pudo generar el listado de activos</h3><p>"
                    + Html.esc(mensaje(e)) + "</p><p>Si el problema es la compilación, abra <b>reportes/listadoActivos.jrxml</b> "
                    + "en iReport o Jaspersoft Studio, compílelo y deje <b>listadoActivos.jasper</b> en la misma carpeta.</p></div>");
        }
    }
}
