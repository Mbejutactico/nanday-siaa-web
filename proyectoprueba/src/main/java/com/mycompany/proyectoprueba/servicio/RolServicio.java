package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.RolDAO;
import com.mycompany.proyectoprueba.dao.jdbc.RolDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Rol;
import com.mycompany.proyectoprueba.modelo.TipoRol;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de los roles. */
public class RolServicio {

    static final String NO_EXISTE = "El rol ya no existe. Actualice la lista.";

    private final Connection conn;
    private final RolDAO dao;

    public RolServicio(Connection conn, RolDAO dao) {
        this.conn = conn;
        this.dao = dao;
    }

    public static RolServicio con(Connection conn) {
        return new RolServicio(conn, new RolDAOJdbc(conn));
    }

    public List<Rol> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Rol> listarActivos() throws SQLException {
        return dao.listarActivos();
    }

    /**
     * Guarda o modifica el rol (para modificar, un id negativo es un rol no válido). Nunca deja el sistema sin un administrador activo.
     * Un nombre repetido se informa como regla de negocio.
     */
    public void guardar(Rol r, boolean nuevo) throws SQLException {
        r.setNombre(r.getNombre() == null ? "" : r.getNombre().trim());
        r.setDescripcion(r.getDescripcion() == null ? "" : r.getDescripcion().trim());
        r.setCodigo(r.getCodigo() == null ? "" : r.getCodigo().trim());
        if (r.getNombre().isEmpty()) {
            throw new ReglaNegocioException("El nombre del rol es obligatorio.");
        }
        if (r.getNombre().length() > 50) {
            throw new ReglaNegocioException("El nombre del rol puede tener hasta 50 caracteres.");
        }
        if (!TipoRol.existe(r.getCodigo())) {
            throw new ReglaNegocioException("Seleccione el tipo de permisos del rol.");
        }
        if (!nuevo && r.getId() < 0) {
            throw new ReglaNegocioException("Rol no válido.");
        }
        conn.setAutoCommit(false);
        try {
            if (nuevo) {
                dao.insertar(r);
            } else if (dao.actualizar(r) == 0) {
                conn.rollback();
                throw new ReglaNegocioException(NO_EXISTE);
            }
            // Nunca dejar el sistema sin un administrador que pueda entrar
            if (dao.contarAdministradoresActivos() == 0) {
                conn.rollback();
                throw new ReglaNegocioException("No se guardó: el sistema quedaría sin ningún usuario administrador activo. "
                        + "Debe quedar al menos un rol activo de tipo Administrador con un usuario activo.");
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            if ("23505".equals(e.getSQLState())) {
                throw new ReglaNegocioException("Ya existe un rol con ese nombre.");
            }
            throw e;
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** No se elimina un rol que tienen usuarios (se avisa que lo desactiven). */
    public void eliminar(int id) throws SQLException {
        int usuarios = dao.contarUsuarios(id);
        if (usuarios > 0) {
            throw ReglaNegocioException.aviso("No se puede eliminar: " + usuarios + (usuarios == 1 ? " usuario tiene" : " usuarios tienen")
                    + " este rol. Asígneles otro rol o desactive el rol en lugar de eliminarlo.");
        }
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }
}
