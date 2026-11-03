package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.RolDAO;
import com.mycompany.proyectoprueba.dao.UsuarioDAO;
import com.mycompany.proyectoprueba.dao.jdbc.RolDAOJdbc;
import com.mycompany.proyectoprueba.dao.jdbc.UsuarioDAOJdbc;
import com.mycompany.proyectoprueba.modelo.HistorialCentro;
import com.mycompany.proyectoprueba.modelo.Rol;
import com.mycompany.proyectoprueba.modelo.TipoRol;
import com.mycompany.proyectoprueba.modelo.Usuario;
import com.mycompany.proyectoprueba.modelo.UsuarioCentro;
import com.mycompany.proyectoprueba.seguridad.Password;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Reglas de negocio de los usuarios y sus centros (principal y adicionales).
 * Cada alta, modificación o baja de un acceso a un centro queda en usuario_centros_historial,
 * en la misma transacción que el cambio.
 */
public class UsuarioServicio {

    static final String NO_EXISTE = "El usuario ya no existe. Actualice la lista.";
    static final String PRINCIPAL = "PRINCIPAL";
    static final String ADICIONAL = "ADICIONAL";

    private final Connection conn;
    private final UsuarioDAO dao;
    private final RolDAO roles;

    public UsuarioServicio(Connection conn, UsuarioDAO dao, RolDAO roles) {
        this.conn = conn;
        this.dao = dao;
        this.roles = roles;
    }

    public static UsuarioServicio con(Connection conn) {
        return new UsuarioServicio(conn, new UsuarioDAOJdbc(conn), new RolDAOJdbc(conn));
    }

    public List<Usuario> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    /** Alta de usuario: la contraseña se guarda solo como hash (SHA-256 + BCrypt). */
    public void guardar(Usuario u, String password) throws SQLException {
        guardar(u, password, null);
    }

