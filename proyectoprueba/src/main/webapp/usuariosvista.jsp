<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Usuarios</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" data-toggle="modal" data-target="#modalUsuario" onclick="abrirModalNuevoUsuario()">Nuevo Usuario</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
    <div class="card-body"><table id="tablaUsuarios" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Nombre</th><th>Apellido</th><th>Email</th><th>Rol</th><th>Activo</th><th>Centro</th><th>Acción</th></tr></thead><tbody id="listaUsuarios"></tbody><tfoot><tr><th>ID</th><th>Nombre</th><th>Apellido</th><th>Email</th><th>Rol</th><th>Activo</th><th>Centro</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal Usuario -->
<div class="modal fade" id="modalUsuario" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header"><h5 id="modalUsuarioLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formUsuario"><input type="hidden" name="tipo" id="tipoUsuario"><input type="hidden" name="pk" id="pkUsuario">
        <div class="row"><div class="col-md-6"><div class="form-group"><label>Nombre</label><input type="text" class="form-control" name="nombre" id="nombreUsuario"></div></div>
        <div class="col-md-6"><div class="form-group"><label>Apellido</label><input type="text" class="form-control" name="apellido" id="apellidoUsuario"></div></div></div>
        <div class="row"><div class="col-md-6"><div class="form-group"><label>Email</label><input type="email" class="form-control" name="email" id="emailUsuario"></div></div>
        <div class="col-md-6"><div class="form-group"><label>Password</label><input type="password" class="form-control" name="password" id="passwordUsuario"><small class="text-muted">Dejar en blanco para no cambiar la contraseña (solo edición)</small></div></div></div>
        <div class="row"><div class="col-md-4"><div class="form-group"><label>Rol</label><select class="form-control" name="id_rol" id="comboRol"></select></div></div>
        <div class="col-md-4"><div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div></div>
        <div class="col-md-4"><div class="form-group"><label>Centro Asignado <small class="text-muted">(necesario para crear OT)</small></label><select class="form-control" name="centro_asignado" id="comboCentro"></select></div></div></div>
    </form><div id="mensajeUsuario"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarUsuario"></button></div>
</div></div></div>
<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarUsuario" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Usuario</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarUsuario"><h5>¿Seguro que desea eliminar este usuario?</h5><div id="mensajeEliminarUsuario"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarUsuario">Eliminar</button></div></div></div></div>

<!-- Centros adicionales del usuario (temporales o permanentes) -->
<div class="modal fade" id="modalCentrosUsuario" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Centros de <span id="cuNombre"></span></h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <p class="text-muted">Además de su <b>centro principal</b>, el usuario trabaja con estos centros según su rol.
                   Con <b>fecha hasta</b>, el acceso es temporal y se corta solo al vencer.</p>
                <input type="hidden" id="cuId">
                <table class="table table-sm table-bordered">
                    <thead class="thead-light"><tr><th>Centro</th><th>Desde</th><th>Hasta</th><th>Observación</th><th>Estado</th><th style="width:90px"></th></tr></thead>
                    <tbody id="cuLista"></tbody>
                </table>
                <h6 id="cuTituloForm">Agregar centro</h6>
                <div class="form-row">
                    <div class="col-md-4 form-group"><select class="form-control" id="cuCentro"></select></div>
                    <div class="col-md-2 form-group"><input type="date" class="form-control" id="cuDesde" title="Desde (vacío = hoy)"></div>
                    <div class="col-md-2 form-group"><input type="date" class="form-control" id="cuHasta" title="Hasta (vacío = sin vencimiento)"></div>
                    <div class="col-md-3 form-group"><input type="text" class="form-control" id="cuObs" placeholder="Motivo (ej.: reemplazo)" maxlength="300"></div>
                    <div class="col-md-1 form-group"><button class="btn btn-success btn-block" id="cuAgregar" title="Agregar"><i class="fa fa-plus"></i></button></div>
                </div>
                <small class="text-muted">Desde vacío = hoy. Hasta vacío = sin vencimiento.</small>
                <button class="btn btn-link btn-sm" id="cuCancelarEdicion" style="display:none">Cancelar edición</button>
                <div id="cuMensaje" class="mt-2"></div>

                <hr>
                <h6><i class="fas fa-history"></i> Historial de accesos</h6>
                <p class="text-muted small mb-2">Cuándo se le dio, modificó o quitó el acceso a cada centro, y quién lo hizo.</p>
                <div class="form-row mb-2">
                    <div class="col-md-4"><select class="form-control form-control-sm" id="cuHistCentro"><option value="">Todos los centros</option></select></div>
                </div>
                <div style="max-height:260px; overflow-y:auto">
                    <table class="table table-sm table-bordered mb-0">
                        <thead class="thead-light"><tr><th>Fecha</th><th>Centro</th><th>Tipo</th><th>Acción</th><th>Período</th><th>Observación</th><th>Registrado por</th></tr></thead>
                        <tbody id="cuHistorial"></tbody>
                    </table>
                </div>
            </div>
            <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button></div>
        </div>
    </div>
