package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.UsuarioDAO;
import com.mycompany.proyectoprueba.dao.jdbc.UsuarioDAOJdbc;
import com.mycompany.proyectoprueba.modelo.Credencial;
import com.mycompany.proyectoprueba.modelo.Usuario;
import com.mycompany.proyectoprueba.seguridad.IntentosLogin;
import com.mycompany.proyectoprueba.seguridad.Password;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Inicio de sesión: verifica email y contraseña (SHA-256 + BCrypt), con bloqueo por intentos
 * fallidos (IntentosLogin) y respuesta de duración pareja (no revela qué emails existen).
 */
public class SesionServicio {

    /** Resultado del intento. */
    public static final class Resultado {
        public final Usuario usuario;     // null si falló
        public final long minutosBloqueo; // > 0 si la cuenta o la IP están bloqueadas

        Resultado(Usuario usuario, long minutosBloqueo) {
            this.usuario = usuario;
            this.minutosBloqueo = minutosBloqueo;
        }
    }

    private final UsuarioDAO dao;

    public SesionServicio(UsuarioDAO dao) {
        this.dao = dao;
    }

    public static SesionServicio con(Connection conn) {
        return new SesionServicio(new UsuarioDAOJdbc(conn));
    }

    /** email ya sin espacios; password tal cual. */
    public Resultado iniciar(String email, String password, String ip) throws SQLException {
        long inicio = Password.inicioIntento();
        long minutos = IntentosLogin.minutosBloqueo(email, ip);
        if (minutos > 0) {
            return new Resultado(null, minutos);
        }
        Credencial c = dao.buscarParaLogin(email);
        if (c != null && Password.verificar(password, c.hashPassword)) {
            IntentosLogin.exito(email);
            return new Resultado(c.usuario, 0);
        }
        IntentosLogin.fallo(email, ip);
        Password.demorarIntentoFallido(inicio);
        return new Resultado(null, 0);
    }
}
