package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.modelo.Ajuste;
import com.mycompany.proyectoprueba.modelo.Almacen;
import com.mycompany.proyectoprueba.modelo.FilaReporteInventario;
import com.mycompany.proyectoprueba.modelo.ItemInsumo;
import com.mycompany.proyectoprueba.modelo.LineaInventario;
import com.mycompany.proyectoprueba.modelo.LineasInventario;
import com.mycompany.proyectoprueba.modelo.MovimientoKardex;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.LineaRemision;
import com.mycompany.proyectoprueba.modelo.Remision;
import com.mycompany.proyectoprueba.modelo.Transferencia;
import com.mycompany.proyectoprueba.modelo.Transportista;
import com.mycompany.proyectoprueba.servicio.CentroCostoServicio;
import com.mycompany.proyectoprueba.servicio.InventarioServicio;
import com.mycompany.proyectoprueba.util.Comun;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
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
 * Inventario de insumos. URL: InventarioServlet
 * Operaciones (tipo):
 *  combos      : listarCentros, listarCentrosCosto, listarAlmacenes, listarInsumos, stockAlmacen
 *  salida OT   : otsConPendientes, pendientesSalida, emitirSalida
 *  remisiones  : listarRemisiones, anularRemision, imprimirRemision
 *  ajustes     : listarAjustes, detalleAjuste, crearAjuste, anularAjuste
 *  transferenc.: listarTransferencias, detalleTransferencia, crearTransferencia, anularTransferencia
 *  conteo      : registrarConteo
 *  Kardex      : kardex, imprimirKardex
 *  reportes    : reporteConsumo, reporteMasUsados, reporteValorizacion, alertasMinimo
 * Las operaciones de escritura responden {ok, error}.
 */
