package com.mycompany.proyectoprueba.seguridad;

import static com.mycompany.proyectoprueba.seguridad.Permisos.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Qué permiso exige cada pantalla, controlador y operación (parámetro "tipo").
 * Basta con tener UNO de los permisos listados. "*" = cualquier usuario con sesión.
 * Lo que NO figura aquí queda reservado al ADMINISTRADOR (lo desconocido se bloquea).
 */
final class ReglasAcceso {

    private static final Map<String, String[]> RUTAS = new HashMap<String, String[]>();
    private static final Map<String, String[]> OPERACIONES = new HashMap<String, String[]>();

    private static void ruta(String ruta, String... permisos) {
        RUTAS.put(ruta, permisos);
    }

    private static void op(String controlador, String tipos, String... permisos) {
        for (String t : tipos.split(",")) {
            OPERACIONES.put("/" + controlador + "#" + t.trim(), permisos);
        }
    }

    /** Controladores (Servlets) del sistema: responden en /NombreServlet. */
    private static final Set<String> CONTROLADORES = new HashSet<String>(Arrays.asList(
            "/PerfilServlet", "/UnidadMedidaServlet", "/CentroServlet", "/CentroCostoServlet", "/AlmacenServlet",
            "/ProveedorServlet", "/RolServlet", "/UsuarioServlet", "/InsumoServlet", "/StockServlet",
            "/DashboardServlet", "/LoginServlet", "/SolicitudCompraServlet", "/OrdenCompraServlet",
            "/OrdenTrabajoServlet", "/InventarioServlet", "/ActivoServlet", "/SolicitudActivoServlet",
            "/InformeServlet", "/ReporteIndividualServlet", "/NotificacionServlet", "/BitacoraServlet"));

    /** true si la ruta es la de un controlador (Servlet). */
    static boolean esControlador(String ruta) {
        return CONTROLADORES.contains(ruta);
    }

