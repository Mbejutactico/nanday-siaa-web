package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.OrdenCompraDAO;
import com.mycompany.proyectoprueba.dao.jdbc.OrdenCompraDAOJdbc;
import com.mycompany.proyectoprueba.inventario.Movimiento;
import com.mycompany.proyectoprueba.inventario.MovimientoStock;
import com.mycompany.proyectoprueba.modelo.DetalleCompra;
import com.mycompany.proyectoprueba.modelo.EquipoEstado;
import com.mycompany.proyectoprueba.modelo.ItemRecepcion;
import com.mycompany.proyectoprueba.modelo.LineaOC;
import com.mycompany.proyectoprueba.modelo.LineaRecepcion;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.modelo.OrdenCompra;
import com.mycompany.proyectoprueba.modelo.RecepcionOC;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Seriales;
import com.mycompany.proyectoprueba.util.Transaccion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Órdenes de compra: consulta, edición mientras están SOLICITADO o PARCIAL, anulación,
 * recepción (insumos al stock/Kardex y activos con número AF-...) y anulación de una recepción.
 */
public class OrdenCompraServicio {

    /** Resultado de una recepción procesada. */
    public static final class Recepcion {
        public final String numero;
        public final String estadoOC;
        public final List<String> activosGenerados;

        Recepcion(String numero, String estadoOC, List<String> activosGenerados) {
            this.numero = numero;
            this.estadoOC = estadoOC;
            this.activosGenerados = activosGenerados;
        }
    }

    /** Resultado de la anulación de una recepción. */
    public static final class Anulacion {
        public final String numero;
        public final int insumos;
        public final int activos;
        public final String estadoOC;

        Anulacion(String numero, int insumos, int activos, String estadoOC) {
            this.numero = numero;
            this.insumos = insumos;
            this.activos = activos;
            this.estadoOC = estadoOC;
        }
    }

    private final Connection conn;
    private final OrdenCompraDAO dao;
    private final Permisos permisos;

    public OrdenCompraServicio(Connection conn, OrdenCompraDAO dao, Permisos permisos) {
        this.conn = conn;
        this.dao = dao;
        this.permisos = permisos;
    }

    public static OrdenCompraServicio con(Connection conn, Permisos permisos) {
        return new OrdenCompraServicio(conn, new OrdenCompraDAOJdbc(conn), permisos);
    }

    // Consultas

    public List<OrdenCompra> listar() throws SQLException {
        return dao.listar(permisos);
    }

    /** Cabecera para editar (solo OC de los centros del usuario) o null. */
    public OrdenCompra cabecera(int idCompra) throws SQLException {
        return dao.cabecera(idCompra, permisos);
    }

    public List<DetalleCompra> lineasEditables(int idCompra) throws SQLException {
        return dao.lineasEditables(idCompra);
    }

    /** Líneas para editar; null si la OC no es de los centros del usuario. */
    public List<DetalleCompra> lineasEditablesSiVisible(int idCompra) throws SQLException {
        return dao.visible(idCompra, permisos) ? dao.lineasEditables(idCompra) : null;
    }

    public OrdenCompra ficha(int idCompra) throws SQLException {
        return dao.ficha(idCompra);
    }

    public boolean veCentro(int idCentro) {
        return permisos.veCentro(idCentro);
    }

    public List<DetalleCompra> lineasFicha(int idCompra) throws SQLException {
        return dao.lineasFicha(idCompra);
    }

    /** Recepciones de la OC con sus líneas. */
    public List<RecepcionOC> recepciones(int idCompra) throws SQLException {
        List<RecepcionOC> lista = dao.recepciones(idCompra);
        Map<Integer, RecepcionOC> porId = new HashMap<Integer, RecepcionOC>();
        for (RecepcionOC r : lista) {
            porId.put(Integer.valueOf(r.id), r);
        }
        for (LineaRecepcion l : dao.lineasDeRecepciones(idCompra)) {
            porId.get(Integer.valueOf(l.idRecepcion)).lineas.add(l);
        }
        return lista;
    }

    /** true si el usuario puede anular recepciones de una OC en ese estado. */
    public boolean puedeAnularRecepciones(String estadoOC) {
        return permisos.tiene(Permisos.OC_ANULAR_RECEPCION) && ("PARCIAL".equals(estadoOC) || "RECIBIDO".equals(estadoOC));
    }

    public List<Opcion> almacenesDelCentro(int idCentro) throws SQLException {
        return dao.almacenesDelCentro(idCentro);
    }