public class InventarioServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void atender(String tipo, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Connection conn = conexion(req);
        InventarioServicio servicio = InventarioServicio.con(conn, permisos(req), idUsuario(req).intValue());

        // Combos
        if ("listarCentros".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar --", servicio.centros());

        } else if ("listarCentrosCosto".equals(tipo)) {
            opciones(req, resp, "-- Seleccionar --", CentroCostoServicio.con(conn).activosDelCentro(numeroConSigno(req.getParameter("id_centro"))));

        } else if ("listarAlmacenes".equals(tipo)) {
            // Almacenes activos de un centro, o todos con id_centro vacío (con el nombre del centro)
            int idCentro = numeroConSigno(req.getParameter("id_centro"));
            List<Opcion> lista = new ArrayList<Opcion>();
            // destino de una transferencia: puede ser un almacén de cualquier centro
            for (Almacen a : servicio.almacenes(idCentro, "true".equals(req.getParameter("destino")))) {
                lista.add(new Opcion(a.getId(), Comun.txt(a.getNombre()) + (idCentro == 0 ? " (" + Comun.txt(a.getCentroNombre()) + ")" : "")));
            }
            opciones(req, resp, "true".equals(req.getParameter("todos")) ? "Todos los almacenes" : "-- Seleccionar --", lista);

        } else if ("listarInsumos".equals(tipo)) {
            req.setAttribute("insumos", servicio.insumos());
            vista(req, resp, "inventario/insumos.jsp");

        } else if ("stockAlmacen".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (ItemInsumo i : servicio.stockDeAlmacen(numeroConSigno(req.getParameter("id_almacen")))) {
                JSONObject o = new JSONObject();
                o.put("id_insumo", i.idInsumo);
                o.put("codigo", i.codigo);
                o.put("nombre", i.nombre);
                o.put("unidad", i.unidad);
                o.put("stock", i.cantidad);
                lista.put(o);
            }
            json(resp, lista);

        // Salida de insumos
        } else if ("otsConPendientes".equals(tipo)) {
            List<Opcion> lista = new ArrayList<Opcion>();
            for (OrdenTrabajo o : servicio.otsConPendientes()) {
                lista.add(new Opcion(o.id, o.numero + " - " + o.titulo + " (" + o.items + " insumo" + (o.items == 1 ? "" : "s") + " por despachar)"));
            }
            opciones(req, resp, "-- Seleccionar orden de trabajo --", lista);

        } else if ("pendientesSalida".equals(tipo)) {
            pendientesSalida(servicio, numeroConSigno(req.getParameter("id_ot")), resp);

        } else if ("emitirSalida".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                InventarioServicio.Documento d = servicio.emitirSalida(numeroConSigno(req.getParameter("id_ot")), transportista(req),
                        Comun.limpio(req.getParameter("observacion")));
                r.put("ok", true);
                r.put("numero", d.numero);
                r.put("id_remision", d.idRemision);
                r.put("fecha", Comun.fechaHora(d.fecha));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        // Remisiones
        } else if ("listarRemisiones".equals(tipo)) {
            listarRemisiones(servicio, req, resp);

        } else if ("detalleRemision".equals(tipo)) {
            java.util.List<LineaRemision> lineas;
            try {
                lineas = servicio.detalleRemision(numeroConSigno(req.getParameter("id")));
            } catch (IllegalArgumentException e) {
                JSONObject r = new JSONObject();
                r.put("error", mensaje(e));
                json(resp, r);
                return;
            }
            JSONArray lista = new JSONArray();
            for (LineaRemision l : lineas) {
                JSONObject o = new JSONObject();
                o.put("orden", l.orden);
                o.put("tipo", l.tipoItem);
                o.put("codigo", Comun.txt(l.codigo));
                o.put("descripcion", Comun.txt(l.descripcion));
                o.put("unidad", Comun.txt(l.unidad));
                o.put("cantidad", l.cantidad);
                o.put("origen", Comun.txt(l.origen));
                lista.put(o);
            }
            json(resp, lista);

        } else if ("anularRemision".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                Timestamp fecha = servicio.anularRemision(numeroConSigno(req.getParameter("id")), Comun.limpio(req.getParameter("motivo")));
                aceptado(r, fecha);
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        // Ajustes
        } else if ("listarAjustes".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (Ajuste a : servicio.ajustes()) {
                JSONObject o = new JSONObject();
                o.put("id", a.id);
                o.put("numero", a.numero);
                o.put("fecha", Comun.fechaHora(a.fecha));
                o.put("tipo", a.tipo);
                o.put("origen", a.origen);
                o.put("motivo", a.motivo);
                o.put("anulado", a.anulado);
                o.put("fecha_anulacion", Comun.fechaHora(a.fechaAnulacion));
                o.put("motivo_anulacion", a.motivoAnulacion);
                o.put("centro", a.centro);
                o.put("centro_costo", a.centroCosto);
                o.put("almacen", a.almacen);
                o.put("usuario", a.usuario);
                o.put("items", a.items);
                o.put("valor", a.valor.toPlainString());
                lista.put(o);
            }
            json(resp, lista);

        } else if ("detalleAjuste".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (ItemInsumo i : servicio.lineasAjuste(numeroConSigno(req.getParameter("id")))) {
                JSONObject o = new JSONObject();
                o.put("codigo", i.codigo);
                o.put("nombre", i.nombre);
                o.put("unidad", i.unidad);
                o.put("cantidad", i.cantidad);
                o.put("costo", decimal(i.costo, "0"));
                lista.put(o);
            }
            json(resp, lista);

        } else if ("crearAjuste".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                InventarioServicio.Documento d = servicio.crearAjuste(req.getParameter("tipo_ajuste"), numeroConSigno(req.getParameter("id_centro")),
                        numeroConSigno(req.getParameter("id_centro_costo")), numeroConSigno(req.getParameter("id_almacen")),
                        Comun.limpio(req.getParameter("motivo")), lineas(req.getParameter("items"), "cantidad", 0));
                r.put("ok", true);
                r.put("numero", d.numero);
                r.put("id", d.id);
                r.put("fecha", Comun.fechaHora(d.fecha));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("anularAjuste".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                aceptado(r, servicio.anularAjuste(numeroConSigno(req.getParameter("id")), Comun.limpio(req.getParameter("motivo"))));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        // Transferencias
        } else if ("listarTransferencias".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (Transferencia t : servicio.transferencias()) {
                JSONObject o = new JSONObject();
                o.put("id", t.id);
                o.put("numero", t.numero);
                o.put("fecha", Comun.fechaHora(t.fecha));
                o.put("observacion", t.observacion);
                o.put("anulada", t.anulada);
                o.put("fecha_anulacion", Comun.fechaHora(t.fechaAnulacion));
                o.put("motivo_anulacion", t.motivoAnulacion);
                o.put("origen", t.origen + " (" + t.centroOrigen + ")");
                o.put("destino", t.destino + " (" + t.centroDestino + ")");
                o.put("usuario", t.usuario);
                o.put("id_remision", t.idRemision);
                o.put("remision", t.remision);
                o.put("items", t.items);
                lista.put(o);
            }
            json(resp, lista);

        } else if ("detalleTransferencia".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (ItemInsumo i : servicio.lineasTransferencia(numeroConSigno(req.getParameter("id")))) {
                JSONObject o = new JSONObject();
                o.put("codigo", i.codigo);
                o.put("nombre", i.nombre);
                o.put("unidad", i.unidad);
                o.put("cantidad", i.cantidad);
                lista.put(o);
            }
            json(resp, lista);

        } else if ("crearTransferencia".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                InventarioServicio.Documento d = servicio.crearTransferencia(numeroConSigno(req.getParameter("id_almacen_origen")),
                        numeroConSigno(req.getParameter("id_almacen_destino")), transportista(req), Comun.limpio(req.getParameter("observacion")),
                        lineas(req.getParameter("items"), "cantidad", 0));
                r.put("ok", true);
                r.put("numero", d.numero);
                r.put("remision", d.remision);
                r.put("id_remision", d.idRemision);
                r.put("fecha", Comun.fechaHora(d.fecha));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        } else if ("anularTransferencia".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                aceptado(r, servicio.anularTransferencia(numeroConSigno(req.getParameter("id")), Comun.limpio(req.getParameter("motivo"))));
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        // Inventario físico
        } else if ("registrarConteo".equals(tipo)) {
            JSONObject r = new JSONObject();
            try {
                InventarioServicio.Conteo c = servicio.registrarConteo(numeroConSigno(req.getParameter("id_almacen")),
                        Comun.limpio(req.getParameter("observacion")), lineas(req.getParameter("items"), "contado", -1));
                r.put("ok", true);
                if (c.numero == null) {
                    r.put("sin_diferencias", true);
                } else {
                    r.put("numero", c.numero);
                    r.put("diferencias", c.diferencias);
                    r.put("fecha", Comun.fechaHora(c.fecha));
                }
            } catch (IllegalArgumentException e) {
                rechazo(r, mensaje(e));
            }
            json(resp, r);

        // Kardex y reportes
        } else if ("kardex".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (MovimientoKardex m : servicio.kardex(numeroConSigno(req.getParameter("id_insumo")), numeroConSigno(req.getParameter("id_almacen")),
                    fecha(req.getParameter("desde")), fecha(req.getParameter("hasta")))) {
                JSONObject o = new JSONObject();
                o.put("fecha", Comun.fechaHora(m.fecha));
                o.put("tipo", m.tipo);
                o.put("documento", m.documento);
                o.put("cantidad", m.cantidad);
                o.put("saldo", m.saldo);
                o.put("costo", decimal(m.costo, ""));
                o.put("observacion", m.observacion);
                o.put("almacen", m.almacen);
                o.put("usuario", m.usuario);
                o.put("centro_costo", m.centroCosto);
                lista.put(o);
            }
            json(resp, lista);

        } else if ("reporteConsumo".equals(tipo)) {
            // Consumo por centro de costo: insumos entregados en OT + ajustes de consumo no anulados
            JSONArray lista = new JSONArray();
            for (FilaReporteInventario f : servicio.consumoPorCentroCosto(fecha(req.getParameter("desde")), fecha(req.getParameter("hasta")))) {
                JSONObject o = new JSONObject();
                o.put("centro", f.centro);
                o.put("centro_costo", f.centroCosto);
                o.put("unidades", f.unidades);
                o.put("valor", decimal(f.valor, "0"));
                o.put("documentos", f.documentos);
                lista.put(o);
            }
            json(resp, lista);

        } else if ("reporteMasUsados".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (FilaReporteInventario f : servicio.masUsados(fecha(req.getParameter("desde")), fecha(req.getParameter("hasta")))) {
                JSONObject o = new JSONObject();
                o.put("codigo", f.codigo);
                o.put("nombre", f.nombre);
                o.put("unidad", f.unidad);
                o.put("unidades", f.unidades);
                o.put("valor", decimal(f.valor, "0"));
                lista.put(o);
            }
            json(resp, lista);

        } else if ("reporteValorizacion".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (FilaReporteInventario f : servicio.valorizacion()) {
                JSONObject o = new JSONObject();
                o.put("centro", f.centro);
                o.put("almacen", f.almacen);
                o.put("insumos", f.insumos);
                o.put("unidades", f.unidades);
                o.put("valor", f.valor.toPlainString());
                lista.put(o);
            }
            json(resp, lista);

        } else if ("alertasMinimo".equals(tipo)) {
            JSONArray lista = new JSONArray();
            for (FilaReporteInventario f : servicio.alertasMinimo()) {
                JSONObject o = new JSONObject();
                o.put("codigo", f.codigo);
                o.put("nombre", f.nombre);
                o.put("almacen", f.almacen);
                o.put("stock", f.stock);
                o.put("minimo", f.minimo);
                lista.put(o);
            }
            json(resp, lista);

        // Impresión (JRXML)
        } else if ("imprimirRemision".equals(tipo) || "imprimirKardex".equals(tipo)) {
            imprimir(servicio, "imprimirRemision".equals(tipo), conn, req, resp);

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

    private static void aceptado(JSONObject r, Timestamp fecha) {
        r.put("ok", true);
        r.put("fecha", Comun.fechaHora(fecha));
    }

    /** Número entero (admite signo) o 0 si viene vacío o inválido. */
    private static int numeroConSigno(String v) {
        return (v != null && v.trim().matches("-?[0-9]{1,9}")) ? Integer.parseInt(v.trim()) : 0;
    }

    /** Fecha del filtro: solo 'yyyy-MM-dd' válido, si no ''. */
    private static String fecha(String v) {
        return (v != null && v.trim().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) ? v.trim() : "";
    }

    private static String decimal(BigDecimal v, String siNulo) {
        return v == null ? siNulo : v.toPlainString();
    }

    private static Transportista transportista(HttpServletRequest req) {
        return new Transportista(Comun.limpio(req.getParameter("transportista_nombre")), Comun.limpio(req.getParameter("transportista_apellido")),
                Comun.limpio(req.getParameter("transportista_ci")), Comun.limpio(req.getParameter("vehiculo_marca")),
                Comun.limpio(req.getParameter("vehiculo_modelo")), Comun.limpio(req.getParameter("vehiculo_chapa")));
    }

    /**
     * Líneas [{id_insumo, &lt;campo&gt;}] enviadas por la pantalla. Los errores de lectura no se
     * informan acá: el servicio los informa cuando llega a usarlas, como antes.
     */
    private static LineasInventario lineas(String items, String campo, int porDefecto) {
        JSONArray arreglo;
        try {
            arreglo = new JSONArray(items);
        } catch (RuntimeException e) {
            return new LineasInventario(e);
        }
        List<LineaInventario> lista = new ArrayList<LineaInventario>();
        for (int i = 0; i < arreglo.length(); i++) {
            try {
                JSONObject it = arreglo.getJSONObject(i);
                lista.add(new LineaInventario(it.optInt("id_insumo", 0), it.optInt(campo, porDefecto)));
            } catch (RuntimeException e) {
                lista.add(new LineaInventario(e));
            }
        }
        return new LineasInventario(lista);
    }

    /** Cabecera de la OT + insumos pendientes de salida (solo lectura). */
    private void pendientesSalida(InventarioServicio servicio, int idOT, HttpServletResponse resp) throws Exception {
        JSONObject f = new JSONObject();
        OrdenTrabajo o = servicio.otParaSalida(idOT);
        if (o == null) {
            f.put("error", "Orden de trabajo no encontrada");
            json(resp, f);
            return;
        }
        f.put("numero", o.numero);
        f.put("titulo", o.titulo);
        f.put("estado", o.estado);
        f.put("centro", o.centro);
        f.put("centro_costo", o.centroCosto);
        f.put("solicitante", o.solicitante);
        JSONArray items = new JSONArray();
        for (ItemInsumo i : servicio.pendientesDeSalida(idOT)) {
            JSONObject it = new JSONObject();
            it.put("codigo", i.codigo);
            it.put("nombre", i.nombre);
            it.put("unidad", i.unidad);
            it.put("cantidad", i.cantidad);
            it.put("almacen", i.almacen);
            items.put(it);
        }
        f.put("items", items);
        json(resp, f);
    }

    /** tipo_remision: '' | INSUMOS | SALIDA_INSUMOS | TRANSFERENCIA | SALIDA_ACTIVOS; estado_remision: '' | VIGENTE | ANULADA. */
    private void listarRemisiones(InventarioServicio servicio, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONArray lista = new JSONArray();
        for (Remision r : servicio.remisiones(req.getParameter("tipo_remision"), req.getParameter("estado_remision"))) {
            JSONObject o = new JSONObject();
            o.put("id", r.id);
            o.put("numero", r.numero);
            o.put("tipo", Comun.txt(r.tipo));
            o.put("fecha", Comun.fechaHora(r.fecha));
            o.put("anulada", r.anulada);
            o.put("motivo_anulacion", r.motivoAnulacion);
            o.put("fecha_anulacion", Comun.fechaHora(r.fechaAnulacion));
            o.put("transportista", r.transportista);
            o.put("ci", r.ci);
            o.put("referencia", r.referencia());
            o.put("emitio", r.emitio);
            o.put("items", r.items);
            lista.put(o);
        }
        json(resp, lista);
    }

    /** Remisión o Kardex en PDF (se compila el .jrxml si hace falta). */
    private void imprimir(InventarioServicio servicio, boolean esRemision, Connection conn, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String usuarioSesion = (String) req.getSession().getAttribute("nombre_completo");
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("USUARIO", usuarioSesion == null ? "Sistema" : usuarioSesion);
            parametros.put("FECHA", new java.util.Date());
            if (esRemision) {
                int idRemision = numeroConSigno(req.getParameter("id"));
                if (!servicio.remisionVisible(idRemision)) {
                    escribir(resp, "<div style='font-family:sans-serif;padding:20px'>No tiene acceso a esa remisión.</div>");
                    return;
                }
                parametros.put("ID_REMISION", Integer.valueOf(idRemision));
            } else {
                parametros.put("ID_INSUMO", Integer.valueOf(numeroConSigno(req.getParameter("id_insumo"))));
                parametros.put("ID_ALMACEN", Integer.valueOf(numeroConSigno(req.getParameter("id_almacen"))));
                parametros.put("DESDE", fecha(req.getParameter("desde")));
                parametros.put("HASTA", fecha(req.getParameter("hasta")));
                parametros.put("CENTROS", permisos(req).centrosCsv());
            }
            String nombre = esRemision ? "remision" : "kardex";
            byte[] pdf = JasperRunManager.runReportToPdf(Reportes.cargar(getServletContext(), nombre), parametros, conn);
            ReportePdf.enviar(resp, pdf, nombre + ".pdf");
        } catch (JRException e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div style='font-family:sans-serif;padding:20px'><h3>No se pudo generar el reporte</h3><p>" + Html.esc(mensaje(e))
                    + "</p><p>Si el problema es la compilación, abra el .jrxml correspondiente en iReport o Jaspersoft Studio, compílelo y deje el .jasper en la carpeta reportes.</p></div>");
        }
    }
}
