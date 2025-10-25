/**
 * JavaScript para el formulario de búsqueda de viaje
 * Maneja autocomplete de Nominatim con Tom Select
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
// INICIALIZACIÓN
// =============================================================================

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar Tom Select para origen y destino
    setupTomSelect('nombreCiudadOrigen');
    setupTomSelect('nombreCiudadDestino');
});
