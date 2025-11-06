/**
 * Script para el sistema de notificaciones por Polling (Peticiones Repetitivas).
 * Consulta /api/notificaciones/pendientes cada 10 segundos para buscar eventos no vistos.
 */

// URL del endpoint REST que devuelve las notificaciones pendientes (ya marcadas como vistas)
const API_URL = '/spring/api/notificaciones/pendientes';
// Intervalo de polling: 10 segundos (10,000 milisegundos)
const POLLING_INTERVAL = 10000;

// Elementos del DOM
const notificationCounter = document.getElementById('notificacion-contador');
const notificationBell = document.getElementById('notificacion-campana');
const notificationContainer = document.getElementById('notifications-list'); // Contenedor para el dropdown de notificaciones (si lo tienes)

/**
 * Muestra una notificación usando un Toast o una alerta simple.
 * @param {object} notif - Objeto de notificación {mensaje, urlDestino, fechaCreacion}.
 */
function mostrarToastNotificacion(notif) {
    // [ADAPTAR AQUÍ] Si usas Toastr o Bootstrap Toasts, integra su lógica aquí.
    // Usaremos un simple alert y log por simplicidad, que puedes reemplazar con un Toast.

    console.log(`[ALERTA NOTIFICACIÓN] ${notif.mensaje}`);

    const isConfirmed = confirm(`¡NUEVA NOTIFICACIÓN! ${notif.mensaje} ¿Ir a detalles?`);

    if (isConfirmed && notif.urlDestino) {
        // [🟢 REDIRECCIÓN] Lleva al usuario a la vista de gestión o detalle.
        window.location.href = notif.urlDestino;
    }
}

/**
 * Actualiza el contador visual en la campana de la navbar.
 * @param {number} count - Número total de notificaciones no vistas.
 */
function actualizarContador(count) {
    if (notificationCounter) {
        if (count > 0) {
            notificationCounter.textContent = count;
            notificationCounter.style.display = 'inline-block'; // Mostrar el badge
        } else {
            notificationCounter.textContent = 0;
            notificationCounter.style.display = 'none'; // Ocultar si está vacío
        }
    }
}

/**
 * Función principal para obtener notificaciones pendientes del servidor.
 */
function checkNotifications() {
    // Nota: El backend ya maneja la autenticación y devuelve 401 si el usuario no está logueado.

    fetch(API_URL)
        .then(response => {
            if (response.status === 401) {
                // Usuario deslogueado: detener el polling silenciosamente.
                throw new Error("UNAUTHORIZED");
            }
            if (!response.ok) {
                // Error de servidor (500)
                throw new Error(`Server error: ${response.status}`);
            }
            return response.json();
        })
        .then(notificaciones => {
            // 1. Mostrar/Procesar notificaciones
            notificaciones.forEach(notif => {
                mostrarToastNotificacion(notif);
            });

            // 2. Actualizar el contador (El servidor ya marcó estos como vistos,
            //    por lo que en la próxima consulta el contador será 0 a menos que lleguen nuevas).
            //    Aquí podemos simplemente actualizar el contador con el tamaño de las que llegaron.
            actualizarContador(notificaciones.length);

        })
        .catch(error => {
            if (error.message !== "UNAUTHORIZED") {
                 console.error("Error de Polling:", error);
            }
            // Si hay error (incluyendo UNAUTHORIZED), el setInterval continuará,
            // pero si la sesión es inválida, puedes optar por detenerlo (ver paso 3).
        });
}


/**
 * Inicializa el Polling y establece el intervalo.
 */
function iniciarPolling() {
    // Asegurarse de que el usuario esté logueado (Comprobación extra en JS)
    // Ya lo hace el th:if en el HTML, pero es una buena práctica.

    // Ejecuta una primera comprobación inmediata
    checkNotifications();

    // Configura el intervalo de repetición
    setInterval(checkNotifications, POLLING_INTERVAL);
}


iniciarPolling();