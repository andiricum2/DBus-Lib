// --- client package ---
package com.andiri.libs.dbus.api;

import com.andiri.libs.dbus.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DbusApiClient {

    private static final String BASE_URL = "http://62.99.53.182/SSIIMovilWSv2/ws/cons/"; // Hardcoded base URL
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String defaultLanguage;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    private static final Set<String> VALID_LANGUAGES = Set.of("es", "eu", "en", "fr");

    // --- Builder Class ---
    public static class Builder {
        private String defaultLanguage = "es";
        private int connectTimeoutMillis = 5000;
        private int readTimeoutMillis = 10000;

        public Builder() { // Base URL is fixed, no need to pass in constructor
        }

        public Builder defaultLanguage(String defaultLanguage) {
            this.defaultLanguage = defaultLanguage;
            return this;
        }

        public Builder connectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        public Builder readTimeoutMillis(int readTimeoutMillis) {
            this.readTimeoutMillis = readTimeoutMillis;
            return this;
        }

        public DbusApiClient build() {
            return new DbusApiClient(this);
        }
    }

    private DbusApiClient(Builder builder) {
        this.defaultLanguage = builder.defaultLanguage;
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.readTimeoutMillis = builder.readTimeoutMillis;

        if (!VALID_LANGUAGES.contains(this.defaultLanguage)) {
            throw new IllegalArgumentException("Invalid default language: " + this.defaultLanguage + ". Must be one of: " + VALID_LANGUAGES);
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(this.connectTimeoutMillis))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // --- Private Helper Methods ---

    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL); // Use BASE_URL here
        urlBuilder.append(path).append("?");
        if (params != null && !params.isEmpty()) {
            params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
        }
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        return urlBuilder.toString();
    }

    private String getJsonResponse(String url) throws ApiException { // Return String JSON
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .timeout(java.time.Duration.ofMillis(this.readTimeoutMillis))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new ApiConnectionException("Error connecting to API", e);
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                // Return pretty printed JSON String
                Object json = objectMapper.readValue(response.body(), Object.class); // Parse to generic object
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json); // Convert back to pretty JSON String
            } catch (Exception e) {
                throw new ApiResponseParseException("Error parsing API response", response.body(), e);
            }
        } else {
            throw createApiExceptionForStatusCode(response.statusCode(), response.body());
        }
    }

    private CompletableFuture<String> getJsonResponseAsync(String url) { // Return CompletableFuture<String> JSON
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .timeout(java.time.Duration.ofMillis(this.readTimeoutMillis))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            // Return pretty printed JSON String
                            Object json = objectMapper.readValue(response.body(), Object.class); // Parse to generic object
                            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json); // Convert back to pretty JSON String
                        } catch (Exception e) {
                            throw new ApiResponseParseException("Error parsing API response", response.body(), e);
                        }
                    } else {
                        throw createApiExceptionForStatusCode(response.statusCode(), response.body());
                    }
                });
    }


    private ApiException createApiExceptionForStatusCode(int statusCode, String responseBody) {
        switch (statusCode) {
            case 400: return new BadRequestException("Bad Request", statusCode, responseBody);
            case 401: return new UnauthorizedException("Unauthorized", statusCode, responseBody);
            case 404: return new NotFoundException("Not Found", statusCode, responseBody);
            case 500: return new ServerErrorException("Internal Server Error", statusCode, responseBody);
            default:  return new ApiException("API Error", statusCode, responseBody);
        }
    }


    private void validateLanguage(String idioma) {
        if (!VALID_LANGUAGES.contains(idioma)) {
            throw new IllegalArgumentException("Invalid language: " + idioma + ". Must be one of: " + VALID_LANGUAGES);
        }
    }

    private String getLanguageOrDefault(String idioma) {
        String languageToUse = idioma == null ? defaultLanguage : idioma;
        validateLanguage(languageToUse);
        return languageToUse;
    }


    // --- API Endpoint Methods (Synchronous) ---

    /**
     * tiemposParada?codParada=%s&idioma=%s
     */
    public String tiemposParada(String codParada, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", codParada);
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParada", params);
        return getJsonResponse(url);
    }

    /**
     * tiemposParadaBus?codParada=%s&codVehiculo=%s&idioma=%s
     */
    public String tiemposParadaBus(String codParada, String codVehiculo, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", codParada);
        params.put("codVehiculo", codVehiculo);
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParadaBus", params);
        return getJsonResponse(url);
    }

    /**
     * datosVehiculo?codVehiculo=%s&codEmpresa=1&petItinerario=%s
     */
    public String datosVehiculo(String codVehiculo, boolean petItinerario) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("codVehiculo", codVehiculo);
        params.put("codEmpresa", "1"); // Hardcoded as per documentation
        params.put("petItinerario", String.valueOf(petItinerario));
        String url = buildUrl("datosVehiculo", params);
        return getJsonResponse(url);
    }

    /**
     * avisos?idioma=%s
     */
    public String avisos(String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idioma", languageToUse);
        String url = buildUrl("avisos", params);
        return getJsonResponse(url);
    }

    /**
     * expedicionesParadaItinerario?idItinerario=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s
     */
    public String expedicionesParadaItinerario(String idItinerario, String idParada, String fecha, String hora, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", idItinerario);
        params.put("idParada", idParada);
        params.put("fecha", fecha);
        params.put("hora", hora);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaItinerario", params);
        return getJsonResponse(url);
    }

    /**
     * expedicionesParadaSentido?idSentido=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s
     */
    public String expedicionesParadaSentido(String idSentido, String idParada, String fecha, String hora, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", idSentido);
        params.put("idParada", idParada);
        params.put("fecha", fecha);
        params.put("hora", hora);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaSentido", params);
        return getJsonResponse(url);
    }

    /**
     * itinerariosLinea?idLinea=%s&idioma=%s  or itinerariosLinea?idLinea=%s&idParada=%s&idioma=%s
     */
    public String itinerariosLinea(String idLinea, String idioma, String... idParada) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", idLinea);
        params.put("idioma", languageToUse);
        String path = "itinerariosLinea";
        if (idParada.length > 0) {
            params.put("idParada", idParada[0]);
        }
        String url = buildUrl(path, params);
        return getJsonResponse(url);
    }

    /**
     * sentidosLinea?idLinea=%s&idioma=%s or sentidosLinea?idLinea=%s&idParada=%s&idioma=%s
     */
    public String sentidosLinea(String idLinea, String idioma, String... idParada) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", idLinea);
        params.put("idioma", languageToUse);
        String path = "sentidosLinea";
        if (idParada.length > 0) {
            params.put("idParada", idParada[0]);
        }
        String url = buildUrl(path, params);
        return getJsonResponse(url);
    }

    /**
     * expedicionesItinerario?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s&idItinerario=%s
     */
    public String expedicionesItinerario(String idParada, String tipoDia, String hInicio, String hFin, String idItinerario) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idParada", idParada);
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicio);
        params.put("hFin", hFin);
        params.put("idItinerario", idItinerario);
        String url = buildUrl("expedicionesItinerario", params);
        return getJsonResponse(url);
    }

    /**
     * lineasParada?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s
     */
    public String lineasParada(String idParada, String tipoDia, String hInicio, String hFin) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idParada", idParada);
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicio);
        params.put("hFin", hFin);
        String url = buildUrl("lineasParada", params);
        return getJsonResponse(url);
    }

    /**
     * paradasItinerario?idItinerario=%s
     */
    public String paradasItinerario(String idItinerario) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", idItinerario);
        String url = buildUrl("paradasItinerario", params);
        return getJsonResponse(url);
    }

    /**
     * paradasSentido?idSentido=%s
     */
    public String paradasSentido(String idSentido) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", idSentido);
        String url = buildUrl("paradasSentido", params);
        return getJsonResponse(url);
    }

    /**
     * recorridoLinea?idLinea=%s
     */
    public String recorridoLinea(String idLinea) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", idLinea);
        String url = buildUrl("recorridoLinea", params);
        return getJsonResponse(url);
    }

    /**
     * listadoLineas
     */
    public String listadoLineas() throws ApiException { // New method for listadoLineas
        String url = buildUrl("listadoLineas", null); // No params for this endpoint
        return getJsonResponse(url);
    }


    // --- API Endpoint Methods (Asynchronous) ---

    /**
     * tiemposParada?codParada=%s&idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> tiemposParadaAsync(String codParada, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", codParada);
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParada", params);
        return getJsonResponseAsync(url);
    }

    /**
     * tiemposParadaBus?codParada=%s&codVehiculo=%s&idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> tiemposParadaBusAsync(String codParada, String codVehiculo, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", codParada);
        params.put("codVehiculo", codVehiculo);
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParadaBus", params);
        return getJsonResponseAsync(url);
    }

    /**
     * datosVehiculo?codVehiculo=%s&codEmpresa=1&petItinerario=%s (Asynchronous)
     */
    public CompletableFuture<String> datosVehiculoAsync(String codVehiculo, boolean petItinerario) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("codVehiculo", codVehiculo);
        params.put("codEmpresa", "1"); // Hardcoded as per documentation
        params.put("petItinerario", String.valueOf(petItinerario));
        String url = buildUrl("datosVehiculo", params);
        return getJsonResponseAsync(url);
    }

    /**
     * avisos?idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> avisosAsync(String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idioma", languageToUse);
        String url = buildUrl("avisos", params);
        return getJsonResponseAsync(url);
    }

    /**
     * expedicionesParadaItinerario?idItinerario=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> expedicionesParadaItinerarioAsync(String idItinerario, String idParada, String fecha, String hora, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", idItinerario);
        params.put("idParada", idParada);
        params.put("fecha", fecha);
        params.put("hora", hora);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaItinerario", params);
        return getJsonResponseAsync(url);
    }

    /**
     * expedicionesParadaSentido?idSentido=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> expedicionesParadaSentidoAsync(String idSentido, String idParada, String fecha, String hora, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", idSentido);
        params.put("idParada", idParada);
        params.put("fecha", fecha);
        params.put("hora", hora);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaSentido", params);
        return getJsonResponseAsync(url);
    }

    /**
     * itinerariosLinea?idLinea=%s&idioma=%s  or itinerariosLinea?idLinea=%s&idParada=%s&idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> itinerariosLineaAsync(String idLinea, String idioma, String... idParada) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", idLinea);
        params.put("idioma", languageToUse);
        String path = "itinerariosLinea";
        if (idParada.length > 0) {
            params.put("idParada", idParada[0]);
        }
        String url = buildUrl(path, params);
        return getJsonResponseAsync(url);
    }

    /**
     * sentidosLinea?idLinea=%s&idioma=%s or sentidosLinea?idLinea=%s&idParada=%s&idioma=%s (Asynchronous)
     */
    public CompletableFuture<String> sentidosLineaAsync(String idLinea, String idioma, String... idParada) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", idLinea);
        params.put("idioma", languageToUse);
        String path = "sentidosLinea";
        if (idParada.length > 0) {
            params.put("idParada", idParada[0]);
        }
        String url = buildUrl(path, params);
        return getJsonResponseAsync(url);
    }

    /**
     * expedicionesItinerario?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s&idItinerario=%s (Asynchronous)
     */
    public CompletableFuture<String> expedicionesItinerarioAsync(String idParada, String tipoDia, String hInicio, String hFin, String idItinerario) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idParada", idParada);
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicio);
        params.put("hFin", hFin);
        params.put("idItinerario", idItinerario);
        String url = buildUrl("expedicionesItinerario", params);
        return getJsonResponseAsync(url);
    }

    /**
     * lineasParada?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s (Asynchronous)
     */
    public CompletableFuture<String> lineasParadaAsync(String idParada, String tipoDia, String hInicio, String hFin) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idParada", idParada);
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicio);
        params.put("hFin", hFin);
        String url = buildUrl("lineasParada", params);
        return getJsonResponseAsync(url);
    }

    /**
     * paradasItinerario?idItinerario=%s (Asynchronous)
     */
    public CompletableFuture<String> paradasItinerarioAsync(String idItinerario) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", idItinerario);
        String url = buildUrl("paradasItinerario", params);
        return getJsonResponseAsync(url);
    }

    /**
     * paradasSentido?idSentido=%s (Asynchronous)
     */
    public CompletableFuture<String> paradasSentidoAsync(String idSentido) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", idSentido);
        String url = buildUrl("paradasSentido", params);
        return getJsonResponseAsync(url);
    }

    /**
     * recorridoLinea?idLinea=%s (Asynchronous)
     */
    public CompletableFuture<String> recorridoLineaAsync(String idLinea) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", idLinea);
        String url = buildUrl("recorridoLinea", params);
        return getJsonResponseAsync(url);
    }

    /**
     * listadoLineas (Asynchronous)
     */
    public CompletableFuture<String> listadoLineasAsync() { // New async method for listadoLineas
        String url = buildUrl("listadoLineas", null); // No params for this endpoint
        return getJsonResponseAsync(url);
    }


    public static void main(String[] args) {
        DbusApiClient apiClient = new DbusApiClient.Builder() // Base URL is fixed, no need to set it
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