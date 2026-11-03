package com.mycompany.proyectoprueba.servicio;

import com.mycompany.proyectoprueba.dao.InformeDAO;
import com.mycompany.proyectoprueba.dao.jdbc.InformeDAOJdbc;
import com.mycompany.proyectoprueba.modelo.FichaInforme;
import com.mycompany.proyectoprueba.modelo.FiltrosInforme;
import com.mycompany.proyectoprueba.modelo.Informe;
import com.mycompany.proyectoprueba.modelo.Opcion;
import com.mycompany.proyectoprueba.seguridad.Permisos;
import com.mycompany.proyectoprueba.util.Comun;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Informes de los procesos: compras, órdenes de trabajo, inventario y activos fijos, con su
 * ficha por registro. La pantalla (JSON) y el PDF usan las mismas filas, armadas una sola vez
 * aquí, siempre limitadas a los centros que el rol del usuario puede ver.
 */
public class InformeServicio {

    private static final String[] ESTADOS_OC = {"SOLICITADO", "PARCIAL", "RECIBIDO", "ANULADO"};
    private static final String[] ESTADOS_OT = {"PENDIENTE", "EN_PROCESO", "FINALIZADA", "ANULADA"};
    private static final String[] TIPOS_ITEM = {"INSUMO", "EQUIPO"};
    private static final String[] MOVIMIENTOS_STOCK = {"SALDO_INICIAL", "ENTRADA_COMPRA", "SALIDA_OT", "DEVOLUCION_OT", "AJUSTE",
        "ANULACION_AJUSTE", "TRANSFERENCIA_SALIDA", "TRANSFERENCIA_ENTRADA", "ANULACION_TRANSFERENCIA", "ANULACION_RECEPCION"};
    private static final String[] MOVIMIENTOS_ACTIVOS = {"INGRESO", "ASIGNACION", "DEVOLUCION", "REUBICACION", "ENVIO", "RECEPCION",
        "RECHAZO", "REPARACION", "RETORNO_REPARACION", "BAJA", "ANULACION_SALIDA", "ANULACION_RECEPCION"};
    private static final String[] ESTADOS_ACTIVO = {"DISPONIBLE", "ASIGNADO", "EN_TRANSITO", "EN_REPARACION", "BAJA"};

    /** Textos para los códigos de estados, movimientos y eventos. */
    private static final Map<String, String> ETIQUETA = new HashMap<String, String>();

    static {
        String[][] e = {{"SOLICITADO", "Solicitado"}, {"PARCIAL", "Parcial"}, {"RECIBIDO", "Recibido"}, {"ANULADO", "Anulado"},
            {"PENDIENTE", "Pendiente"}, {"EN_PROCESO", "En proceso"}, {"FINALIZADA", "Finalizada"}, {"ANULADA", "Anulada"},
            {"SALDO_INICIAL", "Saldo inicial"}, {"ENTRADA_COMPRA", "Entrada por compra"}, {"SALIDA_OT", "Salida por OT"}, {"DEVOLUCION_OT", "Devolución de OT"},
            {"AJUSTE", "Ajuste"}, {"ANULACION_AJUSTE", "Anulación de ajuste"}, {"TRANSFERENCIA_SALIDA", "Transferencia (sale)"},
            {"TRANSFERENCIA_ENTRADA", "Transferencia (entra)"}, {"ANULACION_TRANSFERENCIA", "Anulación de transferencia"}, {"ANULACION_RECEPCION", "Anulación de recepción de OC"},
            {"INGRESO", "Ingreso por compra"}, {"ASIGNACION", "Asignación de responsable"}, {"DEVOLUCION", "Devolución"}, {"REUBICACION", "Cambio de centro de costo"},
            {"ENVIO", "Envío"}, {"RECEPCION", "Recepción"}, {"RECHAZO", "Rechazo"}, {"REPARACION", "Envío a reparación"},
            {"RETORNO_REPARACION", "Retorno de reparación"}, {"BAJA", "Baja"}, {"ANULACION_SALIDA", "Anulación de salida"},
            {"CREACION", "Creación"}, {"MODIFICACION", "Modificación"}, {"SUMINISTRO", "Suministro de insumos"}, {"QUITAR_SUMINISTRO", "Insumo quitado"},
            {"FINALIZACION", "Finalización"}, {"REAPERTURA", "Finalización anulada"}, {"SALIDA", "Salida registrada"}, {"ANULACION_REMISION", "Remisión anulada"},
            {"CONSUMO", "Consumo directo"}, {"ROTURA_PERDIDA", "Rotura / pérdida"}, {"VENCIMIENTO", "Vencimiento"}, {"CORRECCION_CONTEO", "Corrección por conteo"}};
        for (String[] x : e) {
            ETIQUETA.put(x[0], x[1]);
        }
    }

