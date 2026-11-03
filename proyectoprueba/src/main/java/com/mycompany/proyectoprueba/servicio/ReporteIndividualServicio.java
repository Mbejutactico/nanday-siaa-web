package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.ReporteIndividualDAO;
import com.mycompany.proyectoprueba.dao.jdbc.ReporteIndividualDAOJdbc;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Reportes individuales en PDF. Los documentos de un centro (orden de compra, stock de un
 * almacén y hoja de vida de un activo) solo se imprimen para los centros del usuario; los
 * de datos de referencia (unidades, centros, insumos...) los controla el permiso de la pantalla.
 */
public class ReporteIndividualServicio {

    /** Documento que se controla por centro. */
    public enum Documento { ORDEN_COMPRA, STOCK, ACTIVO }

    private final ReporteIndividualDAO dao;
    private final Permisos permisos;

    public ReporteIndividualServicio(ReporteIndividualDAO dao, Permisos permisos) {
        this.dao = dao;
        this.permisos = permisos;
    }

    public static ReporteIndividualServicio con(Connection conn, Permisos permisos) {
        return new ReporteIndividualServicio(new ReporteIndividualDAOJdbc(conn), permisos);
    }

    /** true si el usuario puede imprimir ese documento (null = no se controla por centro). */
    public boolean puedeVer(Documento documento, int id) throws SQLException {
        if (documento == null) {
            return true;
        }
        switch (documento) {
            case ORDEN_COMPRA:
                return dao.ordenCompraVisible(id, permisos);
            case STOCK:
                return dao.stockVisible(id, permisos);
            default:
                return dao.activoVisible(id, permisos);
        }
    }
}
