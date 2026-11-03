<%@include file="header.jsp" %>
<%-- INFORMES de los procesos: Compras, Órdenes de Trabajo, Inventario y Activos Fijos.
     Filtros comunes (fechas, centro, centro de costo, usuario) + filtros propios de cada proceso.
     La pantalla y el PDF muestran las mismas filas. --%>
<style>
    .resumen-informe .info-box { min-height: 70px; }
    .resumen-informe .info-box-number { font-size: 1.1rem; }
    #tablaInforme tbody tr { cursor: pointer; }
    #tablaInforme tbody tr:hover td { background-color: rgba(0, 123, 255, 0.08); }
    #fichaCuerpo th { width: 38%; background: #f8f9fa; }
</style>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><h1><i class="fas fa-chart-line"></i> Informes</h1></div></div>
    <div class="content"><div class="container-fluid">
        <ul class="nav nav-pills mb-3" id="tabsInforme">
            <li class="nav-item"><a class="nav-link active" href="#" data-informe="compras"><i class="fas fa-shopping-cart"></i> Compras</a></li>
            <li class="nav-item"><a class="nav-link" href="#" data-informe="ot"><i class="fas fa-clipboard-list"></i> Órdenes de Trabajo</a></li>
            <li class="nav-item"><a class="nav-link" href="#" data-informe="inventario"><i class="fas fa-warehouse"></i> Inventario</a></li>
            <li class="nav-item"><a class="nav-link" href="#" data-informe="activos"><i class="fas fa-laptop"></i> Activos Fijos</a></li>
        </ul>

        <div class="card card-outline card-primary">
            <div class="card-header"><h3 class="card-title"><i class="fas fa-filter"></i> Filtros</h3></div>
            <div class="card-body">
                <div class="form-row">
                    <div class="col-md-2 form-group"><label>Fecha inicio</label><input type="date" class="form-control" id="fDesde"></div>
                    <div class="col-md-2 form-group"><label>Fecha fin</label><input type="date" class="form-control" id="fHasta"></div>
                    <div class="col-md-3 form-group"><label>Centro</label><select class="form-control" id="fCentro"></select></div>
                    <div class="col-md-2 form-group"><label>Centro de costo</label><select class="form-control" id="fCC"><option value="">Todos</option></select></div>
                    <div class="col-md-3 form-group"><label id="lblUsuario">Usuario</label><select class="form-control" id="fUsuario"></select></div>
                </div>
                <!-- Filtros propios de Compras -->
                <div class="form-row esp esp-compras">
                    <div class="col-md-4 form-group"><label>Proveedor</label><select class="form-control" id="fProveedor"></select></div>
                    <div class="col-md-3 form-group"><label>Estado de la OC</label>
                        <select class="form-control" id="fEstadoOC"><option value="">Todos</option><option value="SOLICITADO">Solicitado</option>
                            <option value="PARCIAL">Parcial</option><option value="RECIBIDO">Recibido</option><option value="ANULADO">Anulado</option></select></div>
                    <div class="col-md-3 form-group"><label>Contiene</label>
                        <select class="form-control" id="fTipoItem"><option value="">Insumos y activos</option><option value="INSUMO">Insumos</option><option value="EQUIPO">Activos</option></select></div>
                    <div class="col-md-12"><small class="text-muted"><i class="fas fa-info-circle"></i> En compras, el centro de costo solo se asigna a los activos recibidos: ese filtro muestra las OC con activos ubicados en ese centro de costo.</small></div>
                </div>
                <!-- Filtros propios de Órdenes de Trabajo -->
                <div class="form-row esp esp-ot" style="display:none;">
                    <div class="col-md-3 form-group"><label>Estado de la OT</label>
                        <select class="form-control" id="fEstadoOT"><option value="">Todos</option><option value="PENDIENTE">Pendiente</option>
                            <option value="EN_PROCESO">En proceso</option><option value="FINALIZADA">Finalizada</option></select></div>
                    <div class="col-md-5 form-group"><label>Insumo entregado</label><select class="form-control fInsumo" id="fInsumoOT"></select></div>
                </div>
                <!-- Filtros propios de Inventario -->
                <div class="form-row esp esp-inventario" style="display:none;">
                    <div class="col-md-3 form-group"><label>Tipo de movimiento</label>
                        <select class="form-control" id="fTipoMovInv"><option value="">Todos</option><option value="ENTRADA_COMPRA">Entrada por compra</option>
                            <option value="SALIDA_OT">Salida por OT</option><option value="DEVOLUCION_OT">Devolución de OT</option><option value="AJUSTE">Ajuste</option>
                            <option value="ANULACION_AJUSTE">Anulación de ajuste</option><option value="TRANSFERENCIA_SALIDA">Transferencia (sale)</option>
                            <option value="TRANSFERENCIA_ENTRADA">Transferencia (entra)</option><option value="ANULACION_TRANSFERENCIA">Anulación de transferencia</option><option value="ANULACION_RECEPCION">Anulación de recepción de OC</option>
                            <option value="SALDO_INICIAL">Saldo inicial</option></select></div>
                    <div class="col-md-4 form-group"><label>Almacén</label><select class="form-control" id="fAlmacen"></select></div>
                    <div class="col-md-5 form-group"><label>Insumo</label><select class="form-control fInsumo" id="fInsumoInv"></select></div>
                </div>
                <!-- Filtros propios de Activos Fijos -->
                <div class="form-row esp esp-activos" style="display:none;">
                    <div class="col-md-3 form-group"><label>Tipo de movimiento</label>
                        <select class="form-control" id="fTipoMovAct"><option value="">Todos</option><option value="INGRESO">Ingreso por compra</option>
                            <option value="ASIGNACION">Asignación de responsable</option><option value="DEVOLUCION">Devolución</option>
                            <option value="REUBICACION">Cambio de centro de costo</option><option value="ENVIO">Envío</option><option value="RECEPCION">Recepción</option>
                            <option value="RECHAZO">Rechazo</option><option value="REPARACION">Envío a reparación</option><option value="RETORNO_REPARACION">Retorno de reparación</option>
                            <option value="BAJA">Baja</option><option value="ANULACION_SALIDA">Anulación de salida</option><option value="ANULACION_RECEPCION">Anulación de recepción de OC</option></select></div>
                    <div class="col-md-3 form-group"><label>Estado actual del activo</label>
                        <select class="form-control" id="fEstadoAct"><option value="">Todos</option><option value="DISPONIBLE">Disponible</option>
                            <option value="ASIGNADO">Con responsable</option><option value="EN_TRANSITO">En tránsito</option>
                            <option value="EN_REPARACION">En reparación</option><option value="BAJA">De baja</option></select></div>
                    <div class="col-md-3 form-group"><label>N° de activo o serie</label><input type="text" class="form-control" id="fBuscar" maxlength="60" placeholder="Ej.: AF-000012"></div>
                </div>
                <div>
                    <button class="btn btn-primary" id="btnConsultar"><i class="fas fa-search"></i> Consultar</button>
                    <button class="btn btn-outline-secondary" id="btnImprimir"><i class="fas fa-file-pdf"></i> Imprimir PDF</button>
                    <button class="btn btn-outline-primary" id="btnImprimirDetallado" title="Cada registro con su ficha completa (ítems, remisiones, historial...)"><i class="fas fa-file-alt"></i> Imprimir detallado</button>
                    <button class="btn btn-link" id="btnLimpiar">Limpiar filtros</button>
                </div>
            </div>
        </div>

        <div class="row resumen-informe" id="resumen"></div>
        <div id="avisoLimite" class="alert alert-warning" style="display:none;"></div>

        <div class="card">
            <div class="card-header"><h3 class="card-title" id="tituloResultado">Resultados</h3>
                <small class="text-muted ml-2" id="ayudaFicha" style="display:none;"><i class="fas fa-mouse-pointer"></i> Haga clic en un registro para ver su ficha completa e imprimirla.</small></div>
            <div class="card-body"><div id="contenedorTabla"><p class="text-muted">Elija los filtros y presione <b>Consultar</b>.</p></div></div>
        </div>
    </div></div>