    private final InformeDAO dao;
    private final Permisos permisos;

    public InformeServicio(InformeDAO dao, Permisos permisos) {
        this.dao = dao;
        this.permisos = permisos;
    }

    public static InformeServicio con(java.sql.Connection conn, Permisos permisos) {
        return new InformeServicio(new InformeDAOJdbc(conn), permisos);
    }

    // Combos

    /** centros, usuarios, proveedores, insumos y almacenes (en ese orden). */
    public Map<String, List<Opcion>> combos() throws SQLException {
        Map<String, List<Opcion>> c = new LinkedHashMap<String, List<Opcion>>();
        c.put("centros", dao.centros(permisos));
        c.put("usuarios", dao.usuarios());
        c.put("proveedores", dao.proveedores());
        c.put("insumos", dao.insumos());
        c.put("almacenes", dao.almacenes(permisos));
        return c;
    }

    /** Centros de costo de un centro del usuario (vacío si no lo ve). */
    public List<Opcion> centrosCosto(int idCentro) throws SQLException {
        if (idCentro > 0 && permisos.veCentro(idCentro)) {
            return dao.centrosCosto(idCentro);
        }
        return Collections.emptyList();
    }

    // Informes

    /** informe: compras, ot, inventario o activos. */
    public Informe generar(String informe, FiltrosInforme f) throws SQLException {
        if ("compras".equals(informe)) {
            return compras(f);
        } else if ("ot".equals(informe)) {
            return ordenesTrabajo(f);
        } else if ("inventario".equals(informe)) {
            return inventario(f);
        } else if ("activos".equals(informe)) {
            return activos(f);
        }
        throw new ReglaNegocioException("Informe no reconocido");
    }

    /** El filtro por centro solo se acepta para un centro del usuario. */
    private void exigirCentro(FiltrosInforme f) {
        if (f.idCentro > 0) {
            permisos.exigirCentro(f.idCentro);
        }
    }

    private Informe compras(FiltrosInforme f) throws SQLException {
        Informe res = new Informe();
        res.titulo = "INFORME DE COMPRAS";
        res.plantilla = "informeCompras";
        res.columnas = new String[][]{{"numero", "N° OC", "L"}, {"fecha", "Fecha", "L"}, {"proveedor", "Proveedor", "L"}, {"centro", "Centro destino", "L"},
            {"solicitante", "Solicitante", "L"}, {"estado", "Estado", "L"}, {"items", "Ítems", "R"}, {"recibido", "Recibido", "R"}, {"total", "Total (Gs.)", "R"}};
        exigirCentro(f);
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Integer> porEstado = new LinkedHashMap<String, Integer>();
        for (Map<String, Object> r : dao.compras(f, valor(f.estado, ESTADOS_OC), valor(f.tipoItem, TIPOS_ITEM), permisos)) {
            Map<String, Object> fila = new HashMap<String, Object>();
            long pedidas = ((Long) r.get("pedidas")).longValue();
            long recibidas = ((Long) r.get("recibidas")).longValue();
            String estado = (String) r.get("estado");
            fila.put("id", r.get("id_compra"));
            fila.put("numero", txt(r, "numero_oc"));
            fila.put("fecha", fecha((java.util.Date) r.get("fecha_compra")));
            fila.put("proveedor", txt(r, "razon_social"));
            fila.put("centro", txt(r, "centro"));
            fila.put("solicitante", txt(r, "solicitante"));
            fila.put("estado", ETIQUETA.containsKey(estado) ? ETIQUETA.get(estado) : Comun.txt(estado));
            fila.put("items", String.valueOf(r.get("items")));
            fila.put("recibido", pedidas == 0 ? "-" : (recibidas * 100 / pedidas) + " %");
            fila.put("total", gs((BigDecimal) r.get("total")));
            if (!res.agregar(fila)) {
                break;
            }
            if (!"ANULADO".equals(estado)) {
                total = total.add((BigDecimal) r.get("total"));
            }
            sumar(porEstado, (String) fila.get("estado"));
        }
        res.resumen("Órdenes de compra", String.valueOf(res.filas.size()));
        res.resumen("Monto total (sin anuladas)", "Gs. " + gs(total));
        agregarConteos(res, porEstado);
        return res;
    }

