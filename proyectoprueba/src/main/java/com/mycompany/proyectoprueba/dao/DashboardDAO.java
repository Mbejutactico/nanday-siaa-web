package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.CompraResumen;
import com.mycompany.proyectoprueba.modelo.OtResumen;
import com.mycompany.proyectoprueba.modelo.PuntoGrafico;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.SQLException;
import java.util.List;

/** Consultas del panel principal. */
public interface DashboardDAO {

    /** Registros activos de una tabla de catálogo (proveedores, almacenes, centros, usuarios, insumos, roles). */
    int contarActivos(String tabla) throws SQLException;

    /**
     * Cantidad de OT visibles para el usuario (sus centros, las que pidió o tiene a cargo).
     * cual: PENDIENTES, EN_PROCESO, FINALIZADAS_MES, A_MI_CARGO o DEMORADAS.
     */
    int contarOt(String cual, int idUsuario, Permisos p) throws SQLException;

    /** Las 5 OT abiertas más antiguas visibles para el usuario. */
    List<OtResumen> otAbiertas(int idUsuario, Permisos p) throws SQLException;

    /** Las 5 últimas OC (solicitadas, parciales o recibidas) de los centros del usuario. */
    List<CompraResumen> ultimasCompras(Permisos p) throws SQLException;

    /** Gráfico: OT visibles para el usuario por estado (creadas en los últimos 12 meses). */
    List<PuntoGrafico> otPorEstado(int idUsuario, Permisos p) throws SQLException;

    /** Gráfico: OC de los centros del usuario por mes (últimos 6 meses, sin anuladas ni borradores), con su monto. */
    List<PuntoGrafico> ocPorMes(Permisos p) throws SQLException;

    /** Gráfico: consumo de insumos (salidas a OT) por centro de costo en los últimos 90 días, en guaraníes. */
    List<PuntoGrafico> consumoPorCentroCosto(Permisos p) throws SQLException;
}
