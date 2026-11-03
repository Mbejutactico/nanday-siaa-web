package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.BitacoraDAO;
import com.mycompany.proyectoprueba.dao.jdbc.BitacoraDAOJdbc;
import com.mycompany.proyectoprueba.modelo.CambioBitacora;
import com.mycompany.proyectoprueba.modelo.FiltrosBitacora;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.RegistroBitacora;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Consulta de la bitácora (solo lectura; los registros los agrega seguridad/Bitacora). */
public class BitacoraServicio {

    /** Máximo de filas que se muestran por consulta (para más, acotar las fechas). */
    public static final int LIMITE = 2000;

    public static final List<String> MODULOS = Arrays.asList("SEGURIDAD", "REFERENCIALES", "COMPRAS", "OT", "INVENTARIO", "ACTIVOS");
    public static final List<String> ACCIONES = Arrays.asList("LOGIN", "LOGIN_FALLIDO", "LOGOUT", "ALTA", "MODIFICACION", "BAJA",
            "ANULACION", "APROBACION", "RECHAZO", "FINALIZACION", "REAPERTURA", "RECEPCION");

    private final BitacoraDAO dao;

    public BitacoraServicio(BitacoraDAO dao) {
        this.dao = dao;
    }

    public static BitacoraServicio con(Connection conn) {
        return new BitacoraServicio(new BitacoraDAOJdbc(conn));
    }

    /** Valida los filtros: fechas con formato correcto y valores de las listas fijas. */
    public static FiltrosBitacora filtros(String desde, String hasta, int idUsuario, String modulo, String accion,
            String texto, String tabla, int idRegistro) {
        FiltrosBitacora f = new FiltrosBitacora();
        f.desde = fecha(desde, "La fecha desde no es válida.");
        f.hasta = fecha(hasta, "La fecha hasta no es válida.");
        if (!f.desde.isEmpty() && !f.hasta.isEmpty() && f.desde.compareTo(f.hasta) > 0) {
            throw new ReglaNegocioException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        f.idUsuario = Math.max(0, idUsuario);
        f.modulo = MODULOS.contains(modulo) ? modulo : "";
        f.accion = ACCIONES.contains(accion) ? accion : "";
        f.texto = texto == null ? "" : texto.trim();
        f.tabla = tabla == null || !tabla.matches("[a-z_]{1,60}") ? "" : tabla;
        f.idRegistro = Math.max(0, idRegistro);
        return f;
    }

    private static String fecha(String v, String error) {
        if (v == null || v.trim().isEmpty()) {
            return "";
        }
        if (!v.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ReglaNegocioException(error);
        }
        return v.trim();
    }

    public List<RegistroBitacora> listar(FiltrosBitacora f) throws SQLException {
        return dao.listar(f, LIMITE);
    }

    public RegistroBitacora obtener(int id) throws SQLException {
        return dao.obtener(id);
    }

    public List<CambioBitacora> cambios(int id) throws SQLException {
        return dao.cambios(id);
    }

    public Map<String, Integer> resumenHoy() throws SQLException {
        return dao.resumenHoy();
    }

    public List<Opcion> usuarios() throws SQLException {
        return dao.usuarios();
    }
}
