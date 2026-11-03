package com.mycompany.proyectoprueba.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Cierre seguro de una transacción manual (setAutoCommit(false)). Se llama en el finally:
 * deshace lo no confirmado y recién después vuelve al modo automático, porque en JDBC
 * setAutoCommit(true) con una transacción abierta la confirma.
 */
public final class Transaccion {

    private Transaccion() {
    }

    public static void finalizar(Connection conn) throws SQLException {
        if (conn == null || conn.isClosed()) {
            return;
        }
        if (!conn.getAutoCommit()) {
            try {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
