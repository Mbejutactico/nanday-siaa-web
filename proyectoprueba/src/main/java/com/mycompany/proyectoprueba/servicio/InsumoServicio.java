package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.InsumoDAO;
import com.mycompany.proyectoprueba.dao.jdbc.InsumoDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Insumo;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de los insumos. */
public class InsumoServicio {

    static final String NO_EXISTE = "El insumo ya no existe. Actualice la lista.";

    private final InsumoDAO dao;

    public InsumoServicio(InsumoDAO dao) {
        this.dao = dao;
    }

    public static InsumoServicio con(Connection conn) {
        return new InsumoServicio(new InsumoDAOJdbc(conn));
    }

    public List<Insumo> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public void guardar(Insumo i) throws SQLException {
        validar(i);
        dao.insertar(i);
    }

    public void modificar(Insumo i) throws SQLException {
        validar(i);
        Validar.existe(dao.actualizar(i), NO_EXISTE);
    }

    public void eliminar(int id) throws SQLException {
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }

    /** Código y descripción obligatorios; el precio no puede ser negativo. */
    private static void validar(Insumo i) {
        i.setCodigo(Validar.obligatorio(i.getCodigo(), "Ingrese el código del insumo."));
        i.setDescripcion(Validar.obligatorio(i.getDescripcion(), "Ingrese la descripción del insumo."));
        if (i.getPrecioCompra() != null && i.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio de compra no puede ser negativo.");
        }
    }
}
