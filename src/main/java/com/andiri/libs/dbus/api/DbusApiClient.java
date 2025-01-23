// --- client package ---
package com.andiri.libs.dbus.api;

import com.andiri.libs.dbus.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *  The {@code DbusApiClient} class provides a Java client to interact with the dBUS API.
 *  It handles communication, request building, response parsing, and error handling for all available API endpoints.
 *  The client is configured using a {@link DbusApiClientBuilder} for setting default language, connection and read timeouts.
 * </p>
 * <p>
 *  It supports both synchronous and asynchronous API calls, returning JSON responses as pretty-printed strings.
 *  Error handling is implemented through custom exceptions defined in the {@link com.andiri.libs.dbus.exceptions} package.
 * </p>
 */
public class DbusApiClient {

    private static final String BASE_URL = "http://62.99.53.182/SSIIMovilWSv2/ws/cons/"; // Hardcoded base URL
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String defaultLanguage;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    private static final Set<String> VALID_LANGUAGES = Set.of("es", "eu", "en", "fr");


    /**
     * Private constructor to create a {@code DbusApiClient} using a {@code Builder}.
     * Validates the default language and initializes the HttpClient and ObjectMapper.
     * @param builder The {@code Builder} instance containing configuration parameters.
     * @throws IllegalArgumentException if the default language is invalid.
     */
    DbusApiClient(DbusApiClientBuilder builder) {
        this.defaultLanguage = builder.defaultLanguage();
        this.connectTimeoutMillis = builder.connectTimeoutMillis();
        this.readTimeoutMillis = builder.readTimeoutMillis();

        if (!VALID_LANGUAGES.contains(this.defaultLanguage)) {
            throw new IllegalArgumentException("Invalid default language: " + this.defaultLanguage + ". Must be one of: " + VALID_LANGUAGES);
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(this.connectTimeoutMillis))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Private helper method to construct the full API URL by appending the path and parameters to the base URL.
     * @param path The API endpoint path.
     * @param params A map of query parameters to be added to the URL.
     * @return The complete API URL as a String.
     */
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

    /**
     * Private helper method to execute a synchronous HTTP GET request and parse the JSON response.
     * Returns the JSON response as a pretty-printed String.
     * @param url The full API URL to call.
     * @return The JSON response as a pretty-printed String.
     * @throws ApiException If the API request fails or the response cannot be parsed.
     */
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

    /**
     * Private helper method to execute an asynchronous HTTP GET request and parse the JSON response.
     * Returns a CompletableFuture that resolves to the JSON response as a pretty-printed String.
     * @param url The full API URL to call.
     * @return A CompletableFuture that resolves to the JSON response as a pretty-printed String.
     */
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

    /**
     * Private helper method to create a specific {@link ApiException} based on the HTTP status code.
     * @param statusCode The HTTP status code of the response.
     * @param responseBody The response body as a String.
     * @return An {@link ApiException} or a subclass representing the specific error.
     */
    private ApiException createApiExceptionForStatusCode(int statusCode, String responseBody) {
        switch (statusCode) {
            case 400: return new BadRequestException("Bad Request", statusCode, responseBody);
            case 401: return new UnauthorizedException("Unauthorized", statusCode, responseBody);
            case 404: return new NotFoundException("Not Found", statusCode, responseBody);
            case 500: return new ServerErrorException("Internal Server Error", statusCode, responseBody);
            default:  return new ApiException("API Error", statusCode, responseBody);
        }
    }

    /**
     * Private helper method to validate if the provided language code is valid.
     * @param idioma The language code to validate.
     * @throws IllegalArgumentException if the language code is not in the list of valid languages.
     */
    private void validateLanguage(String idioma) {
        if (!VALID_LANGUAGES.contains(idioma)) {
            throw new IllegalArgumentException("Invalid language: " + idioma + ". Must be one of: " + VALID_LANGUAGES);
        }
    }

    /**
     * Private helper method to get the language to use for the API request.
     * If the provided language is null, it uses the default language; otherwise, it uses the provided language.
     * It also validates the language code.
     * @param idioma The language code provided for the request (can be null).
     * @return The validated language code to be used for the API request.
     */
    private String getLanguageOrDefault(String idioma) {
        String languageToUse = idioma == null ? defaultLanguage : idioma;
        validateLanguage(languageToUse);
        return languageToUse;
    }


    // --- API Endpoint Methods (Synchronous) ---

    /**
     * <p>
     *  Synchronously retrieves estimated arrival times for buses at a specific stop.
     *  Endpoint: {@code tiemposParada?codParada=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String tiemposParada(int codParada, String idioma) throws ApiException { // idioma ya no es String? sino String
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", String.valueOf(codParada)); // Convertir int a String para el HashMap
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParada", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves estimated arrival times for a specific bus at a specific stop.
     *  Endpoint: {@code tiemposParadaBus?codParada=%s&codVehiculo=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param codVehiculo The code of the bus vehicle.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String tiemposParadaBus(int codParada, int codVehiculo, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", String.valueOf(codParada)); // Convertir int a String para el HashMap
        params.put("codVehiculo", String.valueOf(codVehiculo));
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParadaBus", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves data for a specific bus vehicle.
     *  Endpoint: {@code datosVehiculo?codVehiculo=%s&codEmpresa=1&petItinerario=%s}
     * </p>
     * @param codVehiculo The code of the bus vehicle.
     * @param petItinerario Boolean value (true/false) as String to request itinerary information.
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String datosVehiculo(int codVehiculo, boolean petItinerario) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("codVehiculo", String.valueOf(codVehiculo));
        params.put("codEmpresa", "1"); // Hardcoded as per documentation
        params.put("petItinerario", String.valueOf(petItinerario));
        String url = buildUrl("datosVehiculo", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves general alerts or notices from the dBUS service.
     *  Endpoint: {@code avisos?idioma=%s}
     * </p>
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String avisos(String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idioma", languageToUse);
        String url = buildUrl("avisos", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves trip information for a specific stop and itinerary.
     *  Endpoint: {@code expedicionesParadaItinerario?idItinerario=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s}
     * </p>
     * @param idItinerario The ID of the itinerary.
     * @param idParada The ID of the bus stop.
     * @param fecha Date in ddMMyy format.
     * @param hora Time in HHmm format.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String expedicionesParadaItinerario(int idItinerario, int idParada, LocalDate fecha, LocalTime hora, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String fechaStr = fecha.format(dateFormatter);
        String horaStr = hora.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", String.valueOf(idItinerario));
        params.put("idParada", String.valueOf(idParada));
        params.put("fecha", fechaStr);
        params.put("hora", horaStr);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaItinerario", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves trip information for a specific stop and direction (sense).
     *  Endpoint: {@code expedicionesParadaSentido?idSentido=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s}
     * </p>
     * @param idSentido The ID of the direction (sense).
     * @param idParada The ID of the bus stop.
     * @param fecha Date in ddMMyy format.
     * @param hora Time in HHmm format.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String expedicionesParadaSentido(int idSentido, int idParada, LocalDate fecha, LocalTime hora, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String fechaStr = fecha.format(dateFormatter);
        String horaStr = hora.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idSentido", String.valueOf(idSentido));
        params.put("idParada", String.valueOf(idParada));
        params.put("fecha", fechaStr);
        params.put("hora", horaStr);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaSentido", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves itineraries for a specific line, optionally filtered by a stop.
     *  Endpoints:
     *  <ul>
     *      <li>{@code itinerariosLinea?idLinea=%s&idioma=%s}</li>
     *      <li>{@code itinerariosLinea?idLinea=%s&idParada=%s&idioma=%s}</li>
     *  </ul>
     * </p>
     * @param idLinea The ID of the bus line.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String itinerariosLinea(int idLinea, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        params.put("idioma", languageToUse);
        String url = buildUrl("itinerariosLinea", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves directions (senses) for a specific line, optionally filtered by a stop.
     *  Endpoints:
     *  <ul>
     *      <li>{@code sentidosLinea?idLinea=%s&idioma=%s}</li>
     *      <li>{@code sentidosLinea?idLinea=%s&idParada=%s&idioma=%s}</li>
     *  </ul>
     * </p>
     * @param idLinea The ID of the bus line.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public String sentidosLinea(int idLinea, String idioma) throws ApiException { // Return String JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        params.put("idioma", languageToUse);
        String url = buildUrl("sentidosLinea", params);
        return getJsonResponse(url);
    }

    // TODO

    /**
     * <p>
     *  Synchronously retrieves trips for a specific itinerary, stop, day type, and time range.
     *  Endpoint: {@code expedicionesItinerario?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s&idItinerario=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type // NO HAY INFORMACION PERO SE USA "H" SI NO SE METE NINGUN VALOR.
     * @param hInicio Start time in HHMM format.
     * @param hFin End time in HHMM format.
     * @param idItinerario The ID of the itinerary.
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String expedicionesItinerario(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin, int idItinerario) throws ApiException { // Return String JSON

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String hInicioStr = hInicio.format(timeFormatter);
        String hFinStr = hFin.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idParada", String.valueOf(idParada));
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicioStr);
        params.put("hFin", hFinStr);
        params.put("idItinerario", String.valueOf(idItinerario));
        String url = buildUrl("expedicionesItinerario", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves lines serving a specific stop for a given day type and time range.
     *  Endpoint: {@code lineasParada?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type (LAB, SAB, DOM).
     * @param hInicio Start time in HHMM format.
     * @param hFin End time in HHMM format.
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String lineasParada(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin) throws ApiException { // Return String JSON
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String hInicioStr = hInicio.format(timeFormatter);
        String hFinStr = hFin.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idParada", String.valueOf(idParada));
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicioStr);
        params.put("hFin", hFinStr);
        String url = buildUrl("lineasParada", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves stops for a specific itinerary.
     *  Endpoint: {@code paradasItinerario?idItinerario=%s}
     * </p>
     * @param idItinerario The ID of the itinerary.
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String paradasItinerario(int idItinerario) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", String.valueOf(idItinerario));
        String url = buildUrl("paradasItinerario", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves stops for a specific direction (sense).
     *  Endpoint: {@code paradasSentido?idSentido=%s}
     * </p>
     * @param idSentido The ID of the direction (sense).
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String paradasSentido(int idSentido) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", String.valueOf(idSentido));
        String url = buildUrl("paradasSentido", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves the route path (geometry) for a specific line.
     *  Endpoint: {@code recorridoLinea?idLinea=%s}
     * </p>
     * @param idLinea The ID of the bus line.
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String recorridoLinea(int idLinea) throws ApiException { // Return String JSON
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        String url = buildUrl("recorridoLinea", params);
        return getJsonResponse(url);
    }

    /**
     * <p>
     *  Synchronously retrieves a list of all bus lines.
     *  Endpoint: {@code listadoLineas}
     * </p>
     * @return A JSON string representing the API response, pretty-printed.
     * @throws ApiException If the API request fails.
     */
    public String listadoLineas() throws ApiException { // New method for listadoLineas
        String url = buildUrl("listadoLineas", null); // No params for this endpoint
        return getJsonResponse(url);
    }


    // --- API Endpoint Methods (Asynchronous) ---

    /**
     * <p>
     *  Asynchronously retrieves estimated arrival times for buses at a specific stop.
     *  Endpoint: {@code tiemposParada?codParada=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> tiemposParadaAsync(int codParada, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", String.valueOf(codParada));
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParada", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves estimated arrival times for a specific bus at a specific stop.
     *  Endpoint: {@code tiemposParadaBus?codParada=%s&codVehiculo=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param codVehiculo The code of the bus vehicle.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> tiemposParadaBusAsync(int codParada, int codVehiculo, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", String.valueOf(codParada));
        params.put("codVehiculo", String.valueOf(codVehiculo));
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParadaBus", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves data for a specific bus vehicle.
     *  Endpoint: {@code datosVehiculo?codVehiculo=%s&codEmpresa=1&petItinerario=%s}
     * </p>
     * @param codVehiculo The code of the bus vehicle.
     * @param petItinerario Boolean value (true/false) as String to request itinerary information.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> datosVehiculoAsync(int codVehiculo, boolean petItinerario) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("codVehiculo", String.valueOf(codVehiculo));
        params.put("codEmpresa", "1"); // Hardcoded as per documentation
        params.put("petItinerario", String.valueOf(petItinerario));
        String url = buildUrl("datosVehiculo", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves general alerts or notices from the dBUS service.
     *  Endpoint: {@code avisos?idioma=%s}
     * </p>
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> avisosAsync(String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idioma", languageToUse);
        String url = buildUrl("avisos", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves trip information for a specific stop and itinerary.
     *  Endpoint: {@code expedicionesParadaItinerario?idItinerario=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s}
     * </p>
     * @param idItinerario The ID of the itinerary.
     * @param idParada The ID of the bus stop.
     * @param fecha Date in ddMMyy format.
     * @param hora Time in HHmm format.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> expedicionesParadaItinerarioAsync(int idItinerario, int idParada, LocalDate fecha, LocalTime hora, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String fechaStr = fecha.format(dateFormatter);
        String horaStr = hora.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", String.valueOf(idItinerario));
        params.put("idParada", String.valueOf(idParada));
        params.put("fecha", fechaStr);
        params.put("hora", horaStr);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaItinerario", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves trip information for a specific stop and direction (sense).
     *  Endpoint: {@code expedicionesParadaSentido?idSentido=%s&idParada=%s&fecha=%s&hora=%s&idioma=%s}
     * </p>
     * @param idSentido The ID of the direction (sense).
     * @param idParada The ID of the bus stop.
     * @param fecha Date in ddMMyy format.
     * @param hora Time in HHmm format.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> expedicionesParadaSentidoAsync(int idSentido, int idParada, LocalDate fecha, LocalTime hora, String idioma) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String fechaStr = fecha.format(dateFormatter);
        String horaStr = hora.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idSentido", String.valueOf(idSentido));
        params.put("idParada", String.valueOf(idParada));
        params.put("fecha", fechaStr);
        params.put("hora", horaStr);
        params.put("idioma", languageToUse);
        String url = buildUrl("expedicionesParadaSentido", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves itineraries for a specific line, optionally filtered by a stop.
     *  Endpoints:
     *  <ul>
     *      <li>{@code itinerariosLinea?idLinea=%s&idioma=%s}</li>
     *      <li>{@code itinerariosLinea?idLinea=%s&idParada=%s&idioma=%s}</li>
     *  </ul>
     * </p>
     * @param idLinea The ID of the bus line.
     * @param idioma The language for the response (es, eu, en, fr).
     * @param idParada (Optional) The ID of the bus stop to filter itineraries.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> itinerariosLineaAsync(int idLinea, String idioma, String... idParada) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        params.put("idioma", languageToUse);
        String path = "itinerariosLinea";
        if (idParada.length > 0) {
            params.put("idParada", idParada[0]);
        }
        String url = buildUrl(path, params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves directions (senses) for a specific line, optionally filtered by a stop.
     *  Endpoints:
     *  <ul>
     *      <li>{@code sentidosLinea?idLinea=%s&idioma=%s}</li>
     *      <li>{@code sentidosLinea?idLinea=%s&idParada=%s&idioma=%s}</li>
     *  </ul>
     * </p>
     * @param idLinea The ID of the bus line.
     * @param idioma The language for the response (es, eu, en, fr).
     * @param idParada (Optional) The ID of the bus stop to filter directions.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public CompletableFuture<String> sentidosLineaAsync(int idLinea, String idioma, String... idParada) { // Return CompletableFuture<String> JSON
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        params.put("idioma", languageToUse);
        String path = "sentidosLinea";
        if (idParada.length > 0) {
            params.put("idParada", idParada[0]);
        }
        String url = buildUrl(path, params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves trips for a specific itinerary, stop, day type, and time range.
     *  Endpoint: {@code expedicionesItinerario?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s&idItinerario=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type (LAB, SAB, DOM).
     * @param hInicio Start time in HHMM format.
     * @param hFin End time in HHMM format.
     * @param idItinerario The ID of the itinerary.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> expedicionesItinerarioAsync(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin, int idItinerario) { // Return CompletableFuture<String> JSON
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String hInicioStr = hInicio.format(timeFormatter);
        String hFinStr = hFin.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idParada", String.valueOf(idParada));
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicioStr);
        params.put("hFin", hFinStr);
        params.put("idItinerario", String.valueOf(idItinerario));
        String url = buildUrl("expedicionesItinerario", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves lines serving a specific stop for a given day type and time range.
     *  Endpoint: {@code lineasParada?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type (LAB, SAB, DOM).
     * @param hInicio Start time in HHMM format.
     * @param hFin End time in HHMM format.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> lineasParadaAsync(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin) { // Return CompletableFuture<String> JSON
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String hInicioStr = hInicio.format(timeFormatter);
        String hFinStr = hFin.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idParada", String.valueOf(idParada));
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicioStr);
        params.put("hFin", hFinStr);
        String url = buildUrl("lineasParada", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves stops for a specific itinerary.
     *  Endpoint: {@code paradasItinerario?idItinerario=%s}
     * </p>
     * @param idItinerario The ID of the itinerary.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> paradasItinerarioAsync(int idItinerario) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", String.valueOf(idItinerario));
        String url = buildUrl("paradasItinerario", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves stops for a specific direction (sense).
     *  Endpoint: {@code paradasSentido?idSentido=%s}
     * </p>
     * @param idSentido The ID of the direction (sense).
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> paradasSentidoAsync(int idSentido) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", String.valueOf(idSentido));
        String url = buildUrl("paradasSentido", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *  Asynchronously retrieves the route path (geometry) for a specific line.
     *  Endpoint: {@code recorridoLinea?idLinea=%s}
     * </p>
     * @param idLinea The ID of the bus line.
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> recorridoLineaAsync(int idLinea) { // Return CompletableFuture<String> JSON
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        String url = buildUrl("recorridoLinea", params);
        return getJsonResponseAsync(url);
    }

    /**
     * <p>
     *   Asynchronously retrieves a list of all bus lines.
     *   Endpoint: {@code listadoLineas}
     * </p>
     * @return A CompletableFuture that resolves to a JSON string representing the API response, pretty-printed.
     */
    public CompletableFuture<String> listadoLineasAsync() { // New async method for listadoLineas
        String url = buildUrl("listadoLineas", null); // No params for this endpoint
        return getJsonResponseAsync(url);
    }
}