    private Informe ordenesTrabajo(FiltrosInforme f) throws SQLException {
        Informe res = new Informe();
        res.titulo = "INFORME DE ÓRDENES DE TRABAJO";
        res.plantilla = "informeOT";
        res.columnas = new String[][]{{"numero", "N° OT", "L"}, {"fecha", "Creación", "L"}, {"titulo", "Título", "L"}, {"centro", "Centro destino", "L"},
            {"cc", "Centro de costo", "L"}, {"solicitante", "Solicitante", "L"}, {"responsable", "Responsable", "L"}, {"estado", "Estado", "L"}, {"items", "Insumos", "R"}, {"valor", "Valor (Gs.)", "R"}};
        exigirCentro(f);
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Integer> porEstado = new LinkedHashMap<String, Integer>();
        for (Map<String, Object> r : dao.ordenesTrabajo(f, valor(f.estado, ESTADOS_OT), permisos)) {
            Map<String, Object> fila = new HashMap<String, Object>();
            String estado = (String) r.get("estado");
            fila.put("id", r.get("id_ot"));
            fila.put("numero", txt(r, "numero_ot"));
            fila.put("fecha", Comun.fechaHora((Timestamp) r.get("fecha_creacion")));
            fila.put("titulo", txt(r, "titulo"));
            fila.put("centro", txt(r, "centro"));
            fila.put("cc", cc((String) r.get("cc_codigo"), (String) r.get("cc_desc")));
            fila.put("solicitante", txt(r, "solicitante"));
            fila.put("responsable", txt(r, "responsable"));
            fila.put("estado", ETIQUETA.containsKey(estado) ? ETIQUETA.get(estado) : Comun.txt(estado));
            fila.put("items", String.valueOf(r.get("items")));
            fila.put("valor", gs((BigDecimal) r.get("valor")));
            if (!res.agregar(fila)) {
                break;
            }
            total = total.add((BigDecimal) r.get("valor"));
            sumar(porEstado, (String) fila.get("estado"));
        }
        res.resumen("Órdenes de trabajo", String.valueOf(res.filas.size()));
        res.resumen("Valor de insumos entregados", "Gs. " + gs(total));
        agregarConteos(res, porEstado);
        return res;
    }

    private Informe inventario(FiltrosInforme f) throws SQLException {
        Informe res = new Informe();
        res.titulo = "INFORME DE MOVIMIENTOS DE INVENTARIO";
        res.plantilla = "informeInventario";
        res.columnas = new String[][]{{"fecha", "Fecha", "L"}, {"movimiento", "Movimiento", "L"}, {"documento", "Documento", "L"}, {"insumo", "Insumo", "L"},
            {"almacen", "Almacén", "L"}, {"cc", "C. costo", "L"}, {"entrada", "Entrada", "R"}, {"salida", "Salida", "R"}, {"valor", "Valor (Gs.)", "R"}, {"usuario", "Usuario", "L"}};
        exigirCentro(f);
        long entradas = 0, salidas = 0;
        BigDecimal valorSalidas = BigDecimal.ZERO;
        for (Map<String, Object> r : dao.movimientosStock(f, valor(f.tipoMovimiento, MOVIMIENTOS_STOCK), permisos)) {
            int cant = ((Integer) r.get("cantidad")).intValue();
            BigDecimal costo = r.get("costo_unitario") == null ? BigDecimal.ZERO : (BigDecimal) r.get("costo_unitario");
            BigDecimal valor = costo.multiply(new BigDecimal(Math.abs(cant)));
            String tipo = (String) r.get("tipo");
            Map<String, Object> fila = new HashMap<String, Object>();
            fila.put("id", r.get("id_mov"));
            fila.put("fecha", Comun.fechaHora((Timestamp) r.get("fecha")));
            fila.put("movimiento", ETIQUETA.containsKey(tipo) ? ETIQUETA.get(tipo) : Comun.txt(tipo));
            fila.put("documento", txt(r, "documento"));
            fila.put("insumo", txt(r, "codigo") + " - " + txt(r, "descripcion"));
            fila.put("almacen", txt(r, "almacen"));
            fila.put("cc", txt(r, "cc"));
            fila.put("entrada", cant > 0 ? String.valueOf(cant) : "");
            fila.put("salida", cant < 0 ? String.valueOf(-cant) : "");
            fila.put("valor", gs(valor));
            fila.put("usuario", txt(r, "usuario"));
            if (!res.agregar(fila)) {
                break;
            }
            if (cant > 0) {
                entradas += cant;
            } else {
                salidas += -cant;
                valorSalidas = valorSalidas.add(valor);
            }
        }
        res.resumen("Movimientos", String.valueOf(res.filas.size()));
        res.resumen("Unidades que entraron", String.valueOf(entradas));
        res.resumen("Unidades que salieron", String.valueOf(salidas));
        res.resumen("Valor de las salidas", "Gs. " + gs(valorSalidas));
        return res;
    }

