package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.CambioBitacora;
import com.mycompany.proyectoprueba.modelo.FiltrosBitacora;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.RegistroBitacora;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Acceso a la bitácora (bitacora, bitacora_detalle). */
public interface BitacoraDAO {

    /** Agrega un registro y devuelve su id. */
    int registrar(Integer idUsuario, String email, String ip, String modulo, String accion,
            String tabla, Integer idRegistro, String descripcion) throws SQLException;

    void registrarCambio(int idBitacora, String campo, String anterior, String nuevo) throws SQLException;

    /** Valores actuales (como texto) de una fila, o null si no existe. La tabla y la clave vienen de una lista fija. */
    Map<String, String> foto(String tabla, String pk, int id) throws SQLException;

    /** Texto descriptivo de una fila (expresión SQL fija sobre el alias t). */
    String describir(String tabla, String pk, String expresion, int id) throws SQLException;

    /** Mayor id de la tabla (opcionalmente solo las filas que cumplen la condición, con un "?" para el id del usuario). */
    int mayorId(String tabla, String pk, String condicionUsuario, int idUsuario) throws SQLException;

    /** Cantidad de filas con id mayor al indicado (opcionalmente solo las que cumplen la condición del usuario). */
    int nuevasDesde(String tabla, String pk, int desdeId, String condicionUsuario, int idUsuario) throws SQLException;

    /** Nombre legible de un id de otra tabla (rol, centro, usuario...). */
    String nombre(String sql, int id) throws SQLException;

    List<RegistroBitacora> listar(FiltrosBitacora f, int limite) throws SQLException;

    RegistroBitacora obtener(int id) throws SQLException;

    List<CambioBitacora> cambios(int idBitacora) throws SQLException;

    /** Contadores de hoy: TOTAL, LOGIN, LOGIN_FALLIDO, CAMBIOS (alta/modificación), BAJAS (baja/anulación). */
    Map<String, Integer> resumenHoy() throws SQLException;

    /** Usuarios que figuran en la bitácora (para el filtro). */
    List<Opcion> usuarios() throws SQLException;

    int fallidosHoy() throws SQLException;
}
