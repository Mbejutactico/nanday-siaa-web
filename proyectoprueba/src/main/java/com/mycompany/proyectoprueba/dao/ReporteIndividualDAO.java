package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;

/** Control de acceso de los reportes individuales que son documentos de un centro. */
public interface ReporteIndividualDAO {

    /** La OC existe y su centro destino es de los centros del usuario. */
    boolean ordenCompraVisible(int idCompra, Permisos p) throws SQLException;

    /** El registro de stock existe y su almacén es de los centros del usuario. */
    boolean stockVisible(int idStock, Permisos p) throws SQLException;

    /** El activo existe y está (o va, si aún no se recibió) en uno de los centros del usuario. */
    boolean activoVisible(int idEquipo, Permisos p) throws SQLException;
}
