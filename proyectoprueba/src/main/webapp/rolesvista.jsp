<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Roles</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" onclick="abrirModalNuevoRol()">Nuevo Rol</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
    <div class="card-body">
        <p class="text-muted mb-3"><i class="fas fa-info-circle"></i> El <strong>tipo de permisos</strong> define qué puede hacer cada usuario. El nombre del rol es libre: varios roles pueden compartir el mismo tipo (por ejemplo "Gerente Asunción" y "Gerente Encarnación").</p>
        <table id="tablaRoles" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Nombre</th><th>Tipo de permisos</th><th>Descripción</th><th>Usuarios</th><th>Activo</th><th>Acción</th></tr></thead><tbody id="listaRoles"></tbody><tfoot><tr><th>ID</th><th>Nombre</th><th>Tipo de permisos</th><th>Descripción</th><th>Usuarios</th><th>Activo</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal Rol -->
<div class="modal fade" id="modalRol" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 id="modalRolLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formRol" onsubmit="return false;"><input type="hidden" name="tipo" id="tipoRol"><input type="hidden" name="pk" id="pkRol">
        <div class="form-group"><label>Nombre</label><input type="text" class="form-control" name="nombre" id="nombreRol" maxlength="50" required></div>
        <div class="form-group"><label>Tipo de permisos</label><select class="form-control" name="codigo" id="codigoRol" required></select>
            <small class="form-text text-muted" id="ayudaCodigoRol"></small></div>
        <div class="form-group"><label>Descripción</label><textarea class="form-control" name="descripcion" id="descripcionRol"></textarea></div>
        <div class="form-group"><label>Activo</label><select class="form-control" name="activo" id="activoRol"><option value="true">Sí</option><option value="false">No</option></select></div>
    </form><div id="mensajeRol"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarRol"></button></div>
</div></div></div>
<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarRol" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Rol</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarRol"><h5>¿Seguro que desea eliminar este rol?</h5><div id="mensajeEliminarRol"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarRol">Eliminar</button></div></div></div></div>

<%@include file="footer.jsp" %>

<script>
var dataTableRoles;
var URL_ROL = "RolServlet";

$(document).ready(function() {
    dataTableRoles = $('#tablaRoles').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarTablaRoles();
    cargarTiposPermiso();
    $('#codigoRol').on('change', mostrarAyudaTipo);
});

function cargarTablaRoles() {
    $.ajax({
        type: "post",
        url: URL_ROL,
        data: { tipo: "listar" },
        success: function(r) {
            dataTableRoles.clear();
            var tempDiv = $('<div>').html('<table>' + r + '</table>');
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableRoles.row.add(rowData);
            });
            dataTableRoles.draw();
        }
    });
}

function cargarTiposPermiso(seleccionado) {
    $.post(URL_ROL, { tipo: "listarTipos" }, function(h) {
        $('#codigoRol').html(h);
        if (seleccionado !== undefined) $('#codigoRol').val(seleccionado);
        mostrarAyudaTipo();
    });
}

function mostrarAyudaTipo() {
    var ayuda = $('#codigoRol option:selected').data('ayuda');
    $('#ayudaCodigoRol').text(ayuda ? ayuda : '');
}

function abrirModalNuevoRol() {
    $("#modalRolLabel").html("Nuevo Rol");
    $("#tipoRol").val("Guardar");
    $("#btnGuardarRol").html("Guardar").prop('disabled', false);
    $("#formRol")[0].reset();
    $("#pkRol").val("");
    $("#codigoRol").val("");
    mostrarAyudaTipo();
    $("#mensajeRol").html("");
    $('#modalRol').modal('show');
}

function abrirModalEditarRol(id, nombre, descripcion, activo, codigo) {
    $("#modalRolLabel").html("Editar Rol");
    $("#tipoRol").val("Modificar");
    $("#btnGuardarRol").html("Modificar").prop('disabled', false);
    $("#pkRol").val(id);
    $("#nombreRol").val(nombre);
    $("#descripcionRol").val(descripcion);
    $("#activoRol").val(activo.toString());
    $("#codigoRol").val(codigo);
    mostrarAyudaTipo();
    $("#mensajeRol").html("");
    $('#modalRol').modal('show');
}

function abrirModalEliminarRol(id) {
    $("#pkEliminarRol").val(id);
    $("#mensajeEliminarRol").html("");
    $("#btnConfirmarEliminarRol").prop('disabled', false);
    $('#modalEliminarRol').modal('show');
}
function imprimirReporte() {
    window.open(URL_ROL + '?tipo=imprimir', '_blank');
}
function imprimirRol(id) {
    window.open('ReporteIndividualServlet?reporte=rol&cod=' + id, '_blank');
}
$("#btnGuardarRol").click(function() {
    if ($.trim($("#nombreRol").val()) === '') {
        $("#mensajeRol").html("<div class='alert alert-danger'>El nombre del rol es obligatorio.</div>");
        return;
    }
    if (!$("#codigoRol").val()) {
        $("#mensajeRol").html("<div class='alert alert-danger'>Seleccione el tipo de permisos del rol.</div>");
        return;
    }
    var $b = $(this).prop('disabled', true);
    $.ajax({
        type: "post",
        url: URL_ROL,
        data: $("#formRol").serialize(),
        success: function(r) {
            $("#mensajeRol").html(r);
            if (r.indexOf("alert-success") !== -1) {
                cargarTablaRoles();
                setTimeout(function () { $('#modalRol').modal('hide'); }, 1500);
            } else {
                $b.prop('disabled', false);   // error: el modal queda abierto para corregir
            }
        },
        error: function() {
            $("#mensajeRol").html("<div class='alert alert-danger'>Error de comunicación con el servidor.</div>");
            $b.prop('disabled', false);
        }
    });
});

$("#btnConfirmarEliminarRol").click(function() {
    var id = $("#pkEliminarRol").val();
    var $b = $(this).prop('disabled', true);
    $.ajax({
        type: "post",
        url: URL_ROL,
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarRol").html(r);
            if (r.indexOf("alert-success") !== -1) {
                cargarTablaRoles();
                setTimeout(function () { $('#modalEliminarRol').modal('hide'); }, 1500);
            } else {
                $b.prop('disabled', false);
            }
        },
        error: function() {
            $("#mensajeEliminarRol").html("<div class='alert alert-danger'>Error de comunicación con el servidor.</div>");
            $b.prop('disabled', false);
        }
    });
});
</script>
</body>
</html>
