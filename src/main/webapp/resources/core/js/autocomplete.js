function setupAutocomplete(inputId, latId, lonId) {
    $("#" + inputId).autocomplete({
        source: async function(request, response) {
            try {
                const res = await fetch(`/ciudades/buscar?nombre=${encodeURIComponent(request.term)}`);
                const data = await res.json();
                
                // Devuelve un array con label y value para jQuery UI
                const results = data.map(ciudad => ({
                    label: ciudad.nombre,
                    value: ciudad.nombre,
                    lat: ciudad.latitud,
                    lon: ciudad.longitud
                }));

                response(results);
            } catch (err) {
                console.error(err);
                response([]);
            }
        },
        select: function(event, ui) {
            // Al seleccionar, llenar inputs ocultos
            document.getElementById(latId).value = ui.item.lat;
            document.getElementById(lonId).value = ui.item.lon;
        },
        minLength: 2
    });
}

setupAutocomplete('origen', 'origenLat', 'origenLon');
setupAutocomplete('destino', 'destinoLat', 'destinoLon');