</div>

<%@include file="footer.jsp" %>

<script>
var dataTableUsuarios;

$(document).ready(function() {
    dataTableUsuarios = $('#tablaUsuarios').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarComboRoles();
    cargarComboCentros();
    cargarTablaUsuarios();
});

function cargarComboRoles() {
    $.ajax({ url: "RolServlet", data: { tipo: "listarCombo" }, success: function(r) { $("#comboRol").html(r); } });
}
function cargarComboCentros() {
    $.ajax({ url: "CentroServlet", data: { tipo: "listarCombo" }, success: function(r) { $("#comboCentro").html(r); } });
}

function cargarTablaUsuarios() {
    $.ajax({
        type: "post",
        url: "UsuarioServlet",
        data: { tipo: "listar" },
        success: function(r) {
            dataTableUsuarios.clear();
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableUsuarios.row.add(rowData);
            });
            dataTableUsuarios.draw();
        }
    });
}

function abrirModalNuevoUsuario() {
    $("#modalUsuarioLabel").html("Nuevo Usuario");
    $("#tipoUsuario").val("Guardar");
    $("#btnGuardarUsuario").html("Guardar");
    $("#formUsuario")[0].reset();
    $("#pkUsuario").val("");
    $("#mensajeUsuario").html("");
    $('#modalUsuario').modal('show');
}

function abrirModalEditarUsuario(id, nombre, apellido, email, id_rol, activo, centro_asignado) {
    $("#modalUsuarioLabel").html("Editar Usuario");
    $("#tipoUsuario").val("Modificar");
    $("#btnGuardarUsuario").html("Modificar");
    $("#pkUsuario").val(id);
    $("#nombreUsuario").val(nombre);
    $("#apellidoUsuario").val(apellido);
    $("#emailUsuario").val(email);
    $("select[name='activo']").val(activo.toString());
    $("#passwordUsuario").val(""); // no cambiar contraseña a menos que se escriba
    setTimeout(function() { 
        $("#comboRol").val(id_rol);
        if (centro_asignado && centro_asignado !== "null") $("#comboCentro").val(centro_asignado);
        else $("#comboCentro").val("");
    }, 300);
    $("#mensajeUsuario").html("");
    $('#modalUsuario').modal('show');
}

function abrirModalEliminarUsuario(id) {
    $("#pkEliminarUsuario").val(id);
    $("#mensajeEliminarUsuario").html("");
    $('#modalEliminarUsuario').modal('show');
}

function imprimirUsuario(id) {
    // El PDF individual lo genera ReporteIndividualServlet
    window.open('ReporteIndividualServlet?reporte=usuario&cod=' + id, '_blank');
}
function imprimirReporte() {
    window.open('UsuarioServlet?tipo=imprimir', '_blank');
}

