<%@include file="header.jsp" %>
<div class="content-wrapper">
    <div class="content-header"><div class="container-fluid"><div class="row mb-2"><div class="col-sm-6"><h1>Centros de Costo</h1></div></div></div></div>
    <div class="content"><div class="container-fluid"><div class="card"><div class="card-header"><button class="btn btn-success" data-toggle="modal" data-target="#modalCC" onclick="abrirModalNuevoCC()">Nuevo Centro Costo</button>
                <button class="btn btn-primary" onclick="imprimirReporte()">
                        <i class="fas fa-print"></i> Imprimir Listado
                    </button>
                </div>
    <div class="card-body"><table id="tablaCC" class="table table-bordered table-striped"><thead><tr><th>ID</th><th>Código</th><th>Descripción</th><th>Centro</th><th>Activo</th><th>Acción</th></tr></thead><tbody id="listaCC"></tbody><tfoot><tr><th>ID</th><th>Código</th><th>Descripción</th><th>Centro</th><th>Activo</th><th>Acción</th></tr></tfoot></table></div></div></div></div>
</div>
<!-- Modal Centro Costo -->
<div class="modal fade" id="modalCC" tabindex="-1"><div class="modal-dialog"><div class="modal-content">
    <div class="modal-header"><h5 id="modalCCLabel"></h5><button type="button" class="close" data-dismiss="modal">&times;</button></div>
    <div class="modal-body"><form id="formCC"><input type="hidden" name="tipo" id="tipoCC"><input type="hidden" name="pk" id="pkCC">
        <div class="form-group"><label>Código</label><input type="text" class="form-control" name="codigo" id="codigoCC"></div>
        <div class="form-group"><label>Descripción</label><textarea class="form-control" name="descripcion" id="descripcionCC"></textarea></div>
        <div class="form-group"><label>Centro</label><select class="form-control" name="id_centro" id="comboCentroCC"></select></div>
        <div class="form-group"><label>Activo</label><select class="form-control" name="activo"><option value="true">Sí</option><option value="false">No</option></select></div>
    </form><div id="mensajeCC"></div></div>
    <div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-primary" id="btnGuardarCC"></button></div>
</div></div></div>
<!-- Modal Eliminar -->
<div class="modal fade" id="modalEliminarCC" tabindex="-1"><div class="modal-dialog"><div class="modal-content"><div class="modal-header"><h5>Eliminar Centro Costo</h5><button type="button" class="close" data-dismiss="modal">&times;</button></div><div class="modal-body"><input type="hidden" id="pkEliminarCC"><h5>¿Seguro que desea eliminar este centro de costo?</h5><div id="mensajeEliminarCC"></div></div><div class="modal-footer"><button class="btn btn-secondary" data-dismiss="modal">Cerrar</button><button class="btn btn-danger" id="btnConfirmarEliminarCC">Eliminar</button></div></div></div></div>

<%@include file="footer.jsp" %>

<script>
var dataTableCC;

$(document).ready(function() {
    dataTableCC = $('#tablaCC').DataTable({
        responsive: true, lengthChange: false, autoWidth: false,
        buttons: []
    });
    cargarComboCentros();
    cargarTablaCC();
});

function cargarComboCentros() {
    $.ajax({ url: "CentroServlet", data: { tipo: "listarCombo" }, success: function(r) { $("#comboCentroCC").html(r); } });
}

function cargarTablaCC() {
    $.ajax({
        type: "post",
        url: "CentroCostoServlet",
        data: { tipo: "listar" },
        success: function(r) {
            dataTableCC.clear();
            var tempDiv = $('<div>').html(r);
            tempDiv.find('tr').each(function() {
                var rowData = [];
                $(this).find('td').each(function() { rowData.push($(this).html()); });
                if (rowData.length) dataTableCC.row.add(rowData);
            });
            dataTableCC.draw();
        }
    });
}

function abrirModalNuevoCC() {
    $("#modalCCLabel").html("Nuevo Centro Costo");
    $("#tipoCC").val("Guardar");
    $("#btnGuardarCC").html("Guardar");
    $("#formCC")[0].reset();
    $("#pkCC").val("");
    $("#mensajeCC").html("");
    $('#modalCC').modal('show');
}

function abrirModalEditarCC(id, codigo, descripcion, id_centro, activo) {
    $("#modalCCLabel").html("Editar Centro Costo");
    $("#tipoCC").val("Modificar");
    $("#btnGuardarCC").html("Modificar");
    $("#pkCC").val(id);
    $("#codigoCC").val(codigo);
    $("#descripcionCC").val(descripcion);
    $("select[name='activo']").val(activo.toString());
    setTimeout(function() { $("#comboCentroCC").val(id_centro); }, 300);
    $("#mensajeCC").html("");
    $('#modalCC').modal('show');
}

function abrirModalEliminarCC(id) {
    $("#pkEliminarCC").val(id);
    $("#mensajeEliminarCC").html("");
    $('#modalEliminarCC').modal('show');
}

function imprimirReporte() {
    window.open('CentroCostoServlet?tipo=imprimir', '_blank');
}
function imprimirCentroCosto(id) {
    window.open('ReporteIndividualServlet?reporte=centroCosto&cod=' + id, '_blank');
}
$("#btnGuardarCC").click(function() {
    $.ajax({
        type: "post",
        url: "CentroCostoServlet",
        data: $("#formCC").serialize(),
        success: function(r) {
            $("#mensajeCC").html(r);
            if (r.includes("éxito")) cargarTablaCC();
            setTimeout(() => $('#modalCC').modal('hide'), 1500);
        }
    });
});

$("#btnConfirmarEliminarCC").click(function() {
    var id = $("#pkEliminarCC").val();
    $.ajax({
        type: "post",
        url: "CentroCostoServlet",
        data: { tipo: "Borrar", pk: id },
        success: function(r) {
            $("#mensajeEliminarCC").html(r);
            if (r.includes("éxito")) cargarTablaCC();
            setTimeout(() => $('#modalEliminarCC').modal('hide'), 1500);
        }
    });
});
</script>
</body>
</html>
