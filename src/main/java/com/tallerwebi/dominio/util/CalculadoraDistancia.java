package com.tallerwebi.dominio.util;

/**
 * Utilidad para calcular distancias entre coordenadas geográficas
 * y estimar duración de viajes
 */
public class CalculadoraDistancia {

    private static final int RADIO_TIERRA_KM = 6371;
    private static final double BUFFER_RUTA = 1.3; // 30% adicional por rutas no rectas
    private static final int VELOCIDAD_PROMEDIO_KMH = 60;

    /**
     * Calcula distancia en línea recta entre dos puntos geográficos usando la fórmula de Haversine
     *
     * @param lat1 Latitud del punto origen
     * @param lon1 Longitud del punto origen
     * @param lat2 Latitud del punto destino
     * @param lon2 Longitud del punto destino
     * @return Distancia en kilómetros
     */
    public static double calcularDistanciaHaversine(
        double lat1, double lon1,
        double lat2, double lon2
    ) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) *
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    /**
     * Estima duración de viaje en minutos basado en la distancia
     * Asume: 60 km/h velocidad promedio + 30% buffer por rutas indirectas y tráfico
     *
     * @param distanciaKm Distancia en kilómetros (línea recta)
     * @return Duración estimada en minutos (redondeado hacia arriba)
     */
    public static int calcularDuracionEstimadaMinutos(double distanciaKm) {
        double distanciaReal = distanciaKm * BUFFER_RUTA;
        double horas = distanciaReal / VELOCIDAD_PROMEDIO_KMH;
        return (int) Math.ceil(horas * 60);
    }
}
