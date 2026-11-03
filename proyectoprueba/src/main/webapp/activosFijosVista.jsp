<%@include file="header.jsp" %>
<%-- INVENTARIOS > Activos fijos: inventario por centro de costo, ficha con historial,
     responsable (opcional), cambio de centro de costo e impresión del listado (JRXML) --%>
<style>
    #tablaActivos tbody tr { cursor: pointer; }
    #tablaActivos tbody tr:hover td { background-color: rgba(0, 123, 255, 0.08); }
    .timeline-body p { margin-bottom: 2px; }
    .historial-scroll { max-height: 560px; overflow-y: auto; padding-right: 6px; }
    .info-box .info-box-text { white-space: normal; }
    /* Formulario de movimiento encima de la ficha (modal sobre modal) */
    #modalMovimiento { z-index: 1060; }
</style>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-8">
                    <h1><i class="fas fa-laptop"></i> Listar Activos Fijos</h1>
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
                        <div class="inner"><h3 id="resTotal">0</h3><p>Activos en inventario</p></div>
                        <div class="icon"><i class="fas fa-laptop"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-success">
                        <div class="inner"><h3 id="resDisponible">0</h3><p>Disponibles</p></div>
                        <div class="icon"><i class="fas fa-check-circle"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-primary">
                        <div class="inner"><h3 id="resAsignado">0</h3><p>Con responsable</p></div>
                        <div class="icon"><i class="fas fa-user-check"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-warning">
                        <div class="inner"><h3 id="resMovimiento">0</h3><p>En reparación / tránsito</p></div>
                        <div class="icon"><i class="fas fa-tools"></i></div>
                    </div>
                </div>
                <div class="col-lg col-md-4 col-6">
                    <div class="small-box bg-secondary">
                        <div class="inner"><h3 id="resSolicitado">0</h3><p>Pedidos por recibir</p></div>
                        <div class="icon"><i class="fas fa-truck"></i></div>
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
                                <label>Centro de costo</label>
                                <select class="form-control" id="filtroCentroCosto">
                                    <option value="">Todos los centros de costo</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-2">
                            <div class="form-group">
                                <label>Estado</label>
                                <select class="form-control" id="filtroEstado">
                                    <option value="">Todos</option>
                                    <option value="DISPONIBLE">Disponible</option>
                                    <option value="ASIGNADO">Con responsable</option>
                                    <option value="EN_REPARACION">En reparación</option>
                                    <option value="EN_TRANSITO">En tránsito</option>
                                    <option value="BAJA">De baja</option>
                                    <option value="SOLICITADO">Pedido (por recibir)</option>
                                    <option value="ANULADO">Anulado</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-2" style="padding-top: 32px;">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="filtroPendientes">
                                <label class="custom-control-label" for="filtroPendientes">Incluir pedidos y anulados</label>
                            </div>
                        </div>
                        <div class="col-md-2" style="padding-top: 25px;">
                            <button type="button" class="btn btn-primary btn-block" id="btnFiltrar"><i class="fas fa-search"></i> Buscar</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Listado -->
            <div class="card">
                <div class="card-header">
                    <button class="btn btn-primary" id="btnImprimirListado">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                    <small class="text-muted ml-2"><i class="fas fa-mouse-pointer"></i> Haga clic en un activo para ver su ficha e historial completo.</small>
                </div>
                <div class="card-body">
                    <table id="tablaActivos" class="table table-bordered table-striped">
                        <thead>
                            <tr>
                                <th>N° activo</th>
                                <th>N° de serie</th>
                                <th>Código</th>
                                <th>Descripción</th>
                                <th>Estado</th>
                                <th>Centro</th>
                                <th>Centro de costo</th>
                                <th>Responsable</th>
                                <th>Fecha ingreso</th>
                                <th>Ver</th>
                            </tr>
                        </thead>
                        <tbody></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal ficha + historial -->
