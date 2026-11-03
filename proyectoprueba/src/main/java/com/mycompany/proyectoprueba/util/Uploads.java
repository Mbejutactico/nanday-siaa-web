package com.mycompany.proyectoprueba.util;

import java.io.File;
import javax.servlet.ServletContext;

/**
 * Carpeta de fotos de perfil fuera del proyecto desplegado, para que no se pierdan al
 * volver a desplegar. Se configura con "uploads.dir" en META-INF/context.xml; si no está,
 * usa CARPETA_DEL_USUARIO/nanday_uploads.
 */
public final class Uploads {

    private Uploads() {
    }

    public static File carpetaPerfiles(ServletContext ctx) {
        String base = ctx.getInitParameter("uploads.dir");
        if (base == null || base.trim().isEmpty()) {
            base = System.getProperty("user.home") + File.separator + "nanday_uploads";
        }
        File carpeta = new File(base.trim(), "perfiles");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        return carpeta;
    }

    /** Solo se aceptan los nombres que genera el sistema: perfil_ID.jpg / perfil_ID.png */
    public static boolean nombreValido(String nombre) {
        return nombre != null && nombre.matches("perfil_[0-9]{1,10}\\.(jpg|png)");
    }
}
