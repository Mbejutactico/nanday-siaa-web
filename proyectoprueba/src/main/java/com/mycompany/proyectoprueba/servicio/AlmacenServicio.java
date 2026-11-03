package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.AlmacenDAO;
import com.mycompany.proyectoprueba.dao.jdbc.AlmacenDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Almacen;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de los almacenes. */
public class AlmacenServicio {

    static final String NO_EXISTE = "El almacén ya no existe. Actualice la lista.";

    private final AlmacenDAO dao;

    public AlmacenServicio(AlmacenDAO dao) {
        this.dao = dao;
    }

    public static AlmacenServicio con(Connection conn) {
        return new AlmacenServicio(new AlmacenDAOJdbc(conn));
    }

    public List<Almacen> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public void guardar(Almacen a) throws SQLException {
        validar(a);
        dao.insertar(a);
    }

    public void modificar(Almacen a) throws SQLException {
        validar(a);
        Validar.existe(dao.actualizar(a), NO_EXISTE);
    }

    public void eliminar(int id) throws SQLException {
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }

    /** Nombre y centro obligatorios; los textos se guardan sin espacios sobrantes. */
    private static void validar(Almacen a) {
        a.setNombre(Validar.obligatorio(a.getNombre(), "Ingrese el nombre del almacén."));
        if (a.getIdCentro() == null) {
            throw new ReglaNegocioException("Seleccione el centro al que pertenece el almacén.");
        }
        a.setDescripcion(Validar.limpio(a.getDescripcion()));
        a.setUbicacion(Validar.limpio(a.getUbicacion()));
    }
}
