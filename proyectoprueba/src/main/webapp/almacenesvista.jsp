<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Almacenes</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" data-toggle="modal" data-target="#modalAlmacen" onclick="abrirModalNuevoAlmacen()">Nuevo Almacén</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
        <i class="fas fa-print"></i> Imprimir Listado
    </button>
                </div>
    <div class="card-body"><table id="tablaAlmacenes" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Ubicación</th><th>Centro</th><th>Activo</th><th>Acción</th></tr></thead><tbody id="listaAlmacenes"></tbody><tfoot><tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Ubicación</th><th>Centro</th><th>Activo</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal -->
<div class="modal fade" id="modalAlmacen" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 class="modal-title" id="modalAlmacenLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formAlmacen"><input type="hidden" name="tipo" id="tipoAlmacen"><input type="hidden" name="pk" id="pkAlmacen">
        <div class="form-group"><label>Nombre</label><input type="text" class="form-control" name="nombre" id="nombreAlmacen"></div>
        <div class="form-group"><label>Descripción</label><textarea class="form-control" name="descripcion" id="descripcionAlmacen"></textarea></div>
        <div class="form-group"><label>Ubicación</label><input type="text" class="form-control" name="ubicacion" id="ubicacionAlmacen"></div>
        <div class="form-group"><label>Centro</label><select class="form-control" name="id_centro" id="comboCentroAlmacen"></select></div>
        <div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div>
    </form><div id="mensajeAlmacen"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarAlmacen"></button></div>
</div></div></div>
<div class="modal fade" id="modalEliminarAlmacen" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Almacén</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarAlmacen"><h5>¿Seguro que desea eliminar este almacén?</h5><div id="mensajeEliminarAlmacen"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarAlmacen">Eliminar</button></div></div></div></div>
<%@include file="footer.jsp" %>
<script>
var dataTableAlmacenes;

$(document).ready(function() {
    dataTableAlmacenes = $('#tablaAlmacenes').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarComboCentros();
    cargarTablaAlmacenes();
});

function cargarComboCentros() {
    $.ajax({ url: "CentroServlet", data: { tipo: "listarCombo" }, success: function(r) { $("#comboCentroAlmacen").html(r); } });
}

function cargarTablaAlmacenes() {
    $.ajax({
        type: "post",
        url: "AlmacenServlet",
        data: { tipo: "listar" },
        success: function(r) {
            dataTableAlmacenes.clear();
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableAlmacenes.row.add(rowData);
            });
            dataTableAlmacenes.draw();
        }
    });
}

function abrirModalNuevoAlmacen() {
    $("#modalAlmacenLabel").html("Nuevo Almacén");
    $("#tipoAlmacen").val("Guardar");
    $("#btnGuardarAlmacen").html("Guardar");
    $("#formAlmacen")[0].reset();
    $("#pkAlmacen").val("");
    $("#mensajeAlmacen").html("");
    $('#modalAlmacen').modal('show');
}

function abrirModalEditarAlmacen(id, nombre, descripcion, ubicacion, id_centro, activo) {
    $("#modalAlmacenLabel").html("Editar Almacén");
    $("#tipoAlmacen").val("Modificar");
    $("#btnGuardarAlmacen").html("Modificar");
    $("#pkAlmacen").val(id);
    $("#nombreAlmacen").val(nombre);
    $("#descripcionAlmacen").val(descripcion);
    $("#ubicacionAlmacen").val(ubicacion);
    $("select[name='activo']").val(activo.toString());
    setTimeout(function() { $("#comboCentroAlmacen").val(id_centro); }, 300);
    $("#mensajeAlmacen").html("");
    $('#modalAlmacen').modal('show');
}

function abrirModalEliminarAlmacen(id) {
    $("#pkEliminarAlmacen").val(id);
    $("#mensajeEliminarAlmacen").html("");
    $('#modalEliminarAlmacen').modal('show');
}
function imprimirReporte() {
    window.open('AlmacenServlet?tipo=imprimir', '_blank');
}
function imprimirAlmacen(id) {
    window.open('ReporteIndividualServlet?reporte=almacen&cod=' + id, '_blank');
}
$("#btnGuardarAlmacen").click(function() {
    $.ajax({
        type: "post",
        url: "AlmacenServlet",
        data: $("#formAlmacen").serialize(),
        success: function(r) {
            $("#mensajeAlmacen").html(r);
            if (r.includes("éxito")) cargarTablaAlmacenes();
            setTimeout(() => $('#modalAlmacen').modal('hide'), 1500);
        }
    });
});

$("#btnConfirmarEliminarAlmacen").click(function() {
    var id = $("#pkEliminarAlmacen").val();
    $.ajax({
        type: "post",
        url: "AlmacenServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarAlmacen").html(r);
            if (r.includes("éxito")) cargarTablaAlmacenes();
            setTimeout(() => $('#modalEliminarAlmacen').modal('hide'), 1500);
        }
    });
});
</script>
</body>
</html>
