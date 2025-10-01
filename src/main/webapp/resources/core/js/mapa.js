let seleccionActual = null; // guarda si estás en origen o destino

// Detectar qué input tiene el foco
document.getElementById('origen').addEventListener('focus', () => seleccionActual = 'origen');
document.getElementById('destino').addEventListener('focus', () => seleccionActual = 'destino');

// Inicializar mapa
let map = L.map('map').setView([-34.6, -58.4], 5);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

// Manejo de markers
let markers = { origen: null, destino: null };

// Función auxiliar para mostrar popup temporal
function mostrarPopup(lat, lon, mensaje) {
    L.popup()
        .setLatLng([lat, lon])
        .setContent(`<b>${mensaje}</b>`)
        .openOn(map);
}

// Evento click en mapa
map.on('click', async function (e) {
    if (!seleccionActual) {
        mostrarPopup(e.latlng.lat, e.latlng.lng, "⚠️ Seleccioná primero un campo (Origen o Destino)");
        return;
    }

    const lat = e.latlng.lat;
    const lon = e.latlng.lng;

    // Llamada a Nominatim
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=10&addressdetails=1`;

    try {
        const response = await fetch(url, { headers: { 'Accept-Language': 'es' } });
        const data = await response.json();

        let ciudad = data.address.city || data.address.town || data.address.village || "";

        if (!ciudad) {
            mostrarPopup(lat, lon, "❌ Solo se pueden seleccionar ciudades");
            return;
        }

        // Completar inputs
        document.getElementById(seleccionActual).value = ciudad;
        document.getElementById(seleccionActual + 'Lat').value = lat;
        document.getElementById(seleccionActual + 'Lon').value = lon;

        // Borrar marker previo y poner nuevo
        if (markers[seleccionActual]) {
            map.removeLayer(markers[seleccionActual]);
        }
        markers[seleccionActual] = L.marker([lat, lon])
            .addTo(map)
            .bindPopup(`${seleccionActual === 'origen' ? 'Origen' : 'Destino'}: ${ciudad}`)
            .openPopup();

    } catch (err) {
        console.error('Error al obtener ciudad:', err);
        mostrarPopup(lat, lon, "❌ Error al obtener ciudad. Intenta de nuevo.");
    }
});

// Validación antes de enviar formulario
document.querySelector("form").addEventListener("submit", function (e) {
    const origen = document.getElementById("origen").value.trim();
    const destino = document.getElementById("destino").value.trim();

    if (!origen || !destino) {
        e.preventDefault();
        mostrarPopup(map.getCenter().lat, map.getCenter().lng, "⚠️ Debes seleccionar una ciudad válida para origen y destino");
    }
});
