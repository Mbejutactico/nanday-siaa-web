<%@include file="header.jsp" %>
<%-- ORDEN DE TRABAJO > Crear: el solicitante es el usuario de la sesión; elige el centro
     destino (obligatorio, el que resuelve la OT), centro de costo, responsable (opcional),
     título (obligatorio) y descripción (opcional) --%>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <h1><i class="fas fa-clipboard-list"></i> Crear Orden de Trabajo</h1>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">

            <div class="card card-primary card-outline card-outline-tabs">
                <div class="card-header p-0 border-bottom-0">
                    <ul class="nav nav-tabs" role="tablist">
                        <li class="nav-item">
                            <a class="nav-link active" id="tab-usuario-link" data-toggle="pill" href="#tab-usuario" role="tab">
                                <i class="fas fa-user"></i> Datos del usuario
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="tab-ot-link" data-toggle="pill" href="#tab-ot" role="tab">
                                <i class="fas fa-clipboard-list"></i> Datos de la orden de trabajo
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="card-body">
                    <div class="tab-content">
                        <!-- Datos del usuario (automáticos) -->
                        <div class="tab-pane fade show active" id="tab-usuario" role="tabpanel">
                            <p class="text-muted">Estos datos se completan automáticamente con el usuario que inició sesión.</p>
                            <div class="row">
                                <div class="col-md-6 form-group">
                                    <label>Solicitante</label>
                                    <input type="text" class="form-control" id="usuNombre" readonly>
                                </div>
                                <div class="col-md-6 form-group">
                                    <label>Email</label>
                                    <input type="text" class="form-control" id="usuEmail" readonly>
                                </div>
                                <div class="col-md-4 form-group">
                                    <label>Rol</label>
                                    <input type="text" class="form-control" id="usuRol" readonly>
                                </div>
                                <div class="col-md-4 form-group">
                                    <label>Su centro</label>
                                    <input type="text" class="form-control" id="usuCentro" readonly>
                                </div>
                                <div class="col-md-4 form-group">
                                    <label>Fecha y hora de solicitud</label>
                                    <input type="text" class="form-control" id="usuFecha" readonly>
                                </div>
                            </div>
                            <div class="text-right">
                                <button type="button" class="btn btn-primary" id="btnSiguiente">Siguiente <i class="fas fa-arrow-right"></i></button>
                            </div>
                        </div>

                        <!-- Datos de la OT -->
                        <div class="tab-pane fade" id="tab-ot" role="tabpanel">
                            <form id="formOT" onsubmit="return false;">
                                <div class="row">
                                    <div class="col-md-6 form-group">
                                        <label>Centro destino *</label>
                                        <select class="form-control" id="otCentro">
                                            <option value="">-- Seleccionar centro --</option>
                                        </select>
                                        <small class="text-muted">Centro que va a resolver la orden de trabajo (puede ser otro centro distinto al suyo).</small>
                                    </div>
                                    <div class="col-md-6 form-group">
                                        <label>Centro de costo *</label>
                                        <select class="form-control" id="otCentroCosto">
                                            <option value="">-- Elija el centro destino --</option>
                                        </select>
                                        <small class="text-muted">Sector del centro destino al que corresponde el trabajo.</small>
                                    </div>
                                    <div class="col-md-6 form-group">
                                        <label>Responsable <small class="text-muted">(opcional)</small></label>
                                        <select class="form-control" id="otResponsable">
                                            <option value="">-- Sin responsable --</option>
                                        </select>
                                        <div class="custom-control custom-checkbox mt-1">
                                            <input type="checkbox" class="custom-control-input" id="respTodos">
                                            <label class="custom-control-label small" for="respTodos">Buscar el responsable en todos los centros (al elegirlo se completa su centro)</label>
                                        </div>
                                    </div>
                                    <div class="col-md-12 form-group">
                                        <label>Título *</label>
                                        <input type="text" class="form-control" id="otTitulo" maxlength="200" placeholder="Ejemplo: Solicito 20 mouse">
                                    </div>
                                    <div class="col-md-12 form-group">
                                        <label>Descripción <small class="text-muted">(opcional)</small></label>
                                        <textarea class="form-control" id="otDescripcion" rows="4" maxlength="2000" placeholder="Detalle adicional de lo que se necesita"></textarea>
                                    </div>
                                </div>
                            </form>
                            <div id="mensajeOT"></div>
                            <div class="d-flex justify-content-between">
                                <button type="button" class="btn btn-secondary" id="btnAnterior"><i class="fas fa-arrow-left"></i> Datos del usuario</button>
                                <button type="button" class="btn btn-success" id="btnCrearOT"><i class="fas fa-save"></i> Crear orden de trabajo</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var URL_OT = "OrdenTrabajoServlet";
var usuario = null, responsables = [];

$(document).ready(function () {
    cargarUsuario();
    $('#btnSiguiente').click(function () { $('#tab-ot-link').tab('show'); });
    $('#btnAnterior').click(function () { $('#tab-usuario-link').tab('show'); });
    $('#btnCrearOT').click(crearOT);
    $('#otCentro').change(function () { cargarCCs(); cargarResponsables(); });
    $('#respTodos').change(cargarResponsables);
    $('#otResponsable').change(alElegirResponsable);
});

