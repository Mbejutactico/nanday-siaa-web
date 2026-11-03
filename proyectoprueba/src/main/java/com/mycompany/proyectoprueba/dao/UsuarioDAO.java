package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.Credencial;
import com.mycompany.proyectoprueba.modelo.HistorialCentro;
import com.mycompany.proyectoprueba.modelo.Usuario;
import com.mycompany.proyectoprueba.modelo.UsuarioCentro;
import java.sql.SQLException;
import java.util.List;

/** Acceso a datos de usuarios y sus centros adicionales. */
public interface UsuarioDAO {

    /** Todos (con rol y centro principal), ordenados por id. */
    List<Usuario> listarTodos() throws SQLException;

    /** true si otro usuario (distinto de idExcluido) usa ese email, sin distinguir mayúsculas. */
    boolean emailEnUso(String email, int idExcluido) throws SQLException;

    /** Inserta con la contraseña ya cifrada. Devuelve el id del usuario creado. */
    int insertar(Usuario u, String hashPassword) throws SQLException;

    /** Centro principal (centro_asignado) actual del usuario, o null. */
    Integer centroPrincipal(int idUsuario) throws SQLException;

    /** Modifica los datos; hashPassword null = no cambia la contraseña. */
    int actualizar(Usuario u, String hashPassword) throws SQLException;

    int eliminar(int id) throws SQLException;

    List<UsuarioCentro> listarCentros(int idUsuario) throws SQLException;

    /** El centro adicional asignado al usuario (con sus fechas), o null si no lo tiene. */
    UsuarioCentro buscarCentro(int idUsuario, int idCentro) throws SQLException;

    /** Alta de un centro adicional. Fechas yyyy-MM-dd; desde null = hoy. */
    void insertarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion) throws SQLException;

    /** Modificación de las fechas y la observación de un centro adicional. Devuelve las filas modificadas. */
    int actualizarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion) throws SQLException;

    int quitarCentro(int idUsuario, int idCentro) throws SQLException;

    /** Registra un movimiento en el historial de accesos (accion ALTA, MODIFICACION o BAJA). */
    void registrarHistorial(int idUsuario, int idCentro, String tipoAcceso, String accion, java.sql.Date desde,
            java.sql.Date hasta, String observacion, Integer idRegistra) throws SQLException;

    /** Historial de accesos del usuario, del más reciente al más antiguo. */
    List<HistorialCentro> historialCentros(int idUsuario) throws SQLException;

    // Inicio de sesión y perfil propio

    /**
     * Usuario ACTIVO con ese email, sin distinguir mayúsculas (si hubiera dos que solo difieren
     * en mayúsculas, gana el exacto). Devuelve el usuario con el hash de su contraseña o null.
     */
    Credencial buscarParaLogin(String email) throws SQLException;

    /** Hash de la contraseña guardada o null si el usuario no existe. */
    String hashPassword(int idUsuario) throws SQLException;

    int actualizarPassword(int idUsuario, String hash) throws SQLException;

    int actualizarNombre(int idUsuario, String nombre, String apellido) throws SQLException;

    int actualizarFoto(int idUsuario, String rutaWeb) throws SQLException;
}
