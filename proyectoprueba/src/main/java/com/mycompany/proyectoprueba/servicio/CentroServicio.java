package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.CentroDAO;
import com.mycompany.proyectoprueba.dao.jdbc.CentroDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Centro;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de los centros. */
public class CentroServicio {

    static final String NO_EXISTE = "El centro ya no existe. Actualice la lista.";

    private final CentroDAO dao;

    public CentroServicio(CentroDAO dao) {
        this.dao = dao;
    }

    public static CentroServicio con(Connection conn) {
        return new CentroServicio(new CentroDAOJdbc(conn));
    }

    public List<Centro> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Centro> listarActivos() throws SQLException {
        return dao.listarActivos();
    }

    public void guardar(Centro c) throws SQLException {
        validar(c);
        dao.insertar(c);
    }

    public void modificar(Centro c) throws SQLException {
        validar(c);
        Validar.existe(dao.actualizar(c), NO_EXISTE);
    }

    public void eliminar(int id) throws SQLException {
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }

    /** Código y nombre obligatorios; los textos se guardan sin espacios sobrantes. */
    private static void validar(Centro c) {
        c.setCodigo(Validar.obligatorio(c.getCodigo(), "Ingrese el código del centro."));
        c.setNombre(Validar.obligatorio(c.getNombre(), "Ingrese el nombre del centro."));
        c.setDireccion(Validar.limpio(c.getDireccion()));
    }
}
