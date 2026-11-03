package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.FichaInforme;
import com.mycompany.proyectoprueba.modelo.FiltrosInforme;
import com.mycompany.proyectoprueba.modelo.Informe;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.servicio.InformeServicio;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Informes de compras, órdenes de trabajo, inventario y activos fijos. URL: InformeServlet
 * Operaciones (tipo): combos, centrosCosto, consultar, imprimir (listado general en PDF),
 * imprimirDetallado (cada registro con su ficha), detalle (ficha en JSON), imprimirDetalle (ficha en PDF).
 * Parámetro "informe": compras, ot, inventario o activos.
 */
public class InformeServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        InformeServicio servicio = InformeServicio.con(conexion(req), permisos(req));

        if ("combos".equals(tipo)) {
            JSONObject c = new JSONObject();
            for (Map.Entry<String, List<Opcion>> e : servicio.combos().entrySet()) {
                c.put(e.getKey(), opcionesJson(e.getValue()));
            }
            json(resp, c);

        } else if ("centrosCosto".equals(tipo)) {
            json(resp, opcionesJson(servicio.centrosCosto(numero(req.getParameter("id_centro")))));

        } else if ("consultar".equals(tipo) || "imprimir".equals(tipo) || "imprimirDetallado".equals(tipo)) {
            String informe = req.getParameter("informe");
            FiltrosInforme filtros = filtros(req);
            Informe res = servicio.generar(informe, filtros);
            if ("consultar".equals(tipo)) {
                consultar(res, resp);
            } else {
                imprimir(servicio, informe, filtros, res, "imprimirDetallado".equals(tipo), req, resp);
            }

        } else if ("detalle".equals(tipo) || "imprimirDetalle".equals(tipo)) {
            String informe = req.getParameter("informe");
            int id = numero(req.getParameter("id"));
            FichaInforme f = servicio.ficha(informe, id);
            if ("detalle".equals(tipo)) {
                json(resp, fichaJson(f));
            } else {
                imprimirFicha(f, informe, id, req, resp);
            }

        } else {
            escribir(resp, "error: Operación no reconocida");
        }
    }

    /** {error: mensaje}; los errores no previstos con "Error al generar el informe: ". */
    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        JSONObject err = new JSONObject();
        err.put("error", e instanceof IllegalArgumentException ? mensaje(e) : "Error al generar el informe: " + mensaje(e));
        json(resp, err);
    }

    private static FiltrosInforme filtros(HttpServletRequest r) {
        FiltrosInforme f = new FiltrosInforme();
        f.desde = fecha(r.getParameter("desde"));
        f.hasta = fecha(r.getParameter("hasta"));
        f.idCentro = numero(r.getParameter("id_centro"));
        f.idCentroCosto = numero(r.getParameter("id_centro_costo"));
        f.idUsuario = numero(r.getParameter("id_usuario"));
        f.idProveedor = numero(r.getParameter("id_proveedor"));
        f.idInsumo = numero(r.getParameter("id_insumo"));
        f.idAlmacen = numero(r.getParameter("id_almacen"));
        f.estado = r.getParameter("estado");
        f.tipoItem = r.getParameter("tipo_item");
        f.tipoMovimiento = r.getParameter("tipo_movimiento");
        f.estadoActivo = r.getParameter("estado_activo");
        String buscar = r.getParameter("buscar") == null ? "" : r.getParameter("buscar").trim();
        f.buscar = buscar.length() > 60 ? buscar.substring(0, 60) : buscar;
        return f;
    }

    private static String fecha(String v) {
        return (v != null && v.trim().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) ? v.trim() : "";
    }

    private static JSONArray opcionesJson(List<Opcion> lista) {
        JSONArray l = new JSONArray();
        for (Opcion o : lista) {
            JSONObject j = new JSONObject();
            j.put("id", o.getId());
            j.put("nombre", o.getTexto());
            l.put(j);
        }
        return l;
    }

    /** Columnas, filas, resumen y si se limitó la cantidad de filas. */
    private void consultar(Informe res, HttpServletResponse resp) throws IOException {
        JSONObject r = new JSONObject();
        JSONArray cols = new JSONArray();
        for (String[] col : res.columnas) {
            JSONObject o = new JSONObject();
            o.put("k", col[0]);
            o.put("t", col[1]);
            o.put("num", "R".equals(col[2]));
            cols.put(o);
        }
        r.put("columnas", cols);
        JSONArray filas = new JSONArray();
        for (Map<String, Object> f : res.filas) {
            filas.put(new JSONObject(f));
        }
        r.put("filas", filas);
        JSONArray resumen = new JSONArray();
        for (String[] rr : res.resumen) {
            JSONObject o = new JSONObject();
            o.put("etiqueta", rr[0]);
            o.put("valor", rr[1]);
            resumen.put(o);
        }
        r.put("resumen", resumen);
        r.put("limitado", res.limitado);
        json(resp, r);
    }

    /**
     * PDF: "imprimir" = listado general (una fila por registro); "imprimirDetallado" = cada
     * registro con su ficha completa. Ambos muestran todos los filtros del informe con su valor.
     */
    private void imprimir(InformeServicio servicio, String informe, FiltrosInforme filtros, Informe res, boolean detallado,
            HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String usuarioSesion = (String) req.getSession().getAttribute("nombre_completo");
            List<String[]> aplicados = servicio.filtrosAplicados(informe, filtros);
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("TITULO", res.titulo);
            String avisos = res.limitado ? "   (el informe se limita a las primeras " + Informe.LIMITE + " filas)" : "";
            Collection<Map<String, ?>> datos;
            String plantilla;
            if (detallado) {
                int mitad = (aplicados.size() + 1) / 2;
                p.put("FILTROS_IZQ", filtrosHtml(aplicados.subList(0, mitad)));
                p.put("FILTROS_DER", filtrosHtml(aplicados.subList(mitad, aplicados.size())));
                if (res.filas.size() > Informe.LIMITE_DETALLE) {
                    avisos += "   (se detallan los primeros " + Informe.LIMITE_DETALLE + " registros; acote los filtros para ver el resto)";
                }
                datos = new ArrayList<Map<String, ?>>(servicio.detallado(informe, res));
                plantilla = "informeDetallado";
            } else {
                p.put("FILTROS", filtrosPlano(aplicados));
                datos = new ArrayList<Map<String, ?>>(res.filas);
                plantilla = res.plantilla;
            }
            p.put("RESUMEN", res.resumenTexto() + avisos);
            p.put("USUARIO", usuarioSesion == null ? "Sistema" : usuarioSesion);
            p.put("FECHA", new java.util.Date());
            JasperReport reporte = Reportes.cargar(getServletContext(), plantilla);
            byte[] pdf = JasperRunManager.runReportToPdf(reporte, p, new JRMapCollectionDataSource(datos));
            ReportePdf.enviar(resp, pdf, res.plantilla + (detallado ? "_detallado" : "") + ".pdf");
        } catch (JRException e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div style='font-family:sans-serif;padding:20px'><h3>No se pudo generar el informe</h3><p>" + Html.esc(mensaje(e)) + "</p></div>");
        }
    }

    /** Ficha individual en PDF (reportes/fichaInforme.jrxml). */
    private void imprimirFicha(FichaInforme f, String informe, int id, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String usuarioSesion = (String) req.getSession().getAttribute("nombre_completo");
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("TITULO", f.titulo);
            p.put("NUMERO", f.numero);
            p.put("USUARIO", usuarioSesion == null ? "Sistema" : usuarioSesion);
            p.put("FECHA", new java.util.Date());
            Collection<Map<String, ?>> datos = new ArrayList<Map<String, ?>>(f.filasPdf());
            JasperReport reporte = Reportes.cargar(getServletContext(), "fichaInforme");
            byte[] pdf = JasperRunManager.runReportToPdf(reporte, p, new JRMapCollectionDataSource(datos));
            ReportePdf.enviar(resp, pdf, "ficha_" + informe + "_" + id + ".pdf");
        } catch (JRException e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div style='font-family:sans-serif;padding:20px'><h3>No se pudo generar la ficha</h3><p>" + Html.esc(mensaje(e)) + "</p></div>");
        }
    }

    /** Ficha para la pantalla: título, número y bloques (secciones y tablas). */
    private static JSONObject fichaJson(FichaInforme f) {
        JSONObject o = new JSONObject();
        o.put("titulo", f.titulo);
        o.put("numero", f.numero);
        JSONArray bl = new JSONArray();
        for (FichaInforme.Bloque b : f.bloques) {
            JSONObject x = new JSONObject();
            x.put("tipo", b.tabla ? "tabla" : "seccion");
            x.put("titulo", b.titulo);
            JSONArray filas = new JSONArray();
            for (String[] fila : b.filas) {
                JSONArray a = new JSONArray();
                for (String v : fila) {
                    a.put(v == null ? "" : v);
                }
                filas.put(a);
            }
            if (b.tabla) {
                JSONArray cols = new JSONArray();
                for (String c : b.columnas) {
                    cols.put(c);
                }
                x.put("columnas", cols);
            }
            x.put("filas", filas);
            bl.put(x);
        }
        o.put("bloques", bl);
        return o;
    }

    /** Filtros con estilo (markup "html" de Jasper): etiqueta en negrita y su valor. */
    private static String filtrosHtml(List<String[]> filtros) {
        StringBuilder sb = new StringBuilder();
        for (String[] f : filtros) {
            sb.append(sb.length() == 0 ? "" : "<br/>").append("<b>").append(escXml(f[0])).append(":</b> ").append(escXml(f[1]));
        }
        return sb.toString();
    }

    /** Filtros en una línea, para el listado general. */
    private static String filtrosPlano(List<String[]> filtros) {
        StringBuilder sb = new StringBuilder();
        for (String[] f : filtros) {
            sb.append(sb.length() == 0 ? "" : "   |   ").append(f[0]).append(": ").append(f[1]);
        }
        return sb.toString();
    }

    private static String escXml(String s) {
        return Comun.txt(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