</div>

<!-- Ficha completa de un registro -->
<div class="modal fade" id="modalFicha" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="fichaTitulo"></h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body" id="fichaCuerpo"></div>
            <div class="modal-footer">
                <button class="btn btn-primary" id="btnImprimirFicha"><i class="fas fa-print"></i> Imprimir ficha (PDF)</button>
                <button class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>
<script>
var URL_INF = "InformeServlet";
var informe = 'compras', tabla = null;
var TITULOS = { compras: 'Compras', ot: 'Órdenes de Trabajo', inventario: 'Movimientos de inventario', activos: 'Movimientos de activos fijos' };
var ETIQ_USUARIO = { compras: 'Solicitante', ot: 'Solicitante', inventario: 'Usuario que registró', activos: 'Usuario que registró' };
var COLORES = ['bg-info', 'bg-success', 'bg-warning', 'bg-primary', 'bg-secondary', 'bg-danger', 'bg-indigo', 'bg-teal'];

function opciones(lista, textoTodos) {
    var h = '<option value="">' + textoTodos + '</option>';
    $.each(lista, function (i, o) { h += '<option value="' + parseInt(o.id, 10) + '">' + escHtml(o.nombre) + '</option>'; });
    return h;
}
function hoyISO(d) { return d.getFullYear() + '-' + ('0' + (d.getMonth() + 1)).slice(-2) + '-' + ('0' + d.getDate()).slice(-2); }
function fechasPorDefecto() { var h = new Date(); $('#fHasta').val(hoyISO(h)); $('#fDesde').val(hoyISO(new Date(h.getFullYear(), h.getMonth(), 1))); }

