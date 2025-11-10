document.addEventListener('DOMContentLoaded', function() {
    // 1. Obtención de Elementos (Contenedores Dinámicos)
    const rolSelector = document.getElementById('rolSeleccionado');
    const camposConductor = document.getElementById('camposConductor');
    const camposViajero = document.getElementById('camposViajero');

    // 2. Campo específico de Conductor que necesita 'required'
    const inputLicencia = document.getElementById('fechaDeVencimientoLicencia');

    // 3. Campo específico de Viajero (para el dummy URL)
    const inputFotoPerfilUrl = document.getElementById('fotoPerfilUrl');

    // Validación crítica: Si no encontramos los contenedores, el script no puede ejecutarse
    if (!rolSelector || !camposConductor || !camposViajero) {
        // Debería ser muy raro si el HTML anterior se copió correctamente
        console.error("Error: Elementos de rol dinámico no encontrados.");
        return;
    }

    /**
     * Alterna la visibilidad y el atributo 'required' según el rol.
     * @param {string} rol - El valor seleccionado (CONDUCTOR o VIAJERO).
     */
    function actualizarFormulario(rol) {
        // 1. Ocultar todos por defecto y resetear requerimientos específicos
        camposConductor.style.display = 'none';
        camposViajero.style.display = 'none';

        // Resetear requerimientos específicos
        if (inputLicencia) inputLicencia.required = false;
        if (inputFotoPerfilUrl) inputFotoPerfilUrl.required = false;

        // 2. Mostrar y requerir campos específicos
        if (rol === 'CONDUCTOR') {
            camposConductor.style.display = 'block';
            if (inputLicencia) inputLicencia.required = true;

        } else if (rol === 'VIAJERO') {
            camposViajero.style.display = 'block';
            // Dejo el campo fotoPerfilUrl opcional (no requerido) por ahora, según tu indicación
            // if (inputFotoPerfilUrl) inputFotoPerfilUrl.required = true;
        }
    }

    // A. Inicializar la visibilidad
    actualizarFormulario(rolSelector.value);

    // B. Añadir el listener
    rolSelector.addEventListener('change', function() {
        actualizarFormulario(rolSelector.value);
    });
});