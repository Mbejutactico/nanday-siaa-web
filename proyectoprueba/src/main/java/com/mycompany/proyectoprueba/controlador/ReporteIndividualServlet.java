package com.mycompany.proyectoprueba.controlador;

import com.mycompany.proyectoprueba.servicio.ReporteIndividualServicio;
import com.mycompany.proyectoprueba.servicio.ReporteIndividualServicio.Documento;
import com.mycompany.proyectoprueba.util.Html;
import com.mycompany.proyectoprueba.util.ReportePdf;
import com.mycompany.proyectoprueba.util.Reportes;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;

/**
 * Reportes individuales en PDF. URL: ReporteIndividualServlet?reporte=proveedor&cod=5
 * El parámetro "reporte" elige el reporte y "cod" es el id del registro. El permiso de cada
 * reporte se controla en ReglasAcceso (igual que las operaciones de los demás controladores).
 */
public class ReporteIndividualServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Configuración de un reporte individual. */
    private static final class Reporte {
        final String sinId;        // texto cuando falta el id
        final String jasper;       // /reportes/{jasper}.jasper
        final String parametro;    // parámetro del id en el reporte
        final String titulo;
        final String archivo;      // prefijo del nombre del PDF
        final Documento documento; // control por centro (null = sin control)

        Reporte(String sinId, String jasper, String parametro, String titulo, String archivo, Documento documento) {
            this.sinId = sinId;
            this.jasper = jasper;
            this.parametro = parametro;
            this.titulo = titulo;
            this.archivo = archivo;
            this.documento = documento;
        }
    }

    private static final Map<String, Reporte> REPORTES = new HashMap<String, Reporte>();

    static {
        agregar("almacen", "el ID del almacén", "almacenIndividual", "ID_ALMACEN", "Detalle de Almacén", "almacen_", null);
        agregar("centro", "el ID del centro", "centroIndividual", "ID_CENTRO", "Detalle de Centro", "centro_", null);
        agregar("centroCosto", "el ID del centro de costo", "centroCostoIndividual", "ID_CENTRO_COSTO", "Detalle de Centro de Costo", "centro_costo_", null);
        agregar("insumo", "el ID del insumo", "insumoIndividual", "ID_INSUMO", "Detalle de Insumo", "insumo_", null);
        agregar("oc", "el ID de la Orden de Compra", "ocIndividual", "ID_OC", "Orden de Compra", "OC_", Documento.ORDEN_COMPRA);
        agregar("proveedor", "el ID del proveedor", "proveedorIndividual", "ID_PROVEEDOR", "Detalle de Proveedor", "proveedor_", null);
        agregar("rol", "el ID del rol", "rolIndividual", "ID_ROL", "Detalle de Rol", "rol_", null);
        agregar("stock", "el ID del stock", "stockIndividual", "ID_STOCK", "Detalle de Stock", "stock_", Documento.STOCK);
        agregar("unidad", "el ID de la unidad", "unidadIndividual", "ID_UNIDAD", "Detalle de Unidad de Medida", "unidad_", null);
        agregar("usuario", "el ID del usuario", "usuarioIndividual", "ID_USUARIO", "Detalle de Usuario", "usuario_", null);
    }

    private static void agregar(String nombre, String sinId, String jasper, String parametro, String titulo, String archivo, Documento documento) {
        REPORTES.put(nombre, new Reporte(sinId, jasper, parametro, titulo, archivo, documento));
    }

    private static final String SIN_ACCESO = "<div style='font-family:sans-serif;padding:20px'>No tiene acceso a ese documento con su rol.</div>";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        procesar(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        procesar(req, resp);
    }

    private void procesar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        String nombre = req.getParameter("reporte");
        if ("hojaVida".equals(nombre)) {
            hojaDeVida(req, resp);
            return;
        }
        Reporte r = nombre == null ? null : REPORTES.get(nombre);
        if (r == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            String cod = req.getParameter("cod");
            if (cod == null || cod.isEmpty()) {
                escribir(resp, "Error: No se especificó " + r.sinId);
                return;
            }
            int id = Integer.parseInt(cod);
            if (!ReporteIndividualServicio.con(conexion(req), com.mycompany.proyectoprueba.seguridad.Permisos.de(req)).puedeVer(r.documento, id)) {
                resp.setContentType("text/html; charset=UTF-8");
                escribir(resp, SIN_ACCESO);
                return;
            }
            String rutaJasper = ReportePdf.ruta(getServletContext(), r.jasper);
            File archivo = new File(rutaJasper);
            if (!archivo.exists()) {
                escribir(resp, "Error: El archivo de reporte no existe en: " + rutaJasper);
                return;
            }
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put(r.parametro, Integer.valueOf(id));
            parametros.put("TITULO", r.titulo);
            parametros.put("FECHA", new java.util.Date());
            parametros.put("USUARIO", usuario(req));
            byte[] pdf = JasperRunManager.runReportToPdf(archivo.getPath(), parametros, conexion(req));
            ReportePdf.enviar(resp, pdf, r.archivo + id + ".pdf");
        } catch (Exception e) {
            resp.setContentType("text/html");
            escribir(resp, "<div class='alert alert-danger'>Error al generar el reporte: " + Html.esc(mensaje(e)) + "</div>");
        }
    }

    /** Hoja de vida de un activo fijo (reportes/hojaVidaActivo.jrxml; se compila si hace falta). */
    private void hojaDeVida(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String cod = req.getParameter("cod");
            if (cod == null || !cod.trim().matches("[0-9]{1,9}")) {
                resp.setContentType("text/html; charset=UTF-8");
                escribir(resp, "Error: no se indicó un activo válido");
                return;
            }
            int idEquipo = Integer.parseInt(cod.trim());
            Connection conn = conexion(req);
            if (!ReporteIndividualServicio.con(conn, com.mycompany.proyectoprueba.seguridad.Permisos.de(req)).puedeVer(Documento.ACTIVO, idEquipo)) {
                resp.setContentType("text/html; charset=UTF-8");
                escribir(resp, SIN_ACCESO);
                return;
            }
            JasperReport reporte;
            try {
                reporte = Reportes.cargar(getServletContext(), "hojaVidaActivo");
            } catch (JRException e) {
                getServletContext().log("No se pudo compilar hojaVidaActivo.jrxml", e);
                resp.setContentType("text/html; charset=UTF-8");
                escribir(resp, "<div style='font-family:sans-serif;padding:20px'>"
                        + "<h3>No se pudo preparar el reporte de hoja de vida</h3>"
                        + "<p>" + Html.esc(mensaje(e)) + "</p>"
                        + "<p>Abra <b>src/main/webapp/reportes/hojaVidaActivo.jrxml</b> en iReport o Jaspersoft Studio, "
                        + "compílelo para generar <b>hojaVidaActivo.jasper</b> en la misma carpeta y vuelva a intentar.</p></div>");
                return;
            }
            Map<String, Object> parametros = new HashMap<String, Object>();
            parametros.put("ID_EQUIPO", Integer.valueOf(idEquipo));
            parametros.put("TITULO", "Hoja de vida del activo");
            parametros.put("FECHA", new java.util.Date());
            parametros.put("USUARIO", usuario(req));
            byte[] pdf = JasperRunManager.runReportToPdf(reporte, parametros, conn);
            ReportePdf.enviar(resp, pdf, "HojaVida_Activo_" + idEquipo + ".pdf");
        } catch (Exception e) {
            resp.setContentType("text/html; charset=UTF-8");
            escribir(resp, "<div class='alert alert-danger'>Error al generar la hoja de vida: " + Html.esc(mensaje(e)) + "</div>");
        }
    }

    private Connection conexion(HttpServletRequest req) throws java.sql.SQLException {
        return com.mycompany.proyectoprueba.util.Conexion.obtener(getServletContext(), req);
    }

    private String mensaje(Exception e) {
        return com.mycompany.proyectoprueba.util.Errores.mensaje(getServletContext(), e);
    }

    /** Usuario de la sesión para el reporte ("Sistema" si no hay nombre). */
    private static String usuario(HttpServletRequest req) {
        Object n = req.getSession().getAttribute("nombre_completo");
        return (n instanceof String && !((String) n).isEmpty()) ? (String) n : "Sistema";
    }

    private static void escribir(HttpServletResponse resp, String texto) throws IOException {
        resp.getWriter().print(texto);
    }
}
