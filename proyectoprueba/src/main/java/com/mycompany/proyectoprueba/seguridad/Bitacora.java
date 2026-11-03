package com.mycompany.proyectoprueba.seguridad;

import com.mycompany.proyectoprueba.dao.BitacoraDAO;
import com.mycompany.proyectoprueba.dao.jdbc.BitacoraDAOJdbc;
import java.sql.Connection;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Bitácora del sistema: registra quién hizo qué, cuándo y desde qué IP.
 *
 * Las operaciones que se registran están en la lista OPERACIONES (controlador + tipo).
 * BaseServlet llama a antes() y despues() alrededor de cada operación: antes() guarda
 * cómo estaba el registro y despues() compara. Solo se registra si la operación cambió
 * algo de verdad (si falló una validación no queda nada en la bitácora).
 *
 * Un error al escribir la bitácora nunca interrumpe la operación del usuario.
 */
public final class Bitacora {

    private static final Logger LOG = Logger.getLogger(Bitacora.class.getName());

    // Cómo se detecta que la operación se realizó
    private static final int NUEVA = 1;     // apareció una fila nueva
    private static final int CAMBIO = 2;    // cambió la fila
    private static final int BORRADO = 3;   // la fila se eliminó o cambió (baja lógica)

    private Bitacora() {
    }

    /** Tabla auditada: clave primaria, expresión que la describe (alias t) y nombre para mostrar. */
    private static final class Tabla {
        final String nombre;
        final String pk;
        final String descripcion;
        final String etiqueta;

        Tabla(String nombre, String pk, String descripcion, String etiqueta) {
            this.nombre = nombre;
            this.pk = pk;
            this.descripcion = descripcion;
            this.etiqueta = etiqueta;
        }
    }

    /** Operación auditada. */
    private static final class Operacion {
        final String modulo;
        final String accion;
        final Tabla tabla;
        final int deteccion;
        final String parametroId;      // parámetro con el id, o "sesion" (el propio usuario)
        final String condicionUsuario; // filas nuevas: solo las del usuario (con un "?" para su id)

        Operacion(String modulo, String accion, Tabla tabla, int deteccion, String parametroId, String condicionUsuario) {
            this.modulo = modulo;
            this.accion = accion;
            this.tabla = tabla;
            this.deteccion = deteccion;
            this.parametroId = parametroId;
            this.condicionUsuario = condicionUsuario;
        }
    }

    private static final Map<String, Tabla> TABLAS = new HashMap<String, Tabla>();
    private static final Map<String, Operacion> OPERACIONES = new HashMap<String, Operacion>();

    private static Tabla tabla(String nombre, String pk, String descripcion, String etiqueta) {
        Tabla t = new Tabla(nombre, pk, descripcion, etiqueta);
        TABLAS.put(nombre, t);
        return t;
    }

    private static void op(String controlador, String tipos, String modulo, String accion, Tabla t, int deteccion,
            String parametroId, String condicionUsuario) {
        for (String tipo : tipos.split(",")) {
            OPERACIONES.put("/" + controlador + "#" + tipo.trim(), new Operacion(modulo, accion, t, deteccion, parametroId, condicionUsuario));
        }
    }

    /** Altas, modificaciones y bajas de un referencial (tipos Guardar, Modificar, Borrar con el id en "pk"). */
    private static void abm(String controlador, String modulo, Tabla t) {
        op(controlador, "Guardar", modulo, "ALTA", t, NUEVA, null, null);
        op(controlador, "Modificar", modulo, "MODIFICACION", t, CAMBIO, "pk", null);
        op(controlador, "Borrar", modulo, "BAJA", t, BORRADO, "pk", null);
    }