    public List<Opcion> pendientesDeRecibir() throws SQLException {
        return dao.pendientesDeRecibir(permisos);
    }

    public List<Opcion> centrosCostoDelCentro(int idCentro) throws SQLException {
        permisos.exigirCentro(idCentro);
        return dao.centrosCostoActivos(idCentro);
    }

    public List<Opcion> almacenesActivosDelCentro(int idCentro) throws SQLException {
        permisos.exigirCentro(idCentro);
        return dao.almacenesActivos(idCentro);
    }

    public OrdenCompra paraRecibir(int idCompra) throws SQLException {
        return dao.paraRecibir(idCompra);
    }

    public List<DetalleCompra> lineasPendientes(int idCompra) throws SQLException {
        return dao.lineasPendientes(idCompra);
    }

    // Edición

    public void actualizarCabecera(int idCompra, int idProveedor, int idCentro, String observaciones) throws SQLException {
        String estado = dao.estado(idCompra);
        if (estado == null) {
            throw new ReglaNegocioException("OC no encontrada");
        }
        if (!OrdenCompra.editable(estado)) {
            throw new ReglaNegocioException("No se puede modificar una OC en estado " + estado);
        }
        dao.actualizarCabecera(idCompra, idProveedor, idCentro, observaciones);
    }

    /** Cambia cantidad y precio de una línea (no menos de lo recibido; un activo es siempre 1 unidad). */
    public void actualizarLinea(int idDetalle, int cantidad, BigDecimal precio) throws SQLException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
        LineaOC l = dao.linea(idDetalle);
        if (l == null) {
            throw new ReglaNegocioException("Detalle no encontrado");
        }
        if (!OrdenCompra.editable(l.estadoOC)) {
            throw new ReglaNegocioException("No se puede modificar, OC " + l.estadoOC);
        }
        if (l.isEquipo() && cantidad != 1) {
            throw new ReglaNegocioException("Cada equipo es una unidad; para pedir más agregue otra línea");
        }
        if (cantidad < l.recibida) {
            throw new ReglaNegocioException("La cantidad no puede ser menor a lo ya recibido (" + l.recibida + ")");
        }
        conn.setAutoCommit(false);
        try {
            dao.actualizarLinea(idDetalle, cantidad, precio);
            dao.recalcularEstado(l.idCompra);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Quita una línea sin nada recibido ni historial de recepciones (la OC no queda vacía). */
    public void eliminarLinea(int idDetalle) throws SQLException {
        LineaOC l = dao.linea(idDetalle);
        if (l == null) {
            throw new ReglaNegocioException("Detalle no encontrado");
        }
        if (!OrdenCompra.editable(l.estadoOC)) {
            throw new ReglaNegocioException("No se puede eliminar, OC " + l.estadoOC);
        }
        if (l.recibida > 0) {
            throw new ReglaNegocioException("No se puede eliminar un artículo que ya tiene unidades recibidas");
        }
        if (dao.tieneRecepciones(idDetalle)) {
            throw new ReglaNegocioException("No se puede eliminar: el artículo tiene recepciones (aunque estén anuladas) en su historial. Ajuste la cantidad en su lugar");
        }
        if (l.lineasOC <= 1) {
            throw new ReglaNegocioException("La OC debe tener al menos un artículo. Si ya no se necesita, anúlela");
        }
        conn.setAutoCommit(false);
        try {
            dao.eliminarLinea(idDetalle);
            if (l.isEquipo()) {
                dao.eliminarEquipoSolicitado(l.idEquipo);
            }
            dao.recalcularEstado(l.idCompra);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Validaciones previas al agregado de un insumo a la OC. */
    public void validarAgregado(int idCompra, int cantidad, BigDecimal precio) throws SQLException {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
        String estado = dao.estado(idCompra);
        if (estado == null) {
            throw new ReglaNegocioException("OC no encontrada");
        }
        if (!OrdenCompra.editable(estado)) {
            throw new ReglaNegocioException("No se puede agregar, OC " + estado);
        }
    }

    public void agregarInsumo(int idCompra, int idInsumo, int cantidad, BigDecimal precio) throws SQLException {
        conn.setAutoCommit(false);
        try {
            dao.agregarLineaInsumo(idCompra, idInsumo, cantidad, precio);
            dao.recalcularEstado(idCompra);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    /** Una OC ya anulada se informa como aviso; una OC recibida no se puede anular. */
    public void validarAnulacion(int idCompra) throws SQLException {
        String estado = dao.estado(idCompra);
        if (estado == null) {
            throw new ReglaNegocioException("OC no encontrada");
        }
        if ("ANULADO".equals(estado)) {
            throw ReglaNegocioException.aviso("Esta OC ya está anulada");
        }
        if ("RECIBIDO".equals(estado)) {
            throw new ReglaNegocioException("No se puede anular una OC recibida");
        }
    }

    /** Anula la OC; los equipos pedidos que nunca llegaron quedan anulados. */
    public void anular(int idCompra) throws SQLException {
        conn.setAutoCommit(false);
        try {
            dao.anular(idCompra);
            dao.anularEquiposPendientes(idCompra);
            conn.commit();
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Recepción

    /**
     * Registra una recepción (OC-...-R01, R02...): los activos reciben su número AF-... y quedan
     * DISPONIBLES en el centro de costo indicado; los insumos entran al stock del almacén (Kardex).
     * La OC queda PARCIAL o RECIBIDO. Lanza IllegalArgumentException con el mensaje para el usuario.
     */
    public Recepcion recibir(int idCompra, List<ItemRecepcion> items, int idUsuario) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Bloquea la OC mientras se procesa (dos recepciones simultáneas no se pisan)
            OrdenCompra oc = dao.bloquear(idCompra);
            if (oc == null) {
                throw new IllegalArgumentException("OC no encontrada");
            }
            int idCentroDestino = oc.getIdCentroDestino();
            permisos.exigirCentro(idCentroDestino);
            String numeroOC = oc.getNumero();
            if (!"SOLICITADO".equals(oc.getEstado()) && !"PARCIAL".equals(oc.getEstado())) {
                throw new IllegalArgumentException("La OC no está en estado SOLICITADO o PARCIAL");
            }

            int nro = dao.contarRecepciones(idCompra) + 1;
            String numeroRecepcion = (numeroOC == null ? "OC-" + idCompra : numeroOC) + "-R" + (nro < 10 ? "0" : "") + nro;
            int idRecepcion = dao.crearRecepcion(idCompra, numeroRecepcion, idUsuario);

            int lineasRecibidas = 0;
            Set<String> seriesEnEstaRecepcion = new HashSet<String>();
            List<String> activosGenerados = new ArrayList<String>();

            for (ItemRecepcion it : items) {
                if (it.errorLectura != null) {
                    throw it.errorLectura;
                }
                int idDetalle = it.idDetalle;
                int cantidadRecibir = it.cantidad;
                if (cantidadRecibir < 0) {
                    throw new IllegalArgumentException("La cantidad a recibir no puede ser negativa");
                }
                if (cantidadRecibir == 0) {
                    continue;   // esta línea no se recibe ahora
                }
                // El detalle debe pertenecer a ESTA OC
                LineaOC l = dao.bloquearLinea(idDetalle, idCompra);
                if (l == null) {
                    throw new IllegalArgumentException("El detalle " + idDetalle + " no pertenece a esta OC");
                }
                if (cantidadRecibir > l.getPendiente()) {
                    throw new IllegalArgumentException("Cantidad a recibir excede lo pendiente para el detalle " + idDetalle);
                }

                if (l.isEquipo()) {
                    // Activo fijo: se ubica en un CENTRO DE COSTO (no en un almacén)
                    int idCentroCosto = it.idCentroCosto;
                    if (idCentroCosto <= 0) {
                        throw new IllegalArgumentException("Seleccione el centro de costo de todos los activos que va a recibir");
                    }
                    if (!dao.centroCostoDelCentro(idCentroCosto, idCentroDestino)) {
                        throw new IllegalArgumentException("El centro de costo seleccionado no pertenece al centro destino de la OC");
                    }
                    // Número de serie del fabricante: OPCIONAL, pero si se carga no se puede repetir
                    String serie = it.serie == null ? "" : it.serie.trim();
                    String serieGuardar = null;
                    if (!serie.isEmpty()) {
                        if (serie.length() > 100) {
                            throw new IllegalArgumentException("El número de serie no puede superar 100 caracteres");
                        }
                        if (!seriesEnEstaRecepcion.add(serie.toUpperCase())) {
                            throw new IllegalArgumentException("El número de serie " + serie + " está repetido");
                        }
                        if (dao.serieDuplicada(serie, l.idEquipo)) {
                            throw new IllegalArgumentException("Ya existe un activo con el número de serie " + serie);
                        }
                        serieGuardar = serie;
                    }
                    // Número de activo / patrimonio: lo genera el sistema (único e irrepetible)
                    String numeroActivo = dao.siguienteNumeroActivo();
                    dao.ingresarEquipo(l.idEquipo, numeroActivo, serieGuardar, idCentroDestino, idCentroCosto);
                    dao.recibirLineaEquipo(idDetalle, cantidadRecibir, idCentroDestino, idCentroCosto, l.idEquipo);

                    // Primer evento del historial del activo: su INGRESO por compra
                    Movimiento ingreso = new Movimiento(Movimiento.INGRESO, l.idEquipo, idUsuario);
                    ingreso.centroDestino = Integer.valueOf(idCentroDestino);
                    ingreso.centroCostoDestino = Integer.valueOf(idCentroCosto);
                    ingreso.estadoAnterior = "SOLICITADO";
                    ingreso.estadoNuevo = "DISPONIBLE";
                    ingreso.observaciones = "Ingreso por compra - OC " + numeroOC
                            + (serieGuardar == null ? "" : " - Serie del fabricante: " + serieGuardar);
                    ingreso.registrar(conn);

                    dao.registrarLineaEquipo(idRecepcion, idDetalle, cantidadRecibir, l.idEquipo, idCentroCosto, numeroActivo);
                    activosGenerados.add(numeroActivo + " (" + (l.descripcion == null ? "" : l.descripcion) + ")");
                } else {
                    // Insumo: suma al stock del almacén
                    int idAlmacen = it.idAlmacen;
                    if (idAlmacen <= 0) {
                        throw new IllegalArgumentException("Seleccione el almacén de todos los insumos que va a recibir");
                    }
                    if (!dao.almacenActivoDelCentro(idAlmacen, idCentroDestino)) {
                        throw new IllegalArgumentException("El almacén seleccionado no pertenece al centro destino de la OC");
                    }
                    dao.recibirLineaInsumo(idDetalle, cantidadRecibir, idAlmacen);

                    // Entrada al stock y al Kardex
                    MovimientoStock entrada = new MovimientoStock(MovimientoStock.ENTRADA_COMPRA, l.idInsumo, idAlmacen, cantidadRecibir, idUsuario);
                    entrada.documento = numeroOC;
                    entrada.idCompra = Integer.valueOf(idCompra);
                    entrada.costoUnitario = l.precio;
                    entrada.observacion = "Recepción de compra " + numeroRecepcion;
                    entrada.registrar(conn);

                    dao.registrarLineaInsumo(idRecepcion, idDetalle, cantidadRecibir, idAlmacen);
                }
                lineasRecibidas++;
            }

            if (lineasRecibidas == 0) {
                throw new IllegalArgumentException("Indique al menos un artículo con cantidad a recibir mayor a 0");
            }
            // Estado según TODAS las líneas (insumos y equipos)
            String nuevoEstado = dao.recalcularEstado(idCompra);
            conn.commit();
            return new Recepcion(numeroRecepcion, nuevoEstado, activosGenerados);
        } finally {
            Transaccion.finalizar(conn);
        }
    }

    // Anulación de una recepción

    /**
     * Revierte lo que ingresó en una recepción: descuenta el stock (Kardex ANULACION_RECEPCION) y
     * los activos vuelven a SOLICITADO (solo si siguen tal como ingresaron). La OC queda PARCIAL o
     * SOLICITADO. almacenesElegidos: id de línea de recepción -> almacén, solo para recepciones
     * antiguas sin almacén registrado. Lanza IllegalArgumentException con el mensaje para el usuario.
     */
    public Anulacion anularRecepcion(int idRecepcion, String motivo, Map<String, Integer> almacenesElegidos, int idUsuario) throws SQLException {
        conn.setAutoCommit(false);
        try {
            if (motivo.length() < 10) {
                throw new IllegalArgumentException("Escriba la justificación de la anulación (al menos 10 caracteres).");
            }
            if (motivo.length() > 1000) {
                throw new IllegalArgumentException("La justificación puede tener hasta 1000 caracteres.");
            }
            // La recepción y su OC, bloqueadas mientras se anula
            RecepcionOC r = dao.bloquearRecepcion(idRecepcion);
            if (r == null) {
                throw new IllegalArgumentException("La recepción no existe.");
            }
            permisos.exigirCentro(r.idCentroDestino);
            if (r.anulada) {
                throw new IllegalArgumentException("La recepción " + r.numero + " ya está anulada.");
            }
            if (!"PARCIAL".equals(r.estadoOC) && !"RECIBIDO".equals(r.estadoOC)) {
                throw new IllegalArgumentException("Solo se pueden anular recepciones de una OC en estado PARCIAL o RECIBIDO (estado actual: " + r.estadoOC + ").");
            }
            String obs = "Anulación de la recepción " + r.numero + " de la OC " + r.numeroOC + ". Motivo: " + motivo;

            List<LineaRecepcion> lineas = dao.lineasParaAnular(idRecepcion);
            if (lineas.isEmpty()) {
                throw new IllegalArgumentException("La recepción no tiene artículos.");
            }
            int insumos = 0, activos = 0;
            for (LineaRecepcion l : lineas) {
                String item = l.descripcion;
                if (l.recibidaEnOC < l.cantidad) {
                    throw new IllegalArgumentException("La línea de " + item + " tiene recibido " + l.recibidaEnOC
                            + " y la recepción indica " + l.cantidad + ". Revise la OC antes de anular.");
                }
                if (l.isEquipo()) {
                    // Activo: solo si sigue tal como ingresó (disponible, sin movimientos posteriores)
                    EquipoEstado e = dao.bloquearEquipo(l.idEquipo);
                    if (e == null) {
                        throw new IllegalArgumentException("No se encontró el activo de la línea " + item + ".");
                    }
                    if (!"DISPONIBLE".equals(e.estado)) {
                        throw new IllegalArgumentException("El activo " + e.numeroActivo + " (" + item + ") ya no está disponible (estado "
                                + e.estado + "). Primero revierta lo que se hizo con él.");
                    }
                    if (dao.movimientosPosteriores(l.idEquipo, r.fecha) > 0) {
                        throw new IllegalArgumentException("El activo " + e.numeroActivo + " (" + item + ") ya tuvo movimientos o tiene una solicitud pendiente "
                                + "después de recibirse. Primero revierta esos movimientos.");
                    }
                    Movimiento m = new Movimiento(Movimiento.ANULACION_RECEPCION, l.idEquipo, idUsuario);
                    m.centroOrigen = e.idCentro;
                    m.centroCostoOrigen = e.idCentroCosto;
                    m.estadoAnterior = "DISPONIBLE";
                    m.estadoNuevo = "SOLICITADO";
                    m.observaciones = obs + " Número de activo liberado: " + e.numeroActivo + ".";
                    m.registrar(conn);
                    // Vuelve a ser un pedido pendiente de la OC (sin número de activo ni ubicación)
                    dao.devolverEquipoASolicitado(l.idEquipo, Seriales.temporal(idUsuario, l.idEquipo));
                    dao.restarRecibidoEquipo(l.idDetalle, l.cantidad);
                    activos++;
                } else {
                    // Insumo: sale del stock del almacén donde entró
                    int idAlmacen = l.idAlmacen;
                    if (idAlmacen <= 0) {
                        Integer elegido = almacenesElegidos.get(String.valueOf(l.id));
                        idAlmacen = elegido == null ? 0 : elegido.intValue();
                        if (idAlmacen <= 0) {
                            throw new IllegalArgumentException("Indique de qué almacén se descuenta " + item
                                    + " (la recepción es anterior al registro de almacenes).");
                        }
                        if (!dao.almacenDelCentro(idAlmacen, r.idCentroDestino)) {
                            throw new IllegalArgumentException("El almacén indicado para " + item + " no pertenece al centro destino de la OC.");
                        }
                        dao.asignarAlmacenALinea(l.id, idAlmacen);
                    }
                    // Si ya se usó parte del stock, MovimientoStock lo impide y explica cuánto hay
                    MovimientoStock salida = new MovimientoStock(MovimientoStock.ANULACION_RECEPCION, l.idInsumo, idAlmacen, -l.cantidad, idUsuario);
                    salida.documento = r.numero;
                    salida.idCompra = Integer.valueOf(r.idCompra);
                    salida.costoUnitario = l.precio;
                    salida.observacion = obs;
                    salida.registrar(conn);
                    dao.restarRecibidoInsumo(l.idDetalle, l.cantidad);
                    insumos++;
                }
            }
            dao.marcarRecepcionAnulada(idRecepcion, idUsuario, motivo);
            String nuevoEstado = dao.recalcularEstado(r.idCompra);
            conn.commit();
            return new Anulacion(r.numero, insumos, activos, nuevoEstado);
        } finally {
            Transaccion.finalizar(conn);
        }
    }
}
