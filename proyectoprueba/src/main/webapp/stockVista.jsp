<%@include file="header.jsp" %>
<%-- INVENTARIOS > Stock de insumos (SOLO CONSULTA): resumen, filtros y ficha con ingresos por compras.
     El stock se actualiza al recibir las OC; aquí no se crea, edita ni elimina. --%>
<style>
    #tablaStock tbody tr { cursor: pointer; }
    #tablaStock tbody tr:hover td { background-color: rgba(0, 123, 255, 0.08); }
    .nivel-stock { height: 8px; margin-top: 4px; min-width: 70px; }
    .timeline-body p { margin-bottom: 2px; }
    .historial-scroll { max-height: 420px; overflow-y: auto; padding-right: 6px; }
    .info-box .info-box-text { white-space: normal; }
</style>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-8">
                    <h1><i class="fas fa-boxes"></i> Stock de Insumos</h1>
                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">

            <!-- Resumen -->
            <div class="row">
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-info">
                        <div class="inner"><h3 id="resRegistros">0</h3><p>Registros de stock</p></div>
                        <div class="icon"><i class="fas fa-boxes"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-primary">
                        <div class="inner"><h3 id="resUnidades">0</h3><p>Unidades en stock</p></div>
                        <div class="icon"><i class="fas fa-cubes"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-warning">
                        <div class="inner"><h3 id="resBajo">0</h3><p>Bajo el mínimo</p></div>
                        <div class="icon"><i class="fas fa-exclamation-triangle"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-danger">
                        <div class="inner"><h3 id="resSinStock">0</h3><p>Sin stock</p></div>
                        <div class="icon"><i class="fas fa-times-circle"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-success">
                        <div class="inner"><h3 id="resAlmacenes">0</h3><p>Almacenes con stock</p></div>
                        <div class="icon"><i class="fas fa-warehouse"></i></div>
                    </div>
                </div>
            </div>

            <!-- Filtros -->
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title"><i class="fas fa-filter"></i> Filtros</h3>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-3">
                            <div class="form-group">
                                <label>Centro</label>
                                <select class="form-control" id="filtroCentro">
                                    <option value="">Todos los centros</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="form-group">
                                <label>Almacén</label>
                                <select class="form-control" id="filtroAlmacen">
                                    <option value="">Todos los almacenes</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="form-group">
                                <label>Situación</label>
                                <select class="form-control" id="filtroSituacion">
                                    <option value="">Todas</option>
                                    <option value="NORMAL">Normal</option>
                                    <option value="BAJO_MINIMO">Bajo el mínimo</option>
                                    <option value="SIN_STOCK">Sin stock</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-3" style="padding-top: 32px;">
                            <button type="button" class="btn btn-primary" id="btnFiltrar"><i class="fas fa-search"></i> Buscar</button>
                            <button type="button" class="btn btn-outline-secondary" id="btnLimpiar"><i class="fas fa-eraser"></i> Limpiar</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Listado -->
            <div class="card">
                <div class="card-header">
                    <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                    <button class="btn btn-warning ml-1" onclick="window.open('StockServlet?tipo=imprimirReponer', '_blank')" title="Insumos en o por debajo del stock mínimo">
                        <i class="fas fa-file-pdf"></i> Insumos a reponer
                    </button>
                    <button class="btn btn-success ml-1" id="btnNuevoStock" style="display:none" onclick="abrirAltaStock()">
                        <i class="fas fa-plus"></i> Nuevo registro
                    </button>
                    <small class="text-muted ml-2"><i class="fas fa-mouse-pointer"></i> Haga clic en un registro para ver su ficha.</small>
                </div>
                <div class="card-body">
                    <table id="tablaStock" class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>Código</th>
                                <th>Descripción</th>
                                <th>Centro</th>
                                <th>Almacén</th>
                                <th>Stock actual</th>
                                <th>Mínimo</th>
                                <th>Situación</th>
                                <th>Imprimir</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal ficha del stock -->
<div class="modal fade" id="modalFichaStock" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="tituloFicha"><i class="fas fa-boxes"></i> Ficha de stock</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div class="mb-3" id="accionesFicha"></div>
                <div id="fichaStock"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal alta de registro de stock -->
