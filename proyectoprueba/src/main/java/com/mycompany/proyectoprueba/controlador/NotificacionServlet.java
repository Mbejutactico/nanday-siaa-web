package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Notificacion;
import com.mycompany.proyectoprueba.servicio.NotificacionServicio;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Campana de notificaciones (JSON). URL: NotificacionServlet
 * Operaciones (tipo): listar, marcarVistas.
 */
public class NotificacionServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json; charset=UTF-8");
        int idUsuario = idUsuario(req).intValue();
        NotificacionServicio servicio = NotificacionServicio.con(conexion(req), permisos(req));
        JSONObject r = new JSONObject();

        if ("listar".equals(tipo)) {
            List<Notificacion> pendientes = servicio.pendientes();
            List<Notificacion> novedades = servicio.novedades(idUsuario);
            int nuevas = 0;
            for (Notificacion n : novedades) {
                if (n.nueva) {
                    nuevas++;
                }
            }
            r.put("pendientes", lista(pendientes));
            r.put("novedades", lista(novedades));
            r.put("nuevas", nuevas);

        } else if ("marcarVistas".equals(tipo)) {
            servicio.marcarVistas(idUsuario);
            r.put("ok", true);

        } else {
            r.put("error", "Operación no reconocida");
        }
        escribir(resp, r.toString());
    }

    private static JSONArray lista(List<Notificacion> l) {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        JSONArray a = new JSONArray();
        for (Notificacion n : l) {
            JSONObject o = new JSONObject();
            o.put("clase", n.clase);
            o.put("tipo", n.tipo);
            o.put("texto", n.texto);
            o.put("detalle", n.detalle == null ? "" : n.detalle);
            o.put("cantidad", n.cantidad);
            o.put("url", n.url);
            o.put("ventana_nueva", n.ventanaNueva);
            o.put("fecha", n.fecha == null ? "" : iso.format(n.fecha));
            o.put("nueva", n.nueva);
            o.put("clave", n.clave == null ? "" : n.clave);
            a.put(o);
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
