package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.UsuarioDAO;
import com.mycompany.proyectoprueba.dao.jdbc.UsuarioDAOJdbc;
import com.mycompany.proyectoprueba.seguridad.Password;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;

/** Datos propios del usuario de la sesión: nombre, foto de perfil y contraseña. */
public class PerfilServicio {

    /** Tamaño máximo de la foto (igual que multipart-config de web.xml). */
    public static final long MAX_FOTO = 2L * 1024 * 1024;

    private final UsuarioDAO dao;

    public PerfilServicio(UsuarioDAO dao) {
        this.dao = dao;
    }

    public static PerfilServicio con(Connection conn) {
        return new PerfilServicio(new UsuarioDAOJdbc(conn));
    }

    /** Nombre y apellido obligatorios, hasta 100 caracteres cada uno. */
    public void actualizarDatos(int idUsuario, String nombre, String apellido) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty() || apellido == null || apellido.trim().isEmpty()) {
            throw new ReglaNegocioException("Nombre y apellido son obligatorios");
        }
        if (nombre.trim().length() > 100 || apellido.trim().length() > 100) {
            throw new ReglaNegocioException("Nombre y apellido pueden tener hasta 100 caracteres cada uno");
        }
        dao.actualizarNombre(idUsuario, nombre.trim(), apellido.trim());
    }

    /**
     * Guarda la foto (JPG o PNG reales, hasta 2 MB) como perfil_ID.jpg/png en la carpeta
     * indicada (fuera del proyecto desplegado) y devuelve la ruta web uploads/perfiles/...
     *
     * @param abrir abre el contenido del archivo subido (se lee dos veces: cabecera y copia)
     */
    public String actualizarFoto(int idUsuario, String contentType, long tamano, Contenido abrir, File carpeta)
            throws SQLException, IOException {
        if (tamano > MAX_FOTO) {
            throw new ReglaNegocioException("La imagen supera el tamaño máximo permitido (2MB)");
        }
        String extension;
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            extension = ".jpg";
        } else if ("image/png".equalsIgnoreCase(contentType)) {
            extension = ".png";
        } else {
            throw new ReglaNegocioException("Formato no permitido. Use JPG o PNG");
        }
        // El contenido debe ser realmente una imagen JPG o PNG (no basta con el tipo que declara el navegador)
        byte[] cabecera = new byte[8];
        int leidos;
        InputStream muestra = abrir.abrir();
        try {
            leidos = muestra.read(cabecera);
        } finally {
            muestra.close();
        }
        boolean esJpg = leidos >= 3 && (cabecera[0] & 0xFF) == 0xFF && (cabecera[1] & 0xFF) == 0xD8 && (cabecera[2] & 0xFF) == 0xFF;
        boolean esPng = leidos >= 8 && (cabecera[0] & 0xFF) == 0x89 && cabecera[1] == 'P' && cabecera[2] == 'N' && cabecera[3] == 'G';
        if ((".jpg".equals(extension) && !esJpg) || (".png".equals(extension) && !esPng)) {
            throw new ReglaNegocioException("El archivo no es una imagen JPG o PNG válida");
        }

        // Nombre fijo por usuario (sobrescribe la foto anterior) y sin fotos viejas con otra extensión
        String nombreArchivo = "perfil_" + idUsuario + extension;
        File destino = new File(carpeta, nombreArchivo);
        File posibleJpg = new File(carpeta, "perfil_" + idUsuario + ".jpg");
        File posiblePng = new File(carpeta, "perfil_" + idUsuario + ".png");
        if (posibleJpg.exists() && !posibleJpg.equals(destino)) {
            posibleJpg.delete();
        }
        if (posiblePng.exists() && !posiblePng.equals(destino)) {
            posiblePng.delete();
        }
        InputStream input = abrir.abrir();
        try {
            Files.copy(input, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            input.close();
        }
        String rutaWeb = "uploads/perfiles/" + nombreArchivo;
        dao.actualizarFoto(idUsuario, rutaWeb);
        return rutaWeb;
    }

    /** Cambia la contraseña verificando la actual (mínimo 6 caracteres, distinta de la actual). */
    public void cambiarPassword(int idUsuario, String actual, String nueva, String confirmar) throws SQLException {
        if (actual == null || nueva == null || confirmar == null || actual.trim().isEmpty() || nueva.trim().isEmpty()) {
            throw new ReglaNegocioException("Todos los campos son obligatorios");
        }
        String error = Password.validarNueva(nueva);
        if (error != null) {
            throw new ReglaNegocioException(error);
        }
        if (!nueva.equals(confirmar)) {
            throw new ReglaNegocioException("La confirmación no coincide con la nueva contraseña");
        }
        String guardada = dao.hashPassword(idUsuario);
        if (guardada == null) {
            throw new ReglaNegocioException("Usuario no encontrado");
        }
        if (!Password.verificar(actual, guardada)) {
            throw new ReglaNegocioException("La contraseña actual es incorrecta");
        }
        if (nueva.equals(actual)) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual");
        }
        dao.actualizarPassword(idUsuario, Password.hash(nueva));
    }

    /** Fuente del archivo subido. */
    public interface Contenido {
        InputStream abrir() throws IOException;
    }
}