<div class="modal fade" id="modalAltaStock" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="fas fa-plus"></i> Nuevo registro de stock</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <p class="text-muted small mb-3">
                    Habilita un insumo en un almacén con su stock mínimo. El registro nace con stock 0:
                    la cantidad solo cambia con recepciones de OC, salidas, transferencias y ajustes.
                </p>
                <div class="form-group">
                    <label for="altaInsumo">Insumo</label>
                    <select class="form-control" id="altaInsumo"></select>
                </div>
                <div class="form-group">
                    <label for="altaAlmacen">Almacén</label>
                    <select class="form-control" id="altaAlmacen"></select>
                </div>
                <div class="form-group">
                    <label for="altaMinimo">Stock mínimo</label>
                    <input type="number" class="form-control" id="altaMinimo" min="0" step="1" value="0">
                </div>
                <div id="altaMensaje"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-success" id="btnGuardarAlta"><i class="fas fa-save"></i> Guardar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal modificar stock mínimo -->
<div class="modal fade" id="modalMinimoStock" tabindex="-1">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="fas fa-flag"></i> Modificar mínimo</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <p class="mb-2" id="minimoTitulo"></p>
                <div class="form-group mb-2">
                    <label for="nuevoMinimo">Nuevo stock mínimo</label>
                    <input type="number" class="form-control" id="nuevoMinimo" min="0" step="1">
                </div>
                <div id="minimoMensaje"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-primary" id="btnGuardarMinimo"><i class="fas fa-save"></i> Guardar</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var URL_STOCK = "StockServlet";
var dataTableStock;
var fichaActual = null;

var SITUACIONES = {
    NORMAL:      ['Normal', 'badge-success', 'bg-success'],
    BAJO_MINIMO: ['Bajo el mínimo', 'badge-warning', 'bg-warning'],
    SIN_STOCK:   ['Sin stock', 'badge-danger', 'bg-danger']
};

function badgeSituacion(s) {
    var e = SITUACIONES[s] || [s, 'badge-light', 'bg-secondary'];
    return '<span class="badge ' + e[1] + '">' + escHtml(e[0]) + '</span>';
}

function situacionDe(actual, minimo) {
    if (actual <= 0) return 'SIN_STOCK';
    if (actual <= minimo) return 'BAJO_MINIMO';
    return 'NORMAL';
}

// Barra de nivel: el mínimo equivale al 50% de la barra
function barraNivel(actual, minimo, situacion) {
    var tope = Math.max(minimo * 2, actual, 1);
    var pct = Math.min(100, Math.round(actual * 100 / tope));
    var color = (SITUACIONES[situacion] || ['', '', 'bg-secondary'])[2];
    return '<div class="progress nivel-stock" title="Mínimo: ' + escHtml(minimo) + '">' +
           '<div class="progress-bar ' + color + '" style="width:' + pct + '%"></div></div>';
}

function formatoGs(valor) {
    if (valor === '' || valor === null || valor === undefined) return '';
    var n = Math.round(parseFloat(valor));
    if (isNaN(n)) return '';
    return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

function fechaLegible(iso) {
    if (!iso) return '';
    var p = iso.substring(0, 10).split('-');
    return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] : iso;
}

$(document).ready(function () {
    dataTableStock = $('#tablaStock').DataTable({
        responsive: true,
        lengthChange: false,
        autoWidth: false,
        pageLength: 25,
        order: [],
        columnDefs: [{ orderable: false, targets: -1 }],
        language: {
            info: 'Mostrando _START_ a _END_ de _TOTAL_ registros de stock',
            emptyTable: 'No hay stock para mostrar',
            zeroRecords: 'No se encontraron registros de stock'
        }
    });

    cargarFiltros();
    refrescar();

    $('#btnFiltrar').click(cargarTablaStock);
    $('#filtroAlmacen, #filtroSituacion').change(cargarTablaStock);
    $('#filtroCentro').change(function () {
        cargarAlmacenesFiltro($(this).val());
        cargarTablaStock();
    });
    $('#btnLimpiar').click(function () {
        $('#filtroCentro').val('');
        $('#filtroSituacion').val('');
        cargarAlmacenesFiltro('');
        cargarTablaStock();
    });

    // Clic en la fila abre la ficha (salvo que se haga clic en un botón de acción)
    $('#tablaStock tbody').on('click', 'tr', function (ev) {
        if ($(ev.target).closest('button').length) return;
        var id = $(this).find('.fila-stock').data('id');
        if (id) verFicha(id);
    });

    // Botones de la ficha
    $('#accionesFicha').on('click', '.accion-ficha', function () {
        var accion = $(this).data('accion');
        if (accion === 'imprimir') imprimirStock(fichaActual.id);
        else if (accion === 'minimo') abrirMinimo();
        else if (accion === 'eliminar') eliminarStock();
    });

    if (PERMISOS.STOCK_EDITAR) $('#btnNuevoStock').show();
    $('#btnGuardarAlta').click(guardarAlta);
    $('#btnGuardarMinimo').click(guardarMinimo);

});