<div class="modal fade" id="modalActivo" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="tituloFicha"><i class="fas fa-laptop"></i> Ficha del activo</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div class="mb-3" id="accionesActivo"></div>
                <div id="fichaActivo"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
            </div>
        </div>
    </div>
</div>

<!-- Modal registrar movimiento -->
<div class="modal fade" id="modalMovimiento" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="tituloMovimiento">Registrar movimiento</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <form id="formMovimiento" onsubmit="return false;">
                    <input type="hidden" id="movAccion">
                    <p class="text-muted" id="ayudaMovimiento"></p>

                    <div class="form-group grp grp-cc">
                        <label>Nuevo centro de costo *</label>
                        <select class="form-control" id="movCentroCosto"></select>
                    </div>
                    <div class="form-group grp grp-usuario">
                        <label>Responsable (usuario del sistema)</label>
                        <select class="form-control" id="movUsuario"></select>
                    </div>
                    <div class="grp grp-persona">
                        <label>Persona externa (nombre, apellido y CI)</label>
                        <div class="form-row">
                            <div class="col-md-4 form-group"><input type="text" class="form-control" id="movPersonaNombre" placeholder="Nombre" maxlength="100"></div>
                            <div class="col-md-4 form-group"><input type="text" class="form-control" id="movPersonaApellido" placeholder="Apellido" maxlength="100"></div>
                            <div class="col-md-4 form-group"><input type="text" class="form-control" id="movPersonaCi" placeholder="CI" maxlength="20"></div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Observaciones</label>
                        <textarea class="form-control" id="movObservaciones" rows="2" maxlength="2000"></textarea>
                    </div>
                </form>
                <div id="mensajeMovimiento"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                <button class="btn btn-primary" id="btnGuardarMovimiento"><i class="fas fa-save"></i> Registrar</button>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var URL_ACTIVOS = "ActivoServlet";
var tablaActivos;
var activoActual = null;   // ficha abierta

var ETIQUETAS_ESTADO = {
    DISPONIBLE:    ['Disponible', 'badge-success'],
    ASIGNADO:      ['Con responsable', 'badge-primary'],
    EN_TRANSITO:   ['En tránsito', 'badge-warning'],
    EN_REPARACION: ['En reparación', 'badge-warning'],
    BAJA:          ['De baja', 'badge-dark'],
    SOLICITADO:    ['Pedido', 'badge-secondary'],
    ANULADO:       ['Anulado', 'badge-danger']
};

// Tipo de movimiento: [título, ícono, color]
var TIPOS_MOV = {
    INGRESO:            ['Ingreso por compra', 'fa-box-open', 'bg-success'],
    ASIGNACION:         ['Asignación de responsable', 'fa-user-check', 'bg-indigo'],
    DEVOLUCION:         ['Devolución (sin responsable)', 'fa-undo', 'bg-teal'],
    REUBICACION:        ['Cambio de centro de costo', 'fa-exchange-alt', 'bg-primary'],
    ENVIO:              ['Envío a otro centro', 'fa-truck', 'bg-primary'],
    RECEPCION:          ['Recepción en centro destino', 'fa-check', 'bg-info'],
    RECHAZO:            ['Rechazo del envío', 'fa-times', 'bg-danger'],
    REPARACION:         ['Envío a reparación', 'fa-tools', 'bg-warning'],
    RETORNO_REPARACION: ['Retorno de reparación', 'fa-wrench', 'bg-olive'],
    ANULACION_SALIDA:   ['Salida anulada', 'fa-undo', 'bg-secondary'],
    ANULACION_RECEPCION: ['Recepción de OC anulada', 'fa-undo', 'bg-danger'],
    BAJA:               ['Baja del activo', 'fa-ban', 'bg-gray-dark']
};

