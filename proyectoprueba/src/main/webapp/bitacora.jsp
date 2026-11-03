<%@include file="header.jsp" %>
<%-- SEGURIDAD > Bitácora (solo administrador, solo consulta): quién hizo qué, cuándo y desde qué IP. --%>
<style>
    #tablaBitacora tbody tr { cursor: pointer; }
    #tablaBitacora tbody tr:hover td { background-color: rgba(0, 123, 255, 0.08); }
    #tablaBitacora td { vertical-align: middle; }
    .badge-accion { font-size: .72rem; letter-spacing: .02em; }
    #tablaCambios td { word-break: break-word; }
    #tablaCambios .anterior { color: #8A1C1C; }
    #tablaCambios .nuevo { color: #1B5E20; }
</style>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-8">
                    <h1><i class="fas fa-history"></i> Bitácora del sistema</h1>
                </div>
                <div class="col-sm-4">
                    <ol class="breadcrumb float-sm-right">
                        <li class="breadcrumb-item"><a href="dashboard.jsp">Inicio</a></li>
                        <li class="breadcrumb-item active">Bitácora</li>
                    </ol>
                </div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">

            <!-- Resumen de hoy -->
            <div class="row">
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-info">
                        <div class="inner"><h3 id="resTotal">0</h3><p>Acciones de hoy</p></div>
                        <div class="icon"><i class="fas fa-list"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-success">
                        <div class="inner"><h3 id="resLogin">0</h3><p>Inicios de sesión</p></div>
                        <div class="icon"><i class="fas fa-sign-in-alt"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-danger" id="lnkFallidos" style="cursor:pointer;" title="Ver los inicios de sesión fallidos de hoy">
                        <div class="inner"><h3 id="resFallidos">0</h3><p>Inicios fallidos</p></div>
                        <div class="icon"><i class="fas fa-user-lock"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-6 col-6">
                    <div class="small-box bg-primary">
                        <div class="inner"><h3 id="resCambios">0</h3><p>Altas y modificaciones</p></div>
                        <div class="icon"><i class="fas fa-edit"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-6 col-12">
                    <div class="small-box bg-warning">
                        <div class="inner"><h3 id="resBajas">0</h3><p>Bajas y anulaciones</p></div>
                        <div class="icon"><i class="fas fa-ban"></i></div>
                    </div>
                </div>
            </div>

            <!-- Filtros -->
            <div class="card">
                <div class="card-header"><h3 class="card-title"><i class="fas fa-filter"></i> Filtros</h3></div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-2 form-group">
                            <label for="fDesde">Desde</label>
                            <input type="date" class="form-control" id="fDesde">
                        </div>
                        <div class="col-md-2 form-group">
                            <label for="fHasta">Hasta</label>
                            <input type="date" class="form-control" id="fHasta">
                        </div>
                        <div class="col-md-3 form-group">
                            <label for="fUsuario">Usuario</label>
                            <select class="form-control" id="fUsuario"><option value="">Todos</option></select>
                        </div>
                        <div class="col-md-2 form-group">
                            <label for="fModulo">Módulo</label>
                            <select class="form-control" id="fModulo">
                                <option value="">Todos</option>
                                <option value="SEGURIDAD">Seguridad</option>
                                <option value="REFERENCIALES">Referenciales</option>
                                <option value="COMPRAS">Compras</option>
                                <option value="OT">Órdenes de trabajo</option>
                                <option value="INVENTARIO">Inventario</option>
                                <option value="ACTIVOS">Activos fijos</option>
                            </select>
                        </div>
                        <div class="col-md-3 form-group">
                            <label for="fAccion">Acción</label>
                            <select class="form-control" id="fAccion">
                                <option value="">Todas</option>
                            </select>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 form-group">
                            <label for="fTexto">Buscar</label>
                            <input type="text" class="form-control" id="fTexto" maxlength="100" placeholder="Descripción, email, IP o valor modificado">
                        </div>
                        <div class="col-md-6 form-group" style="padding-top: 32px;">
                            <button type="button" class="btn btn-primary" id="btnBuscar"><i class="fas fa-search"></i> Buscar</button>
                            <button type="button" class="btn btn-outline-secondary" id="btnLimpiar"><i class="fas fa-eraser"></i> Limpiar</button>
                            <button type="button" class="btn btn-info" id="btnPdf"><i class="fas fa-file-pdf"></i> PDF</button>
                        </div>
                    </div>
                    <div id="avisoRegistro" class="alert alert-info py-2 mb-0" style="display:none;">
                        <i class="fas fa-history"></i> Historial de <b id="avisoRegistroTexto"></b>
                        <a href="bitacora.jsp" class="alert-link ml-2">Ver toda la bitácora</a>
                    </div>
                </div>
            </div>

            <!-- Listado -->
            <div class="card">
                <div class="card-header">
                    <small class="text-muted"><i class="fas fa-mouse-pointer"></i> Haga clic en un registro para ver el detalle.
                        Solo consulta: la bitácora no se puede modificar ni borrar.</small>
                    <span class="badge badge-warning ml-2" id="avisoLimite" style="display:none;"></span>
                </div>
                <div class="card-body">
                    <table id="tablaBitacora" class="table table-bordered table-striped" style="width:100%">
                        <thead>
                            <tr><th>Fecha</th><th>Usuario</th><th>IP</th><th>Módulo</th><th>Acción</th><th>Descripción</th><th>orden</th></tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Ficha de un registro -->
