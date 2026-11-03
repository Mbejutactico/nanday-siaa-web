// Ñanday SIAA - avisos, confirmaciones, indicador de carga y alertas de stock.
// Requiere jQuery, Bootstrap y SweetAlert2 (todo local, en plugins/).
(function ($) {

    // ---------- Avisos y confirmaciones (SweetAlert2) ----------

    function esc(t) {
        return String(t === null || t === undefined ? '' : t)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    // Tipo de aviso según el texto: error, éxito o advertencia
    function tipoDe(texto) {
        var t = String(texto || '').toLowerCase();
        if (/^error|no se pud|no fue posible|fall[óo]|no se encontr/.test(t)) return 'error';
        if (/\bno\b|debe|supera|seleccione|complete|indique|ingrese|cargue|agregue/.test(t)) return 'warning';
        if (/(registrad|guardad|actualizad|modificad|eliminad|recibid|aprobad|anulad|cread|agregad|quitad|enviad|finalizad|habilitad)[oa]\(?s?\)?|correctamente|con éxito/.test(t)) return 'success';
        return 'warning';
    }

    // Aviso pendiente: si la página cambia enseguida (por ejemplo, después de guardar),
    // el aviso se muestra en la página siguiente.
    var CLAVE_PENDIENTE = 'nanday.avisoPendiente';
    function guardarPendiente(tipo, texto) {
        try { sessionStorage.setItem(CLAVE_PENDIENTE, JSON.stringify({ tipo: tipo, texto: String(texto), t: Date.now() })); } catch (e) { }
    }
    function borrarPendiente() {
        try { sessionStorage.removeItem(CLAVE_PENDIENTE); } catch (e) { }
    }
    function leerPendiente() {
        try {
            var p = JSON.parse(sessionStorage.getItem(CLAVE_PENDIENTE) || 'null');
            borrarPendiente();
            return p && (Date.now() - p.t) < 8000 ? p : null;
        } catch (e) { return null; }
    }

    // Versiones simples por si SweetAlert2 no se cargó: las pantallas siguen funcionando
    var alertaNavegador = window.alert, confirmNavegador = window.confirm;
    window.notificar = function (tipo, texto) { alertaNavegador(texto); };
    window.aviso = function (texto) { alertaNavegador(texto); };
    window.preguntar = function (texto, siAcepta) { if (confirmNavegador(texto)) siAcepta(); };
    window.confirmado = function (texto) { return confirmNavegador(texto); };

    if (!window.Swal) {
        return;   // sin SweetAlert2 quedan los mensajes propios del navegador
    }

    // Bootstrap 4 retiene el foco dentro de sus modales; mientras haya un aviso abierto se libera
    function liberarFoco() { $(document).off('focusin.bs.modal'); }
    function enfocarAviso(popup) {
        if (popup && !popup.contains(document.activeElement) && Swal.getConfirmButton()) {
            Swal.getConfirmButton().focus();
        }
    }

    // Si un modal termina de abrirse con un aviso ya visible, el aviso conserva el foco
    $(document).on('shown.bs.modal', function () {
        if (Swal.isVisible() && !Swal.getPopup().classList.contains('swal2-toast')) {
            liberarFoco();
            enfocarAviso(Swal.getPopup());
        }
    });

    var Toast = Swal.mixin({
        toast: true, position: 'top-end', showConfirmButton: false,
        timer: 3500, timerProgressBar: true
    });

    var BASE = {
        confirmButtonColor: '#2E7D32', cancelButtonColor: '#6c757d',
        confirmButtonText: 'Aceptar', cancelButtonText: 'Cancelar',
        willOpen: liberarFoco, didOpen: enfocarAviso
    };

    // Notificación breve en la esquina (no interrumpe el trabajo).
    // persistir = true: si la página cambia antes de que se vea, se muestra en la siguiente.
    window.notificar = function (tipo, texto, persistir) {
        if (persistir) guardarPendiente(tipo, texto);
        Toast.fire({ icon: tipo || 'info', title: String(texto), didClose: persistir ? borrarPendiente : undefined });
    };

    // Aviso en ventana: error o dato que falta completar
    window.aviso = function (texto, tipo) {
        tipo = tipo || tipoDe(texto);
        guardarPendiente(tipo, texto);
        return Swal.fire($.extend({}, BASE, {
            icon: tipo,
            title: tipo === 'error' ? 'Ocurrió un problema' : (tipo === 'success' ? 'Listo' : 'Atención'),
            html: esc(texto).replace(/\n/g, '<br>'),
            didClose: borrarPendiente
        }));
    };

    // Todos los alert() del sistema usan el aviso del sistema: los de éxito como notificación breve
    window.alert = function (texto) {
        if (tipoDe(texto) === 'success') {
            window.notificar('success', texto, true);
        } else {
            window.aviso(texto);
        }
    };

    // Pregunta con botones Sí / Cancelar; si acepta, ejecuta siAcepta()
    window.preguntar = function (texto, siAcepta, opciones) {
        opciones = opciones || {};
        var peligro = /eliminar|quitar|anular|cancelar|baja/i.test(texto);
        Swal.fire($.extend({}, BASE, {
            icon: opciones.icono || (peligro ? 'warning' : 'question'),
            title: opciones.titulo || 'Confirmar',
            html: esc(texto).replace(/\n/g, '<br>'),
            showCancelButton: true,
            reverseButtons: true,
            confirmButtonText: opciones.boton || 'Sí, continuar',
            cancelButtonText: opciones.cancelar || 'Cancelar',
            confirmButtonColor: peligro ? '#C62828' : '#2E7D32'
        })).then(function (r) {
            if (r.isConfirmed) siAcepta();
        });
    };

    // Reemplazo de "if (!confirm(texto)) return;" dentro de una función:
    //   if (!confirmado(texto, this, arguments)) return;
    // Muestra la pregunta y devuelve false; si el usuario acepta, vuelve a llamar a la
    // misma función con los mismos datos y esta vez confirmado() devuelve true.
    var aprobada = null;
    window.confirmado = function (texto, contexto, argumentos, opciones) {
        if (aprobada !== null && aprobada === texto) {
            aprobada = null;
            return true;
        }
        var funcion = argumentos.callee;
        window.preguntar(texto, function () {
            aprobada = texto;
            try {
                funcion.apply(contexto, argumentos);
            } finally {
                aprobada = null;
            }
        }, opciones);
        return false;
    };

    // ---------- Indicador de carga ----------

    var $carga = $('<div id="nandayCarga" class="nanday-carga" role="status" aria-live="polite">' +
        '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Cargando...</div>');
    var $barra = $('<div class="nanday-barra-carga" aria-hidden="true"></div>');
    var enCurso = 0, demora = null;

    function mostrarCarga() { demora = null; $carga.addClass('visible'); $barra.addClass('visible'); }
    function ocultarCarga() {
        if (demora) { clearTimeout(demora); demora = null; }
        $carga.removeClass('visible'); $barra.removeClass('visible');
    }

    $(function () {
        $('body').append($carga).append($barra);
        var pendiente = leerPendiente();
        if (pendiente) {
            if (pendiente.tipo === 'success') window.notificar('success', pendiente.texto);
            else window.aviso(pendiente.texto, pendiente.tipo);
        }
    });

    // Solo aparece si la respuesta tarda más de 0,4 s (las consultas rápidas no parpadean)
    $(document).ajaxSend(function () {
        enCurso++;
        if (!demora && !$carga.hasClass('visible')) demora = setTimeout(mostrarCarga, 400);
    });
    $(document).ajaxComplete(function () {
        enCurso = Math.max(0, enCurso - 1);
        if (enCurso === 0) ocultarCarga();
    });

    // Los PDF se abren en una pestaña nueva: se avisa que se está generando
    var abrirVentana = window.open;
    window.open = function (url) {
        if (typeof url === 'string' && /imprimir|reporte=/i.test(url)) {
            window.notificar('info', 'Generando el PDF... se abre en una pestaña nueva.');
        }
        return abrirVentana.apply(window, arguments);
    };

    // ---------- Alertas de stock mínimo (cuadro del tablero) ----------

    var consultaAlertas = null;
    window.Nanday = {
        // Una sola consulta por página
        alertasStock: function () {
            if (!consultaAlertas) {
                consultaAlertas = $.ajax({ url: 'InventarioServlet', type: 'post', dataType: 'json',
                    data: { tipo: 'alertasMinimo' }, global: false });
            }
            return consultaAlertas;
        }
    };

    // ---------- Campana de notificaciones (barra superior) ----------
    // Pendientes: siguen mientras no se resuelvan. Novedades: dejan de contarse al abrir la campana.

    var ICONOS = {
        STOCK: 'fas fa-exclamation-triangle text-warning',
        APROBAR: 'fas fa-check-double text-primary',
        RECIBIR: 'fas fa-inbox text-info',
        OT: 'fas fa-clipboard-list text-success',
        SOLICITUD: 'fas fa-dolly text-secondary',
        SEGURIDAD: 'fas fa-user-lock text-danger'
    };
    var CLAVE_AVISADAS = 'nanday.notifAvisadas';
    var ACTUALIZAR_MS = 60000, INACTIVIDAD_MS = 5 * 60000;
    var ultimaActividad = Date.now(), ultimoDato = null, consultando = false;

    function hace(iso) {
        if (!iso) return '';
        var d = new Date(iso.replace(' ', 'T'));
        if (isNaN(d.getTime())) return '';
        var min = Math.round((Date.now() - d.getTime()) / 60000);
        if (min < 1) return 'recién';
        if (min < 60) return 'hace ' + min + ' min';
        var h = Math.round(min / 60);
        if (h < 24) return 'hace ' + h + (h === 1 ? ' hora' : ' horas');
        var dias = Math.round(h / 24);
        return dias === 1 ? 'ayer' : 'hace ' + dias + ' días';
    }

    function leerAvisadas() {
        try { return JSON.parse(sessionStorage.getItem(CLAVE_AVISADAS) || '[]'); } catch (e) { return null; }
    }
    function guardarAvisadas(l) {
        try { sessionStorage.setItem(CLAVE_AVISADAS, JSON.stringify(l.slice(-50))); } catch (e) { }
    }

    function enlace(n, contenido, clase) {
        return '<a class="dropdown-item campana-item ' + (clase || '') + '" href="' + esc(n.url) + '"' +
            (n.ventana_nueva ? ' target="_blank" rel="noopener"' : '') + '>' + contenido + '</a>';
    }

    function dibujar(d, primeraVez) {
        var pend = d.pendientes || [], nov = d.novedades || [];
        var total = d.nuevas || 0;
        $.each(pend, function (i, n) { total += n.cantidad; });

        var $badge = $('#campanaCantidad');
        if (total > 0) {
            $badge.text(total > 99 ? '99+' : total)
                .toggleClass('badge-danger', d.nuevas > 0).toggleClass('badge-warning', !(d.nuevas > 0)).show();
        } else {
            $badge.hide();
        }
        $('#campanaTitulo').text(total > 0 ? (total === 1 ? '1 notificación' : total + ' notificaciones') : 'Notificaciones');

        var h = '';
        if (pend.length) {
            h += '<h6 class="dropdown-header campana-seccion">Pendientes</h6>';
            $.each(pend, function (i, n) {
                h += enlace(n, '<i class="' + (ICONOS[n.tipo] || 'fas fa-bell') + ' mr-2"></i><span class="campana-texto">' + esc(n.texto) + '</span>');
            });
        }
        if (nov.length) {
            h += '<h6 class="dropdown-header campana-seccion">Novedades</h6>';
            $.each(nov, function (i, n) {
                h += enlace(n, '<i class="' + (ICONOS[n.tipo] || 'fas fa-bell') + ' mr-2"></i><span class="campana-texto">' +
                    (n.nueva ? '<span class="campana-punto" title="Nueva"></span>' : '') + esc(n.texto) +
                    (n.detalle ? '<br><small class="text-muted">' + esc(n.detalle) + '</small>' : '') +
                    '<br><small class="text-muted">' + esc(hace(n.fecha)) + '</small></span>', n.nueva ? 'campana-nueva' : '');
            });
        }
        if (!pend.length && !nov.length) {
            h = '<span class="dropdown-item text-muted small campana-vacia"><i class="fas fa-check text-success mr-2"></i>No hay notificaciones.</span>';
        }
        $('#campanaLista').html(h);

        // Aviso breve de las novedades que todavía no se avisaron en esta sesión
        var avisadas = leerAvisadas();
        if (avisadas === null && primeraVez) return;   // sin almacenamiento: solo se avisa lo que llega después
        avisadas = avisadas || [];
        var nuevas = $.grep(nov, function (n) { return n.nueva && n.clave && avisadas.indexOf(n.clave) < 0; });
        if (nuevas.length) {
            window.notificar('info', nuevas.length === 1 ? nuevas[0].texto : nuevas.length + ' notificaciones nuevas');
            $.each(nuevas, function (i, n) { avisadas.push(n.clave); });
            guardarAvisadas(avisadas);
        }
    }

    function consultar(primeraVez) {
        if (consultando) return;
        consultando = true;
        // global: false = no muestra el indicador de carga
        $.ajax({ url: 'NotificacionServlet', type: 'post', dataType: 'json', data: { tipo: 'listar' }, global: false })
            .done(function (d) {
                if (d && !d.error) { ultimoDato = d; dibujar(d, primeraVez); }
            })
            .always(function () { consultando = false; });
    }

    $(function () {
        var $campana = $('#campanaNotif');
        if (!$campana.length) return;
        consultar(true);

        // Se actualiza cada minuto mientras el usuario está usando el sistema.
        // Si deja de usarlo, se detiene: así la sesión puede vencer por inactividad.
        $(document).on('mousemove keydown click scroll touchstart', function () { ultimaActividad = Date.now(); });
        setInterval(function () {
            if (document.hidden || Date.now() - ultimaActividad > INACTIVIDAD_MS) return;
            consultar(false);
        }, ACTUALIZAR_MS);

        // Al abrir la campana las novedades quedan como vistas
        $campana.on('shown.bs.dropdown', function () {
            if (!ultimoDato || !(ultimoDato.nuevas > 0)) return;
            $.ajax({ url: 'NotificacionServlet', type: 'post', dataType: 'json', data: { tipo: 'marcarVistas' }, global: false });
        });
        $campana.on('hidden.bs.dropdown', function () {
            if (!ultimoDato || !(ultimoDato.nuevas > 0)) return;
            $.each(ultimoDato.novedades || [], function (i, n) { n.nueva = false; });
            ultimoDato.nuevas = 0;
            dibujar(ultimoDato, false);
        });
    });

})(jQuery);