    static {
        Tabla proveedores = tabla("proveedores", "id_proveedor", "t.razon_social", "Proveedor");
        Tabla insumos = tabla("insumos", "id_insumo", "t.codigo || ' - ' || t.descripcion", "Insumo");
        Tabla unidades = tabla("unidad_medida", "id_unidad_medida", "t.nombre", "Unidad de medida");
        Tabla centros = tabla("centros", "id_centro", "t.nombre", "Centro");
        Tabla centrosCosto = tabla("centros_costo", "id_centro_costo", "t.codigo", "Centro de costo");
        Tabla almacenes = tabla("almacenes", "id_almacen", "t.nombre", "Almacén");
        Tabla roles = tabla("roles", "id_rol", "t.nombre", "Rol");
        Tabla usuarios = tabla("usuarios", "id_usuario", "t.nombre || ' ' || t.apellido || ' (' || t.email || ')'", "Usuario");
        Tabla accesos = tabla("usuario_centros_historial", "id_historial",
                "(SELECT u.email FROM usuarios u WHERE u.id_usuario = t.id_usuario) || ': ' || lower(t.accion) || ' de acceso ' "
                + "|| lower(t.tipo_acceso) || ' a ' || (SELECT c.nombre FROM centros c WHERE c.id_centro = t.id_centro)", "Acceso a centro");
        Tabla stock = tabla("stock_almacen", "id_stock",
                "(SELECT i.descripcion FROM insumos i WHERE i.id_insumo = t.id_insumo) || ' en ' "
                + "|| (SELECT a.nombre FROM almacenes a WHERE a.id_almacen = t.id_almacen)", "Stock");
        Tabla compras = tabla("compras", "id_compra", "COALESCE(t.numero_oc, 'OC #' || t.id_compra)", "Orden de compra");
        Tabla recepciones = tabla("recepciones_oc", "id_recepcion",
                "COALESCE(t.numero, 'Recepción #' || t.id_recepcion) || ' de ' "
                + "|| COALESCE((SELECT c.numero_oc FROM compras c WHERE c.id_compra = t.id_compra), '')", "Recepción de OC");
        Tabla ot = tabla("ot", "id_ot", "t.numero_ot || ' - ' || t.titulo", "Orden de trabajo");
        Tabla remisiones = tabla("remisiones", "id_remision", "t.numero_remision", "Remisión");
        Tabla ajustes = tabla("ajustes", "id_ajuste", "t.numero", "Ajuste");
        Tabla transferencias = tabla("transferencias", "id_transferencia", "t.numero", "Transferencia");
        Tabla solicitudes = tabla("solicitudes_activos", "id_solicitud", "t.numero", "Solicitud de activos");
        Tabla equipos = tabla("equipos", "id_equipo", "t.serial_activo", "Activo");
        Tabla movActivos = tabla("movimientos_activos", "id_movimiento",
                "COALESCE((SELECT e.serial_activo FROM equipos e WHERE e.id_equipo = t.id_equipo), '') || ' ' || lower(t.tipo_movimiento)",
                "Movimiento de activo");

        // Referenciales y seguridad
        abm("ProveedorServlet", "REFERENCIALES", proveedores);
        abm("InsumoServlet", "REFERENCIALES", insumos);
        abm("UnidadMedidaServlet", "REFERENCIALES", unidades);
        abm("CentroServlet", "REFERENCIALES", centros);
        abm("CentroCostoServlet", "REFERENCIALES", centrosCosto);
        abm("AlmacenServlet", "REFERENCIALES", almacenes);
        abm("RolServlet", "SEGURIDAD", roles);
        abm("UsuarioServlet", "SEGURIDAD", usuarios);
        op("UsuarioServlet", "agregarCentroUsuario", "SEGURIDAD", "ALTA", accesos, NUEVA, null, "id_usuario_registra = ?");
        op("UsuarioServlet", "modificarCentroUsuario", "SEGURIDAD", "MODIFICACION", accesos, NUEVA, null, "id_usuario_registra = ?");
        op("UsuarioServlet", "quitarCentroUsuario", "SEGURIDAD", "BAJA", accesos, NUEVA, null, "id_usuario_registra = ?");
        op("PerfilServlet", "ActualizarDatos,CambiarPassword,ActualizarFoto", "SEGURIDAD", "MODIFICACION", usuarios, CAMBIO, "sesion", null);

        // Compras
        op("SolicitudCompraServlet", "guardar", "COMPRAS", "ALTA", compras, NUEVA, null, "id_usuario_solicitante = ? AND estado <> 'BORRADOR'");
        op("OrdenCompraServlet", "actualizarOC", "COMPRAS", "MODIFICACION", compras, CAMBIO, "id_compra", null);
        op("OrdenCompraServlet", "anularOC", "COMPRAS", "ANULACION", compras, CAMBIO, "id", null);
        op("OrdenCompraServlet", "procesarRecepcion", "COMPRAS", "RECEPCION", recepciones, NUEVA, null, "id_usuario = ?");
        op("OrdenCompraServlet", "anularRecepcion", "COMPRAS", "ANULACION", recepciones, CAMBIO, "id_recepcion", null);

        // Órdenes de trabajo
        op("OrdenTrabajoServlet", "crear", "OT", "ALTA", ot, NUEVA, null, "id_usuario_solic = ?");
        op("OrdenTrabajoServlet", "modificar", "OT", "MODIFICACION", ot, CAMBIO, "id", null);
        op("OrdenTrabajoServlet", "eliminar", "OT", "BAJA", ot, BORRADO, "id", null);
        op("OrdenTrabajoServlet", "finalizar", "OT", "FINALIZACION", ot, CAMBIO, "id", null);
        op("OrdenTrabajoServlet", "reabrir", "OT", "REAPERTURA", ot, CAMBIO, "id", null);

        // Inventario
        op("StockServlet", "crear", "INVENTARIO", "ALTA", stock, NUEVA, null, null);
        op("StockServlet", "modificarMinimo", "INVENTARIO", "MODIFICACION", stock, CAMBIO, "id", null);
        op("StockServlet", "eliminar", "INVENTARIO", "BAJA", stock, BORRADO, "id", null);
        op("InventarioServlet", "emitirSalida", "INVENTARIO", "ALTA", remisiones, NUEVA, null, "id_usuario_emite = ?");
        op("InventarioServlet", "anularRemision", "INVENTARIO", "ANULACION", remisiones, CAMBIO, "id", null);
        op("InventarioServlet", "crearAjuste,registrarConteo", "INVENTARIO", "ALTA", ajustes, NUEVA, null, "id_usuario = ?");
        op("InventarioServlet", "anularAjuste", "INVENTARIO", "ANULACION", ajustes, CAMBIO, "id", null);
        op("InventarioServlet", "crearTransferencia", "INVENTARIO", "ALTA", transferencias, NUEVA, null, "id_usuario = ?");
        op("InventarioServlet", "anularTransferencia", "INVENTARIO", "ANULACION", transferencias, CAMBIO, "id", null);

        // Activos fijos
        op("SolicitudActivoServlet", "crearSolicitud", "ACTIVOS", "ALTA", solicitudes, NUEVA, null, "id_usuario_solicita = ?");
        op("SolicitudActivoServlet", "aprobar", "ACTIVOS", "APROBACION", solicitudes, CAMBIO, "id", null);
        op("SolicitudActivoServlet", "rechazar", "ACTIVOS", "RECHAZO", solicitudes, CAMBIO, "id", null);
        op("SolicitudActivoServlet", "anularSalida", "ACTIVOS", "ANULACION", solicitudes, CAMBIO, "id", null);
        op("SolicitudActivoServlet", "recibir", "ACTIVOS", "RECEPCION", movActivos, NUEVA, null, "id_usuario_registro = ?");
        op("SolicitudActivoServlet", "rechazarRecepcion", "ACTIVOS", "RECHAZO", movActivos, NUEVA, null, "id_usuario_registro = ?");
        op("ActivoServlet", "registrarMovimiento", "ACTIVOS", "MODIFICACION", equipos, CAMBIO, "id_equipo", null);
    }

