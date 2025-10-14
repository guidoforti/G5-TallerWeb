/**
 * JavaScript para el formulario de publicación de viaje
 * Maneja autocomplete de Nominatim y paradas dinámicas
 */

// =============================================================================
// UTILIDADES
// =============================================================================

/**
 * Función debounce para evitar múltiples requests
 * @param {Function} func - Función a ejecutar
 * @param {number} wait - Tiempo de espera en ms
 * @returns {Function} Función con debounce aplicado
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// =============================================================================
// AUTOCOMPLETE CON NOMINATIM
// =============================================================================

/**
 * Configura autocomplete con Nominatim para un input
 * @param {string} inputId - ID del input
 * @param {string} datalistId - ID del datalist asociado
 */
function setupNominatimAutocomplete(inputId, datalistId) {
    const input = document.getElementById(inputId);
    const datalist = document.getElementById(datalistId);

    if (!input || !datalist) {
        console.error(`No se encontró el input ${inputId} o datalist ${datalistId}`);
        return;
    }

    // Evento input con debounce
    input.addEventListener('input', debounce(async (e) => {
        const query = e.target.value.trim();

        // Si el query es muy corto, limpiar el datalist
        if (query.length < 2) {
            datalist.innerHTML = '';
            return;
        }

        try {
            // Consultar endpoint de Nominatim
            const response = await fetch(`/spring/api/nominatim/buscar?query=${encodeURIComponent(query)}`);

            if (!response.ok) {
                console.error('Error en la respuesta del servidor');
                return;
            }

            const sugerencias = await response.json();

            // Limpiar datalist
            datalist.innerHTML = '';

            // Agregar nuevas opciones
            sugerencias.forEach(ciudad => {
                const option = document.createElement('option');
                option.value = ciudad;
                datalist.appendChild(option);
            });

        } catch (error) {
            console.error('Error al buscar ciudades:', error);
        }
    }, 300));
}

// =============================================================================
// MANEJO DE PARADAS DINÁMICAS
// =============================================================================

let paradaCount = 0;

/**
 * Agrega una nueva parada al formulario
 */
function agregarParada() {
    const container = document.getElementById('paradasContainer');

    if (!container) {
        console.error('No se encontró el container de paradas');
        return;
    }

    paradaCount++;

    // Crear div contenedor para la parada
    const paradaDiv = document.createElement('div');
    paradaDiv.className = 'parada-item mb-3';
    paradaDiv.id = `parada-${paradaCount}`;
    paradaDiv.dataset.index = paradaCount;

    // HTML de la parada
    paradaDiv.innerHTML = `
        <div class="d-flex align-items-center gap-2">
            <span class="badge bg-unrumbo text-white parada-numero" style="background-color: #E16A3D;">${paradaCount}</span>
            <div class="flex-grow-1">
                <input type="text"
                       id="parada-input-${paradaCount}"
                       name="nombresParadas"
                       class="form-control"
                       list="sugerenciasParada-${paradaCount}"
                       placeholder="Ej: Rosario"
                       required>
                <datalist id="sugerenciasParada-${paradaCount}"></datalist>
            </div>
            <button type="button"
                    class="btn btn-sm btn-outline-danger"
                    onclick="eliminarParada(${paradaCount})">
                ✕
            </button>
        </div>
    `;

    container.appendChild(paradaDiv);

    // Configurar autocomplete para esta parada
    setupNominatimAutocomplete(`parada-input-${paradaCount}`, `sugerenciasParada-${paradaCount}`);
}

/**
 * Elimina una parada del formulario
 * @param {number} index - Índice de la parada a eliminar
 */
function eliminarParada(index) {
    const paradaDiv = document.getElementById(`parada-${index}`);

    if (paradaDiv) {
        paradaDiv.remove();
        actualizarNumerosOrden();
    }
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
// VALIDACIÓN DEL FORMULARIO
// =============================================================================

/**
 * Valida que los valores de los inputs estén en sus respectivos datalists
 * @returns {boolean} true si es válido, false si no
 */
function validarFormulario() {
    const inputs = [
        { id: 'nombreCiudadOrigen', label: 'Origen' },
        { id: 'nombreCiudadDestino', label: 'Destino' }
    ];

    // Validar origen y destino
    for (const {id, label} of inputs) {
        const input = document.getElementById(id);
        if (input && input.value.trim()) {
            const datalistId = input.getAttribute('list');
            const datalist = document.getElementById(datalistId);

            if (datalist) {
                const options = Array.from(datalist.options).map(opt => opt.value);
                if (!options.includes(input.value)) {
                    alert(`Por favor, seleccioná una opción válida de la lista para ${label}`);
                    input.focus();
                    return false;
                }
            }
        }
    }

    // Validar paradas
    const paradaInputs = document.querySelectorAll('[name="nombresParadas"]');
    for (const input of paradaInputs) {
        if (input.value.trim()) {
            const datalistId = input.getAttribute('list');
            const datalist = document.getElementById(datalistId);

            if (datalist) {
                const options = Array.from(datalist.options).map(opt => opt.value);
                if (!options.includes(input.value)) {
                    alert('Por favor, seleccioná opciones válidas de la lista para todas las paradas');
                    input.focus();
                    return false;
                }
            }
        }
    }

    return true;
}

// =============================================================================
// INICIALIZACIÓN
// =============================================================================

document.addEventListener('DOMContentLoaded', function() {
    // Configurar autocomplete para origen y destino
    setupNominatimAutocomplete('nombreCiudadOrigen', 'sugerenciasOrigen');
    setupNominatimAutocomplete('nombreCiudadDestino', 'sugerenciasDestino');

    // Configurar botón de agregar parada
    const btnAgregarParada = document.getElementById('btnAgregarParada');
    if (btnAgregarParada) {
        btnAgregarParada.addEventListener('click', agregarParada);
    }

    // Configurar validación del formulario
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!validarFormulario()) {
                e.preventDefault();
            }
        });
    }
});