    private Informe activos(FiltrosInforme f) throws SQLException {
        Informe res = new Informe();
        res.titulo = "INFORME DE MOVIMIENTOS DE ACTIVOS FIJOS";
        res.plantilla = "informeActivos";
        res.columnas = new String[][]{{"fecha", "Fecha", "L"}, {"movimiento", "Movimiento", "L"}, {"numero", "N° movimiento", "L"}, {"activo", "N° activo", "L"},
            {"descripcion", "Descripción", "L"}, {"origen", "Origen", "L"}, {"destino", "Destino", "L"}, {"usuario", "Registrado por", "L"}};
        exigirCentro(f);
        Map<String, Integer> porTipo = new LinkedHashMap<String, Integer>();
        for (Map<String, Object> r : dao.movimientosActivos(f, valor(f.tipoMovimiento, MOVIMIENTOS_ACTIVOS), valor(f.estadoActivo, ESTADOS_ACTIVO), permisos)) {
            String serial = txt(r, "serial_activo");
            String tipo = (String) r.get("tipo_movimiento");
            Map<String, Object> fila = new HashMap<String, Object>();
            fila.put("id", r.get("id_movimiento"));
            fila.put("fecha", Comun.fechaHora((Timestamp) r.get("fecha_movimiento")));
            fila.put("movimiento", ETIQUETA.containsKey(tipo) ? ETIQUETA.get(tipo) : Comun.txt(tipo));
            fila.put("numero", txt(r, "numero_movimiento_unico"));
            fila.put("activo", serial.startsWith("PEND-") ? "Pendiente" : serial);
            fila.put("descripcion", txt(r, "descripcion"));
            fila.put("origen", lugar((String) r.get("centro_origen"), (String) r.get("cc_origen")));
            fila.put("destino", lugar((String) r.get("centro_destino"), (String) r.get("cc_destino")));
            fila.put("usuario", txt(r, "usuario"));
            if (!res.agregar(fila)) {
                break;
            }
            sumar(porTipo, (String) fila.get("movimiento"));
        }
        res.resumen("Movimientos", String.valueOf(res.filas.size()));
        agregarConteos(res, porTipo);
        return res;
    }

    // PDF: filtros aplicados y detallado

    /** Todos los filtros del informe con su valor (o "Todos" si no se eligió), con los nombres reales. */
    public List<String[]> filtrosAplicados(String informe, FiltrosInforme f) throws SQLException {
        List<String[]> l = new ArrayList<String[]>();
        l.add(new String[]{"Fecha inicio", f.desde.isEmpty() ? "Sin límite" : fechaIso(f.desde)});
        l.add(new String[]{"Fecha fin", f.hasta.isEmpty() ? "Sin límite" : fechaIso(f.hasta)});
        l.add(new String[]{"Centro", nombre(f.idCentro > 0 ? dao.nombreCentro(f.idCentro) : "", f.idCentro, "Todos los centros")});
        l.add(new String[]{"Centro de costo", nombre(f.idCentroCosto > 0 ? dao.nombreCentroCosto(f.idCentroCosto) : "", f.idCentroCosto, "Todos")});
        String usuario = nombre(f.idUsuario > 0 ? dao.nombreUsuario(f.idUsuario) : "", f.idUsuario, "Todos");
        if ("compras".equals(informe)) {
            l.add(new String[]{"Solicitante", usuario});
            l.add(new String[]{"Proveedor", nombre(f.idProveedor > 0 ? dao.nombreProveedor(f.idProveedor) : "", f.idProveedor, "Todos")});
            String estado = valor(f.estado, ESTADOS_OC);
            l.add(new String[]{"Estado de la OC", estado.isEmpty() ? "Todos" : etiqueta(estado)});
            String tipoItem = valor(f.tipoItem, TIPOS_ITEM);
            l.add(new String[]{"Contiene", "INSUMO".equals(tipoItem) ? "Insumos" : ("EQUIPO".equals(tipoItem) ? "Activos" : "Insumos y activos")});
        } else if ("ot".equals(informe)) {
            l.add(new String[]{"Solicitante", usuario});
            String estado = valor(f.estado, ESTADOS_OT);
            l.add(new String[]{"Estado de la OT", estado.isEmpty() ? "Todos" : etiqueta(estado)});
            l.add(new String[]{"Insumo entregado", nombre(f.idInsumo > 0 ? dao.nombreInsumo(f.idInsumo) : "", f.idInsumo, "Todos")});
        } else if ("inventario".equals(informe)) {
            l.add(new String[]{"Usuario que registró", usuario});
            String tipoMov = valor(f.tipoMovimiento, MOVIMIENTOS_STOCK);
            l.add(new String[]{"Tipo de movimiento", tipoMov.isEmpty() ? "Todos" : etiqueta(tipoMov)});
            l.add(new String[]{"Almacén", nombre(f.idAlmacen > 0 ? dao.nombreAlmacen(f.idAlmacen) : "", f.idAlmacen, "Todos")});
            l.add(new String[]{"Insumo", nombre(f.idInsumo > 0 ? dao.nombreInsumo(f.idInsumo) : "", f.idInsumo, "Todos")});
        } else if ("activos".equals(informe)) {
            l.add(new String[]{"Usuario que registró", usuario});
            String tipoMov = valor(f.tipoMovimiento, MOVIMIENTOS_ACTIVOS);
            l.add(new String[]{"Tipo de movimiento", tipoMov.isEmpty() ? "Todos" : etiqueta(tipoMov)});
            String estadoActivo = valor(f.estadoActivo, ESTADOS_ACTIVO);
            l.add(new String[]{"Estado actual del activo", estadoActivo.isEmpty() ? "Todos" : ("BAJA".equals(estadoActivo) ? "De baja" : etiquetaEstado(estadoActivo))});
            l.add(new String[]{"N° de activo o serie", f.buscar.isEmpty() ? "Cualquiera" : "Contiene \"" + f.buscar + "\""});
        }
        return l;
    }

