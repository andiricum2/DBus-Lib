// --- client package ---
package com.andiri.libs.dbus.api;

import com.andiri.libs.dbus.exceptions.ApiException;

public class DbusApiClientDemo {

    public static void main(String[] args) {
        DbusApiClient apiClient = new DbusApiClientBuilder() // Use DbusApiClientBuilder
                .defaultLanguage("es")
                .connectTimeoutMillis(3000)
                .readTimeoutMillis(7000)
                .build();

        try {
            // Example usage for tiemposParada (synchronous)
            String tiemposParadaResponse = apiClient.tiemposParada("1", null);
            System.out.println("Tiempos Parada Response (Sync):\n" + tiemposParadaResponse);

            // Example usage for recorridoLinea (synchronous)
            String recorridoLineaResponse = apiClient.recorridoLinea("1");
            System.out.println("\nRecorrido Linea Response (Sync):\n" + recorridoLineaResponse);

            // Example usage for avisos (synchronous)
            String avisosResponse = apiClient.avisos("es");
            System.out.println("\nAvisos Response (Sync):\n" + avisosResponse);

            // Example usage for tiemposParadaAsync (asynchronous)
            apiClient.tiemposParadaAsync("1", "es").thenAccept(responseAsync -> {
                System.out.println("\nTiempos Parada Response (Async):\n" + responseAsync);
            }).join(); // .join() to wait for the async operation to complete in main thread for example purpose

        } catch (ApiException e) {
            System.err.println("\nAPI Error: " + e.getMessage());
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBody());
        } catch (Exception e) {
            System.err.println("\nGeneral Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}