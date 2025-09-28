
    /*<![CDATA[*/
    let seleccionActual = null;

    document.getElementById('origen').addEventListener('focus', () => seleccionActual = 'origen');
    document.getElementById('destino').addEventListener('focus', () => seleccionActual = 'destino');

    const map = L.map('map').setView([-34.6037, -58.3816], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    map.on('click', async function(e) {
        if(!seleccionActual) return;

        const lat = e.latlng.lat;
        const lon = e.latlng.lng;

        // Llamada a Nominatim para obtener ciudad
        const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&zoom=10&addressdetails=1`;

        try {
            const response = await fetch(url, { headers: { 'Accept-Language': 'es' } });
            const data = await response.json();

            let ciudad = data.address.city || data.address.town || data.address.village || data.address.county || '';
            if(!ciudad) ciudad = "Desconocido";

            document.getElementById(seleccionActual).value = ciudad;
            document.getElementById(seleccionActual + 'Lat').value = lat;
            document.getElementById(seleccionActual + 'Lon').value = lon;

        } catch(err) {
            console.error('Error al obtener ciudad:', err);
            alert("No se pudo obtener la ciudad de la ubicación seleccionada");
        }
    });
    /*]]>*/
