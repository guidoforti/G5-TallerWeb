// notificacionesConductor.js

// Nota: No se puede usar th:inline aquí, por eso la variable ID_CONDUCTOR
// debe pasarse como parámetro a la función principal.

let contadorNotificaciones = 0;
const contadorElement = document.getElementById('notificacion-contador');
// Asegúrate de que este contextPath coincida con la configuración de tu Jetty/Spring
const contextPath = '/spring';

/**
 * Inicia la conexión WebSocket y la suscripción para el conductor.
 * @param {number} idConductor - El ID del conductor logueado.
 */
function iniciarConexionNotificaciones(idConductor) {
    if (!idConductor || idConductor === 0) {
        console.error("ID de conductor inválido. No se puede iniciar el WebSocket.");
        return;
    }

    console.log("Intentando conectar WebSocket para el conductor ID:", idConductor);

    const socket = new SockJS(contextPath + '/ws-notificaciones');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Conexión STOMP exitosa. Suscribiéndose...');

        // Suscripción al topic personal: /topic/notificaciones/{idConductor}
        const topicDestino = `/topic/notificaciones/${idConductor}`;

        stompClient.subscribe(topicDestino, function (mensajeJSON) {
            const notificacion = JSON.parse(mensajeJSON.body);

            console.log("✅ ¡Nueva Notificación Recibida!", notificacion.mensaje);

            // Lógica de UI
            actualizarContador(1);
            mostrarToastNotificacion(notificacion.mensaje);
        });
    }, function (error) {
        console.error("❌ Error de WebSocket. Reconectando en 5s...", error);
        // Implementación de reconexión
        setTimeout(() => iniciarConexionNotificaciones(idConductor), 5000);
    });
}

// Funciones utilitarias para manejo de UI (también fuera, aquí)

function actualizarContador(incremento) {
    contadorNotificaciones += incremento;

    if (contadorElement) {
        contadorElement.textContent = contadorNotificaciones;
        // Mostrar u ocultar el badge
        contadorElement.style.display = contadorNotificaciones > 0 ? 'block' : 'none';
    }
}

function mostrarToastNotificacion(mensaje) {
    // 1. Configurar Toastr (opcional, pero ayuda a la coherencia)
    toastr.options = {
        "closeButton": true,
        "progressBar": true,
        "positionClass": "toast-top-right", // Posición común
        "showDuration": "300",
        "hideDuration": "1000",
        "timeOut": "5000", // La notificación dura 5 segundos
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    };

    // 2. Mostrar la notificación
    toastr.info(mensaje, "¡Nueva Solicitud de Reserva!"); // Usa toastr.info o toastr.success/warning
}