<div class="modal fade" id="modalDetalle" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="detTitulo"></h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div class="row" id="detDatos"></div>
                <div id="detCambios"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-primary mr-auto" id="btnHistorialRegistro"><i class="fas fa-history"></i> Ver todo el historial de este registro</button>
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>
<script>
var URL_BIT = 'BitacoraServlet', tablaBit, detalleActual = null, registroFiltro = { tabla: '', id: 0 };
var ACCIONES = {
    LOGIN: ['Inicio de sesión', 'badge-secondary'], LOGIN_FALLIDO: ['Inicio fallido', 'badge-danger'], LOGOUT: ['Cierre de sesión', 'badge-secondary'],
    ALTA: ['Alta', 'badge-success'], MODIFICACION: ['Modificación', 'badge-primary'], BAJA: ['Baja', 'badge-danger'],
    ANULACION: ['Anulación', 'badge-danger'], APROBACION: ['Aprobación', 'badge-success'], RECHAZO: ['Rechazo', 'badge-warning'],
    FINALIZACION: ['Finalización', 'badge-info'], REAPERTURA: ['Reapertura', 'badge-warning'], RECEPCION: ['Recepción', 'badge-info']
};
var MODULOS = { SEGURIDAD: 'Seguridad', REFERENCIALES: 'Referenciales', COMPRAS: 'Compras', OT: 'Órdenes de trabajo', INVENTARIO: 'Inventario', ACTIVOS: 'Activos fijos' };

function badgeAccion(a) {
    var x = ACCIONES[a] || [a, 'badge-light'];
    return '<span class="badge badge-accion ' + x[1] + '">' + escHtml(x[0]) + '</span>';
}
function hoyIso() {
    var d = new Date(), m = d.getMonth() + 1, dia = d.getDate();
    return d.getFullYear() + '-' + (m < 10 ? '0' : '') + m + '-' + (dia < 10 ? '0' : '') + dia;
}
function filtros() {
    return { desde: $('#fDesde').val(), hasta: $('#fHasta').val(), id_usuario: $('#fUsuario').val(), modulo: $('#fModulo').val(),
             accion: $('#fAccion').val(), texto: $.trim($('#fTexto').val()), tabla: registroFiltro.tabla, id_registro: registroFiltro.id };
}

function cargarResumen() {
    $.post(URL_BIT, { tipo: 'resumen' }, function (r) {
        if (!r || r.error) return;
        $('#resTotal').text(r.TOTAL); $('#resLogin').text(r.LOGIN); $('#resFallidos').text(r.LOGIN_FALLIDO);
        $('#resCambios').text(r.CAMBIOS); $('#resBajas').text(r.BAJAS);
    }, 'json');
}