function cargarFiltros() {
    $.post(URL_STOCK, { tipo: 'listarCentros' }, function (html) { $('#filtroCentro').html(html); });
    cargarAlmacenesFiltro('');
}

function cargarAlmacenesFiltro(idCentro) {
    $.post(URL_STOCK, { tipo: 'listarAlmacenesFiltro', id_centro: idCentro || '' }, function (html) {
        $('#filtroAlmacen').html(html);
    });
}

function refrescar() {
    cargarResumen();
    cargarTablaStock();
}

function cargarResumen() {
    $.ajax({
        type: 'post', url: URL_STOCK, data: { tipo: 'resumen' }, dataType: 'json',
        success: function (r) {
            $('#resRegistros').text(r.registros || 0);
            $('#resUnidades').text(formatoGs(r.unidades) || '0');
            $('#resBajo').text(r.bajo_minimo || 0);
            $('#resSinStock').text(r.sin_stock || 0);
            $('#resAlmacenes').text(r.almacenes || 0);
        }
    });
}

function cargarTablaStock() {
    $.ajax({
        type: "post",
        url: URL_STOCK,
        data: {
            tipo: "listarJson",
            id_centro: $('#filtroCentro').val(),
            id_almacen: $('#filtroAlmacen').val(),
            situacion: $('#filtroSituacion').val()
        },
        dataType: 'json',
        success: function (lista) {
            dataTableStock.clear();
            $.each(lista, function (i, s) {
                var id = parseInt(s.id, 10);
                var acciones =
                    '<span class="fila-stock" data-id="' + id + '"></span>' +
                    '<button class="btn btn-info btn-sm" onclick="imprimirStock(' + id + ')" title="Imprimir"><i class="fa fa-print"></i></button>';
                dataTableStock.row.add([
                    escHtml(s.codigo),
                    escHtml(s.descripcion),
                    escHtml(s.centro),
                    escHtml(s.almacen),
                    '<strong>' + escHtml(s.actual) + '</strong> ' + escHtml(s.unidad) + barraNivel(s.actual, s.minimo, s.situacion),
                    escHtml(s.minimo),
                    badgeSituacion(s.situacion),
                    acciones
                ]);
            });
            dataTableStock.draw();
        },
        error: function (xhr) {
            alert('Error al cargar el stock: ' + $('<div>').html(xhr.responseText).text());
        }
    });
}

function filaFicha(etiqueta, valor) {
    return '<tr><th style="width:38%">' + escHtml(etiqueta) + '</th><td>' + (valor === '' ? '<span class="text-muted">-</span>' : valor) + '</td></tr>';
}

function infoBox(icono, color, texto, valor) {
    return '<div class="col-md-3 col-sm-6"><div class="info-box shadow-sm">' +
        '<span class="info-box-icon ' + color + '"><i class="fas ' + icono + '"></i></span>' +
        '<div class="info-box-content"><span class="info-box-text">' + escHtml(texto) + '</span>' +
        '<span class="info-box-number">' + valor + '</span></div></div></div>';
}

function verFicha(id) {
    $.ajax({
        type: 'post', url: URL_STOCK, data: { tipo: 'detalle', pk: id }, dataType: 'json',
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            fichaActual = f;
            dibujarFicha(f);
            $('#modalFichaStock').modal('show');
        },
        error: function () { alert('Error al obtener la ficha de stock.'); }
    });
}

