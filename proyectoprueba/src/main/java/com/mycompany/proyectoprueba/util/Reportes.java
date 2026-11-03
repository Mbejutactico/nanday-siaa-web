package com.mycompany.proyectoprueba.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;

/**
 * Carga un reporte de /reportes. Usa el .jasper compilado si está al día;
 * si falta o el .jrxml es más nuevo, compila el .jrxml, guarda el .jasper
 * para la próxima vez y lo mantiene en memoria.
 */
public final class Reportes {

    private static final Map<String, Object[]> CACHE = new HashMap<String, Object[]>();

    private Reportes() {
    }

    public static synchronized JasperReport cargar(ServletContext ctx, String nombre) throws JRException {
        String rutaJrxml = ctx.getRealPath("/reportes/" + nombre + ".jrxml");
        String rutaJasper = ctx.getRealPath("/reportes/" + nombre + ".jasper");
        File jrxml = rutaJrxml == null ? null : new File(rutaJrxml);
        File jasper = rutaJasper == null ? null : new File(rutaJasper);

        boolean hayJrxml = jrxml != null && jrxml.isFile();
        boolean hayJasper = jasper != null && jasper.isFile();

        if (hayJasper && (!hayJrxml || jasper.lastModified() >= jrxml.lastModified())) {
            return (JasperReport) JRLoader.loadObject(jasper);
        }
        if (!hayJrxml) {
            throw new JRException("No se encontró el reporte " + nombre + ".jrxml en la carpeta reportes");
        }

        Object[] enCache = CACHE.get(nombre);
        if (enCache != null && ((Long) enCache[0]).longValue() == jrxml.lastModified()) {
            return (JasperReport) enCache[1];
        }
        JasperReport compilado = JasperCompileManager.compileReport(jrxml.getPath());
        CACHE.put(nombre, new Object[]{Long.valueOf(jrxml.lastModified()), compilado});
        try {
            JRSaver.saveObject(compilado, jasper);   // la próxima vez no hay que compilar
        } catch (Exception ignorada) {
            // sin permiso de escritura: se sigue usando la versión en memoria
        }
        return compilado;
    }
}
