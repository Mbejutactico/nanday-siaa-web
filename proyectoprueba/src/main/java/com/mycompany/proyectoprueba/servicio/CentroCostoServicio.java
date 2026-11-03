package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.CentroCostoDAO;
import com.mycompany.proyectoprueba.dao.jdbc.CentroCostoDAOJdbc;
import com.mycompany.proyectoprueba.modelo.CentroCosto;
import com.mycompany.proyectoprueba.modelo.Opcion;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de los centros de costo. */
public class CentroCostoServicio {

    static final String NO_EXISTE = "El centro de costo ya no existe. Actualice la lista.";

    private final CentroCostoDAO dao;

    public CentroCostoServicio(CentroCostoDAO dao) {
        this.dao = dao;
    }

    public static CentroCostoServicio con(Connection conn) {
        return new CentroCostoServicio(new CentroCostoDAOJdbc(conn));
    }

    public List<CentroCosto> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    /** Centros de costo activos de un centro, para los combos. */
    public List<Opcion> activosDelCentro(int idCentro) throws SQLException {
        return dao.activosDelCentro(idCentro);
    }

    public void guardar(CentroCosto cc) throws SQLException {
        validar(cc);
        dao.insertar(cc);
    }

    public void modificar(CentroCosto cc) throws SQLException {
        validar(cc);
        Validar.existe(dao.actualizar(cc), NO_EXISTE);
    }

    public void eliminar(int id) throws SQLException {
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }

    /** Código y centro obligatorios; los textos se guardan sin espacios sobrantes. */
    private static void validar(CentroCosto cc) {
        cc.setCodigo(Validar.obligatorio(cc.getCodigo(), "Ingrese el código del centro de costo."));
        if (cc.getIdCentro() == null) {
            throw new ReglaNegocioException("Seleccione el centro al que pertenece el centro de costo.");
        }
        cc.setDescripcion(Validar.limpio(cc.getDescripcion()));
    }
}
