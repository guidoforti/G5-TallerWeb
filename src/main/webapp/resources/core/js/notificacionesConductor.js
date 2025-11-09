// ====================================================================
// notificacionesConductor.js - Lógica de WebSocket y UI para Notificaciones
//
// Dependencias: jQuery, Toastr, SockJS, STOMP
// Recibe: idUsuario (Long), contadorInicial (Integer)
// ====================================================================

let contadorNotificaciones = 0;
const contadorElement = document.getElementById('notificacion-contador');
// Asegúrate de que este contextPath coincida con la configuración de tu Jetty/Spring
const contextPath = '/spring';

// 1. CONFIGURACIÓN GLOBAL DE TOASTR (Se ejecuta una sola vez al cargar el script)
toastr.options = {
    "closeButton": true,
    "progressBar": true,
    "positionClass": "toast-top-right",
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut"
};

/**
 * Función para actualizar el contador visual en la navbar.
 * @param {number} incremento - Cantidad a sumar (ej: 1 o -1), o 0 para solo mostrar.
 */
function actualizarContador(incremento) {
    contadorNotificaciones += incremento;

    // Aseguramos que el contador nunca sea negativo
    if (contadorNotificaciones < 0) {
        contadorNotificaciones = 0;
    }

    if (contadorElement) {
        contadorElement.textContent = contadorNotificaciones;
        // Muestra u oculta el badge
        contadorElement.style.display = contadorNotificaciones > 0 ? 'block' : 'none';
    }
}

/**
 * Función AJAX para marcar una notificación como leída en el backend.
 * @param {number} idNotificacion - El ID de la notificación persistente.
 */
function marcarNotificacionComoLeida(idNotificacion) {
    // El Context Path es necesario para el endpoint REST de Spring
    $.post(contextPath + '/notificaciones/marcar-leida/' + idNotificacion)
        .done(function() {
            // Descontamos visualmente solo si el AJAX fue exitoso
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
    // Mostrar la notificación usando el mensaje del DTO
    // El título es genérico para el rol de Conductor
    const toast = toastr.info(notificacionDTO.mensaje, "¡Nueva Notificación!");

    // CLAVE: Agregar el evento de click (Requiere jQuery)
    if (toast && notificacionDTO.urlDestino) {
        toast.on('click', function() {

            // 1. Marcar como leída via AJAX y descontar el contador
            // Esto solo se debe hacer si el contador es > 0, para no decrementar dos veces.
            if (contadorNotificaciones > 0) {
                marcarNotificacionComoLeida(notificacionDTO.idNotificacion);
            }

            // 2. Redireccionar al destino (Deep Link)
            window.location.href = contextPath + notificacionDTO.urlDestino;
        });
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

        // 3. Suscripción al topic personal: /topic/notificaciones/{idUsuario}
        const topicDestino = `/topic/notificaciones/${idUsuario}`;

        stompClient.subscribe(topicDestino, function (mensajeJSON) {
            const notificacionDTO = JSON.parse(mensajeJSON.body);

            console.log("✅ ¡Nueva Notificación Recibida!", notificacionDTO.mensaje);

            // Lógica de UI
            actualizarContador(1); // Incrementa visualmente
            mostrarToastNotificacion(notificacionDTO); // Pasa el DTO completo para la redirección
        });
    }, function (error) {
        console.error("❌ Error de WebSocket. Reconectando en 5s...", error);
        // Implementación de reconexión
        setTimeout(() => iniciarConexionNotificaciones(idUsuario, contadorNotificaciones), 5000);
    });
}