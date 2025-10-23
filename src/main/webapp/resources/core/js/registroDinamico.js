document.addEventListener('DOMContentLoaded', function() {
    const rolSelector = document.getElementById('rolSeleccionado');
    const camposConductor = document.getElementById('camposConductor');
    const camposViajero = document.getElementById('camposViajero');

    // Campos específicos para requerimiento/desrequerimiento
    const inputLicencia = document.getElementById('fechaDeVencimientoLicencia');
    const inputEdad = document.getElementById('edad');

    /**
     * Alterna la visibilidad y el atributo 'required' según el rol.
     * @param {string} rol - El valor seleccionado (CONDUCTOR o VIAJERO).
     */
    function actualizarFormulario(rol) {
        // 1. Ocultar y deshabilitar todos por defecto
        camposConductor.style.display = 'none';
        camposViajero.style.display = 'none';
        inputLicencia.required = false;
        inputEdad.required = false;

        // 2. Mostrar y requerir campos específicos
        if (rol === 'CONDUCTOR') {
            camposConductor.style.display = 'block';
            inputLicencia.required = true;
        } else if (rol === 'VIAJERO') {
            camposViajero.style.display = 'block';
            inputEdad.required = true;
        }
    }

    // A. Inicializar el formulario con el valor actual (útil para reintentos con error)
    actualizarFormulario(rolSelector.value);

    // B. Añadir el listener para cuando el usuario cambia el rol
    rolSelector.addEventListener('change', function() {
        actualizarFormulario(rolSelector.value);
    });
});