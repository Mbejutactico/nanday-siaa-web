<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Centros</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" data-toggle="modal" data-target="#modalCentro" onclick="abrirModalNuevoCentro()">Nuevo Centro</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
    <div class="card-body"><table id="tablaCentros" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Código</th><th>Nombre</th><th>Dirección</th><th>Activo</th><th>Acción</th></tr></thead><tbody id="listaCentros"></tbody><tfoot><tr><th>ID</th><th>Código</th><th>Nombre</th><th>Dirección</th><th>Activo</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal Centro -->
<div class="modal fade" id="modalCentro" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 id="modalCentroLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formCentro"><input type="hidden" name="tipo" id="tipoCentro"><input type="hidden" name="pk" id="pkCentro">
        <div class="form-group"><label>Código</label><input type="text" class="form-control" name="codigo" id="codigoCentro"></div>
        <div class="form-group"><label>Nombre</label><input type="text" class="form-control" name="nombre" id="nombreCentro"></div>
        <div class="form-group"><label>Dirección</label><input type="text" class="form-control" name="direccion" id="direccionCentro"></div>
        <div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div>
    </form><div id="mensajeCentro"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarCentro"></button></div>
</div></div></div>
<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarCentro" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Centro</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarCentro"><h5>¿Seguro que desea eliminar este centro?</h5><div id="mensajeEliminarCentro"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarCentro">Eliminar</button></div></div></div></div>

<%@include file="footer.jsp" %>

<script>
var dataTableCentros;

$(document).ready(function() {
    dataTableCentros = $('#tablaCentros').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarTablaCentros();
});

function cargarTablaCentros() {
    $.ajax({
        type: "post",
        url: "CentroServlet",
        data: { tipo: "listar" },
        success: function(r) {
            dataTableCentros.clear();
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableCentros.row.add(rowData);
            });
            dataTableCentros.draw();
        }
    });
}

function abrirModalNuevoCentro() {
    $("#modalCentroLabel").html("Nuevo Centro");
    $("#tipoCentro").val("Guardar");
    $("#btnGuardarCentro").html("Guardar");
    $("#formCentro")[0].reset();
    $("#pkCentro").val("");
    $("#mensajeCentro").html("");
    $('#modalCentro').modal('show');
}

function abrirModalEditarCentro(id, codigo, nombre, direccion, activo) {
    $("#modalCentroLabel").html("Editar Centro");
    $("#tipoCentro").val("Modificar");
    $("#btnGuardarCentro").html("Modificar");
    $("#pkCentro").val(id);
    $("#codigoCentro").val(codigo);
    $("#nombreCentro").val(nombre);
    $("#direccionCentro").val(direccion);
    $("select[name='activo']").val(activo.toString());
    $("#mensajeCentro").html("");
    $('#modalCentro').modal('show');
}

function abrirModalEliminarCentro(id) {
    $("#pkEliminarCentro").val(id);
    $("#mensajeEliminarCentro").html("");
    $('#modalEliminarCentro').modal('show');
}
function imprimirCentro(id) {
    window.open('ReporteIndividualServlet?reporte=centro&cod=' + id, '_blank');
}
function imprimirReporte() {
    window.open('CentroServlet?tipo=imprimir', '_blank');
}
$("#btnGuardarCentro").click(function() {
    $.ajax({
        type: "post",
        url: "CentroServlet",
        data: $("#formCentro").serialize(),
        success: function(r) {
            $("#mensajeCentro").html(r);
            if (r.includes("éxito")) cargarTablaCentros();
            setTimeout(() => $('#modalCentro').modal('hide'), 1500);
        }
    });
});

$("#btnConfirmarEliminarCentro").click(function() {
    var id = $("#pkEliminarCentro").val();
    $.ajax({
        type: "post",
        url: "CentroServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarCentro").html(r);
            if (r.includes("éxito")) cargarTablaCentros();
            setTimeout(() => $('#modalEliminarCentro').modal('hide'), 1500);
        }
    });
});
</script>
</body>
</html>
