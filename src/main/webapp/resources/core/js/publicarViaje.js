/**
 * JavaScript para el formulario de publicación de viaje
 * Maneja autocomplete de Nominatim con Tom Select y paradas dinámicas
 */

// =============================================================================
// CONFIGURACIÓN DE TOM SELECT
// =============================================================================

/**
 * Configuración base para Tom Select con Nominatim
 * @returns {Object} Objeto de configuración para Tom Select
 */
function getTomSelectConfig() {
    return {
        valueField: 'text',
        labelField: 'text',
        searchField: 'text',
        create: false,  // No permitir texto libre, solo selección de lista
        loadThrottle: 300,  // Debounce de 300ms
        placeholder: 'Escribí para buscar...',
        preload: false,  // No cargar datos hasta que el usuario escriba

        // Control de carga: solo buscar si hay al menos 2 caracteres
        shouldLoad: function(query) {
            return query.length >= 2;
        },

        // Función para cargar datos remotos desde Nominatim
        load: function(query, callback) {
            const url = `/spring/api/nominatim/buscar?query=${encodeURIComponent(query)}`;

            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error en la respuesta del servidor');
                    }
                    return response.json();
                })
                .then(data => {
                    // Transformar array de strings a objetos con propiedad 'text'
                    const options = data.map(ciudad => ({ text: ciudad }));
                    callback(options);
                })
                .catch(error => {
                    console.error('Error al buscar ciudades:', error);
                    callback();  // Llamar callback sin datos en caso de error
                });
        },

        // Mensaje cuando no hay resultados
        render: {
            no_results: function() {
                return '<div class="no-results">No se encontraron ciudades</div>';
            }
        }
    };
}

/**
 * Inicializa Tom Select en un elemento select
 * @param {string} selectId - ID del elemento select
 * @returns {TomSelect|null} Instancia de Tom Select o null si falla
 */
function setupTomSelect(selectId) {
    const selectElement = document.getElementById(selectId);

    if (!selectElement) {
        console.error(`No se encontró el elemento select: ${selectId}`);
        return null;
    }

    // Si ya tiene Tom Select, destruirlo primero
    if (selectElement.tomselect) {
        selectElement.tomselect.destroy();
    }

    try {
        return new TomSelect(`#${selectId}`, getTomSelectConfig());
    } catch (error) {
        console.error(`Error al inicializar Tom Select en ${selectId}:`, error);
        return null;
    }
}

// =============================================================================
// MANEJO DE PARADAS DINÁMICAS
// =============================================================================

// Contador para IDs únicos de paradas (nunca decrementa)
let paradaIdCounter = 0;

// Almacena instancias de Tom Select para las paradas
const tomSelectInstances = new Map();

/**
 * Obtiene el número actual de paradas en el formulario
 * @returns {number} Cantidad de paradas
 */
function obtenerCantidadParadas() {
    const container = document.getElementById('paradasContainer');
    return container.querySelectorAll('.parada-item').length;
}

/**
 * Agrega una nueva parada al formulario
 */
function agregarParada() {
    const container = document.getElementById('paradasContainer');

    if (!container) {
        console.error('No se encontró el container de paradas');
        return;
    }

    // Incrementar ID único (solo para identificación, no para mostrar)
    paradaIdCounter++;
    const uniqueId = paradaIdCounter;

    // Calcular el número de orden visual basado en paradas existentes
    const numeroOrden = obtenerCantidadParadas() + 1;

    // Crear div contenedor para la parada
    const paradaDiv = document.createElement('div');
    paradaDiv.className = 'parada-item mb-3';
    paradaDiv.id = `parada-${uniqueId}`;
    paradaDiv.dataset.uniqueId = uniqueId;

    // HTML de la parada
    paradaDiv.innerHTML = `
        <div class="d-flex align-items-center gap-2">
            <span class="badge bg-unrumbo text-white parada-numero" style="background-color: #E16A3D;">${numeroOrden}</span>
            <div class="flex-grow-1">
                <select id="parada-input-${uniqueId}"
                        name="nombresParadas"
                        class="form-control"
                        placeholder="Ej: Rosario"
                        required>
                </select>
            </div>
            <button type="button"
                    class="btn btn-sm btn-outline-danger btn-eliminar-parada"
                    data-parada-id="${uniqueId}">
                ✕
            </button>
        </div>
    `;

    container.appendChild(paradaDiv);

    // Agregar event listener al botón de eliminar
    const btnEliminar = paradaDiv.querySelector('.btn-eliminar-parada');
    btnEliminar.addEventListener('click', function() {
        eliminarParada(uniqueId);
    });

    // Inicializar Tom Select para esta parada
    const tomSelectInstance = setupTomSelect(`parada-input-${uniqueId}`);
    if (tomSelectInstance) {
        tomSelectInstances.set(uniqueId, tomSelectInstance);
    }
}

/**
 * Elimina una parada del formulario
 * @param {number} uniqueId - ID único de la parada a eliminar
 */
function eliminarParada(uniqueId) {
    const paradaDiv = document.getElementById(`parada-${uniqueId}`);

    if (!paradaDiv) {
        return;
    }

    // Destruir la instancia de Tom Select antes de eliminar el elemento
    const tomSelectInstance = tomSelectInstances.get(uniqueId);
    if (tomSelectInstance) {
        tomSelectInstance.destroy();
        tomSelectInstances.delete(uniqueId);
    }

    // Eliminar el elemento del DOM
    paradaDiv.remove();

    // Actualizar números de orden
    actualizarNumerosOrden();
}

/**
 * Actualiza los números de orden de las paradas después de eliminar
 */
function actualizarNumerosOrden() {
    const container = document.getElementById('paradasContainer');
    const paradas = container.querySelectorAll('.parada-item');

    paradas.forEach((parada, index) => {
        const numero = index + 1;
        const badge = parada.querySelector('.parada-numero');
        if (badge) {
            badge.textContent = numero;
        }
    });
}

// =============================================================================
// INICIALIZACIÓN
// =============================================================================

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar Tom Select para origen y destino
    setupTomSelect('nombreCiudadOrigen');
    setupTomSelect('nombreCiudadDestino');

    // Configurar botón de agregar parada
    const btnAgregarParada = document.getElementById('btnAgregarParada');
    if (btnAgregarParada) {
        btnAgregarParada.addEventListener('click', agregarParada);
    }

    // No es necesaria validación manual ya que Tom Select
    // solo permite selección de la lista (create: false)
});