    static {
        String[] OT_CUALQUIERA = {OT_GESTION, OT_SUMINISTRAR, OT_FINALIZAR};
        // Pantallas
        ruta("/dashboard.jsp", "*");
        ruta("/header.jsp", "*");
        ruta("/footer.jsp", "*");
        for (String p : new String[]{"centrosvista", "centro_costovista", "almacenesvista", "unidad_medidavista", "rolesvista", "usuariosvista"}) {
            ruta("/" + p + ".jsp", REF_ADMIN);
        }
        ruta("/proveedorvista.jsp", PROVEEDORES);
        ruta("/insumosvista.jsp", INSUMOS_EDITAR);
        ruta("/solicitudCompra.jsp", SOLICITUD_COMPRA);
        ruta("/listarOC.jsp", OC_VER);
        ruta("/recepcionOC.jsp", OC_RECEPCION);
        ruta("/otCrear.jsp", OT_GESTION);
        ruta("/otListado.jsp", OT_CUALQUIERA);
        ruta("/otSuministrar.jsp", OT_SUMINISTRAR);
        ruta("/otFinalizar.jsp", OT_FINALIZAR);
        for (String p : new String[]{"stockVista", "kardex", "salidaInsumos", "ajustesInventario", "transferencias", "inventarioFisico"}) {
            ruta("/" + p + ".jsp", INVENTARIO);
        }
        ruta("/reportesInventario.jsp", INVENTARIO, INFORMES);
        ruta("/informes.jsp", INFORMES);
        ruta("/activosFijosVista.jsp", ACTIVOS);
        ruta("/activosSalida.jsp", ACTIVOS);
        ruta("/activosBaja.jsp", ACTIVOS);
        ruta("/activosAprobaciones.jsp", ACTIVOS_APROBAR, ACTIVOS_RECEPCION);

        // Reportes PDF individuales (ReporteIndividualServlet?reporte=...): por defecto solo administrador
        op("ReporteIndividualServlet", "oc", OC_VER);
        op("ReporteIndividualServlet", "stock", INVENTARIO);
        op("ReporteIndividualServlet", "hojaVida", ACTIVOS);
        op("ReporteIndividualServlet", "proveedor", PROVEEDORES);
        op("ReporteIndividualServlet", "insumo", INSUMOS_EDITAR);
        op("ReporteIndividualServlet", "almacen,centro,centroCosto,rol,unidad,usuario", REF_ADMIN);

        // Controladores (permiso base)
        for (String c : new String[]{"CentroServlet", "CentroCostoServlet", "AlmacenServlet", "RolServlet",
            "UsuarioServlet", "UnidadMedidaServlet"}) {
            ruta("/" + c, REF_ADMIN);
        }
        ruta("/ProveedorServlet", PROVEEDORES);
        ruta("/InsumoServlet", INSUMOS_EDITAR);
        ruta("/SolicitudCompraServlet", SOLICITUD_COMPRA);
        ruta("/OrdenCompraServlet", OC_VER);
        ruta("/OrdenTrabajoServlet", OT_CUALQUIERA);
        ruta("/InventarioServlet", INVENTARIO);
        ruta("/StockServlet", INVENTARIO);
        ruta("/ActivoServlet", ACTIVOS);
        ruta("/SolicitudActivoServlet", ACTIVOS);
        ruta("/InformeServlet", INFORMES);
        ruta("/DashboardServlet", "*");
        ruta("/PerfilServlet", "*");
        ruta("/NotificacionServlet", "*");
        // bitacora.jsp y BitacoraServlet no figuran: quedan solo para el administrador

        // Operaciones puntuales
        op("UnidadMedidaServlet", "listarCombo", REF_ADMIN, INSUMOS_EDITAR);
        op("InsumoServlet", "Borrar", INSUMOS_ELIMINAR);
        op("OrdenCompraServlet", "cargarCombos,cargarInsumos,actualizarOC,actualizarDetalleOC,eliminarDetalleOC,agregarDetalleOC,anularOC", OC_MODIFICAR);
        op("OrdenCompraServlet", "anularRecepcion", OC_ANULAR_RECEPCION);
        op("OrdenCompraServlet", "listarOCPendientes,listarAlmacenesPorCentro,listarCentrosCostoPorCentro,obtenerDetalleRecepcion,procesarRecepcion", OC_RECEPCION);
        op("OrdenTrabajoServlet", "contextoUsuario,listarCentrosCosto,listarResponsables,crear,modificar,eliminar", OT_GESTION);
        op("OrdenTrabajoServlet", "listarAbiertas,listarInsumos,almacenesConStock,suministrar,quitarSuministro", OT_SUMINISTRAR);
        op("OrdenTrabajoServlet", "finalizar,reabrir,ahora", OT_FINALIZAR);
        op("InventarioServlet", "reporteConsumo,reporteMasUsados,reporteValorizacion", INVENTARIO, INFORMES);
        op("InventarioServlet", "alertasMinimo", INVENTARIO, SOLICITUD_COMPRA);
        op("StockServlet", "imprimirReponer", INVENTARIO, SOLICITUD_COMPRA);
        op("StockServlet", "combosAlta,crear,modificarMinimo,eliminar", STOCK_EDITAR);
        op("InventarioServlet", "imprimirRemision", INVENTARIO, OT_GESTION, OT_SUMINISTRAR, ACTIVOS);
        op("SolicitudActivoServlet", "aprobar,rechazar,anularSalida", ACTIVOS_APROBAR);
        op("SolicitudActivoServlet", "enRecepcion,recibir,rechazarRecepcion", ACTIVOS_RECEPCION);
        op("SolicitudActivoServlet", "listarSolicitudes,detalleSolicitud,imprimirActaBaja", ACTIVOS, ACTIVOS_APROBAR, ACTIVOS_RECEPCION);
    }

    private ReglasAcceso() {
    }

    /** Permisos exigidos para la ruta y operación; null = solo administrador. */
    static String[] requeridos(String ruta, String tipo) {
        if (tipo != null) {
            String[] porOperacion = OPERACIONES.get(ruta + "#" + tipo);
            if (porOperacion != null) {
                return porOperacion;
            }
        }
        return RUTAS.get(ruta);
    }

    static boolean permitido(Permisos p, String ruta, String tipo) {
        if (p.esAdministrador()) {
            return true;
        }
        String[] req = requeridos(ruta, tipo);
        return req != null && p.tieneAlguno(req);
    }
}