function cargar() {
    var f = filtros();
    if (f.desde && f.hasta && f.desde > f.hasta) { alert('La fecha desde no puede ser posterior a la fecha hasta.'); return; }
    $.post(URL_BIT, $.extend({ tipo: 'listar' }, f), function (r) {
        if (!r || r.error) { alert(r && r.error ? r.error : 'Error al consultar la bitácora.'); return; }
        tablaBit.clear();
        $.each(r.registros, function (i, b) {
            tablaBit.row.add([
                '<span class="fila-bit" data-id="' + b.id + '">' + escHtml(b.fecha) + '</span>',
                escHtml(b.usuario), escHtml(b.ip), escHtml(MODULOS[b.modulo] || b.modulo), badgeAccion(b.accion),
                escHtml(b.descripcion) + (b.cambios > 0 ? ' <span class="badge badge-light" title="Campos modificados">' + b.cambios + (b.cambios === 1 ? ' campo' : ' campos') + '</span>' : ''),
                b.orden
            ]);
        });
        tablaBit.draw();
        // Historial abierto desde un listado: se muestra el nombre del registro en lugar de "tabla #id"
        if (registroFiltro.id && /#\d+$/.test($('#avisoRegistroTexto').text()) && r.registros.length) {
            $('#avisoRegistroTexto').text(r.registros[0].descripcion.replace(/ · Motivo:.*$/, ''));
        }
        if (r.registros.length >= r.limite) {
            $('#avisoLimite').text('Se muestran los ' + r.limite + ' más recientes: acote las fechas para ver otros.').show();
        } else {
            $('#avisoLimite').hide();
        }
    }, 'json').fail(function () { alert('Error al consultar la bitácora.'); });
}

function verDetalle(id) {
    $.post(URL_BIT, { tipo: 'detalle', id: id }, function (d) {
        if (!d || d.error) { alert(d && d.error ? d.error : 'Error al obtener el registro.'); return; }
        detalleActual = d;
        $('#detTitulo').html(badgeAccion(d.accion) + ' ' + escHtml(d.descripcion));
        $('#detDatos').html(
            '<div class="col-md-6"><b>Usuario:</b> ' + escHtml(d.usuario) + (d.email && d.email !== d.usuario ? ' <small class="text-muted">(' + escHtml(d.email) + ')</small>' : '') + '</div>' +
            '<div class="col-md-6"><b>Fecha:</b> ' + escHtml(d.fecha) + '</div>' +
            '<div class="col-md-6"><b>IP:</b> ' + escHtml(d.ip) + '</div>' +
            '<div class="col-md-6"><b>Módulo:</b> ' + escHtml(MODULOS[d.modulo] || d.modulo) + '</div>' +
            (d.tabla ? '<div class="col-md-12"><b>Registro:</b> ' + escHtml(d.tabla) + (d.id_registro ? ' #' + d.id_registro : '') + '</div>' : ''));
        if (d.cambios.length) {
            var h = '<h6 class="mt-3"><i class="fas fa-exchange-alt"></i> Campos modificados</h6>' +
                '<table class="table table-sm table-bordered" id="tablaCambios"><thead><tr><th>Campo</th><th>Valor anterior</th><th>Valor nuevo</th></tr></thead><tbody>';
            $.each(d.cambios, function (i, c) {
                h += '<tr><td>' + escHtml(c.campo) + '</td><td class="anterior">' + escHtml(c.anterior) + '</td><td class="nuevo">' + escHtml(c.nuevo) + '</td></tr>';
            });
            $('#detCambios').html(h + '</tbody></table>');
        } else {
            $('#detCambios').html('');
        }
        $('#btnHistorialRegistro').toggle(!!(d.tabla && d.id_registro));
        $('#modalDetalle').modal('show');
    }, 'json');
}