$("#btnGuardarUsuario").click(function() {
    var tipo = $("#tipoUsuario").val();
    var formData = $("#formUsuario").serializeArray();
    if (tipo === "Modificar" && $("#passwordUsuario").val().trim() === "") {
        formData = formData.filter(f => f.name !== "password");
    }
    $.ajax({
        type: "post",
        url: "UsuarioServlet",
        data: formData,
        success: function(r) {
            $("#mensajeUsuario").html(r);
            if(r.includes("éxito")) {
                cargarTablaUsuarios();
                setTimeout(() => $('#modalUsuario').modal('hide'), 1500);
            }
        }
    });
});

$("#btnConfirmarEliminarUsuario").click(function() {
    var id = $("#pkEliminarUsuario").val();
    $.ajax({
        type: "post",
        url: "UsuarioServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarUsuario").html(r);
            if(r.includes("éxito")) cargarTablaUsuarios();
            setTimeout(() => $('#modalEliminarUsuario').modal('hide'), 1500);
        }
    });
});
</script>
<script>
// Centros adicionales
var URL_USU = "UsuarioServlet";
function fechaDMA(iso) { if (!iso) return ''; var p = iso.split('-'); return p[2] + '/' + p[1] + '/' + p[0]; }
var cuHistorial = [];
var cuEditando = null;

function abrirCentrosUsuario(id, nombre) {
    $('#cuId').val(id); $('#cuNombre').text(nombre); $('#cuMensaje').html('');
    $.post('CentroServlet', { tipo: 'listarCombo' }, function (h) { $('#cuCentro').html(h); salirEdicionCentro(); });
    cargarCentrosUsuario();
    $('#modalCentrosUsuario').modal('show');
}
function cargarCentrosUsuario() {
    $.post(URL_USU, { tipo: 'centrosUsuario', pk: $('#cuId').val() }, function (lista) {
        cargarHistorialCentros();
        if (!lista.length) { $('#cuLista').html('<tr><td colspan="6" class="text-center text-muted">Solo trabaja con su centro principal</td></tr>'); return; }
        var h = '';
        $.each(lista, function (i, c) {
            var id = parseInt(c.id_centro, 10);
            h += '<tr><td>' + escHtml(c.centro) + '</td><td>' + escHtml(fechaDMA(c.desde)) + '</td><td>' + (c.hasta ? escHtml(fechaDMA(c.hasta)) : '<span class="text-muted">sin vencimiento</span>') +
                '</td><td>' + escHtml(c.observacion) + '</td><td>' + (c.vigente ? '<span class="badge badge-success">Vigente</span>' : '<span class="badge badge-secondary">No vigente</span>') +
                '</td><td class="text-nowrap"><button class="btn btn-outline-primary btn-sm mr-1 cu-editar" title="Editar" data-i="' + i + '"><i class="fa fa-edit"></i></button>' +
                '<button class="btn btn-outline-danger btn-sm" title="Quitar" onclick="quitarCentroUsuario(' + id + ')"><i class="fa fa-times"></i></button></td></tr>';
        });
        $('#cuLista').html(h).data('lista', lista);
    }, 'json');
}
$('#cuLista').on('click', '.cu-editar', function () {
    var c = $('#cuLista').data('lista')[$(this).data('i')];
    cuEditando = parseInt(c.id_centro, 10);
    $('#cuCentro').val(String(cuEditando)).prop('disabled', true);
    $('#cuDesde').val(c.desde); $('#cuHasta').val(c.hasta); $('#cuObs').val(c.observacion);
    $('#cuTituloForm').text('Editar centro: ' + c.centro);
    $('#cuAgregar').removeClass('btn-success').addClass('btn-primary').attr('title', 'Guardar cambios').html('<i class="fa fa-save"></i>');
    $('#cuCancelarEdicion').show();
    $('#cuMensaje').html('');
});
$('#cuCancelarEdicion').click(function () { salirEdicionCentro(); $('#cuMensaje').html(''); });
function salirEdicionCentro() {
    cuEditando = null;
    $('#cuCentro').prop('disabled', false).val('');
    $('#cuDesde, #cuHasta, #cuObs').val('');
    $('#cuTituloForm').text('Agregar centro');
    $('#cuAgregar').removeClass('btn-primary').addClass('btn-success').attr('title', 'Agregar').html('<i class="fa fa-plus"></i>');
    $('#cuCancelarEdicion').hide();
}
$('#cuAgregar').click(function () {
    if (!$('#cuCentro').val()) { alert('Seleccione el centro.'); return; }
    $.post(URL_USU, { tipo: cuEditando ? 'modificarCentroUsuario' : 'agregarCentroUsuario', pk: $('#cuId').val(), id_centro: $('#cuCentro').val(),
            desde: $('#cuDesde').val(), hasta: $('#cuHasta').val(), observacion: $('#cuObs').val() },
        function (r) { $('#cuMensaje').html(r); if (r.indexOf('éxito') !== -1) { salirEdicionCentro(); cargarCentrosUsuario(); } });
});
function quitarCentroUsuario(idCentro) {
    if (!confirmado('¿Quitar este centro adicional? Quedará registrado en el historial.', this, arguments)) return;
    $.post(URL_USU, { tipo: 'quitarCentroUsuario', pk: $('#cuId').val(), id_centro: idCentro }, function (r) {
        $('#cuMensaje').html(r); if (cuEditando === idCentro) salirEdicionCentro(); cargarCentrosUsuario();
    });
}