// Botones disponibles según el estado del activo
var ACCIONES = {
    DISPONIBLE: ['ASIGNACION', 'REUBICACION'],
    ASIGNADO:   ['DEVOLUCION', 'REUBICACION']
};
var BOTONES = {
    ASIGNACION:  ['Asignar responsable', 'btn-indigo bg-indigo', 'fa-user-check'],
    DEVOLUCION:  ['Quitar responsable (devolución)', 'btn-success', 'fa-undo'],
    REUBICACION: ['Cambiar centro de costo', 'btn-primary', 'fa-exchange-alt']
};

function badgeEstado(estado) {
    var e = ETIQUETAS_ESTADO[estado] || [estado, 'badge-light'];
    return '<span class="badge ' + e[1] + '">' + escHtml(e[0]) + '</span>';
}

function formatoGs(valor) {
    if (valor === '' || valor === null || valor === undefined) return '';
    var n = Math.round(parseFloat(valor));
    if (isNaN(n)) return '';
    return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

function fechaLegible(iso) {   // "2026-09-23 14:05" -> "23/09/2026"
    if (!iso) return '';
    var p = iso.substring(0, 10).split('-');
    return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] : iso;
}

function diasDesde(iso) {
    if (!iso) return null;
    var d = new Date(iso.substring(0, 10) + 'T00:00:00');
    if (isNaN(d.getTime())) return null;
    var hoy = new Date(); hoy.setHours(0, 0, 0, 0);
    return Math.max(0, Math.round((hoy - d) / 86400000));
}

function lugar(centro, sub) {
    if (!centro && !sub) return '';
    return escHtml(centro) + (sub ? ' <small class="text-muted">(' + escHtml(sub) + ')</small>' : '');
}

function persona(nombre, apellido, ci) {
    var t = $.trim((nombre || '') + ' ' + (apellido || ''));
    if (!t) return '';
    return escHtml(t) + (ci ? ' <small class="text-muted">CI ' + escHtml(ci) + '</small>' : '');
}

$(document).ready(function () {
    tablaActivos = $('#tablaActivos').DataTable({
        responsive: true,
        lengthChange: false,
        autoWidth: false,
        pageLength: 25,
        order: [],
        columnDefs: [{ orderable: false, targets: -1 }],
        language: {
            info: 'Mostrando _START_ a _END_ de _TOTAL_ activos',
            infoEmpty: 'Sin activos',
            emptyTable: 'No hay activos para mostrar',
            zeroRecords: 'No se encontraron activos'
        }
    });

    cargarCentros();
    cargarCentrosCostoFiltro('');
    cargarResumen();
    cargarActivos();

    $('#btnFiltrar').click(cargarActivos);
    $('#filtroCentroCosto, #filtroEstado, #filtroPendientes').change(cargarActivos);
    $('#filtroCentro').change(function () {
        cargarCentrosCostoFiltro($(this).val());
        cargarActivos();
    });
    $('#filtroEstado').change(function () {
        var v = $(this).val();
        if (v === 'SOLICITADO' || v === 'ANULADO') {
            $('#filtroPendientes').prop('checked', true);
        }
    });

    // Imprimir listado (JRXML) con los filtros elegidos
    $('#btnImprimirListado').click(function () {
        var q = $.param({
            tipo: 'imprimir',
            id_centro: $('#filtroCentro').val() || '',
            id_centro_costo: $('#filtroCentroCosto').val() || '',
            estado: $('#filtroEstado').val() || '',
            pendientes: $('#filtroPendientes').is(':checked') ? 'true' : 'false'
        });
        window.open(URL_ACTIVOS + '?' + q, '_blank');
    });

    // Clic en cualquier parte de la fila (o en el botón) abre la ficha
    $('#tablaActivos tbody').on('click', 'tr', function () {
        var id = $(this).find('.ver-activo').data('id');
        if (id) verActivo(id);
    });

    $('#accionesActivo').on('click', '.accion-mov', function () {
        abrirMovimiento($(this).data('accion'));
    });
    $('#accionesActivo').on('click', '#btnHojaVida', function () {
        window.open('ReporteIndividualServlet?reporte=hojaVida&cod=' + encodeURIComponent(activoActual.id), '_blank');
    });

    $('#movUsuario').change(function () {
        // Si elige un usuario del sistema no hacen falta los datos de persona externa
        $('.grp-persona').toggle(!$(this).val());
    });

    $('#btnGuardarMovimiento').click(guardarMovimiento);

    // Modal sobre modal: el fondo del segundo va entre las dos ventanas y,
    // al cerrarlo, la ficha conserva el scroll
    $('#modalMovimiento').on('shown.bs.modal', function () {
        $('.modal-backdrop').last().css('z-index', 1055);
    });
    $('#modalMovimiento').on('hidden.bs.modal', function () {
        if ($('#modalActivo').hasClass('show')) {
            $('body').addClass('modal-open');
        }
    });
});