$(document).ready(function () {
    fechasPorDefecto();
    $.post(URL_INF, { tipo: 'combos' }, function (c) {
        $('#fCentro').html(opciones(c.centros, 'Todos los centros'));
        $('#fUsuario').html(opciones(c.usuarios, 'Todos'));
        $('#fProveedor').html(opciones(c.proveedores, 'Todos'));
        $('.fInsumo').html(opciones(c.insumos, 'Todos'));
        $('#fAlmacen').html(opciones(c.almacenes, 'Todos'));
    }, 'json');
    $('#fCentro').change(function () {
        var c = $(this).val();
        if (!c) { $('#fCC').html('<option value="">Todos</option>'); return; }
        $.post(URL_INF, { tipo: 'centrosCosto', id_centro: c }, function (l) { $('#fCC').html(opciones(l, 'Todos')); }, 'json');
    });
    $('#tabsInforme a').click(function (e) {
        e.preventDefault();
        $('#tabsInforme a').removeClass('active'); $(this).addClass('active');
        informe = $(this).data('informe');
        $('.esp').hide(); $('.esp-' + informe).show();
        $('#lblUsuario').text(ETIQ_USUARIO[informe]);
        limpiarResultado();
    });
    $('#btnConsultar').click(consultar);
    $('#btnImprimir').click(imprimir);
    $('#btnImprimirDetallado').click(function () { imprimir('imprimirDetallado'); });
    $('#btnLimpiar').click(function () {
        $('select').val(''); $('#fBuscar').val(''); $('#fCC').html('<option value="">Todos</option>'); fechasPorDefecto(); limpiarResultado();
    });
});

function limpiarResultado() {
    if (tabla) { tabla.destroy(); tabla = null; }
    $('#resumen').html(''); $('#avisoLimite').hide();
    $('#tituloResultado').text('Resultados: ' + TITULOS[informe]);
    $('#contenedorTabla').html('<p class="text-muted">Elija los filtros y presione <b>Consultar</b>.</p>');
}

// Filtros del informe activo
function filtros() {
    var d = { informe: informe, desde: $('#fDesde').val(), hasta: $('#fHasta').val(), id_centro: $('#fCentro').val(), id_centro_costo: $('#fCC').val(), id_usuario: $('#fUsuario').val() };
    if (informe === 'compras') { d.id_proveedor = $('#fProveedor').val(); d.estado = $('#fEstadoOC').val(); d.tipo_item = $('#fTipoItem').val(); }
    if (informe === 'ot') { d.estado = $('#fEstadoOT').val(); d.id_insumo = $('#fInsumoOT').val(); }
    if (informe === 'inventario') { d.tipo_movimiento = $('#fTipoMovInv').val(); d.id_almacen = $('#fAlmacen').val(); d.id_insumo = $('#fInsumoInv').val(); }
    if (informe === 'activos') { d.tipo_movimiento = $('#fTipoMovAct').val(); d.estado_activo = $('#fEstadoAct').val(); d.buscar = $.trim($('#fBuscar').val()); }
    return d;
}

function validar() {
    var d = $('#fDesde').val(), h = $('#fHasta').val();
    if (d && h && d > h) { alert('La fecha inicio no puede ser posterior a la fecha fin.'); return false; }
    return true;
}

