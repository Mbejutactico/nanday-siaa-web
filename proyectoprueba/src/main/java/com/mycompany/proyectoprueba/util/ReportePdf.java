package com.mycompany.proyectoprueba.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;

/** Genera un reporte Jasper ya compilado (.jasper de /reportes) y lo envía como PDF. */
public final class ReportePdf {

    private ReportePdf() {
    }

    /**
     * Envía /reportes/{nombre}.jasper como PDF "inline" con nombre {prefijo}_yyyyMMdd_HHmmss.pdf.
     * Devuelve false (y no envía nada) si el archivo del reporte no existe.
     */
    public static boolean enviarCompilado(ServletContext ctx, HttpServletResponse resp, String nombre,
            Map<String, Object> parametros, Connection conn, String prefijo) throws JRException, IOException {
        String ruta = ctx.getRealPath("/reportes/" + nombre + ".jasper");
        File archivo = ruta == null ? null : new File(ruta);
        if (archivo == null || !archivo.exists()) {
            return false;
        }
        byte[] pdf = JasperRunManager.runReportToPdf(archivo.getPath(), parametros, conn);
        enviar(resp, pdf, prefijo + "_" + sello() + ".pdf");
        return true;
    }

    /** Envía un PDF ya generado como "inline" con el nombre de archivo indicado. */
    public static void enviar(HttpServletResponse resp, byte[] pdf, String nombreArchivo) throws IOException {
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=" + nombreArchivo);
        resp.setContentLength(pdf.length);
        OutputStream os = resp.getOutputStream();
        os.write(pdf);
        os.flush();
        os.close();
    }

    /** yyyyMMdd_HHmmss de ahora, para los nombres de archivo. */
    public static String sello() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    /** Ruta física del reporte (para el mensaje de error cuando no existe). */
    public static String ruta(ServletContext ctx, String nombre) {
        return ctx.getRealPath("/reportes/" + nombre + ".jasper");
    }
}
