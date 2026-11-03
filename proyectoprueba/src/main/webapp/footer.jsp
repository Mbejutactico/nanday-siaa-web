<!-- Panel lateral derecho -->
<aside class="control-sidebar control-sidebar-dark">
    <div class="p-3">
        <h5>Panel de Control</h5>
        <p>Configuración rápida del sistema.</p>
    </div>
</aside>
<!-- fin del panel lateral derecho -->

<!-- Main Footer con estilo Ñanday -->
<footer class="main-footer">
    <div class="float-right d-none d-sm-block">
        <b>Versión</b> 1.0
    </div>
    <strong>&copy; <span id="anioActual"></span> Ñanday SIAA - Sistema de Gestión de Activos e Insumos.</strong> Todos los derechos reservados.
</footer>
</div>
<!-- fin del contenedor general -->

<!-- jQuery -->
<script src="plugins/jquery/jquery.min.js"></script>
<script>
    // Protección CSRF: todas las peticiones AJAX al propio sistema llevan el token de la sesión
    // (lo publica header.jsp en <meta name="csrf-token">; lo valida FiltroSeguridad).
    (function () {
        var meta = document.querySelector('meta[name="csrf-token"]');
        var token = meta ? meta.getAttribute('content') : '';
        $.ajaxPrefilter(function (opciones, original, xhr) {
            if (!opciones.crossDomain && token) {
                xhr.setRequestHeader('X-CSRF-Token', token);
            }
        });
    })();
</script>
<!-- Bootstrap 4 -->
<script src="plugins/bootstrap/js/bootstrap.bundle.min.js"></script>
<!-- DataTables y complementos -->
<script src="plugins/datatables/jquery.dataTables.min.js"></script>
<script src="plugins/datatables-bs4/js/dataTables.bootstrap4.min.js"></script>
<script src="plugins/datatables-responsive/js/dataTables.responsive.min.js"></script>
<script src="plugins/datatables-responsive/js/responsive.bootstrap4.min.js"></script>
<script src="plugins/datatables-buttons/js/dataTables.buttons.min.js"></script>
<script src="plugins/datatables-buttons/js/buttons.bootstrap4.min.js"></script>
<script src="plugins/jszip/jszip.min.js"></script>
<script src="plugins/pdfmake/pdfmake.min.js"></script>
<script src="plugins/pdfmake/vfs_fonts.js"></script>
<script src="plugins/datatables-buttons/js/buttons.html5.min.js"></script>
<script src="plugins/datatables-buttons/js/buttons.print.min.js"></script>
<script src="plugins/datatables-buttons/js/buttons.colVis.min.js"></script>
<!-- Script de AdminLTE -->
<script src="dist/js/adminlte.min.js"></script>
<script src="js/perfil.js"></script>
<!-- Avisos, confirmaciones e indicador de carga -->
<script src="plugins/sweetalert2/sweetalert2.min.js"></script>
<script src="js/nanday-ui.js"></script>

<!-- Idioma español para TODAS las tablas (DataTables) del sistema -->
<script>
    if ($.fn.dataTable) {
        $.extend(true, $.fn.dataTable.defaults, {
            language: {
                processing: "Procesando...",
                search: "Buscar:",
                lengthMenu: "Mostrar _MENU_ registros",
                info: "Mostrando _START_ a _END_ de _TOTAL_ registros",
                infoEmpty: "Mostrando 0 a 0 de 0 registros",
                infoFiltered: "(filtrado de _MAX_ registros en total)",
                infoThousands: ".",
                decimal: ",",
                thousands: ".",
                loadingRecords: "Cargando...",
                zeroRecords: "No se encontraron resultados",
                emptyTable: "No hay datos disponibles en esta tabla",
                paginate: {
                    first: "Primero",
                    previous: "Anterior",
                    next: "Siguiente",
                    last: "Último"
                },
                aria: {
                    sortAscending: ": activar para ordenar de forma ascendente",
                    sortDescending: ": activar para ordenar de forma descendente"
                },
                buttons: {
                    copy: "Copiar",
                    csv: "CSV",
                    excel: "Excel",
                    pdf: "PDF",
                    print: "Imprimir",
                    colvis: "Columnas visibles",
                    copyTitle: "Copiado al portapapeles",
                    copySuccess: { _: "%d filas copiadas", 1: "1 fila copiada" }
                }
            }
        });
    }
</script>

<!-- Al terminar de llegar los datos por AJAX, AdminLTE recalcula la altura del
     contenido para que el footer quede siempre debajo (todas las pantallas) -->
<script>
    $(document).ajaxStop(function () {
        $(window).trigger('resize');
    });
</script>

<!-- Script para mostrar el año actual dinámicamente -->
<script>
    // Escapa texto antes de insertarlo como HTML (evita XSS con datos que vienen del servidor en JSON)
    function escHtml(valor) {
        if (valor === null || valor === undefined) return '';
        return String(valor)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    document.getElementById('anioActual').innerText = new Date().getFullYear();
</script>