function consultar() {
    if (!validar()) return;
    var $b = $('#btnConsultar').prop('disabled', true);
    $.ajax({ type: 'post', url: URL_INF, dataType: 'json', data: $.extend({ tipo: 'consultar' }, filtros()),
        success: function (r) {
            $b.prop('disabled', false);
            if (r.error) { alert(r.error); return; }
            var h = '';
            $.each(r.resumen, function (i, x) {
                h += '<div class="col-lg-3 col-md-4 col-sm-6"><div class="info-box shadow-sm"><span class="info-box-icon ' + COLORES[i % COLORES.length] + '"><i class="fas fa-chart-bar"></i></span>' +
                     '<div class="info-box-content"><span class="info-box-text">' + escHtml(x.etiqueta) + '</span><span class="info-box-number">' + escHtml(x.valor) + '</span></div></div></div>';
            });
            $('#resumen').html(h);
            if (r.limitado) { $('#avisoLimite').text('Se muestran las primeras 5.000 filas. Acote el período o agregue filtros para ver el resto.').show(); } else { $('#avisoLimite').hide(); }
            if (tabla) { tabla.destroy(); tabla = null; }
            var t = '<table class="table table-bordered table-striped table-sm" id="tablaInforme" style="width:100%"><thead><tr>';
            $.each(r.columnas, function (i, c) { t += '<th' + (c.num ? ' class="text-right"' : '') + '>' + escHtml(c.t) + '</th>'; });
            t += '</tr></thead><tbody>';
            $.each(r.filas, function (i, f) {
                t += '<tr data-id="' + parseInt(f.id, 10) + '">';
                $.each(r.columnas, function (j, c) { t += '<td' + (c.num ? ' class="text-right"' : '') + '>' + escHtml(f[c.k]) + '</td>'; });
                t += '</tr>';
            });
            $('#contenedorTabla').html(t + '</tbody></table>');
            $('#ayudaFicha').toggle(r.filas.length > 0);
            tabla = $('#tablaInforme').DataTable({ responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [],
                language: { emptyTable: 'No hay datos para los filtros elegidos' } });
        },
        error: function () { $b.prop('disabled', false); alert('Error al consultar el informe.'); } });
}

// Ficha completa de un registro
var fichaActual = null;
$(document).on('click', '#tablaInforme tbody tr', function () {
    var id = $(this).data('id');
    if (id) verFicha(informe, id);
});
$(document).on('click', '#btnImprimirFicha', function () {
    if (fichaActual) window.open(URL_INF + '?' + $.param({ tipo: 'imprimirDetalle', informe: fichaActual.informe, id: fichaActual.id }), '_blank');
});

function verFicha(inf, id) {
    $.ajax({ type: 'post', url: URL_INF, dataType: 'json', data: { tipo: 'detalle', informe: inf, id: id },
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            fichaActual = { informe: inf, id: id };
            $('#fichaTitulo').html('<i class="fas fa-file-alt"></i> ' + escHtml(f.titulo) + (f.numero ? ' <small class="text-muted">N° ' + escHtml(f.numero) + '</small>' : ''));
            // Secciones de datos en dos columnas; tablas a lo ancho
            var h = '', fila = '';
            function cerrarFila() { if (fila) { h += '<div class="row">' + fila + '</div>'; fila = ''; } }
            $.each(f.bloques, function (i, b) {
                if (b.tipo === 'seccion') {
                    if (!b.filas.length) return;
                    var s = '<div class="col-lg-6"><h6 class="text-primary mt-2">' + escHtml(b.titulo) + '</h6><table class="table table-sm table-bordered">';
                    $.each(b.filas, function (j, c) { s += '<tr><th>' + escHtml(c[0]) + '</th><td>' + escHtml(c[1]) + '</td></tr>'; });
                    fila += s + '</table></div>';
                } else {
                    cerrarFila();
                    h += '<h6 class="text-primary mt-3">' + escHtml(b.titulo) + ' <span class="badge badge-secondary">' + b.filas.length + '</span></h6>';
                    if (!b.filas.length) { h += '<p class="text-muted">Sin registros.</p>'; return; }
                    h += '<div class="table-responsive"><table class="table table-sm table-striped table-bordered"><thead class="thead-light"><tr>';
                    $.each(b.columnas, function (j, c) { h += '<th>' + escHtml(c) + '</th>'; });
                    h += '</tr></thead><tbody>';
                    $.each(b.filas, function (j, r) { h += '<tr>'; $.each(r, function (k, v) { h += '<td>' + escHtml(v) + '</td>'; }); h += '</tr>'; });
                    h += '</tbody></table></div>';
                }
            });
            cerrarFila();
            $('#fichaCuerpo').html(h);
            $('#modalFicha').modal('show');
        },
        error: function () { alert('Error al obtener la ficha.'); } });
}

// Imprime con los filtros actuales: 'imprimir' = listado general, 'imprimirDetallado' = con la ficha de cada registro
function imprimir(tipo) {
    if (!validar()) return;
    window.open(URL_INF + '?' + $.param($.extend({ tipo: (tipo === 'imprimirDetallado' ? tipo : 'imprimir') }, filtros())), '_blank');
}
</script>
</body>
</html>
