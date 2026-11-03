package com.mycompany.proyectoprueba.dao;

import com.mycompany.proyectoprueba.modelo.AlmacenStock;
import com.mycompany.proyectoprueba.modelo.ContextoUsuario;
import com.mycompany.proyectoprueba.modelo.Insumo;
import com.mycompany.proyectoprueba.modelo.InsumoDisponible;
import com.mycompany.proyectoprueba.modelo.OrdenTrabajo;
import com.mycompany.proyectoprueba.modelo.ResponsableOT;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/** Acceso a datos de las órdenes de trabajo (ot, detalle_ot, ot_eventos). */
public interface OrdenTrabajoDAO {

    /** Resultado del alta: id y fecha de creación. */
    final class Alta {
        public final int id;
        public final Timestamp fechaCreacion;

        public Alta(int id, Timestamp fechaCreacion) {
            this.id = id;
            this.fechaCreacion = fechaCreacion;
        }
    }

    // Consultas de pantalla
    ContextoUsuario contextoUsuario(int idUsuario) throws SQLException;

    /** Responsables posibles de un centro (0 = todos). */
    List<ResponsableOT> responsables(int idCentro) throws SQLException;

    /** Cantidad de OT visibles por estado. */
    Map<String, Integer> resumenPorEstado(Permisos p, int idUsuario) throws SQLException;

    /** estado: "" | ABIERTAS | un estado; mias: "" | SOLICITADAS | A_CARGO. */
    List<OrdenTrabajo> listar(String estado, String mias, Permisos p, int idUsuario) throws SQLException;

    /** Cabecera para la ficha o null si no existe. */
    OrdenTrabajo cabecera(int idOT) throws SQLException;

    List<OrdenTrabajo.Linea> lineas(int idOT) throws SQLException;

    List<OrdenTrabajo.Remision> remisiones(int idOT) throws SQLException;

    List<OrdenTrabajo.Evento> eventos(int idOT) throws SQLException;

    /** OT abiertas (PENDIENTE / EN_PROCESO) de los centros del usuario. */
    List<OrdenTrabajo> abiertas(Permisos p) throws SQLException;

    List<InsumoDisponible> insumosActivosConTotal() throws SQLException;

    List<AlmacenStock> almacenesConStock(int idInsumo, Permisos p) throws SQLException;

    // Validaciones
    boolean centroActivo(int idCentro) throws SQLException;

    boolean centroCostoDelCentro(int idCentroCosto, int idCentro) throws SQLException;

    boolean responsableValido(int idUsuario, int idCentro) throws SQLException;

    boolean tieneLineas(int idOT) throws SQLException;

    boolean tieneRemisiones(int idOT) throws SQLException;

    /** Insumos suministrados sin remisión de salida vigente. */
    boolean pendientesDeSalida(int idOT) throws SQLException;

    /** Alguna remisión de salida (insumos o activos) vigente. */
    boolean tieneSalida(int idOT) throws SQLException;

    boolean almacenDeSusCentros(int idAlmacen, Permisos p) throws SQLException;

    /** Insumo activo (código, descripción y precio de compra, 0 si no tiene) o null. */
    Insumo insumoActivo(int idInsumo) throws SQLException;

    // Nombres para el historial
    String nombreCentro(int idCentro) throws SQLException;

    String nombreUsuario(int idUsuario) throws SQLException;

    String nombreAlmacen(int idAlmacen) throws SQLException;

    /** {titulo, descripcion ("" si es NULL)} actuales. */
    String[] tituloYDescripcion(int idOT) throws SQLException;

    // Escritura (dentro de la transacción del servicio)
    /** Número OT-AAAAMMDD-0001 del correlativo diario. */
    String siguienteNumero() throws SQLException;

    Alta insertar(String numero, int idCentro, int idCentroCosto, String titulo, String descripcion,
            int idSolicitante, Integer idResponsable) throws SQLException;

    /** Lee la OT con bloqueo (FOR UPDATE) o null si no existe. */
    OrdenTrabajo.Bloqueo bloquear(int idOT) throws SQLException;

    void actualizar(int idOT, String titulo, String descripcion, int idCentroCosto, int idCentro, Integer idResponsable) throws SQLException;

    void eliminar(int idOT) throws SQLException;

    void insertarLinea(int idOT, int idInsumo, int cantidad, int idAlmacen, BigDecimal precio, int idUsuario) throws SQLException;

    OrdenTrabajo.LineaSuministrada lineaSuministrada(int idDetalle) throws SQLException;

    void eliminarLinea(int idDetalle) throws SQLException;

    void marcarEnProceso(int idOT) throws SQLException;

    /** EN_PROCESO si tiene líneas, si no PENDIENTE. */
    void recalcularEstado(int idOT) throws SQLException;

    void finalizar(int idOT, int idUsuario, String observacion) throws SQLException;

    void reabrir(int idOT, int idUsuario, String motivo) throws SQLException;

    void evento(int idOT, String tipo, int idUsuario, String detalle) throws SQLException;

    /** Fecha y hora actual de la base. */
    Timestamp ahora() throws SQLException;
}
