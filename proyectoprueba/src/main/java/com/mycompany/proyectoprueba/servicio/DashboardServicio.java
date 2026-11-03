package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.DashboardDAO;
import com.mycompany.proyectoprueba.dao.jdbc.DashboardDAOJdbc;
import com.mycompany.proyectoprueba.modelo.CompraResumen;
import com.mycompany.proyectoprueba.modelo.OtResumen;
import com.mycompany.proyectoprueba.modelo.PuntoGrafico;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Datos del panel principal, cada panel limitado a lo que el rol puede ver. */
public class DashboardServicio {

    private final DashboardDAO dao;
    private final Permisos permisos;

    public DashboardServicio(DashboardDAO dao, Permisos permisos) {
        this.dao = dao;
        this.permisos = permisos;
    }

    public static DashboardServicio con(Connection conn, Permisos permisos) {
        return new DashboardServicio(new DashboardDAOJdbc(conn), permisos);
    }

    /** Contadores de los accesos rápidos (clave para la pantalla -> cantidad). */
    public Map<String, Integer> contadores() throws SQLException {
        // Solo las cifras de las pantallas que el rol puede abrir (las mismas tarjetas que muestra el tablero)
        Map<String, Integer> m = new LinkedHashMap<String, Integer>();
        contar(m, "TOTAL_PROVEEDORES", "proveedores", "/proveedorvista.jsp");
        contar(m, "TOTAL_ALMACENES", "almacenes", "/almacenesvista.jsp");
        contar(m, "TOTAL_CENTROS", "centros", "/centrosvista.jsp");
        contar(m, "TOTAL_USUARIOS", "usuarios", "/usuariosvista.jsp");
        contar(m, "TOTAL_INSUMOS", "insumos", "/insumosvista.jsp");
        contar(m, "TOTAL_ROLES", "roles", "/rolesvista.jsp");
        return m;
    }

    private void contar(Map<String, Integer> m, String clave, String tabla, String pantalla) throws SQLException {
        if (permisos.puedeVer(pantalla)) {
            m.put(clave, dao.contarActivos(tabla));
        }
    }

    public boolean veOt() {
        return permisos.tieneAlguno(Permisos.OT_GESTION, Permisos.OT_SUMINISTRAR, Permisos.OT_FINALIZAR);
    }

    public boolean veCompras() {
        return permisos.tiene(Permisos.OC_VER);
    }

    /** Contadores de OT: pendientes, en_proceso, finalizadas_mes, a_mi_cargo, demoradas. */
    public Map<String, Integer> contadoresOt(int idUsuario) throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<String, Integer>();
        m.put("pendientes", dao.contarOt("PENDIENTES", idUsuario, permisos));
        m.put("en_proceso", dao.contarOt("EN_PROCESO", idUsuario, permisos));
        m.put("finalizadas_mes", dao.contarOt("FINALIZADAS_MES", idUsuario, permisos));
        m.put("a_mi_cargo", dao.contarOt("A_MI_CARGO", idUsuario, permisos));
        m.put("demoradas", dao.contarOt("DEMORADAS", idUsuario, permisos));
        return m;
    }

    public List<OtResumen> otAbiertas(int idUsuario) throws SQLException {
        return dao.otAbiertas(idUsuario, permisos);
    }

    public List<CompraResumen> ultimasCompras() throws SQLException {
        return dao.ultimasCompras(permisos);
    }

    /** El consumo por centro de costo lo ven quienes trabajan con inventario o informes. */
    public boolean veConsumo() {
        return permisos.tieneAlguno(Permisos.INVENTARIO, Permisos.INFORMES);
    }

    public List<PuntoGrafico> otPorEstado(int idUsuario) throws SQLException {
        return dao.otPorEstado(idUsuario, permisos);
    }

    public List<PuntoGrafico> ocPorMes() throws SQLException {
        return dao.ocPorMes(permisos);
    }

    public List<PuntoGrafico> consumoPorCentroCosto() throws SQLException {
        return dao.consumoPorCentroCosto(permisos);
    }
}