function filtrarRegistro(tabla, id, texto) {
    registroFiltro = { tabla: tabla, id: id };
    $('#fDesde, #fHasta, #fTexto').val(''); $('#fUsuario, #fModulo, #fAccion').val('');
    $('#avisoRegistroTexto').text(texto || (tabla + ' #' + id));
    $('#avisoRegistro').show();
    cargar();
}

$(document).ready(function () {
    $.each(ACCIONES, function (k, v) { $('#fAccion').append($('<option>').val(k).text(v[0])); });
    $.post(URL_BIT, { tipo: 'usuarios' }, function (l) {
        if (!$.isArray(l)) return;
        $.each(l, function (i, u) { $('#fUsuario').append($('<option>').val(u.id).text(u.texto)); });
    }, 'json');

    tablaBit = $('#tablaBitacora').DataTable({
        responsive: true, lengthChange: false, autoWidth: false, pageLength: 25, order: [[6, 'desc']],
        // La fecha se ordena por la columna oculta "orden" (milisegundos)
        columnDefs: [{ targets: 6, visible: false, searchable: false }, { targets: 0, orderData: [6] }],
        dom: 'Bfrtip',
        buttons: [{ extend: 'excelHtml5', text: '<i class="fas fa-file-excel"></i> Excel', className: 'btn-success btn-sm', title: 'Bitacora',
                    exportOptions: { columns: ':visible' } }],
        language: { emptyTable: 'No hay registros con esos filtros', info: 'Mostrando _START_ a _END_ de _TOTAL_ registros' }
    });
    $('#tablaBitacora tbody').on('click', 'tr', function () {
        var id = $(this).find('.fila-bit').data('id');
        if (id) verDetalle(id);
    });

    // Filtros que vienen en la dirección: historial de un registro o inicios fallidos de hoy
    var q = new URLSearchParams(location.search);
    if (q.get('accion')) $('#fAccion').val(q.get('accion'));
    if (q.get('hoy') === '1') { $('#fDesde').val(hoyIso()); $('#fHasta').val(hoyIso()); }
    var tablaUrl = q.get('tabla'), idUrl = parseInt(q.get('id'), 10);
    if (tablaUrl && idUrl > 0) {
        filtrarRegistro(tablaUrl, idUrl, q.get('nombre') || '');
    } else {
        if (!q.get('hoy')) {   // por defecto: últimos 7 días
            var d = new Date(); d.setDate(d.getDate() - 7);
            var m = d.getMonth() + 1, dia = d.getDate();
            $('#fDesde').val(d.getFullYear() + '-' + (m < 10 ? '0' : '') + m + '-' + (dia < 10 ? '0' : '') + dia);
        }
        cargar();
    }
    cargarResumen();

    $('#btnBuscar').click(cargar);
    $('#fTexto').on('keydown', function (e) { if (e.key === 'Enter') cargar(); });
    $('#fUsuario, #fModulo, #fAccion').change(cargar);
    $('#btnLimpiar').click(function () {
        registroFiltro = { tabla: '', id: 0 }; $('#avisoRegistro').hide();
        $('#fDesde, #fHasta, #fTexto').val(''); $('#fUsuario, #fModulo, #fAccion').val('');
        if (history.replaceState) history.replaceState(null, '', 'bitacora.jsp');
        cargar();
    });
    $('#btnPdf').click(function () { window.open(URL_BIT + '?' + $.param($.extend({ tipo: 'imprimir' }, filtros())), '_blank'); });
    $('#lnkFallidos').click(function (e) {
        e.preventDefault();
        registroFiltro = { tabla: '', id: 0 }; $('#avisoRegistro').hide();
        $('#fDesde').val(hoyIso()); $('#fHasta').val(hoyIso()); $('#fUsuario, #fModulo').val(''); $('#fTexto').val('');
        $('#fAccion').val('LOGIN_FALLIDO'); cargar();
    });
    $('#btnHistorialRegistro').click(function () {
        if (!detalleActual) return;
        $('#modalDetalle').modal('hide');
        filtrarRegistro(detalleActual.tabla, detalleActual.id_registro, detalleActual.descripcion);
    });
});
</script>
</body>
</html>
