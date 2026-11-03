package com.mycompany.proyectoprueba.dao.jdbc;

import com.mycompany.proyectoprueba.dao.UsuarioDAO;
import com.mycompany.proyectoprueba.modelo.Credencial;
import com.mycompany.proyectoprueba.modelo.Usuario;
import com.mycompany.proyectoprueba.modelo.UsuarioCentro;
import com.mycompany.proyectoprueba.modelo.HistorialCentro;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UsuarioDAOJdbc extends JdbcBase implements UsuarioDAO {

    public UsuarioDAOJdbc(Connection conn) {
        super(conn);
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        return lista("SELECT u.id_usuario, u.nombre, u.apellido, u.email, u.id_rol, r.nombre as rol_nombre, u.activo, u.centro_asignado, c.nombre as centro_nombre "
                + "FROM usuarios u LEFT JOIN roles r ON u.id_rol = r.id_rol LEFT JOIN centros c ON u.centro_asignado = c.id_centro ORDER BY u.id_usuario", rs -> {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setEmail(rs.getString("email"));
                    u.setIdRol(enteroONull(rs, "id_rol"));
                    u.setRolNombre(rs.getString("rol_nombre"));
                    u.setActivo(rs.getBoolean("activo"));
                    u.setCentroAsignado(enteroONull(rs, "centro_asignado"));
                    u.setCentroNombre(rs.getString("centro_nombre"));
                    return u;
                });
    }

    @Override
    public boolean emailEnUso(String email, int idExcluido) throws SQLException {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return existe("SELECT 1 FROM usuarios WHERE lower(email) = lower(?) AND id_usuario <> ?", email, idExcluido);
    }

    @Override
    public int insertar(Usuario u, String hashPassword) throws SQLException {
        return insertarConId("INSERT INTO usuarios (nombre, apellido, email, password, id_rol, activo, centro_asignado) VALUES (?,?,?,?,?,?,?) RETURNING id_usuario",
                u.getNombre(), u.getApellido(), u.getEmail(), hashPassword, entero(u.getIdRol()), u.isActivo(), entero(u.getCentroAsignado()));
    }

    @Override
    public int actualizar(Usuario u, String hashPassword) throws SQLException {
        if (hashPassword != null) {
            return ejecutar("UPDATE usuarios SET nombre=?, apellido=?, email=?, password=?, id_rol=?, activo=?, centro_asignado=? WHERE id_usuario=?",
                    u.getNombre(), u.getApellido(), u.getEmail(), hashPassword, entero(u.getIdRol()), u.isActivo(), entero(u.getCentroAsignado()), u.getId());
        }
        return ejecutar("UPDATE usuarios SET nombre=?, apellido=?, email=?, id_rol=?, activo=?, centro_asignado=? WHERE id_usuario=?",
                u.getNombre(), u.getApellido(), u.getEmail(), entero(u.getIdRol()), u.isActivo(), entero(u.getCentroAsignado()), u.getId());
    }

    @Override
    public int eliminar(int id) throws SQLException {
        return ejecutar("DELETE FROM usuarios WHERE id_usuario=?", id);
    }

    @Override
    public List<UsuarioCentro> listarCentros(int idUsuario) throws SQLException {
        return lista("SELECT uc.id_centro, c.nombre, uc.fecha_desde, uc.fecha_hasta, uc.observacion, "
                + "       (uc.fecha_desde <= CURRENT_DATE AND (uc.fecha_hasta IS NULL OR uc.fecha_hasta >= CURRENT_DATE)) AS vigente "
                + "FROM usuario_centros uc JOIN centros c ON c.id_centro = uc.id_centro WHERE uc.id_usuario = ? ORDER BY c.nombre",
                rs -> new UsuarioCentro(rs.getInt(1), rs.getString(2), rs.getDate(3), rs.getDate(4), rs.getString(5), rs.getBoolean(6)),
                idUsuario);
    }

    @Override
    public Integer centroPrincipal(int idUsuario) throws SQLException {
        return uno("SELECT centro_asignado FROM usuarios WHERE id_usuario = ?", rs -> enteroONull(rs, "centro_asignado"), idUsuario);
    }

    @Override
    public UsuarioCentro buscarCentro(int idUsuario, int idCentro) throws SQLException {
        return uno("SELECT uc.id_centro, c.nombre, uc.fecha_desde, uc.fecha_hasta, uc.observacion, "
                + "       (uc.fecha_desde <= CURRENT_DATE AND (uc.fecha_hasta IS NULL OR uc.fecha_hasta >= CURRENT_DATE)) AS vigente "
                + "FROM usuario_centros uc JOIN centros c ON c.id_centro = uc.id_centro WHERE uc.id_usuario = ? AND uc.id_centro = ?",
                rs -> new UsuarioCentro(rs.getInt(1), rs.getString(2), rs.getDate(3), rs.getDate(4), rs.getString(5), rs.getBoolean(6)),
                idUsuario, idCentro);
    }

    @Override
    public void insertarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion) throws SQLException {
        ejecutar("INSERT INTO usuario_centros (id_usuario, id_centro, fecha_desde, fecha_hasta, observacion) "
                + "VALUES (?, ?, COALESCE(CAST(? AS date), CURRENT_DATE), CAST(? AS date), ?)",
                idUsuario, idCentro, texto(desde), texto(hasta), texto(observacion));
    }

    @Override
    public int actualizarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion) throws SQLException {
        return ejecutar("UPDATE usuario_centros SET fecha_desde = COALESCE(CAST(? AS date), CURRENT_DATE), fecha_hasta = CAST(? AS date), observacion = ? "
                + "WHERE id_usuario = ? AND id_centro = ?",
                texto(desde), texto(hasta), texto(observacion), idUsuario, idCentro);
    }

    @Override
    public void registrarHistorial(int idUsuario, int idCentro, String tipoAcceso, String accion, java.sql.Date desde,
            java.sql.Date hasta, String observacion, Integer idRegistra) throws SQLException {
        ejecutar("INSERT INTO usuario_centros_historial (id_usuario, id_centro, tipo_acceso, accion, fecha_desde, fecha_hasta, observacion, id_usuario_registra) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                idUsuario, idCentro, tipoAcceso, accion,
                desde == null ? Nulo.FECHA : desde, hasta == null ? Nulo.FECHA : hasta, texto(observacion), entero(idRegistra));
    }

    @Override
    public List<HistorialCentro> historialCentros(int idUsuario) throws SQLException {
        return lista("SELECT h.fecha_registro, h.id_centro, c.nombre, h.tipo_acceso, h.accion, h.fecha_desde, h.fecha_hasta, h.observacion, "
                + "       NULLIF(TRIM(COALESCE(r.nombre, '') || ' ' || COALESCE(r.apellido, '')), '') AS registra "
                + "FROM usuario_centros_historial h JOIN centros c ON c.id_centro = h.id_centro "
                + "LEFT JOIN usuarios r ON r.id_usuario = h.id_usuario_registra "
                + "WHERE h.id_usuario = ? ORDER BY h.fecha_registro DESC, h.id_historial DESC",
                rs -> {
                    HistorialCentro x = new HistorialCentro();
                    x.fechaRegistro = rs.getTimestamp(1);
                    x.idCentro = rs.getInt(2);
                    x.centro = rs.getString(3);
                    x.tipoAcceso = rs.getString(4);
                    x.accion = rs.getString(5);
                    x.desde = rs.getDate(6);
                    x.hasta = rs.getDate(7);
                    x.observacion = rs.getString(8);
                    x.registradoPor = rs.getString(9);
                    return x;
                }, idUsuario);
    }

    @Override
    public int quitarCentro(int idUsuario, int idCentro) throws SQLException {
        return ejecutar("DELETE FROM usuario_centros WHERE id_usuario = ? AND id_centro = ?", idUsuario, idCentro);
    }

    @Override
    public Credencial buscarParaLogin(String email) throws SQLException {
        return uno("SELECT id_usuario, nombre, apellido, email, password, id_rol, activo, foto_perfil "
                + "FROM usuarios WHERE lower(email) = lower(?) AND activo = true ORDER BY (email = ?) DESC LIMIT 1", rs -> {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setEmail(rs.getString("email"));
                    u.setIdRol(Integer.valueOf(rs.getInt("id_rol")));
                    u.setActivo(rs.getBoolean("activo"));
                    u.setFotoPerfil(rs.getString("foto_perfil"));
                    return new Credencial(u, rs.getString("password"));
                }, email, email);
    }

    @Override
    public String hashPassword(int idUsuario) throws SQLException {
        return uno("SELECT password FROM usuarios WHERE id_usuario=?", rs -> rs.getString("password"), idUsuario);
    }

    @Override
    public int actualizarPassword(int idUsuario, String hash) throws SQLException {
        return ejecutar("UPDATE usuarios SET password=? WHERE id_usuario=?", hash, idUsuario);
    }

    @Override
    public int actualizarNombre(int idUsuario, String nombre, String apellido) throws SQLException {
        return ejecutar("UPDATE usuarios SET nombre=?, apellido=? WHERE id_usuario=?", nombre, apellido, idUsuario);
    }

    @Override
    public int actualizarFoto(int idUsuario, String rutaWeb) throws SQLException {
        return ejecutar("UPDATE usuarios SET foto_perfil=? WHERE id_usuario=?", rutaWeb, idUsuario);
    }
}