function dibujarFicha(f) {
    var sit = situacionDe(f.actual, f.minimo);
    var colorSit = (SITUACIONES[sit] || ['', '', 'bg-secondary'])[2];
    var unidad = f.abreviatura ? ' ' + escHtml(f.abreviatura) : '';

    $('#tituloFicha').html('<i class="fas fa-boxes"></i> ' + escHtml(f.descripcion) +
        ' <small class="text-muted">- ' + escHtml(f.almacen) + '</small>');

    $('#accionesFicha').html(
        '<button type="button" class="btn btn-outline-secondary btn-sm mr-1 mb-1 accion-ficha" data-accion="imprimir"><i class="fas fa-print"></i> Imprimir (PDF)</button>' +
        (PERMISOS.STOCK_EDITAR
            ? '<button type="button" class="btn btn-outline-primary btn-sm mr-1 mb-1 accion-ficha" data-accion="minimo"><i class="fas fa-flag"></i> Modificar mínimo</button>' +
              (f.actual == 0 ? '<button type="button" class="btn btn-outline-danger btn-sm mr-1 mb-1 accion-ficha" data-accion="eliminar"><i class="fas fa-trash"></i> Eliminar registro</button>' : '')
            : ''));

    var faltante = Math.max(0, f.minimo - f.actual);
    var html = '<div class="row">' +
        infoBox('fa-cubes', 'bg-info', 'Stock actual', escHtml(f.actual) + unidad) +
        infoBox('fa-flag', 'bg-secondary', 'Stock mínimo', escHtml(f.minimo) + unidad) +
        infoBox('fa-heartbeat', colorSit, 'Situación', badgeSituacion(sit) +
            (faltante > 0 ? ' <small class="text-muted">faltan ' + faltante + '</small>' : '')) +
        infoBox('fa-coins', 'bg-dark', 'Valor en stock', 'Gs. ' + formatoGs(f.valor)) +
        '</div>';

    html += '<div class="row"><div class="col-lg-5">' +
        '<h6 class="text-primary"><i class="fas fa-info-circle"></i> Insumo</h6>' +
        '<table class="table table-sm table-bordered">' +
        filaFicha('Código', escHtml(f.codigo)) +
        filaFicha('Descripción', escHtml(f.descripcion)) +
        filaFicha('Unidad de medida', escHtml(f.unidad) + (f.abreviatura ? ' <small class="text-muted">(' + escHtml(f.abreviatura) + ')</small>' : '')) +
        filaFicha('Precio de compra', 'Gs. ' + formatoGs(f.precio)) +
        '</table>' +
        '<h6 class="text-primary"><i class="fas fa-map-marker-alt"></i> Ubicación</h6>' +
        '<table class="table table-sm table-bordered">' +
        filaFicha('Centro', escHtml(f.centro)) +
        filaFicha('Almacén', escHtml(f.almacen)) +
        filaFicha('Ubicación física', escHtml(f.ubicacion)) +
        '</table>' +
        '<h6 class="text-primary"><i class="fas fa-chart-bar"></i> Nivel de stock</h6>' +
        barraNivel(f.actual, f.minimo, sit).replace('nivel-stock', 'nivel-stock mb-1" style="height:14px') +
        '<small class="text-muted">La marca del mínimo está en la mitad de la barra.</small>' +
        '<h6 class="text-primary mt-3"><i class="fas fa-warehouse"></i> El mismo insumo en otros almacenes</h6>' +
        dibujarOtros(f.otros || []) +
        '</div>' +
        '<div class="col-lg-7"><h6 class="text-primary"><i class="fas fa-history"></i> Ingresos por compras en este almacén</h6>' +
        '<div class="historial-scroll">' + dibujarIngresos(f.ingresos || [], unidad) + '</div></div></div>';

    $('#fichaStock').html(html);
}

function dibujarOtros(otros) {
    if (otros.length === 0) {
        return '<p class="text-muted">Este insumo no tiene stock registrado en otros almacenes.</p>';
    }
    var html = '<table class="table table-sm table-striped"><thead><tr><th>Centro</th><th>Almacén</th><th>Actual</th><th>Situación</th></tr></thead><tbody>';
    $.each(otros, function (i, o) {
        html += '<tr><td>' + escHtml(o.centro) + '</td><td>' + escHtml(o.almacen) + '</td><td>' + escHtml(o.actual) +
            '</td><td>' + badgeSituacion(situacionDe(o.actual, o.minimo)) + '</td></tr>';
    });
    return html + '</tbody></table>';
}

function dibujarIngresos(ingresos, unidad) {
    if (ingresos.length === 0) {
        return '<p class="text-muted">No hay ingresos por compras registrados en este almacén. ' +
               'Los ingresos aparecen al recibir una OC de este insumo en este almacén.</p>';
    }
    var html = '<div class="timeline">';
    var fechaAnterior = '';
    $.each(ingresos, function (i, g) {
        var dia = fechaLegible(g.fecha);
        if (dia !== fechaAnterior) {
            html += '<div class="time-label"><span class="bg-secondary">' + escHtml(dia) + '</span></div>';
            fechaAnterior = dia;
        }
        html += '<div><i class="fas fa-truck-loading bg-success"></i><div class="timeline-item">' +
            '<h3 class="timeline-header"><strong>+' + escHtml(g.recibido) + unidad + '</strong> recibidos - OC ' + escHtml(g.numero_oc) + '</h3>' +
            '<div class="timeline-body">' +
            '<p><i class="fas fa-truck text-muted"></i> Proveedor: ' + escHtml(g.proveedor) + '</p>' +
            '<p><i class="fas fa-clipboard-check text-muted"></i> Recibido ' + escHtml(g.recibido) + ' de ' + escHtml(g.pedido) + ' pedidos' +
            (g.recibido < g.pedido ? ' <span class="badge badge-warning">parcial</span>' : '') + '</p>' +
            '<p><i class="fas fa-tag text-muted"></i> Precio unitario: Gs. ' + formatoGs(g.precio) + '</p>' +
            '</div></div></div>';
    });
    return html + '<div><i class="fas fa-clock bg-gray"></i></div></div>';
}