function cargarUsuario() {
    $.ajax({
        type: 'post', url: URL_OT, data: { tipo: 'contextoUsuario' }, dataType: 'json',
        success: function (u) {
            usuario = u;
            $('#usuNombre').val(u.nombre);
            $('#usuEmail').val(u.email);
            $('#usuRol').val(u.rol);
            $('#usuCentro').val(u.centro || 'Sin centro asignado');
            $('#usuFecha').val(u.fecha);
            // Centro destino: cualquier centro activo; por defecto, el del usuario
            var h = '<option value="">-- Seleccionar centro --</option>';
            $.each(u.centros || [], function (i, c) { h += '<option value="' + parseInt(c.id, 10) + '">' + escHtml(c.nombre) + '</option>'; });
            $('#otCentro').html(h);
            if (u.id_centro && $('#otCentro option[value="' + parseInt(u.id_centro, 10) + '"]').length) $('#otCentro').val(String(u.id_centro));
            cargarCCs();
            cargarResponsables();
        },
        error: function () { alert('No se pudieron cargar los datos del usuario.'); }
    });
}

function cargarCCs(seleccionar) {
    var c = $('#otCentro').val();
    if (!c) { $('#otCentroCosto').html('<option value="">-- Elija el centro destino --</option>'); return; }
    $.post(URL_OT, { tipo: 'listarCentrosCosto', id_centro: c }, function (html) {
        $('#otCentroCosto').html(html);
        if (seleccionar) $('#otCentroCosto').val(seleccionar);
    });
}

// Responsables: los del centro destino, o de todos los centros si se marcó la casilla (o si no hay centro elegido)
function cargarResponsables(mantener) {
    var todos = $('#respTodos').is(':checked') || !$('#otCentro').val();
    var elegido = mantener || $('#otResponsable').val();
    $.post(URL_OT, { tipo: 'listarResponsables', id_centro: todos ? 0 : $('#otCentro').val() }, function (lista) {
        responsables = lista || [];
        var h = '<option value="">-- Sin responsable --</option>';
        $.each(responsables, function (i, r) {
            h += '<option value="' + parseInt(r.id, 10) + '">' + escHtml(r.nombre) + ' - ' + escHtml(r.rol) + (todos && r.centro ? ' (' + escHtml(r.centro) + ')' : '') + '</option>';
        });
        $('#otResponsable').html(h);
        if (elegido && $('#otResponsable option[value="' + elegido + '"]').length) $('#otResponsable').val(elegido);
    }, 'json');
}

// Si se eligió un responsable de otro centro, el centro destino pasa a ser el suyo
function alElegirResponsable() {
    var id = parseInt($('#otResponsable').val(), 10);
    if (!id) return;
    var r = null;
    $.each(responsables, function (i, x) { if (x.id === id) r = x; });
    if (!r || !r.id_centro || String(r.id_centro) === $('#otCentro').val()) return;
    $('#otCentro').val(String(r.id_centro));
    $('#respTodos').prop('checked', false);
    cargarCCs();
    cargarResponsables(String(id));
    $('#mensajeOT').html('<div class="alert alert-info py-2"><i class="fas fa-info-circle"></i> El centro destino se cambió a <b>' + escHtml(r.centro) + '</b>, el centro de ' + escHtml(r.nombre) + '.</div>');
}

function crearOT() {
    var centro = $('#otCentro').val(), cc = $('#otCentroCosto').val(), titulo = $.trim($('#otTitulo').val());
    if (!centro) { alert('Seleccione el centro destino (el centro que va a resolver la orden de trabajo).'); $('#otCentro').focus(); return; }
    if (!cc) { alert('Seleccione el centro de costo.'); return; }
    if (!titulo) { alert('Ingrese un título. Ejemplo: Solicito 20 mouse'); $('#otTitulo').focus(); return; }

    var $btn = $('#btnCrearOT').prop('disabled', true);
    $.ajax({
        type: 'post', url: URL_OT, dataType: 'json',
        data: { tipo: 'crear', id_centro: centro, id_centro_costo: cc, id_responsable: $('#otResponsable').val(), titulo: titulo, descripcion: $('#otDescripcion').val() },
        success: function (r) {
            if (r.ok) {
                $('#mensajeOT').html('<div class="alert alert-success"><i class="fas fa-check-circle"></i> Orden de trabajo <b>' + escHtml(r.numero_ot) +
                    '</b> creada correctamente el ' + escHtml(r.fecha) + ' para <b>' + escHtml($('#otCentro option:selected').text()) + '</b>. ' +
                    '<a href="otListado.jsp?ot=' + encodeURIComponent(r.id_ot) + '" class="alert-link">Ver la orden de trabajo</a>.</div>');
                $('#otTitulo').val('');
                $('#otDescripcion').val('');
                $('#otCentroCosto').val('');
                $('#otResponsable').val('');
            } else {
                $('#mensajeOT').html('<div class="alert alert-danger">' + escHtml(r.error) + '</div>');
            }
            $btn.prop('disabled', false);
        },
        error: function () {
            $('#mensajeOT').html('<div class="alert alert-danger">Error al crear la orden de trabajo.</div>');
            $btn.prop('disabled', false);
        }
    });
}
</script>
</body>
</html>
