package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.UnidadMedidaDAO;
import com.mycompany.proyectoprueba.dao.jdbc.UnidadMedidaDAOJdbc;
import com.mycompany.proyectoprueba.modelo.UnidadMedida;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de las unidades de medida. */
public class UnidadMedidaServicio {

    static final String NO_EXISTE = "La unidad de medida ya no existe. Actualice la lista.";

    private final UnidadMedidaDAO dao;

    public UnidadMedidaServicio(UnidadMedidaDAO dao) {
        this.dao = dao;
    }

    /** Servicio con el DAO JDBC sobre la conexión de la petición. */
    public static UnidadMedidaServicio con(Connection conn) {
        return new UnidadMedidaServicio(new UnidadMedidaDAOJdbc(conn));
    }

    public List<UnidadMedida> listarTodas() throws SQLException {
        return dao.listarTodas();
    }

    public List<UnidadMedida> listarActivas() throws SQLException {
        return dao.listarActivas();
    }

    public void guardar(UnidadMedida u) throws SQLException {
        validar(u);
        dao.insertar(u);
    }

    public void modificar(UnidadMedida u) throws SQLException {
        validar(u);
        Validar.existe(dao.actualizar(u), NO_EXISTE);
    }

    /** No se puede eliminar una unidad que usan uno o más insumos. */
    public void eliminar(int id) throws SQLException {
        if (dao.contarInsumos(id) > 0) {
            throw new ReglaNegocioException("No se puede eliminar: la unidad está siendo utilizada por uno o más insumos.");
        }
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }

    /** El nombre es obligatorio; nombre y abreviatura se guardan sin espacios sobrantes. */
    private static void validar(UnidadMedida u) {
        u.setNombre(Validar.obligatorio(u.getNombre(), "Ingrese el nombre de la unidad de medida."));
        u.setAbreviatura(Validar.limpio(u.getAbreviatura()));
    }
}
