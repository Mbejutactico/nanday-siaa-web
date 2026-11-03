<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-6"><h1>Unidades de Medida</h1></div>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="container-fluid">
            <div class="card">
                <div class="card-header">
                    <button class="btn btn-success" data-toggle="modal" data-target="#modalUnidad" onclick="abrirModalNuevaUnidad()">Nueva Unidad</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
                <div class="card-body">
                    <table id="tablaUnidades" class="table table-bordered table-striped">
                        <thead>
                            <tr><th>ID</th><th>Nombre</th><th>Abreviatura</th><th>Activo</th><th>Acción</th></tr>
                        </thead>
                        <tbody id="listaUnidades"></tbody>
                        <tfoot>
                            <tr><th>ID</th><th>Nombre</th><th>Abreviatura</th><th>Activo</th><th>Acción</th></tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Unidad -->
<div class="modal fade" id="modalUnidad" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header"><h5 id="modalUnidadLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
            <div class="modal-body">
                <form id="formUnidad">
                    <input type="hidden" name="tipo" id="tipoUnidad">
                    <input type="hidden" name="pk" id="pkUnidad">
                    <div class="form-group"><label>Nombre</label><input type="text" class="form-control" name="nombre" id="nombreUnidad" required></div>
                    <div class="form-group"><label>Abreviatura</label><input type="text" class="form-control" name="abreviatura" id="abreviaturaUnidad"></div>
                    <div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div>
                </form>
                <div id="mensajeUnidad"></div>
            </div>
            <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarUnidad"></button></div>
        </div>
    </div>
</div>

<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarUnidad" tabindex="-1">
    <div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Unidad</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarUnidad"><h5>¿Seguro que desea eliminar esta unidad?</h5><div id="mensajeEliminarUnidad"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarUnidad">Eliminar</button></div></div></div>
</div>

<%@include file="footer.jsp" %>

<script>
var dataTableUnidades;
$(document).ready(function() {
    dataTableUnidades = $('#tablaUnidades').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarTablaUnidades();
});

function cargarTablaUnidades() {
    $.ajax({
        type: "post",
        url: "UnidadMedidaServlet",
        data: { tipo: "listar" },
        success: function(r) {
            dataTableUnidades.clear();
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableUnidades.row.add(rowData);
            });
            dataTableUnidades.draw();
        }
    });
}

function abrirModalNuevaUnidad() {
    $("#modalUnidadLabel").html("Nueva Unidad de Medida");
    $("#tipoUnidad").val("Guardar");
    $("#btnGuardarUnidad").html("Guardar");
    $("#formUnidad")[0].reset();
    $("#pkUnidad").val("");
    $("#mensajeUnidad").html("");
    $('#modalUnidad').modal('show');
}

function abrirModalEditarUnidad(id, nombre, abreviatura, activo) {
    $("#modalUnidadLabel").html("Editar Unidad de Medida");
    $("#tipoUnidad").val("Modificar");
    $("#btnGuardarUnidad").html("Modificar");
    $("#pkUnidad").val(id);
    $("#nombreUnidad").val(nombre);
    $("#abreviaturaUnidad").val(abreviatura);
    $("select[name='activo']").val(activo.toString());
    $("#mensajeUnidad").html("");
    $('#modalUnidad').modal('show');
}

function abrirModalEliminarUnidad(id) {
    $("#pkEliminarUnidad").val(id);
    $("#mensajeEliminarUnidad").html("");
    $('#modalEliminarUnidad').modal('show');
}
function imprimirReporte() {
    window.open('UnidadMedidaServlet?tipo=imprimir', '_blank');
}
function imprimirUnidad(id) {
    window.open('ReporteIndividualServlet?reporte=unidad&cod=' + id, '_blank');
}
$("#btnGuardarUnidad").click(function() {
    $.ajax({
        type: "post",
        url: "UnidadMedidaServlet",
        data: $("#formUnidad").serialize(),
        success: function(r) {
            $("#mensajeUnidad").html(r);
            if (r.includes("éxito")) cargarTablaUnidades();
            setTimeout(() => $('#modalUnidad').modal('hide'), 1500);
        }
    });
});

$("#btnConfirmarEliminarUnidad").click(function() {
    var id = $("#pkEliminarUnidad").val();
    $.ajax({
        type: "post",
        url: "UnidadMedidaServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarUnidad").html(r);
            if (r.includes("éxito")) cargarTablaUnidades();
            setTimeout(() => $('#modalEliminarUnidad').modal('hide'), 1500);
        }
    });
});
</script>
</body>
</html>
