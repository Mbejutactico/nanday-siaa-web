package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.ProveedorDAO;
import com.mycompany.proyectoprueba.dao.jdbc.ProveedorDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Proveedor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Reglas de negocio de los proveedores. */
public class ProveedorServicio {

    static final String NO_EXISTE = "El proveedor ya no existe. Actualice la lista.";

    private final ProveedorDAO dao;

    public ProveedorServicio(ProveedorDAO dao) {
        this.dao = dao;
    }

    public static ProveedorServicio con(Connection conn) {
        return new ProveedorServicio(new ProveedorDAOJdbc(conn));
    }

    public List<Proveedor> listarTodos() throws SQLException {
        return dao.listarTodos();
    }

    public List<Proveedor> listarActivos() throws SQLException {
        return dao.listarActivos();
    }

    public void guardar(Proveedor p) throws SQLException {
        validar(p);
        dao.insertar(p);
    }

    public void modificar(Proveedor p) throws SQLException {
        validar(p);
        Validar.existe(dao.actualizar(p), NO_EXISTE);
    }

    public void eliminar(int id) throws SQLException {
        Validar.existe(dao.eliminar(id), NO_EXISTE);
    }

    /** Razón social y RUC obligatorios; los textos se guardan sin espacios sobrantes. */
    private static void validar(Proveedor p) {
        p.setRazonSocial(Validar.obligatorio(p.getRazonSocial(), "Ingrese la razón social del proveedor."));
        p.setRuc(Validar.obligatorio(p.getRuc(), "Ingrese el RUC del proveedor."));
        p.setDireccion(Validar.limpio(p.getDireccion()));
        p.setTelefono(Validar.limpio(p.getTelefono()));
        p.setEmail(Validar.email(p.getEmail(), "El email del proveedor no es válido (ejemplo: ventas@empresa.com)."));
    }
}
