<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Proveedores</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" data-toggle="modal" data-target="#modalProveedor" onclick="abrirModalNuevoProveedor()">Nuevo Proveedor</button>
                    <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
    <div class="card-body"><table id="tablaProveedores" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Razón Social</th><th>RUC</th><th>Teléfono</th><th>Email</th><th>Activo</th><th>Acción</th></tr></thead><tbody id="listaProveedores"></tbody><tfoot><tr><th>ID</th><th>Razón Social</th><th>RUC</th><th>Teléfono</th><th>Email</th><th>Activo</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal Proveedor -->
<div class="modal fade" id="modalProveedor" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 id="modalProveedorLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formProveedor"><input type="hidden" name="tipo" id="tipoProveedor"><input type="hidden" name="pk" id="pkProveedor">
        <div class="form-group"><label>Razón Social</label><input type="text" class="form-control" name="razon_social" id="razonSocial"></div>
        <div class="row"><div class="col-md-6"><div class="form-group"><label>RUC</label><input type="text" class="form-control" name="ruc" id="ruc"></div></div>
        <div class="col-md-6"><div class="form-group"><label>Teléfono</label><input type="text" class="form-control" name="telefono" id="telefono"></div></div></div>
        <div class="form-group"><label>Dirección</label><input type="text" class="form-control" name="direccion" id="direccion"></div>
        <div class="form-group"><label>Email</label><input type="email" class="form-control" name="email" id="email"></div>
        <div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div>
    </form><div id="mensajeProveedor"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarProveedor"></button></div>
</div></div></div>
<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarProveedor" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Proveedor</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarProveedor"><h5>¿Seguro que desea eliminar este proveedor?</h5><div id="mensajeEliminarProveedor"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarProveedor">Eliminar</button></div></div></div></div>

<%@include file="footer.jsp" %>

<script>
var dataTableProveedores;

$(document).ready(function() {
    // Inicializar DataTable una sola vez
    dataTableProveedores = $('#tablaProveedores').DataTable({
        responsive: true,
        lengthChange: false,
        autoWidth: false,
        buttons: []
    });
    cargarTablaProveedores();
});

function cargarTablaProveedores() {
    $.ajax({
        type: "post",
        url: "ProveedorServlet",
        data: { tipo: "listar" },
        success: function(r) {
            // Limpiar y agregar nuevas filas manteniendo la instancia DataTable
            dataTableProveedores.clear();
            // r es HTML de filas, debemos convertirlo a array de datos
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var row = $(this);
                var rowData = [];
                row.find('td').each(function() {
                    rowData.push($(this).html());
                });
                if (rowData.length > 0) {
                    dataTableProveedores.row.add(rowData);
                }
            });
            dataTableProveedores.draw();
        },
        error: function() { 
            $("#listaProveedores").html('<tr><td colspan="7" class="text-danger">Error al cargar datos</td></tr>');
        }
    });
}

function abrirModalNuevoProveedor() {
    $("#modalProveedorLabel").html("Nuevo Proveedor");
    $("#tipoProveedor").val("Guardar");
    $("#btnGuardarProveedor").html("Guardar");
    $("#formProveedor")[0].reset();
    $("#pkProveedor").val("");
    $("#mensajeProveedor").html("");
    $('#modalProveedor').modal('show');
}

function abrirModalEditarProveedor(id, razon_social, ruc, direccion, telefono, email, activo) {
    $("#modalProveedorLabel").html("Editar Proveedor");
    $("#tipoProveedor").val("Modificar");
    $("#btnGuardarProveedor").html("Modificar");
    $("#pkProveedor").val(id);
    $("#razonSocial").val(razon_social);
    $("#ruc").val(ruc);
    $("#direccion").val(direccion);
    $("#telefono").val(telefono);
    $("#email").val(email);
    $("select[name='activo']").val(activo.toString());
    $("#mensajeProveedor").html("");
    $('#modalProveedor').modal('show');
}

function abrirModalEliminarProveedor(id) {
    $("#pkEliminarProveedor").val(id);
    $("#mensajeEliminarProveedor").html("");
    $('#modalEliminarProveedor').modal('show');
}
function imprimirReporte() {
    // Abre el PDF en una nueva pestaña
    window.open('ProveedorServlet?tipo=imprimir', '_blank');
}
function imprimirProveedor(id) {
    // Abre el PDF individual que genera ReporteIndividualServlet
    window.open('ReporteIndividualServlet?reporte=proveedor&cod=' + id, '_blank');
}
$("#btnGuardarProveedor").click(function() {
    $.ajax({
        type: "post",
        url: "ProveedorServlet",
        data: $("#formProveedor").serialize(),
        success: function(r) {
            $("#mensajeProveedor").html(r);
            if (r.includes("éxito")) {
                cargarTablaProveedores(); // Recarga los datos sin destruir la tabla
                setTimeout(() => $('#modalProveedor').modal('hide'), 1500);
            }
        }
    });
});

$("#btnConfirmarEliminarProveedor").click(function() {
    var id = $("#pkEliminarProveedor").val();
    $.ajax({
        type: "post",
        url: "ProveedorServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarProveedor").html(r);
            if (r.includes("éxito")) {
                cargarTablaProveedores();
                setTimeout(() => $('#modalEliminarProveedor').modal('hide'), 1500);
            }
        }
    });
});
</script>
</body>
</html>