function cargarCentros() {
    $.post(URL_ACTIVOS, { tipo: 'listarCentros' }, function (html) { $('#filtroCentro').html(html); });
}

function cargarCentrosCostoFiltro(idCentro) {
    $.post(URL_ACTIVOS, { tipo: 'listarCentrosCosto', filtro: 'true', id_centro: idCentro || '' }, function (html) {
        $('#filtroCentroCosto').html(html);
    });
}

function cargarResumen() {
    $.ajax({
        type: 'post', url: URL_ACTIVOS, data: { tipo: 'resumen' }, dataType: 'json',
        success: function (r) {
            $('#resTotal').text(r.TOTAL || 0);
            $('#resDisponible').text(r.DISPONIBLE || 0);
            $('#resAsignado').text(r.ASIGNADO || 0);
            $('#resMovimiento').text((r.EN_REPARACION || 0) + (r.EN_TRANSITO || 0));
            $('#resSolicitado').text(r.SOLICITADO || 0);
        }
    });
}

function cargarActivos() {
    $.ajax({
        type: 'post',
        url: URL_ACTIVOS,
        data: {
            tipo: 'listar',
            id_centro: $('#filtroCentro').val(),
            id_centro_costo: $('#filtroCentroCosto').val(),
            estado: $('#filtroEstado').val(),
            pendientes: $('#filtroPendientes').is(':checked') ? 'true' : 'false'
        },
        dataType: 'json',
        success: function (lista) {
            tablaActivos.clear();
            $.each(lista, function (i, a) {
                tablaActivos.row.add([
                    a.numero_activo ? '<strong>' + escHtml(a.numero_activo) + '</strong>' : '<span class="text-muted">Pendiente</span>',
                    a.numero_serie ? escHtml(a.numero_serie) : '<span class="text-muted">-</span>',
                    escHtml(a.codigo),
                    escHtml(a.descripcion),
                    badgeEstado(a.estado) + (a.situacion ? '<br><small class="text-muted">' + escHtml(a.situacion) + '</small>' : ''),
                    escHtml(a.centro),
                    a.centro_costo ? escHtml(a.centro_costo) : '<span class="text-muted">Sin asignar</span>',
                    a.responsable ? escHtml(a.responsable) : '<span class="text-muted">-</span>',
                    escHtml(fechaLegible(a.fecha_ingreso)),
                    '<button type="button" class="btn btn-info btn-sm ver-activo" data-id="' + escHtml(a.id) + '" title="Ver ficha"><i class="fas fa-eye"></i></button>'
                ]);
            });
            tablaActivos.draw();
        },
        error: function (xhr) {
            alert('Error al cargar los activos: ' + $('<div>').html(xhr.responseText).text());
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

function verActivo(id) {
    $.ajax({
        type: 'post', url: URL_ACTIVOS, data: { tipo: 'detalle', id: id }, dataType: 'json',
        success: function (f) {
            if (f.error) { alert(f.error); return; }
            activoActual = f;
            dibujarFicha(f);
            $('#modalActivo').modal('show');
        },
        error: function () { alert('Error al obtener la ficha del activo.'); }
    });
}

function dibujarFicha(f) {
    var movs = f.movimientos || [];
    var ultimo = movs.length > 0 ? movs[0] : null;
    var dias = ultimo ? diasDesde(ultimo.fecha) : diasDesde(f.fecha_ingreso);
    var ubicacion = lugar(f.centro, f.centro_costo) || '<span class="text-muted">-</span>';

    $('#tituloFicha').html('<i class="fas fa-laptop"></i> ' + escHtml(f.descripcion) +
        (f.numero_activo ? ' <small class="text-muted">- ' + escHtml(f.numero_activo) + '</small>' : ''));

    var botones = '<button type="button" class="btn btn-outline-secondary btn-sm mr-1 mb-1" id="btnHojaVida"><i class="fas fa-file-pdf"></i> Hoja de vida (PDF)</button>';
    // Con una solicitud de salida o baja abierta no se registran otros movimientos
    $.each(f.situacion ? [] : (ACCIONES[f.estado] || []), function (i, acc) {
        var b = BOTONES[acc];
        botones += '<button type="button" class="btn ' + b[1] + ' btn-sm mr-1 mb-1 accion-mov" data-accion="' + acc + '"><i class="fas ' + b[2] + '"></i> ' + escHtml(b[0]) + '</button>';
    });
    $('#accionesActivo').html(botones);

    var html = '<div class="row">' +
        infoBox('fa-info-circle', 'bg-info', 'Estado actual', badgeEstado(f.estado)) +
        infoBox('fa-map-marker-alt', 'bg-primary', 'Ubicación', ubicacion + (f.situacion ? '<br><small class="text-warning">' + escHtml(f.situacion) + '</small>' : '')) +
        infoBox('fa-user', 'bg-indigo', 'Responsable', f.responsable ? escHtml(f.responsable) : '<small class="text-muted">Sin asignar</small>') +
        infoBox('fa-hourglass-half', 'bg-warning', ultimo ? 'Desde el último movimiento' : 'Desde el ingreso',
                dias === null ? '-' : dias + (dias === 1 ? ' día' : ' días')) +
        '</div>';

    html += '<div class="row"><div class="col-lg-5">' +
        '<h6 class="text-primary"><i class="fas fa-info-circle"></i> Datos del activo</h6>' +
        '<table class="table table-sm table-bordered">' +
        filaFicha('N° de activo', f.numero_activo ? '<strong>' + escHtml(f.numero_activo) + '</strong>' : '<span class="text-muted">Pendiente de recepción</span>') +
        filaFicha('N° de serie', f.numero_serie ? escHtml(f.numero_serie) : '<span class="text-muted">No tiene</span>') +
        filaFicha('Código', escHtml(f.codigo)) +
        filaFicha('Descripción', escHtml(f.descripcion)) +
        filaFicha('Fecha de ingreso', escHtml(fechaLegible(f.fecha_ingreso))) +
        '</table>' +
        '<h6 class="text-primary"><i class="fas fa-map-marker-alt"></i> Ubicación</h6>' +
        '<table class="table table-sm table-bordered">' +
        filaFicha('Centro', escHtml(f.centro)) +
        filaFicha('Centro de costo', f.centro_costo ? escHtml(f.centro_costo) : '<span class="text-muted">Sin asignar</span>') +
        filaFicha('Responsable', f.responsable ? escHtml(f.responsable) : '<span class="text-muted">Sin asignar</span>') +
        '</table>' +
        '<h6 class="text-primary"><i class="fas fa-file-invoice"></i> Origen (compra)</h6>' +
        '<table class="table table-sm table-bordered">' +
        filaFicha('Orden de compra', f.numero_oc ? escHtml(f.numero_oc) + ' <small class="text-muted">(' + escHtml(f.estado_oc) + ')</small>' : '') +
        filaFicha('Fecha de compra', escHtml(fechaLegible(f.fecha_compra))) +
        filaFicha('Proveedor', f.proveedor ? escHtml(f.proveedor) + (f.ruc ? ' <small class="text-muted">RUC ' + escHtml(f.ruc) + '</small>' : '') : '') +
        filaFicha('Solicitante', escHtml(f.solicitante)) +
        filaFicha('Valor de compra', f.valor ? 'Gs. ' + formatoGs(f.valor) : '') +
        '</table></div>' +
        '<div class="col-lg-7"><h6 class="text-primary"><i class="fas fa-history"></i> Historial de movimientos</h6>' +
        '<div class="historial-scroll">' + dibujarTimeline(movs) + '</div></div></div>';

    $('#fichaActivo').html(html);
}

function dibujarTimeline(movs) {
    if (movs.length === 0) {
        return '<p class="text-muted">Sin movimientos registrados. El primer movimiento (ingreso) se registra al recibir la OC.</p>';
    }
    var html = '<div class="timeline">';
    var fechaAnterior = '';
    $.each(movs, function (i, m) {
        var dia = fechaLegible(m.fecha);
        if (dia !== fechaAnterior) {
            html += '<div class="time-label"><span class="bg-secondary">' + escHtml(dia) + '</span></div>';
            fechaAnterior = dia;
        }
        var t = TIPOS_MOV[m.tipo] || [m.tipo, 'fa-circle', 'bg-gray'];
        var cuerpo = '';
        var desde = lugar(m.centro_origen, m.sub_origen), hacia = lugar(m.centro_destino, m.sub_destino);
        if (m.tipo === 'INGRESO') {
            if (hacia) cuerpo += '<p><i class="fas fa-map-marker-alt text-muted"></i> Ubicado en: ' + hacia + '</p>';
        } else if (desde || hacia) {
            cuerpo += '<p><i class="fas fa-route text-muted"></i> ' + (desde || '-') + ' &rarr; ' + (hacia || '-') + '</p>';
        }
        if (m.estado_anterior && m.estado_nuevo && m.estado_anterior !== m.estado_nuevo) {
            cuerpo += '<p><i class="fas fa-flag text-muted"></i> Estado: ' + badgeEstado(m.estado_anterior) + ' &rarr; ' + badgeEstado(m.estado_nuevo) + '</p>';
        }
        if (m.receptor) cuerpo += '<p><i class="fas fa-user-check text-muted"></i> ' + (m.tipo === 'ASIGNACION' ? 'Responsable' : 'Recibe') + ': ' + escHtml(m.receptor) + '</p>';
        var p = persona(m.persona_nombre, m.persona_apellido, m.persona_ci);
        if (p) cuerpo += '<p><i class="fas fa-id-card text-muted"></i> ' + (m.tipo === 'ASIGNACION' ? 'Responsable' : 'Persona') + ': ' + p + '</p>';
        var veh = $.trim([m.vehiculo_marca, m.vehiculo_modelo].join(' '));
        if (veh || m.vehiculo_chapa) {
            cuerpo += '<p><i class="fas fa-car text-muted"></i> Vehículo: ' + escHtml(veh) + (m.vehiculo_chapa ? ' - Chapa ' + escHtml(m.vehiculo_chapa) : '') + '</p>';
        }
        if (m.remision) cuerpo += '<p><i class="fas fa-file-alt text-muted"></i> Remisión N° ' + escHtml(m.remision) + '</p>';
        if (m.observaciones) cuerpo += '<p><i class="fas fa-comment text-muted"></i> ' + escHtml(m.observaciones) + '</p>';
        if (m.justificacion) cuerpo += '<p class="text-danger"><i class="fas fa-exclamation-circle"></i> Motivo del rechazo: ' + escHtml(m.justificacion) + '</p>';

        html += '<div><i class="fas ' + t[1] + ' ' + t[2] + '"></i><div class="timeline-item">' +
            '<span class="time"><i class="fas fa-clock"></i> ' + escHtml(m.fecha.substring(11, 16)) + '</span>' +
            '<h3 class="timeline-header"><strong>' + escHtml(t[0]) + '</strong> <small class="text-muted">' + escHtml(m.numero) + '</small></h3>' +
            (cuerpo ? '<div class="timeline-body">' + cuerpo + '</div>' : '') +
            '<div class="timeline-footer"><small class="text-muted">Registrado por ' + escHtml(m.registrado_por || 'sistema') + '</small></div>' +
            '</div></div>';
    });
    html += '<div><i class="fas fa-clock bg-gray"></i></div></div>';
    return html;
}

// Formulario de movimiento
var CONFIG_MOV = {
    ASIGNACION:  { titulo: 'Asignar responsable', grupos: ['usuario', 'persona'], ayuda: 'Elija un usuario del sistema o cargue los datos de una persona externa. Asignar un responsable es opcional.' },
    DEVOLUCION:  { titulo: 'Quitar responsable (devolución)', grupos: [], ayuda: 'El activo queda sin responsable, en el mismo centro de costo.' },
    REUBICACION: { titulo: 'Cambiar centro de costo', grupos: ['cc'], ayuda: 'Mueve el activo a otro centro de costo (sector) del mismo centro. El responsable no cambia.' }
};

function abrirMovimiento(accion) {
    var c = CONFIG_MOV[accion];
    if (!c || !activoActual) return;
    $('#formMovimiento')[0].reset();
    $('#mensajeMovimiento').html('');
    $('#movAccion').val(accion);
    $('#tituloMovimiento').text(c.titulo);
    $('#ayudaMovimiento').text(c.ayuda);
    $('#formMovimiento .grp').hide();
    $.each(c.grupos, function (i, g) { $('#formMovimiento .grp-' + g).show(); });

    if (accion === 'REUBICACION') {
        $('#movCentroCosto').html('<option value="">Cargando...</option>');
        $.post(URL_ACTIVOS, { tipo: 'listarCentrosCosto', id_centro: activoActual.id_centro }, function (html) {
            $('#movCentroCosto').html(html);
            $('#movCentroCosto option[value="' + activoActual.id_centro_costo + '"]').remove();
        });
    }
    if (accion === 'ASIGNACION') {
        $.post(URL_ACTIVOS, { tipo: 'listarUsuarios' }, function (html) { $('#movUsuario').html(html); });
    }
    $('#btnGuardarMovimiento').prop('disabled', false);
    $('#modalMovimiento').modal('show');
}

function guardarMovimiento() {
    var accion = $('#movAccion').val();
    var datos = {
        tipo: 'registrarMovimiento',
        id_equipo: activoActual.id,
        accion: accion,
        observaciones: $('#movObservaciones').val(),
        id_centro_costo: $('#movCentroCosto').val(),
        id_usuario_receptor: $('#movUsuario').val(),
        persona_nombre: $('#movPersonaNombre').val(),
        persona_apellido: $('#movPersonaApellido').val(),
        persona_ci: $('#movPersonaCi').val()
    };
    if (accion === 'ASIGNACION' && datos.id_usuario_receptor) {
        datos.persona_nombre = datos.persona_apellido = datos.persona_ci = '';
    }
    var $btn = $('#btnGuardarMovimiento').prop('disabled', true);
    $.ajax({
        type: 'post', url: URL_ACTIVOS, data: datos, dataType: 'json',
        success: function (r) {
            if (r.ok) {
                $('#modalMovimiento').modal('hide');
                verActivo(activoActual.id);   // refresca la ficha con el nuevo movimiento
                cargarActivos();
                cargarResumen();
            } else {
                $('#mensajeMovimiento').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>');
                $btn.prop('disabled', false);
            }
        },
        error: function (xhr) {
            $('#mensajeMovimiento').html('<div class="alert alert-danger">Error al registrar: ' + escHtml(xhr.status + ' ' + xhr.statusText) + '</div>');
            $btn.prop('disabled', false);
        }
    });
}
</script>
</body>
</html>