    /** Alta de usuario; idSesion = quien lo registra (queda en el historial de accesos). */
    public void guardar(Usuario u, String password, Integer idSesion) throws SQLException {
        obligatorios(u);
        validarCentroSegunRol(u);
        String error = Password.validarNueva(password);
        if (error != null) {
            throw new ReglaNegocioException(error);
        }
        if (dao.emailEnUso(u.getEmail(), 0)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese email (no se distinguen mayúsculas de minúsculas)");
        }
        String hash = Password.hash(password);
        conn.setAutoCommit(false);
        try {
            int id = dao.insertar(u, hash);
            if (u.getCentroAsignado() != null) {
                dao.registrarHistorial(id, u.getCentroAsignado().intValue(), PRINCIPAL, "ALTA", null, null,
                        "Centro principal asignado al crear el usuario", idSesion);
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /**
     * Modificación. password vacío = no se cambia. No se puede desactivar el propio usuario
     * ni dejar el sistema sin un administrador activo.
     */
    public void modificar(Usuario u, String password, Integer idSesion) throws SQLException {
        obligatorios(u);
        validarCentroSegunRol(u);
        if (dao.emailEnUso(u.getEmail(), u.getId())) {
            throw new ReglaNegocioException("Ya existe otro usuario con ese email (no se distinguen mayúsculas de minúsculas)");
        }
        if (idSesion != null && idSesion.intValue() == u.getId() && !u.isActivo()) {
            throw new ReglaNegocioException("No puede desactivar el usuario con el que inició sesión");
        }
        String hash = null;
        if (password != null && !password.trim().isEmpty()) {
            String error = Password.validarNueva(password);
            if (error != null) {
                throw new ReglaNegocioException(error);
            }
            hash = Password.hash(password);
        }
        conn.setAutoCommit(false);
        try {
            Integer anterior = dao.centroPrincipal(u.getId());
            if (dao.actualizar(u, hash) == 0) {
                conn.rollback();
                throw new ReglaNegocioException(NO_EXISTE);
            }
            Integer nuevo = u.getCentroAsignado();
            if (anterior == null ? nuevo != null : !anterior.equals(nuevo)) {
                if (anterior != null) {
                    dao.registrarHistorial(u.getId(), anterior.intValue(), PRINCIPAL, "BAJA", null, null,
                            nuevo == null ? "Se quitó el centro principal" : "Cambio de centro principal", idSesion);
                }
                if (nuevo != null) {
                    dao.registrarHistorial(u.getId(), nuevo.intValue(), PRINCIPAL, "ALTA", null, null,
                            anterior == null ? "Centro principal asignado" : "Cambio de centro principal", idSesion);
                }
            }
            if (roles.contarAdministradoresActivos() == 0) {
                conn.rollback();
                throw new ReglaNegocioException("No se guardó: el sistema quedaría sin ningún usuario administrador activo. "
                        + "Debe quedar al menos un usuario activo con un rol activo de tipo Administrador.");
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** No se puede eliminar el propio usuario ni el último administrador activo. */
    public void eliminar(int id, Integer idSesion) throws SQLException {
        if (idSesion != null && idSesion.intValue() == id) {
            throw new ReglaNegocioException("No puede eliminar el usuario con el que inició sesión");
        }
        conn.setAutoCommit(false);
        try {
            if (dao.eliminar(id) == 0) {
                conn.rollback();
                throw new ReglaNegocioException(NO_EXISTE);
            }
            if (roles.contarAdministradoresActivos() == 0) {
                conn.rollback();
                throw new ReglaNegocioException("No se eliminó: es el último usuario administrador activo del sistema.");
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    public List<UsuarioCentro> listarCentros(int idUsuario) throws SQLException {
        return dao.listarCentros(idUsuario);
    }

    public List<HistorialCentro> historialCentros(int idUsuario) throws SQLException {
        return dao.historialCentros(idUsuario);
    }

    /** Compatibilidad: alta de centro adicional sin usuario registrador. */
    public void agregarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion) throws SQLException {
        agregarCentro(idUsuario, idCentro, desde, hasta, observacion, null);
    }

    /**
     * Alta de un centro adicional. Fechas yyyy-MM-dd (desde vacío = hoy, hasta vacío = sin vencimiento).
     * Si el usuario ya tiene ese centro se rechaza: para cambiar fechas u observación se usa Editar.
     */
    public void agregarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion, Integer idSesion) throws SQLException {
        String[] d = datosCentro(desde, hasta, observacion);
        conn.setAutoCommit(false);
        try {
            Integer principal = dao.centroPrincipal(idUsuario);
            if (principal != null && principal.intValue() == idCentro) {
                throw new ReglaNegocioException("Ese es el centro principal del usuario: ya tiene acceso. Elija otro centro.");
            }
            if (dao.buscarCentro(idUsuario, idCentro) != null) {
                throw new ReglaNegocioException("El usuario ya tiene ese centro adicional. Use Editar para cambiar sus fechas u observación.");
            }
            dao.insertarCentro(idUsuario, idCentro, d[0], d[1], d[2]);
            UsuarioCentro c = dao.buscarCentro(idUsuario, idCentro);
            dao.registrarHistorial(idUsuario, idCentro, ADICIONAL, "ALTA", c.getDesde(), c.getHasta(), c.getObservacion(), idSesion);
            conn.commit();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ReglaNegocioException("El usuario ya tiene ese centro adicional. Use Editar para cambiar sus fechas u observación.");
            }
            throw e;
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Modificación de las fechas y la observación de un centro adicional (queda en el historial). */
    public void modificarCentro(int idUsuario, int idCentro, String desde, String hasta, String observacion, Integer idSesion) throws SQLException {
        String[] d = datosCentro(desde, hasta, observacion);
        conn.setAutoCommit(false);
        try {
            UsuarioCentro antes = dao.buscarCentro(idUsuario, idCentro);
            if (antes == null || dao.actualizarCentro(idUsuario, idCentro, d[0], d[1], d[2]) == 0) {
                throw new ReglaNegocioException("Ese centro adicional ya no está asignado al usuario. Actualice la lista.");
            }
            UsuarioCentro c = dao.buscarCentro(idUsuario, idCentro);
            if (iguales(antes.getDesde(), c.getDesde()) && iguales(antes.getHasta(), c.getHasta())
                    && iguales(antes.getObservacion(), c.getObservacion())) {
                throw new ReglaNegocioException("No hay cambios para guardar");
            }
            dao.registrarHistorial(idUsuario, idCentro, ADICIONAL, "MODIFICACION", c.getDesde(), c.getHasta(), c.getObservacion(), idSesion);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    public void quitarCentro(int idUsuario, int idCentro) throws SQLException {
        quitarCentro(idUsuario, idCentro, null);
    }

    /** Baja de un centro adicional: el historial guarda el período que tenía. */
    public void quitarCentro(int idUsuario, int idCentro, Integer idSesion) throws SQLException {
        conn.setAutoCommit(false);
        try {
            UsuarioCentro antes = dao.buscarCentro(idUsuario, idCentro);
            if (antes == null || dao.quitarCentro(idUsuario, idCentro) == 0) {
                throw new ReglaNegocioException("Ese centro adicional ya no está asignado al usuario. Actualice la lista.");
            }
            dao.registrarHistorial(idUsuario, idCentro, ADICIONAL, "BAJA", antes.getDesde(), antes.getHasta(),
                    antes.getObservacion(), idSesion);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Valida las fechas (yyyy-MM-dd; otro formato se ignora) y limpia la observación. */
    private static String[] datosCentro(String desde, String hasta, String observacion) {
        boolean desdeOk = desde != null && desde.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}");
        boolean hastaOk = hasta != null && hasta.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}");
        if (hastaOk && desdeOk && hasta.compareTo(desde) < 0) {
            throw new ReglaNegocioException("La fecha hasta no puede ser anterior a la fecha desde");
        }
        String obs = observacion == null || observacion.trim().isEmpty() ? null : observacion.trim();
        if (obs != null && obs.length() > 300) {
            throw new ReglaNegocioException("La observación admite hasta 300 caracteres");
        }
        return new String[]{desdeOk ? desde : null, hastaOk ? hasta : null, obs};
    }

    private static boolean iguales(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    /** Nombre, apellido, email y rol obligatorios; nombre y apellido sin espacios sobrantes. */
    private static void obligatorios(Usuario u) {
        u.setNombre(Validar.obligatorio(u.getNombre(), "Ingrese el nombre del usuario."));
        u.setApellido(Validar.obligatorio(u.getApellido(), "Ingrese el apellido del usuario."));
        u.setEmail(Validar.email(Validar.obligatorio(u.getEmail(), "Ingrese el email del usuario."),
                "El email del usuario no es válido (ejemplo: nombre@empresa.com)."));
        if (u.getIdRol() == null) {
            throw new ReglaNegocioException("Seleccione el rol del usuario.");
        }
    }

    /** Gerente y Auxiliar trabajan sobre sus centros: necesitan un centro principal. */
    private void validarCentroSegunRol(Usuario u) throws SQLException {
        Rol r = roles.buscar(u.getIdRol().intValue());
        if (r != null && TipoRol.necesitaCentro(r.getCodigo()) && u.getCentroAsignado() == null) {
            throw new ReglaNegocioException("El rol " + r.getNombre() + " trabaja sobre sus centros: asigne un centro principal");
        }
    }
}
