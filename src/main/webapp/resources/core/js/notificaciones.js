// ====================================================================
// notificaciones.js - Lógica de WebSocket y UI (Genérica para todos los roles)
// ====================================================================

let contadorNotificaciones = 0;
const contadorElement = document.getElementById('notificacion-contador');
// Asegúrate de que este contextPath coincida con la configuración de tu Jetty/Spring
const contextPath = '/spring';

// 1. CONFIGURACIÓN GLOBAL DE TOASTR
// Se fuerzan las opciones de Easing a 'linear' para evitar el S.easing error
toastr.options = {
    "closeButton": true,
    "progressBar": false,
    "positionClass": "toast-top-right",

    "showDuration": "0",
    "hideDuration": "0",
    "timeOut": "5000",
    "extendedTimeOut": "0",
    "showEasing": "swing",
    "hideEasing": "swing",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut",
    "escapeHtml": true
};

/**
 * Función para actualizar el contador visual en la navbar.
 * @param {number} incremento - Cantidad a sumar (ej: 1 o -1), o 0 para solo mostrar.
 */
function actualizarContador(incremento) {
    contadorNotificaciones += incremento;

    if (contadorNotificaciones < 0) {
        contadorNotificaciones = 0;
    }

    if (contadorElement) {
        contadorElement.textContent = contadorNotificaciones;
        contadorElement.style.display = contadorNotificaciones > 0 ? 'block' : 'none';
    }
}

/**
 * Función AJAX para marcar una notificación como leída en el backend.
 * @param {number} idNotificacion - El ID de la notificación persistente.
 */
function marcarNotificacionComoLeida(idNotificacion) {
    $.post(contextPath + '/notificaciones/marcar-leida/' + idNotificacion)
        .done(function() {
            actualizarContador(-1);
        })
        .fail(function(xhr) {
            console.error("Error al marcar como leída:", xhr.status);
        });
}


/**
 * Muestra el toast de notificación y añade la lógica de click (Deep Link).
 * @param {object} notificacionDTO - DTO recibido por WebSocket.
 */
function mostrarToastNotificacion(notificacionDTO) {
    const titulo = "¡Notificación de Viaje!";
    const toast = toastr.info(notificacionDTO.mensaje, titulo);

    if (toast && notificacionDTO.urlDestino) {

        // 1. Manejador de clic para el cuerpo del toast (redirige)
        toast.on('click', function() {
            // Detenemos la animación/cierre automático para evitar conflictos con la redirección
            toastr.clear(toast, {force: true});

            if (contadorNotificaciones > 0) {
                marcarNotificacionComoLeida(notificacionDTO.idNotificacion);
            }
            // Pequeño delay para permitir que el clear termine
            setTimeout(() => {
                window.location.href = contextPath + notificacionDTO.urlDestino;
            }, 100);
        });

        // 2. Manejador para el botón de cerrar ('X').
        toast.find('.toast-close-button').on('click', function(e) {
            e.stopPropagation();
            toastr.clear(toast, {force: true});
        });

        // Manejador de hover (Evita que se cierre mientras el cursor está encima)
        toast.hover(
            function() { toastr.options.timeOut = 0; },
            function() { toastr.options.timeOut = 5000; }
        );
    }
}


/**
 * Inicia la conexión WebSocket y la suscripción.
 * @param {number} idUsuario - El ID del usuario logueado (Conductor o Viajero).
 * @param {number} contadorInicial - Contador de no leídas desde el backend.
 */
function iniciarConexionNotificaciones(idUsuario, contadorInicial) {
    if (!idUsuario || idUsuario === 0) {
        console.error("ID de usuario inválido. No se puede iniciar el WebSocket.");
        return;
    }

    // 1. Inicializar el contador con el valor del backend
    contadorNotificaciones = contadorInicial || 0;
    actualizarContador(0); // Muestra el valor inicial

    console.log("Intentando conectar WebSocket para el usuario ID:", idUsuario);

    // 2. Conexión al endpoint
    const socket = new SockJS(contextPath + '/ws-notificaciones');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Conexión STOMP exitosa. Suscribiéndose...');

        // Resincronización del contador al conectar (usando el nuevo endpoint)
        $.get(contextPath + '/notificaciones/contar-no-leidas').done(function(data) {
                const contadorReal = parseInt(data);
                contadorNotificaciones = contadorReal;
                actualizarContador(0);
            });

        // 3. Suscripción al topic personal: /topic/notificaciones/{idUsuario}
        const topicDestino = `/topic/notificaciones/${idUsuario}`;

        stompClient.subscribe(topicDestino, function (mensajeJSON) {
            const notificacionDTO = JSON.parse(mensajeJSON.body);

            console.log("✅ ¡Nueva Notificación Recibida!", notificacionDTO.mensaje);

            // Lógica de UI
            actualizarContador(1);
            mostrarToastNotificacion(notificacionDTO);
        });
    }, function (error) {
        console.error("❌ Error de WebSocket. Reconectando en 5s...", error);
        // Implementación de reconexión
        setTimeout(() => iniciarConexionNotificaciones(idUsuario, contadorNotificaciones), 5000);
    });
}