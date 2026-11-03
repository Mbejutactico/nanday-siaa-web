package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.IngresoStock;
import com.mycompany.proyectoprueba.modelo.ResumenStock;
import com.mycompany.proyectoprueba.modelo.Stock;
import com.mycompany.proyectoprueba.servicio.StockServicio;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperRunManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Stock por almacén. URL: StockServlet
 * Consulta (tipo): resumen, listarCentros, listarAlmacenesFiltro, listarJson, detalle, imprimir, imprimirReponer.
 * Mantenimiento (permiso STOCK_EDITAR): combosAlta, crear, modificarMinimo, eliminar.
 */
public class StockServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        StockServicio servicio = StockServicio.con(conexion(req), permisos(req));

        if ("resumen".equals(tipo)) {
            ResumenStock r = servicio.resumen();
            JSONObject res = new JSONObject();
            res.put("registros", r.registros);
            res.put("unidades", r.unidades);
            res.put("sin_stock", r.sinStock);
            res.put("bajo_minimo", r.bajoMinimo);
            res.put("almacenes", r.almacenes);
            json(resp, res);

        } else if ("listarCentros".equals(tipo)) {
            opciones(req, resp, "Todos los centros", servicio.centros());

        } else if ("listarAlmacenesFiltro".equals(tipo)) {
            opciones(req, resp, "Todos los almacenes", servicio.almacenes(numero(req.getParameter("id_centro"))));

        } else if ("listarJson".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (Stock s : servicio.listar(numero(req.getParameter("id_centro")), numero(req.getParameter("id_almacen")), req.getParameter("situacion"))) {
                JSONObject o = new JSONObject();
                o.put("id", s.getId());
                o.put("codigo", Comun.txt(s.getCodigo()));
                o.put("descripcion", Comun.txt(s.getDescripcion()));
                o.put("unidad", Comun.txt(s.getAbreviatura()));
                o.put("centro", Comun.txt(s.getCentro()));
                o.put("almacen", Comun.txt(s.getAlmacen()));
                o.put("actual", s.getActual());
                o.put("minimo", s.getMinimo());
                o.put("situacion", s.getSituacion());
                o.put("valor", s.getValorTexto());
                lista.put(o);
            }
            json(resp, lista);

        } else if ("detalle".equals(tipo)) {
            detalle(servicio, req, resp);

        } else if ("imprimir".equals(tipo)) {
            imprimir(req, resp);

        } else if ("imprimirReponer".equals(tipo)) {
            imprimirReponer(req, resp);

        } else if ("combosAlta".equals(tipo)) {
            JSONObject r = new JSONObject();
            r.put("insumos", opcionesJson(servicio.insumosParaAlta()));
            r.put("almacenes", opcionesJson(servicio.almacenesParaAlta()));
            json(resp, r);

        } else if ("crear".equals(tipo) || "modificarMinimo".equals(tipo) || "eliminar".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                if ("crear".equals(tipo)) {
                    r.put("id", servicio.crear(numero(req.getParameter("id_insumo")), numero(req.getParameter("id_almacen")), req.getParameter("minimo")));
                    r.put("mensaje", "Registro de stock creado");
                } else if ("modificarMinimo".equals(tipo)) {
                    servicio.modificarMinimo(numero(req.getParameter("id")), req.getParameter("minimo"));
                    r.put("mensaje", "Stock mínimo actualizado");
                } else {
                    servicio.eliminar(numero(req.getParameter("id")));
                    r.put("mensaje", "Registro de stock eliminado");
                }
                r.put("ok", true);
            } catch (IllegalArgumentException e) { // reglas de negocio y acceso a centros
                r.put("ok", false);
                r.put("mensaje", e.getMessage());
            }
            json(resp, r);
        }
    }

    private static JSONArray opcionesJson(java.util.List<com.mycompany.proyectoprueba.modelo.Opcion> lista) {
        JSONArray a = new JSONArray();
        for (com.mycompany.proyectoprueba.modelo.Opcion o : lista) {
            JSONObject x = new JSONObject();
            x.put("id", o.getId());
            x.put("texto", o.getTexto());
            a.put(x);
        }
        return a;
    }

    private void detalle(StockServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject f = new JSONObject();
        Stock s = servicio.buscar(numero(req.getParameter("pk")));
        if (s == null) {
            f.put("error", "Registro de stock no encontrado");
            json(resp, f);
            return;
        }
        f.put("id", s.getId());
        f.put("codigo", Comun.txt(s.getCodigo()));
        f.put("descripcion", Comun.txt(s.getDescripcion()));
        f.put("unidad", Comun.txt(s.getUnidad()));
        f.put("abreviatura", Comun.txt(s.getAbreviatura()));
        f.put("almacen", Comun.txt(s.getAlmacen()));
        f.put("ubicacion", Comun.txt(s.getUbicacion()));
        f.put("centro", Comun.txt(s.getCentro()));
        f.put("actual", s.getActual());
        f.put("minimo", s.getMinimo());
        f.put("precio", s.getPrecioTexto());
        f.put("valor", s.getValorTexto());

        JSONArray ingresos = new JSONArray();
        for (IngresoStock g : servicio.ingresos(s)) {
            JSONObject o = new JSONObject();
            o.put("numero_oc", Comun.txt(g.numeroOc));
            o.put("fecha", g.fecha == null ? "" : g.fecha.toString());
            o.put("estado_oc", Comun.txt(g.estadoOc));
            o.put("proveedor", Comun.txt(g.proveedor));
            o.put("pedido", g.pedido);
            o.put("recibido", g.recibido);
            o.put("precio", g.precio.toPlainString());
            ingresos.put(o);
        }
        f.put("ingresos", ingresos);

        JSONArray otros = new JSONArray();
        for (Stock o : servicio.otrosAlmacenes(s)) {
            JSONObject x = new JSONObject();
            x.put("almacen", Comun.txt(o.getAlmacen()));
            x.put("centro", Comun.txt(o.getCentro()));
            x.put("actual", o.getActual());
            x.put("minimo", o.getMinimo());
            otros.put(x);
        }
        f.put("otros", otros);
        json(resp, f);
    }

    /** Listado de stock en PDF, solo de los centros del usuario (se compila desde stock_listado.jrxml). */
    private void imprimir(HttpServletRequest req, HttpServletResponse resp) throws java.io.IOException {
        try {
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("TITULO", "Listado de Stock");
            p.put("FECHA", new Date());
            p.put("USUARIO", usuarioReporte(req));
            p.put("CENTROS", permisos(req).centrosCsv());
            byte[] pdf = JasperRunManager.runReportToPdf(Reportes.cargar(getServletContext(), "stock_listado"), p, conexion(req));
            ReportePdf.enviar(resp, pdf, "stock_" + ReportePdf.sello() + ".pdf");
        } catch (Exception e) {
            resp.setContentType("text/html");
            escribir(resp, "<div class='alert alert-danger'>Error al generar reporte: " + Html.esc(mensaje(e)) + "</div>");
        }
    }

    /** Insumos en o por debajo del stock mínimo (de los centros del usuario), en PDF (stock_reponer.jrxml). */
    private void imprimirReponer(HttpServletRequest req, HttpServletResponse resp) throws java.io.IOException {
        try {
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("TITULO", "Insumos a reponer");
            p.put("FECHA", new Date());
            p.put("USUARIO", usuarioReporte(req));
            p.put("CENTROS", permisos(req).centrosCsv());
            byte[] pdf = JasperRunManager.runReportToPdf(Reportes.cargar(getServletContext(), "stock_reponer"), p, conexion(req));
            ReportePdf.enviar(resp, pdf, "insumos_a_reponer_" + ReportePdf.sello() + ".pdf");
        } catch (Exception e) {
            resp.setContentType("text/html");
            escribir(resp, "<div class='alert alert-danger'>Error al generar reporte: " + Html.esc(mensaje(e)) + "</div>");
        }
    }
}
