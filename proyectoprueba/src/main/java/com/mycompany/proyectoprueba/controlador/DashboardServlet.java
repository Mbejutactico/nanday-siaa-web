package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.CompraResumen;
import com.mycompany.proyectoprueba.modelo.OtResumen;
import com.mycompany.proyectoprueba.modelo.PuntoGrafico;
import com.mycompany.proyectoprueba.servicio.DashboardServicio;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Panel principal (JSON). URL: DashboardServlet
 * Operaciones (tipo): contar, resumenOT, ultimasCompras, graficos.
 */
public class DashboardServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json; charset=UTF-8");
        DashboardServicio servicio = DashboardServicio.con(conexion(req), permisos(req));
        JSONObject r = new JSONObject();

        if ("contar".equals(tipo)) {
            for (Map.Entry<String, Integer> e : servicio.contadores().entrySet()) {
                r.put(e.getKey(), e.getValue().intValue());
            }

        } else if ("resumenOT".equals(tipo)) {
            if (!servicio.veOt()) {
                r.put("sin_permiso", true);
            } else {
                int idUsuario = idUsuario(req).intValue();
                for (Map.Entry<String, Integer> e : servicio.contadoresOt(idUsuario).entrySet()) {
                    r.put(e.getKey(), e.getValue().intValue());
                }
                JSONArray lista = new JSONArray();
                for (OtResumen o : servicio.otAbiertas(idUsuario)) {
                    JSONObject x = new JSONObject();
                    x.put("id", o.id);
                    x.put("numero", o.numero);
                    x.put("titulo", o.titulo == null ? "" : o.titulo);
                    x.put("estado", o.estado);
                    x.put("centro", o.centro == null ? "" : o.centro);
                    x.put("centro_costo", o.centroCosto == null ? "" : o.centroCosto);
                    x.put("dias", o.dias);
                    x.put("items", o.items);
                    x.put("responsable", o.responsable == null ? "" : o.responsable);
                    lista.put(x);
                }
                r.put("abiertas", lista);
            }

        } else if ("ultimasCompras".equals(tipo)) {
            if (!servicio.veCompras()) {
                r.put("sin_permiso", true);
            } else {
                JSONArray lista = new JSONArray();
                for (CompraResumen c : servicio.ultimasCompras()) {
                    JSONObject x = new JSONObject();
                    x.put("id", c.id);
                    x.put("numero", c.numero == null ? "" : c.numero);
                    x.put("proveedor", c.proveedor == null ? "" : c.proveedor);
                    x.put("fecha", c.fecha == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(c.fecha));
                    x.put("estado", c.estado);
                    lista.put(x);
                }
                r.put("compras", lista);
            }

        } else if ("graficos".equals(tipo)) {
            // Cada gráfico solo se envía si el rol puede ver esos datos
            if (servicio.veOt()) {
                r.put("ot_estado", serie(servicio.otPorEstado(idUsuario(req).intValue())));
            }
            if (servicio.veCompras()) {
                r.put("oc_mes", serie(servicio.ocPorMes()));
            }
            if (servicio.veConsumo()) {
                r.put("consumo_cc", serie(servicio.consumoPorCentroCosto()));
            }

        } else {
            r.put("error", "Operación no reconocida");
        }
        escribir(resp, r.toString());
    }

    /** Serie de un gráfico: [{etiqueta, cantidad, valor}]. */
    private static JSONArray serie(List<PuntoGrafico> puntos) {
        JSONArray a = new JSONArray();
        for (PuntoGrafico g : puntos) {
            JSONObject x = new JSONObject();
            x.put("etiqueta", g.etiqueta == null ? "" : g.etiqueta);
            x.put("cantidad", g.cantidad);
            x.put("valor", g.valor.toPlainString());
            a.put(x);
        }
        return a;
    }

    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        JSONObject r = new JSONObject();
        r.put("error", mensaje(e));
        escribir(resp, r.toString());
    }
}
