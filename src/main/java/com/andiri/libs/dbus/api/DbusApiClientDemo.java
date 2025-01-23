// --- client package ---
package com.andiri.libs.dbus.api;

import com.andiri.libs.dbus.exceptions.ApiException;
import TiemposParadaResponse;

public class DbusApiClientDemo {

    public static void main(String[] args) {
        DbusApiClient client = new DbusApiClientBuilder() // Use DbusApiClientBuilder
                .defaultLanguage("es")
                .connectTimeoutMillis(5000)
                .readTimeoutMillis(10000)
                .build();


        try {
            TiemposParadaResponse tiemposParadaResponse = client.tiemposParada(200, "es");
            if (tiemposParadaResponse != null && tiemposParadaResponse.getTiempos() != null) {
                tiemposParadaResponse.getTiempos().forEach(tiempo -> {
                    System.out.println("Hora: " + tiempo.getHora()); // Assuming 'Tiempo' model has getHora()
                    System.out.println("Minutos: " + tiempo.getMinutos()); // Assuming 'Tiempo' model has getMinutos()
                    // ... access other properties of 'Tiempo' model
                });
            } else {
                System.out.println("No tiempos de parada disponibles o respuesta vacía.");
            }

        } catch (ApiException e) {
            System.err.println("Error al obtener tiempos de parada: " + e.getMessage());
            if (e.getResponseBody() != null) {
                System.err.println("Response Body: " + e.getResponseBody());
            }
        }
    }
}