// Impresión
// ---- Mantenimiento del registro de stock (permiso STOCK_EDITAR) ----

function mensajeEn(div, ok, texto) {
    $(div).html('<div class="alert alert-' + (ok ? 'success' : 'danger') + ' py-2 mb-0">' + escHtml(texto) + '</div>');
}

function llenarCombo(sel, lista, primera) {
    var html = '<option value="">' + escHtml(primera) + '</option>';
    $.each(lista, function (i, o) { html += '<option value="' + o.id + '">' + escHtml(o.texto) + '</option>'; });
    $(sel).html(html);
}

function abrirAltaStock() {
    $('#altaMensaje').empty();
    $('#altaMinimo').val('0');
    $.ajax({
        type: 'post', url: URL_STOCK, data: { tipo: 'combosAlta' }, dataType: 'json',
        success: function (r) {
            llenarCombo('#altaInsumo', r.insumos, 'Seleccione un insumo');
            llenarCombo('#altaAlmacen', r.almacenes, 'Seleccione un almacén');
            $('#modalAltaStock').modal('show');
        },
        error: function () { alert('No se pudieron cargar los insumos y almacenes.'); }
    });
}

function guardarAlta() {
    $.ajax({
        type: 'post', url: URL_STOCK, dataType: 'json',
        data: { tipo: 'crear', id_insumo: $('#altaInsumo').val(), id_almacen: $('#altaAlmacen').val(), minimo: $('#altaMinimo').val() },
        success: function (r) {
            if (!r.ok) { mensajeEn('#altaMensaje', false, r.mensaje); return; }
            $('#modalAltaStock').modal('hide');
            refrescar();
            alert(r.mensaje);
        },
        error: function () { mensajeEn('#altaMensaje', false, 'Error al guardar el registro.'); }
    });
}

function abrirMinimo() {
    $('#minimoMensaje').empty();
    $('#minimoTitulo').html('<b>' + escHtml(fichaActual.descripcion) + '</b><br><small class="text-muted">' +
        escHtml(fichaActual.almacen) + ' - stock actual: ' + escHtml(fichaActual.actual) + '</small>');
    $('#nuevoMinimo').val(fichaActual.minimo);
    $('#modalMinimoStock').modal('show');
}

function guardarMinimo() {
    $.ajax({
        type: 'post', url: URL_STOCK, dataType: 'json',
        data: { tipo: 'modificarMinimo', id: fichaActual.id, minimo: $('#nuevoMinimo').val() },
        success: function (r) {
            if (!r.ok) { mensajeEn('#minimoMensaje', false, r.mensaje); return; }
            $('#modalMinimoStock').modal('hide');
            refrescar();
            verFicha(fichaActual.id);
        },
        error: function () { mensajeEn('#minimoMensaje', false, 'Error al guardar el mínimo.'); }
    });
}

function eliminarStock() {
    if (!confirmado('¿Eliminar el registro de "' + fichaActual.descripcion + '" en ' + fichaActual.almacen + '?\n' +
            'Solo se permite si está en 0 y nunca tuvo movimientos.', this, arguments)) return;
    $.ajax({
        type: 'post', url: URL_STOCK, dataType: 'json',
        data: { tipo: 'eliminar', id: fichaActual.id },
        success: function (r) {
            if (!r.ok) { alert(r.mensaje); return; }
            $('#modalFichaStock').modal('hide');
            refrescar();
            alert(r.mensaje);
        },
        error: function () { alert('Error al eliminar el registro.'); }
    });
}

function imprimirReporte() {
    window.open('StockServlet?tipo=imprimir', '_blank');
}

function imprimirStock(id) {
    window.open('ReporteIndividualServlet?reporte=stock&cod=' + encodeURIComponent(id), '_blank');
}

</script>
</body>
</html>
