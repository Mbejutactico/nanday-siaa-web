package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.CambioBitacora;
import com.mycompany.proyectoprueba.modelo.FiltrosBitacora;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.RegistroBitacora;
import com.mycompany.proyectoprueba.servicio.BitacoraServicio;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperRunManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Bitácora del sistema (solo administrador, solo consulta). URL: BitacoraServlet
 * Operaciones (tipo): resumen, usuarios, listar, detalle, imprimir.
 */
public class BitacoraServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        BitacoraServicio servicio = BitacoraServicio.con(conexion(req));

        if ("resumen".equals(tipo)) {
            JSONObject r = new JSONObject();
            for (Map.Entry<String, Integer> e : servicio.resumenHoy().entrySet()) {
                r.put(e.getKey(), e.getValue().intValue());
            }
            json(resp, r);

        } else if ("usuarios".equals(tipo)) {
            JSONArray a = new JSONArray();
            for (Opcion o : servicio.usuarios()) {
                a.put(new JSONObject().put("id", o.getId()).put("texto", o.getTexto()));
            }
            json(resp, a);

        } else if ("listar".equals(tipo)) {
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            JSONObject r = new JSONObject();
            JSONArray a = new JSONArray();
            for (RegistroBitacora b : servicio.listar(filtros(req))) {
                JSONObject o = new JSONObject();
                o.put("id", b.id);
                o.put("fecha", f.format(b.fecha));
                o.put("orden", b.fecha.getTime());
                o.put("usuario", nombre(b));
                o.put("ip", b.ip == null ? "" : b.ip);
                o.put("modulo", b.modulo);
                o.put("accion", b.accion);
                o.put("descripcion", b.descripcion == null ? "" : b.descripcion);
                o.put("cambios", b.cambios);
                a.put(o);
            }
            r.put("registros", a);
            r.put("limite", BitacoraServicio.LIMITE);
            json(resp, r);

        } else if ("detalle".equals(tipo)) {
            RegistroBitacora b = servicio.obtener(numero(req.getParameter("id")));
            JSONObject r = new JSONObject();
            if (b == null) {
                r.put("error", "El registro no existe.");
            } else {
                r.put("id", b.id);
                r.put("fecha", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(b.fecha));
                r.put("usuario", nombre(b));
                r.put("email", b.email == null ? "" : b.email);
                r.put("ip", b.ip == null ? "" : b.ip);
                r.put("modulo", b.modulo);
                r.put("accion", b.accion);
                r.put("tabla", b.tabla == null ? "" : b.tabla);
                r.put("id_registro", b.idRegistro == null ? 0 : b.idRegistro.intValue());
                r.put("descripcion", b.descripcion == null ? "" : b.descripcion);
                JSONArray c = new JSONArray();
                for (CambioBitacora x : servicio.cambios(b.id)) {
                    c.put(new JSONObject().put("campo", x.campo)
                            .put("anterior", x.anterior == null ? "" : x.anterior)
                            .put("nuevo", x.nuevo == null ? "" : x.nuevo));
                }
                r.put("cambios", c);
            }
            json(resp, r);

        } else if ("imprimir".equals(tipo)) {
            imprimir(req, resp);

        } else {
            escribir(resp, "error: Operación no reconocida");
        }
    }

    private static String nombre(RegistroBitacora b) {
        String n = b.usuario == null ? "" : b.usuario.trim();
        return n.isEmpty() ? (b.email == null ? "" : b.email) : n;
    }

    private FiltrosBitacora filtros(HttpServletRequest req) {
        return BitacoraServicio.filtros(req.getParameter("desde"), req.getParameter("hasta"), numero(req.getParameter("id_usuario")),
                req.getParameter("modulo"), req.getParameter("accion"), req.getParameter("texto"),
                req.getParameter("tabla"), numero(req.getParameter("id_registro")));
    }

    /** Bitácora en PDF con los mismos filtros de la pantalla (bitacora.jrxml). */
    private void imprimir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            FiltrosBitacora f = filtros(req);
            Map<String, Object> p = new HashMap<String, Object>();
            p.put("TITULO", "Bitácora del sistema");
            p.put("FECHA", new Date());
            p.put("USUARIO", usuarioReporte(req));
            p.put("DESDE", f.desde);
            p.put("HASTA", f.hasta);
            p.put("ID_USUARIO", Integer.valueOf(f.idUsuario));
            p.put("MODULO", f.modulo);
            p.put("ACCION", f.accion);
            p.put("TEXTO", f.texto);
            p.put("TABLA", f.tabla);
            p.put("ID_REGISTRO", Integer.valueOf(f.idRegistro));
            p.put("FILTROS", descripcionFiltros(f));
            byte[] pdf = JasperRunManager.runReportToPdf(Reportes.cargar(getServletContext(), "bitacora"), p, conexion(req));
            ReportePdf.enviar(resp, pdf, "bitacora_" + ReportePdf.sello() + ".pdf");
        } catch (Exception e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div class='alert alert-danger'>Error al generar reporte: " + Html.esc(mensaje(e)) + "</div>");
        }
    }

    private static String descripcionFiltros(FiltrosBitacora f) {
        StringBuilder sb = new StringBuilder();
        if (!f.desde.isEmpty()) {
            sb.append("Desde ").append(f.desde).append("  ");
        }
        if (!f.hasta.isEmpty()) {
            sb.append("Hasta ").append(f.hasta).append("  ");
        }
        if (!f.modulo.isEmpty()) {
            sb.append("Módulo ").append(f.modulo).append("  ");
        }
        if (!f.accion.isEmpty()) {
            sb.append("Acción ").append(f.accion).append("  ");
        }
        if (f.idUsuario > 0) {
            sb.append("Usuario #").append(f.idUsuario).append("  ");
        }
        if (!f.texto.isEmpty()) {
            sb.append("Texto \"").append(f.texto).append("\"  ");
        }
        if (!f.tabla.isEmpty() && f.idRegistro > 0) {
            sb.append("Registro ").append(f.tabla).append(" #").append(f.idRegistro);
        }
        return sb.length() == 0 ? "Sin filtros" : sb.toString().trim();
    }

    @Override
    protected void error(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        JSONObject r = new JSONObject();
        boolean sinTabla = e instanceof java.sql.SQLException && "42P01".equals(((java.sql.SQLException) e).getSQLState());
        r.put("error", sinTabla ? "La bitácora todavía no está creada en la base de datos: ejecute el script bitacora.sql." : mensaje(e));
        escribir(resp, r.toString());
    }
}