    // ---------------------------------------------------------------- seguimiento de una operación

    /** Estado guardado antes de la operación. */
    public static final class Seguimiento {
        private final Operacion op;
        private final HttpServletRequest req;
        private final Connection conn;
        private int id;
        private Map<String, String> antes;
        private String descripcion;
        private int mayorAntes;

        private Seguimiento(Operacion op, HttpServletRequest req, Connection conn) {
            this.op = op;
            this.req = req;
            this.conn = conn;
        }
    }

    /** true si la operación se registra en la bitácora. */
    public static boolean audita(String ruta, String tipo) {
        return tipo != null && OPERACIONES.containsKey(ruta + "#" + tipo);
    }

    /** Antes de la operación: guarda cómo estaba el registro. Devuelve null si no corresponde. */
    public static Seguimiento antes(HttpServletRequest req, Connection conn, String ruta, String tipo) {
        Operacion op = tipo == null ? null : OPERACIONES.get(ruta + "#" + tipo);
        if (op == null || conn == null) {
            return null;
        }
        try {
            Seguimiento s = new Seguimiento(op, req, conn);
            BitacoraDAO dao = new BitacoraDAOJdbc(conn);
            Tabla t = op.tabla;
            if (op.deteccion == NUEVA) {
                s.mayorAntes = dao.mayorId(t.nombre, t.pk, op.condicionUsuario, idUsuario(req));
            } else {
                s.id = idDeLaPeticion(req, op.parametroId);
                if (s.id <= 0) {
                    return null;
                }
                s.antes = dao.foto(t.nombre, t.pk, s.id);
                if (s.antes == null) {
                    return null;
                }
                s.descripcion = dao.describir(t.nombre, t.pk, t.descripcion, s.id);
            }
            return s;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Bitácora (antes): " + e.getMessage());
            return null;
        }
    }

