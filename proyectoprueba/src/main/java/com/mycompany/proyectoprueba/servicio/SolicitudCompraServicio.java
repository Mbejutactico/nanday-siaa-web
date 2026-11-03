package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.SolicitudCompraDAO;
import com.mycompany.proyectoprueba.dao.jdbc.SolicitudCompraDAOJdbc;
import com.mycompany.proyectoprueba.modelo.BorradorCompra;
import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.InsumoPrecio;
import com.mycompany.proyectoprueba.util.Seriales;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Solicitud de compra UNIFICADA: en un mismo borrador se cargan INSUMOS (consumibles, van a
 * stock) y EQUIPOS (activos fijos: una línea por unidad, que recibe su número de serie en la
 * recepción de la OC). Al guardarla pasa a SOLICITADO con el número OC-AAAAMMDD-0001.
 */
public class SolicitudCompraServicio {

    static final String PROVEEDOR_INACTIVO = "El proveedor no está registrado o está inactivo.";

    private final Connection conn;
    private final SolicitudCompraDAO dao;

    public SolicitudCompraServicio(Connection conn, SolicitudCompraDAO dao) {
        this.conn = conn;
        this.dao = dao;
    }

    public static SolicitudCompraServicio con(Connection conn) {
        return new SolicitudCompraServicio(conn, new SolicitudCompraDAOJdbc(conn));
    }

    public List<InsumoPrecio> insumosActivos() throws SQLException {
        return dao.insumosActivos();
    }

    public BorradorCompra borrador(int idUsuario) throws SQLException {
        return dao.borrador(idUsuario);
    }

    public int idBorrador(int idUsuario) throws SQLException {
        return dao.idBorrador(idUsuario);
    }

    public List<DetalleCompra> detalle(int idCompra) throws SQLException {
        return dao.detalle(idCompra);
    }

    /**
     * Validaciones previas al agregado de un insumo. Devuelve el precio del insumo, que se toma
     * de la base (no del navegador).
     */
    public BigDecimal validarInsumo(int idInsumo, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0.");
        }
        BigDecimal precio = dao.precioInsumoActivo(idInsumo);
        if (precio == null) {
            throw new ReglaNegocioException("El insumo seleccionado no existe o está inactivo.");
        }
        return precio;
    }

    /** Agrega el insumo al borrador (lo crea si no existe); si ya estaba, suma la cantidad. */
    public void agregarInsumo(int idUsuario, int idProveedor, int idCentroDestino, String observaciones,
            int idInsumo, int cantidad, BigDecimal precio) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int idCompra = obtenerOCrearBorrador(idUsuario, idProveedor, idCentroDestino, observaciones);
            if (dao.sumarCantidadInsumo(idCompra, idInsumo, cantidad) == 0) {
                dao.insertarLineaInsumo(idCompra, idInsumo, cantidad, precio);
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Validaciones previas al agregado de activos. */
    public void validarActivos(int cantidad, BigDecimal precio) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0.");
        }
        if (cantidad > 100) {
            throw new ReglaNegocioException("Máximo 100 activos por vez.");
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor a 0.");
        }
    }

    /**
     * Cada unidad es un equipo individual SOLICITADO (con número provisorio, el real se carga en
     * la recepción de la OC) y su propia línea de detalle.
     */
    public void agregarActivos(int idUsuario, int idProveedor, int idCentroDestino, String observaciones,
            int idInsumoBase, int cantidad, BigDecimal precio) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int idCompra = obtenerOCrearBorrador(idUsuario, idProveedor, idCentroDestino, observaciones);
            for (int i = 0; i < cantidad; i++) {
                int idEquipo = dao.crearEquipoSolicitado(Seriales.temporal(idUsuario, i), idInsumoBase);
                dao.insertarLineaEquipo(idCompra, idEquipo, precio);
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Quita una línea del borrador del usuario (y el equipo pendiente si era un activo). */
    public void eliminarLinea(int idUsuario, int idDetalle) throws SQLException {
        conn.setAutoCommit(false);
        try {
            DetalleCompra linea = dao.lineaDelBorrador(idDetalle, idUsuario);
            if (linea == null) {
                throw new ReglaNegocioException("El artículo no pertenece a su solicitud en curso.");
            }
            dao.eliminarLinea(idDetalle);
            if (linea.isEquipo()) {
                dao.eliminarEquipoSolicitado(linea.getIdEquipo() == null ? 0 : linea.getIdEquipo().intValue());
            }
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /**
     * Emite la solicitud: guarda la cabecera que está en pantalla (si viene completa), asigna el
     * número de OC del día y la pasa a SOLICITADO. Devuelve el número.
     */
    public String emitir(int idUsuario, String idProveedor, String idCentroDestino, String observaciones) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // FOR UPDATE: un doble clic no genera dos números
            BorradorCompra b = dao.bloquearBorrador(idUsuario);
            if (b == null) {
                throw new ReglaNegocioException("No hay solicitud en curso.");
            }
            if (dao.contarLineas(b.idCompra) == 0) {
                throw new ReglaNegocioException("La solicitud debe tener al menos un artículo.");
            }
            if (!vacio(idProveedor) && !vacio(idCentroDestino)) {
                int prov = Integer.parseInt(idProveedor.trim());
                if (!dao.proveedorActivo(prov)) {
                    throw new ReglaNegocioException(PROVEEDOR_INACTIVO);
                }
                dao.actualizarCabecera(b.idCompra, prov, Integer.parseInt(idCentroDestino.trim()), observaciones);
            }
            // OC-AAAAMMDD-0001: el correlativo del día se obtiene de forma atómica (correlativo_oc)
            int correlativo = dao.siguienteCorrelativo(b.fecha);
            String numero = "OC-" + new SimpleDateFormat("yyyyMMdd").format(b.fecha) + "-" + String.format("%04d", correlativo);
            dao.emitir(b.idCompra, numero);
            conn.commit();
            return numero;
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Borra el borrador del usuario y sus equipos pendientes. false si no tenía borrador. */
    public boolean cancelar(int idUsuario) throws SQLException {
        conn.setAutoCommit(false);
        try {
            List<Integer> equipos = dao.equiposDelBorrador(idUsuario);
            int filas = dao.eliminarBorrador(idUsuario);   // el detalle se borra en cascada
            for (Integer idEquipo : equipos) {
                dao.eliminarEquipoSolicitado(idEquipo.intValue());
            }
            conn.commit();
            return filas > 0;
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** RF-003: las órdenes de compra solo se emiten a proveedores registrados y activos. */
    private int obtenerOCrearBorrador(int idUsuario, int idProveedor, int idCentroDestino, String observaciones) throws SQLException {
        int idCompra = dao.idBorrador(idUsuario);
        if (idCompra != 0) {
            return idCompra;
        }
        if (!dao.proveedorActivo(idProveedor)) {
            throw new ReglaNegocioException(PROVEEDOR_INACTIVO);
        }
        return dao.crearBorrador(idUsuario, idProveedor, idCentroDestino, observaciones);
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
