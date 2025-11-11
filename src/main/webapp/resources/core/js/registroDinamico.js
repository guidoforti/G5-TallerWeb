document.addEventListener('DOMContentLoaded', function() {
    // 1. Obtención de Elementos
    const rolSelector = document.getElementById('rolSeleccionado');
    const camposConductor = document.getElementById('camposConductor');

    // Campo específico de Conductor (Licencia)
    const inputLicencia = document.getElementById('fechaDeVencimientoLicencia');

    // Validación crítica: Si no encontramos los contenedores principales, el script no puede ejecutarse
    if (!rolSelector || !camposConductor || !inputLicencia) {
        console.error("Error: Faltan elementos críticos (selector de rol o camposConductor).");
        return;
    }

    /**
     * Alterna la visibilidad y el atributo 'required' según el rol.
     * @param {string} rol - El valor seleccionado (CONDUCTOR o VIAJERO).
     */
    function actualizarFormulario(rol) {
        // 1. Ocultar la sección del conductor y resetear requerimientos
        camposConductor.style.display = 'none';
        inputLicencia.required = false;

        // 2. Mostrar y requerir solo para CONDUCTOR
        if (rol === 'CONDUCTOR') {
            camposConductor.style.display = 'block';
            inputLicencia.required = true;

        }
        // 3. Si es VIAJERO, no hacemos nada (los campos comunes ya se muestran)
    }

    // A. Inicializar la visibilidad
    actualizarFormulario(rolSelector.value);

    // B. Añadir el listener
    rolSelector.addEventListener('change', function() {
        actualizarFormulario(rolSelector.value);
    });
});