    /** Después de la operación: si cambió algo, lo registra. */
    public static void despues(Seguimiento s) {
        if (s == null) {
            return;
        }
        try {
            BitacoraDAO dao = new BitacoraDAOJdbc(s.conn);
            Operacion op = s.op;
            Tabla t = op.tabla;
            if (op.deteccion == NUEVA) {
                int uid = idUsuario(s.req);
                int mayor = dao.mayorId(t.nombre, t.pk, op.condicionUsuario, uid);
                if (mayor <= s.mayorAntes) {
                    return;   // no se creó nada
                }
                int nuevas = dao.nuevasDesde(t.nombre, t.pk, s.mayorAntes, op.condicionUsuario, uid);
                String desc = texto(t, dao.describir(t.nombre, t.pk, t.descripcion, mayor))
                        + (nuevas > 1 ? " (" + nuevas + " registros)" : "");
                guardar(s.req, s.conn, op.modulo, op.accion, t.nombre, Integer.valueOf(mayor), conMotivo(desc, s.req), null);
                return;
            }
            Map<String, String> despues = dao.foto(t.nombre, t.pk, s.id);
            if (despues == null) {
                if (op.deteccion == BORRADO) {   // eliminación física
                    guardar(s.req, s.conn, op.modulo, op.accion, t.nombre, Integer.valueOf(s.id), conMotivo(texto(t, s.descripcion), s.req), null);
                }
                return;
            }
            List<String[]> cambios = diferencias(dao, t, s.antes, despues);
            if (cambios.isEmpty()) {
                return;   // la operación no se realizó (validación, error o sin cambios)
            }
            guardar(s.req, s.conn, op.modulo, op.accion, t.nombre, Integer.valueOf(s.id), conMotivo(texto(t, s.descripcion), s.req), cambios);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Bitácora (después): " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- registros directos

    /** Registro directo (inicio y cierre de sesión). Nunca lanza excepciones. */
    public static void registrar(HttpServletRequest req, Connection conn, String modulo, String accion,
            String tabla, Integer idRegistro, String descripcion) {
        guardar(req, conn, modulo, accion, tabla, idRegistro, descripcion, null);
    }

    /** Inicio de sesión fallido: queda el email que se escribió (no hay usuario en la sesión). */
    public static void inicioFallido(HttpServletRequest req, Connection conn, String email, String motivo) {
        try {
            escribir(conn, null, email, req.getRemoteAddr(), "SEGURIDAD", "LOGIN_FALLIDO", "usuarios", null, motivo, null);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Bitácora (inicio fallido): " + e.getMessage());
        }
    }

    private static void guardar(HttpServletRequest req, Connection conn, String modulo, String accion, String tabla,
            Integer idRegistro, String descripcion, List<String[]> cambios) {
        try {
            HttpSession sesion = req.getSession(false);
            Integer idUsuario = null;
            String email = null;
            if (sesion != null) {
                Object id = sesion.getAttribute("id_usuario");
                idUsuario = id instanceof Integer ? (Integer) id : null;
                Object em = sesion.getAttribute("email");
                email = em == null ? null : em.toString();
            }
            escribir(conn, idUsuario, email, req.getRemoteAddr(), modulo, accion, tabla, idRegistro, descripcion, cambios);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Bitácora: " + e.getMessage());
        }
    }

    /**
     * Escribe cabecera y detalle juntos. Si la conexión está dentro de una transacción usa un
     * punto de guardado: un error de la bitácora no deja la transacción del usuario en falla.
     */
    private static void escribir(Connection conn, Integer idUsuario, String email, String ip, String modulo, String accion,
            String tabla, Integer idRegistro, String descripcion, List<String[]> cambios) throws Exception {
        boolean automatico = conn.getAutoCommit();
        Savepoint punto = null;
        if (automatico) {
            conn.setAutoCommit(false);
        } else {
            punto = conn.setSavepoint();
        }
        try {
            BitacoraDAO dao = new BitacoraDAOJdbc(conn);
            int id = dao.registrar(idUsuario, email, ip, modulo, accion, tabla, idRegistro, descripcion);
            if (cambios != null) {
                for (String[] c : cambios) {
                    dao.registrarCambio(id, c[0], c[1], c[2]);
                }
            }
            if (automatico) {
                conn.commit();
            } else {
                conn.releaseSavepoint(punto);
            }
        } catch (Exception e) {
            if (automatico) {
                conn.rollback();
            } else {
                conn.rollback(punto);
            }
            throw e;
        } finally {
            if (automatico) {
                conn.setAutoCommit(true);
            }
        }
    }

    // ---------------------------------------------------------------- ayudas

    private static int idUsuario(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Object id = s == null ? null : s.getAttribute("id_usuario");
        return id instanceof Integer ? ((Integer) id).intValue() : 0;
    }

    private static int idDeLaPeticion(HttpServletRequest req, String parametro) {
        if ("sesion".equals(parametro)) {
            return idUsuario(req);
        }
        try {
            return Integer.parseInt(req.getParameter(parametro).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String texto(Tabla t, String nombre) {
        return t.etiqueta + (nombre == null || nombre.trim().isEmpty() ? "" : " '" + nombre.trim() + "'");
    }

    private static String conMotivo(String desc, HttpServletRequest req) {
        String m = req.getParameter("motivo");
        return m == null || m.trim().isEmpty() ? desc : desc + " · Motivo: " + m.trim();
    }

    /** Columnas que no se comparan (cambian solas o no aportan). */
    private static final List<String> IGNORADAS = java.util.Arrays.asList("notif_vistas_hasta", "id_ultimo_movimiento");
    /** Columnas cuyo valor no se guarda: solo se indica que cambió. */
    private static final List<String> RESERVADAS = java.util.Arrays.asList("password", "foto_perfil");

    /** Nombres de campo con tilde o más claros (el resto se muestra como la columna, sin "id_" ni "_"). */
    private static final Map<String, String> ETIQUETAS = new HashMap<String, String>();

    static {
        String[][] e = {{"password", "contraseña"}, {"foto_perfil", "foto de perfil"}, {"razon_social", "razón social"},
            {"telefono", "teléfono"}, {"direccion", "dirección"}, {"descripcion", "descripción"}, {"ubicacion", "ubicación"},
            {"codigo", "código"}, {"centro_asignado", "centro principal"}, {"stock_minimo", "stock mínimo"},
            {"precio_compra", "precio de compra"}, {"observacion", "observación"}, {"observaciones", "observaciones"},
            {"fecha_anulacion", "fecha de anulación"}, {"motivo_anulacion", "motivo de anulación"},
            {"id_usuario_anula", "anulado por"}, {"id_usuario_resuelve", "resuelto por"}, {"fecha_resolucion", "fecha de resolución"},
            {"id_usuario_responsable", "responsable"}, {"motivo_rechazo", "motivo del rechazo"}, {"id_remision", "remisión"},
            {"fecha_finalizacion", "fecha de finalización"}, {"id_usuario_finaliza", "finalizado por"}};
        for (String[] x : e) {
            ETIQUETAS.put(x[0], x[1]);
        }
    }

    /** Nombre legible de los ids de otras tablas (rol, centro, usuario...). */
    private static final String[][] REFERENCIAS = {
        {"^id_rol$", "SELECT nombre FROM roles WHERE id_rol = ?"},
        {"^id_centro_costo.*", "SELECT codigo FROM centros_costo WHERE id_centro_costo = ?"},
        {"^(id_centro|id_centro_destino|id_centro_origen|id_centro_actual|centro_asignado)$", "SELECT nombre FROM centros WHERE id_centro = ?"},
        {"^id_unidad_medida$", "SELECT nombre FROM unidad_medida WHERE id_unidad_medida = ?"},
        {"^id_almacen.*", "SELECT nombre FROM almacenes WHERE id_almacen = ?"},
        {"^id_usuario.*", "SELECT nombre || ' ' || apellido FROM usuarios WHERE id_usuario = ?"},
        {"^id_proveedor$", "SELECT razon_social FROM proveedores WHERE id_proveedor = ?"},
        {"^id_insumo.*", "SELECT descripcion FROM insumos WHERE id_insumo = ?"},
        {"^id_remision$", "SELECT numero_remision FROM remisiones WHERE id_remision = ?"},
        {"^id_ot$", "SELECT numero_ot FROM ot WHERE id_ot = ?"},
        {"^id_compra$", "SELECT numero_oc FROM compras WHERE id_compra = ?"},
        {"^id_equipo$", "SELECT serial_activo FROM equipos WHERE id_equipo = ?"}
    };

    private static List<String[]> diferencias(BitacoraDAO dao, Tabla t, Map<String, String> antes, Map<String, String> despues) {
        List<String[]> l = new ArrayList<String[]>();
        for (Map.Entry<String, String> e : despues.entrySet()) {
            String col = e.getKey();
            if (col.equals(t.pk) || IGNORADAS.contains(col) || !antes.containsKey(col)) {
                continue;
            }
            String a = antes.get(col);
            String d = e.getValue();
            if (a == null ? d == null : a.equals(d)) {
                continue;
            }
            String campo = ETIQUETAS.containsKey(col) ? ETIQUETAS.get(col)
                    : (col.startsWith("id_") ? col.substring(3) : col).replace('_', ' ');
            if (RESERVADAS.contains(col)) {
                l.add(new String[]{campo, "(oculto)", "(modificado)"});
            } else {
                l.add(new String[]{campo, legible(dao, col, a), legible(dao, col, d)});
            }
        }
        return l;
    }

    private static String legible(BitacoraDAO dao, String columna, String valor) {
        if (valor == null) {
            return "";
        }
        if ("t".equals(valor)) {
            return "Sí";
        }
        if ("f".equals(valor)) {
            return "No";
        }
        if (valor.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+")) {
            valor = valor.substring(0, valor.indexOf('.'));
        }
        if (valor.matches("\\d+")) {
            for (String[] r : REFERENCIAS) {
                if (columna.matches(r[0])) {
                    try {
                        String n = dao.nombre(r[1], Integer.parseInt(valor));
                        if (n != null) {
                            return n + " (" + valor + ")";
                        }
                    } catch (Exception ignorada) {
                        // queda el número
                    }
                    break;
                }
            }
        }
        return valor;
    }
}