    /** Nombre del registro elegido en un filtro, el texto por defecto si no se eligió, o "N° id" si no existe. */
    private static String nombre(String encontrado, int id, String siNo) {
        if (id <= 0) {
            return siNo;
        }
        return encontrado.isEmpty() ? "N° " + id : encontrado;
    }

    /**
     * Filas del PDF detallado: por cada registro una cabecera (REG) seguida de su ficha
     * completa y un separador (SEP); como máximo LIMITE_DETALLE registros.
     */
    public List<Map<String, Object>> detallado(String informe, Informe res) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<Map<String, Object>>();
        int n = 0;
        for (Map<String, Object> f : res.filas) {
            if (n >= Informe.LIMITE_DETALLE) {
                filas.add(FichaInforme.filaPdf("NOTA", "0", new String[]{"... y " + (res.filas.size() - Informe.LIMITE_DETALLE)
                        + " registros más que no se detallan. Acote los filtros (por ejemplo, el período) para verlos."}));
                break;
            }
            n++;
            FichaInforme fi = ficha(informe, ((Integer) f.get("id")).intValue());
            String titulo = n + ".  " + fi.titulo + (fi.numero == null || fi.numero.isEmpty() ? "" : "  N° " + fi.numero);
            StringBuilder lado = new StringBuilder();
            for (String k : new String[]{"fecha", "movimiento", "estado"}) {
                Object v = f.get(k);
                if (v != null && !v.toString().isEmpty()) {
                    lado.append(lado.length() == 0 ? "" : "  |  ").append(v);
                }
            }
            filas.add(FichaInforme.filaPdf("REG", "0", new String[]{titulo, lado.toString()}));
            filas.addAll(fi.filasPdf());
            filas.add(FichaInforme.filaPdf("SEP", "0", new String[0]));
        }
        if (filas.isEmpty()) {
            filas.add(FichaInforme.filaPdf("NOTA", "0", new String[]{"No hay registros que cumplan con los filtros seleccionados."}));
        }
        return filas;
    }

    // Fichas

    public FichaInforme ficha(String informe, int id) throws SQLException {
        if ("compras".equals(informe)) {
            return fichaCompra(id);
        } else if ("ot".equals(informe)) {
            return fichaOT(id);
        } else if ("inventario".equals(informe)) {
            return fichaMovStock(id);
        } else if ("activos".equals(informe)) {
            return fichaMovActivo(id);
        }
        throw new ReglaNegocioException("Informe no reconocido");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private FichaInforme fichaCompra(int id) throws SQLException {
        Map<String, Object> r = dao.compra(id);
        exigir(r != null, "Orden de compra no encontrada");
        exigir(permisos.veCentro(entero(r, "id_centro_destino")), "La orden de compra es de un centro al que no tiene acceso");
        FichaInforme f = new FichaInforme("ORDEN DE COMPRA", txt(r, "numero_oc"));
        f.seccion("Datos de la orden de compra");
        f.campo("N° OC", txt(r, "numero_oc"));
        f.campo("Fecha", fecha((java.util.Date) r.get("fecha_compra")));
        f.campo("Estado", etiqueta((String) r.get("estado")));
        f.campo("Centro destino", txt(r, "centro"));
        f.campo("Solicitante", txt(r, "solicitante"));
        f.campo("Registrada por", txt(r, "comprador"));
        f.campo("Observaciones", txt(r, "observaciones"));
        f.seccion("Proveedor");
        f.campo("Razón social", txt(r, "razon_social"));
        f.campo("RUC", txt(r, "ruc"));
        f.campo("Teléfono", txt(r, "telefono"));
        f.campo("Dirección", txt(r, "direccion"));
        BigDecimal total = BigDecimal.ZERO;
        f.tabla("Detalle", "Tipo", "Código", "Descripción", "Cantidad", "Recibido", "Precio unit. (Gs.)", "Subtotal (Gs.)");
        for (Map<String, Object> d : dao.lineasCompra(id)) {
            BigDecimal precio = d.get("precio_unitario") == null ? BigDecimal.ZERO : (BigDecimal) d.get("precio_unitario");
            int cantidad = entero(d, "cantidad");
            BigDecimal sub = precio.multiply(new BigDecimal(cantidad));
            total = total.add(sub);
            boolean activo = "EQUIPO".equals(d.get("tipo_item"));
            String serial = txt(d, "serial_activo");
            String desc = txt(d, "descripcion");
            if (activo && !serial.isEmpty() && !serial.startsWith("PEND-")) {
                desc += " (N° activo " + serial + (d.get("numero_serie") == null ? "" : ", serie " + d.get("numero_serie")) + ")";
            }
            f.fila(activo ? "Activo" : "Insumo", txt(d, "codigo"), desc, String.valueOf(cantidad),
                    String.valueOf(entero(d, "cantidad_recibida")), gs(precio), gs(sub));
        }
        f.seccion("Totales");
        f.campo("Total de la orden", "Gs. " + gs(total));
        return f;
    }

    private FichaInforme fichaOT(int id) throws SQLException {
        Map<String, Object> r = dao.ordenTrabajo(id);
        exigir(r != null, "Orden de trabajo no encontrada");
        exigir(permisos.veCentro(entero(r, "id_centro")), "La orden de trabajo es de un centro al que no tiene acceso");
        FichaInforme f = new FichaInforme("ORDEN DE TRABAJO", txt(r, "numero_ot"));
        f.seccion("Datos de la orden de trabajo");
        f.campo("N° OT", txt(r, "numero_ot"));
        f.campo("Título", txt(r, "titulo"));
        f.campo("Descripción", txt(r, "descripcion"));
        f.campo("Estado", etiqueta((String) r.get("estado")));
        f.campo("Fecha y hora de creación", Comun.fechaHora((Timestamp) r.get("fecha_creacion")));
        f.campo("Centro destino", txt(r, "centro"));
        f.campo("Centro de costo", cc((String) r.get("cc_codigo"), (String) r.get("cc_desc")));
        f.campo("Solicitante", txt(r, "solicitante"));
        f.campo("Responsable", r.get("responsable") == null ? "Sin responsable" : txt(r, "responsable"));
        if (r.get("fecha_finalizacion") != null) {
            f.seccion("Finalización");
            f.campo("Fecha y hora", Comun.fechaHora((Timestamp) r.get("fecha_finalizacion")));
            f.campo("Finalizada por", txt(r, "finalizado_por"));
            f.campo("Observación", txt(r, "observacion_finalizacion"));
        }
        if (r.get("fecha_anulacion_fin") != null) {
            f.seccion("Última anulación de la finalización");
            f.campo("Fecha y hora", Comun.fechaHora((Timestamp) r.get("fecha_anulacion_fin")));
            f.campo("Anulada por", txt(r, "anulado_por"));
            f.campo("Motivo", txt(r, "motivo_anulacion_fin"));
        }
        tabla(f, "Insumos suministrados", new String[]{"Código", "Insumo", "Cantidad", "Almacén", "Fecha", "Entregó", "Remisión"}, dao.insumosOT(id));
        tabla(f, "Remisiones", new String[]{"N° remisión", "Tipo", "Fecha", "Transportista", "Estado"}, dao.remisionesOT(id));
        tabla(f, "Historial", new String[]{"Fecha", "Evento", "Usuario", "Detalle"}, dao.historialOT(id));
        return f;
    }

    /** Tabla de la ficha con las filas de texto dadas (los códigos conocidos se muestran con su texto). */
    private static void tabla(FichaInforme f, String titulo, String[] columnas, List<String[]> filas) {
        f.tabla(titulo, columnas);
        for (String[] v : filas) {
            for (int i = 0; i < v.length; i++) {
                if (ETIQUETA.containsKey(v[i])) {
                    v[i] = ETIQUETA.get(v[i]);
                }
            }
            f.fila(v);
        }
    }

    private FichaInforme fichaMovStock(int id) throws SQLException {
        Map<String, Object> r = dao.movimientoStock(id);
        exigir(r != null, "Movimiento no encontrado");
        exigir(permisos.veCentro(entero(r, "id_centro")), "El movimiento es de un centro al que no tiene acceso");
        int cant = entero(r, "cantidad");
        BigDecimal costo = (BigDecimal) r.get("costo_unitario");
        String tipo = (String) r.get("tipo");
        FichaInforme f = new FichaInforme("MOVIMIENTO DE INVENTARIO", txt(r, "documento"));
        f.seccion("Movimiento");
        f.campo("Fecha y hora", Comun.fechaHora((Timestamp) r.get("fecha")));
        f.campo("Tipo", etiqueta(tipo));
        f.campo("Documento", txt(r, "documento"));
        f.campo(cant > 0 ? "Entrada" : "Salida", Math.abs(cant) + " " + txt(r, "unidad"));
        f.campo("Saldo resultante en el almacén", String.valueOf(entero(r, "saldo")));
        f.campo("Costo unitario", costo == null ? "" : "Gs. " + gs(costo));
        f.campo("Valor del movimiento", costo == null ? "" : "Gs. " + gs(costo.multiply(new BigDecimal(Math.abs(cant)))));
        f.campo("Registrado por", txt(r, "usuario"));
        f.campo("Observación", txt(r, "observacion"));
        f.seccion("Insumo y ubicación");
        f.campo("Insumo", txt(r, "codigo") + " - " + txt(r, "descripcion"));
        f.campo("Almacén", txt(r, "almacen"));
        f.campo("Centro", txt(r, "centro"));
        f.campo("Centro de costo", cc((String) r.get("cc_codigo"), (String) r.get("cc_desc")));
        f.seccion("Documento de origen");
        if (r.get("numero_ot") != null) {
            f.campo("Orden de trabajo", r.get("numero_ot") + " - " + txt(r, "ot_titulo"));
        }
        if (r.get("numero_oc") != null) {
            f.campo("Orden de compra", r.get("numero_oc") + " - " + txt(r, "razon_social"));
        }
        if (r.get("aj_numero") != null) {
            f.campo("Ajuste", r.get("aj_numero") + " (" + etiqueta((String) r.get("aj_tipo")) + ")" + (Boolean.TRUE.equals(r.get("aj_anulado")) ? " - ANULADO" : ""));
            f.campo("Motivo del ajuste", txt(r, "aj_motivo"));
        }
        if (r.get("tr_numero") != null) {
            f.campo("Transferencia", r.get("tr_numero") + ": " + txt(r, "tr_origen") + " -> " + txt(r, "tr_destino")
                    + (Boolean.TRUE.equals(r.get("tr_anulada")) ? " - ANULADA" : ""));
        }
        f.campo("Tipo de documento", "SALDO_INICIAL".equals(tipo) ? "Saldo inicial al habilitar el Kardex" : "");
        return f;
    }

    private FichaInforme fichaMovActivo(int id) throws SQLException {
        Map<String, Object> r = dao.movimientoActivo(id);
        exigir(r != null, "Movimiento no encontrado");
        exigir(permisos.veCentro(entero(r, "id_centro_origen")) || permisos.veCentro(entero(r, "id_centro_destino")),
                "El movimiento es de un centro al que no tiene acceso");
        String serial = txt(r, "serial_activo");
        FichaInforme f = new FichaInforme("MOVIMIENTO DE ACTIVO FIJO", txt(r, "numero_movimiento_unico"));
        f.seccion("Movimiento");
        f.campo("N° de movimiento", txt(r, "numero_movimiento_unico"));
        f.campo("Tipo", etiqueta((String) r.get("tipo_movimiento")));
        f.campo("Fecha y hora", Comun.fechaHora((Timestamp) r.get("fecha_movimiento")));
        if (r.get("estado_anterior") != null || r.get("estado_nuevo") != null) {
            f.campo("Cambio de estado", etiquetaEstado((String) r.get("estado_anterior")) + " -> " + etiquetaEstado((String) r.get("estado_nuevo")));
        }
        f.campo("Registrado por", txt(r, "registrado_por"));
        f.campo("Observaciones", txt(r, "observaciones"));
        f.campo("Justificación del rechazo", txt(r, "justificacion_rechazo"));
        f.seccion("Activo");
        f.campo("N° de activo", serial.startsWith("PEND-") ? "Pendiente de recepción" : serial);
        f.campo("N° de serie", txt(r, "numero_serie"));
        f.campo("Descripción", txt(r, "codigo") + " - " + txt(r, "descripcion"));
        f.campo("Estado actual", etiquetaEstado((String) r.get("estado_actual")));
        f.campo("Ubicación actual", lugar((String) r.get("c_actual"), (String) r.get("cc_actual")));
        f.seccion("Origen y destino");
        f.campo("Origen", lugar((String) r.get("c_origen"), (String) r.get("cc_origen")));
        f.campo("Destino", lugar((String) r.get("c_destino"), (String) r.get("cc_destino")));
        f.campo("Proveedor (reparación)", "REPARACION".equals(r.get("tipo_movimiento")) ? txt(r, "proveedor") : "");
        f.seccion("Responsable y transporte");
        f.campo("Entregó", txt(r, "emisor"));
        f.campo("Recibió / responsable", txt(r, "receptor"));
        String persona = (txt(r, "datos_receptor_nombre") + " " + txt(r, "datos_receptor_apellido")).trim();
        f.campo("Persona", persona.isEmpty() ? "" : persona + (r.get("datos_receptor_ci") == null ? "" : " (CI " + r.get("datos_receptor_ci") + ")"));
        String vehiculo = (txt(r, "datos_receptor_vehiculo_marca") + " " + txt(r, "datos_receptor_vehiculo_modelo")).trim();
        f.campo("Vehículo", vehiculo + (r.get("datos_receptor_vehiculo_chapa") == null ? "" : " - Chapa " + r.get("datos_receptor_vehiculo_chapa")));
        f.campo("Remisión", txt(r, "numero_remision"));
        f.campo("Solicitud", txt(r, "sol_numero"));
        return f;
    }

    // Utilidades

    private static String txt(Map<String, Object> m, String clave) {
        return Comun.txt((String) m.get(clave));
    }

    private static int entero(Map<String, Object> m, String clave) {
        return ((Integer) m.get(clave)).intValue();
    }

    private static String etiqueta(String codigo) {
        return codigo == null ? "" : (ETIQUETA.containsKey(codigo) ? ETIQUETA.get(codigo) : codigo);
    }

    private static String etiquetaEstado(String e) {
        if (e == null) {
            return "-";
        }
        if ("DISPONIBLE".equals(e)) return "Disponible";
        if ("ASIGNADO".equals(e)) return "Con responsable";
        if ("EN_TRANSITO".equals(e)) return "En tránsito";
        if ("EN_REPARACION".equals(e)) return "En reparación";
        if ("SOLICITADO".equals(e)) return "Pedido";
        return etiqueta(e);
    }

    private static void sumar(Map<String, Integer> m, String clave) {
        Integer v = m.get(clave);
        m.put(clave, Integer.valueOf(v == null ? 1 : v.intValue() + 1));
    }

    private static void agregarConteos(Informe res, Map<String, Integer> m) {
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            res.resumen(e.getKey(), String.valueOf(e.getValue()));
        }
    }

    /** Valor de filtro solo si está en la lista permitida; si no, "". */
    private static String valor(String v, String... permitidos) {
        if (v != null) {
            for (String p : permitidos) {
                if (p.equals(v)) {
                    return v;
                }
            }
        }
        return "";
    }

    /** yyyy-MM-dd -> dd/MM/yyyy */
    private static String fechaIso(String v) {
        return v.substring(8, 10) + "/" + v.substring(5, 7) + "/" + v.substring(0, 4);
    }

    private static String fecha(java.util.Date d) {
        return d == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(d);
    }

    /** Importe en guaraníes: 1.234.567 */
    private static String gs(BigDecimal v) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        return new DecimalFormat("#,##0", s).format(v == null ? BigDecimal.ZERO : v);
    }

    private static String cc(String codigo, String descripcion) {
        if (codigo == null) {
            return "";
        }
        return descripcion == null || descripcion.trim().isEmpty() ? codigo : codigo + " - " + descripcion;
    }

    private static String lugar(String centro, String cc) {
        if (centro == null && cc == null) {
            return "";
        }
        return Comun.txt(centro) + (cc == null ? "" : " / " + cc);
    }
}