// ---- Historial de accesos ----
var ACCIONES_CU = { ALTA: ['Alta', 'badge-success'], MODIFICACION: ['Modificación', 'badge-info'], BAJA: ['Baja', 'badge-danger'] };
function cargarHistorialCentros() {
    $.post(URL_USU, { tipo: 'historialCentrosUsuario', pk: $('#cuId').val() }, function (lista) {
        cuHistorial = lista;
        var centros = {}, sel = $('#cuHistCentro').val(), op = '<option value="">Todos los centros</option>';
        $.each(lista, function (i, h) { centros[h.centro] = true; });
        $.each(Object.keys(centros).sort(), function (i, c) { op += '<option value="' + escHtml(c) + '">' + escHtml(c) + '</option>'; });
        $('#cuHistCentro').html(op).val(centros[sel] ? sel : '');
        dibujarHistorialCentros();
    }, 'json');
}
$('#cuHistCentro').change(dibujarHistorialCentros);
function dibujarHistorialCentros() {
    var filtro = $('#cuHistCentro').val(), h = '';
    $.each(cuHistorial, function (i, x) {
        if (filtro && x.centro !== filtro) return;
        var a = ACCIONES_CU[x.accion] || [x.accion, 'badge-secondary'];
        var periodo = x.tipo === 'PRINCIPAL' ? '<span class="text-muted">permanente</span>'
            : escHtml(x.desde ? fechaDMA(x.desde) : '') + ' al ' + (x.hasta ? escHtml(fechaDMA(x.hasta)) : '<span class="text-muted">sin vencimiento</span>');
        h += '<tr><td class="text-nowrap">' + escHtml(x.fecha) + '</td><td>' + escHtml(x.centro) + '</td><td>' +
            (x.tipo === 'PRINCIPAL' ? '<span class="badge badge-dark">Principal</span>' : '<span class="badge badge-light border">Adicional</span>') +
            '</td><td><span class="badge ' + a[1] + '">' + escHtml(a[0]) + '</span></td><td class="text-nowrap">' + periodo +
            '</td><td>' + escHtml(x.observacion) + '</td><td>' + escHtml(x.registra) + '</td></tr>';
    });
    $('#cuHistorial').html(h || '<tr><td colspan="7" class="text-center text-muted">Sin movimientos registrados</td></tr>');
}
</script>
</body>
</html>