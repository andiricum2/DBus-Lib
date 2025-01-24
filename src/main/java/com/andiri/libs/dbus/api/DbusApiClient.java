// --- client package ---
package com.andiri.libs.dbus.api;

import com.andiri.libs.dbus.exceptions.*;
import com.andiri.libs.dbus.model.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.MediaType;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 *  The {@code DbusApiClient} class provides a Java client to interact with the dBUS API using OkHttp.
 *  It handles communication, request building, response parsing, and error handling for all available API endpoints.
 *  The client is configured using a {@link DbusApiClientBuilder} for setting default language, connection and read timeouts, and optionally providing a custom OkHttpClient.
 * </p>
 * <p>
 *  It supports both synchronous and asynchronous API calls, now returning Java models instead of JSON strings.
 *  Error handling is implemented through custom exceptions defined in the {@link com.andiri.libs.dbus.exceptions} package.
 * </p>
 */
public class DbusApiClient {

    private static final String BASE_URL = "http://62.99.53.182/SSIIMovilWSv2/ws/cons/"; // Hardcoded base URL
    private final ObjectMapper objectMapper;
    private final String defaultLanguage;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Executor for async requests
    private final OkHttpClient httpClient;
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final Set<String> VALID_LANGUAGES = new HashSet<>(java.util.Arrays.asList("es", "eu", "en", "fr"));

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

        this.objectMapper = new ObjectMapper();
        this.httpClient = builder.httpClient() != null ? builder.httpClient() : new OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofMillis(this.connectTimeoutMillis))
                .readTimeout(java.time.Duration.ofMillis(this.readTimeoutMillis))
                .build();
    }

    /**
     * Private helper method to construct the full API URL by appending the path and parameters to the base URL.
     * @param path The API endpoint path.
     * @param params A map of query parameters to be added to the URL.
     * @return The complete API URL as a String.
     */
    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL); // Use BASE_URL here
        try {
            urlBuilder.append(path).append("?");
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()))
                            .append("&");
                }
            }
            if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
                urlBuilder.deleteCharAt(urlBuilder.length() - 1);
            }
            return urlBuilder.toString();
        } catch (IOException e) {
            throw new ApiConnectionException("Error encoding URL parameters", e); // Or handle more gracefully
        }
    }

    /**
     * Private helper method to execute a synchronous HTTP GET request and parse the JSON response to a specific model class using OkHttp.
     * @param url The full API URL to call.
     * @param responseClass The class of the model to which the JSON response should be deserialized.
     * @param <T> The type of the response model.
     * @return The deserialized response model.
     * @throws ApiException If the API request fails or the response cannot be parsed.
     */
    private <T> T getJsonResponse(String url, Class<T> responseClass) throws ApiException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            int statusCode = response.code();

            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    try {
                        return objectMapper.readValue(responseBody.string(), responseClass);
                    } catch (Exception e) {
                        throw new ApiResponseParseException("Error parsing API response", responseBody.string(), e);
                    }
                } else {
                    throw new ApiResponseParseException("Empty API response body", "", null);
                }
            } else {
                String responseBodyString = "";
                ResponseBody errorBody = response.body();
                if (errorBody != null) {
                    try {
                        responseBodyString = errorBody.string();
                    } catch (IOException ioException) {
                        responseBodyString = "Could not read error stream";
                    }
                }
                throw createApiExceptionForStatusCode(statusCode, responseBodyString);
            }

        } catch (IOException e) {
            throw new ApiConnectionException("Error connecting to API", e);
        }
    }

    /**
     * Private helper method to execute an asynchronous HTTP GET request and parse the JSON response to a specific model class using OkHttp.
     * @param url The full API URL to call.
     * @param responseClass The class of the model to which the JSON response should be deserialized.
     * @param <T> The type of the response model.
     * @return A CompletableFuture that resolves to the deserialized response model.
     */
    private <T> CompletableFuture<T> getJsonResponseAsync(String url, Class<T> responseClass) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getJsonResponse(url, responseClass);
            } catch (ApiException e) {
                throw new CompletionException(e); // Wrap ApiException for CompletableFuture
            }
        }, executorService).exceptionally(throwable -> {
            if (throwable instanceof CompletionException) {
                Throwable cause = throwable.getCause();
                if (cause instanceof ApiException) {
                    throw (ApiException) cause; // Re-throw the original ApiException
                }
            }
            if (throwable instanceof ApiException) {
                throw (ApiException) throwable;
            }
            throw new ApiConnectionException("Error executing asynchronous API call", throwable); // Generic async exception
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


    // --- API Endpoint Methods (Asynchronous) ---

    /**
     * <p>
     *  Asynchronously retrieves estimated arrival times for buses at a specific stop.
     *  Endpoint: {@code tiemposParada?codParada=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A {@link CompletableFuture} that resolves to a {@link TiemposParadaResponse} representing the API response.
     */
    public CompletableFuture<TiemposParadaResponse> tiemposParadaAsync(int codParada, String idioma) {
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", String.valueOf(codParada));
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParada", params);
        return getJsonResponseAsync(url, TiemposParadaResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves estimated arrival times for a specific bus at a specific stop.
     *  Endpoint: {@code tiemposParadaBus?codParada=%s&codVehiculo=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param codVehiculo The code of the bus vehicle.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A {@link CompletableFuture} that resolves to a {@link TiemposParadaResponse} representing the API response.
     */
    public CompletableFuture<TiemposParadaResponse> tiemposParadaBusAsync(int codParada, int codVehiculo, String idioma) {
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("codParada", String.valueOf(codParada));
        params.put("codVehiculo", String.valueOf(codVehiculo));
        params.put("idioma", languageToUse);
        String url = buildUrl("tiemposParadaBus", params);
        return getJsonResponseAsync(url, TiemposParadaResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves data for a specific bus vehicle.
     *  Endpoint: {@code datosVehiculo?codVehiculo=%s&codEmpresa=1&petItinerario=%s}
     * </p>
     * @param codVehiculo The code of the bus vehicle.
     * @param petItinerario Boolean value (true/false) as String to request itinerary information.
     * @return A {@link CompletableFuture} that resolves to a {@link DatosVehiculoResponse} representing the API response.
     */
    public CompletableFuture<DatosVehiculoResponse> datosVehiculoAsync(int codVehiculo, boolean petItinerario) {
        Map<String, String> params = new HashMap<>();
        params.put("codVehiculo", String.valueOf(codVehiculo));
        params.put("codEmpresa", "1"); // Hardcoded as per documentation
        params.put("petItinerario", String.valueOf(petItinerario));
        String url = buildUrl("datosVehiculo", params);
        return getJsonResponseAsync(url, DatosVehiculoResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves general alerts or notices from the dBUS service.
     *  Endpoint: {@code avisos?idioma=%s}
     * </p>
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A {@link CompletableFuture} that resolves to a {@link AvisosResponse} representing the API response.
     */
    public CompletableFuture<AvisosResponse> avisosAsync(String idioma) {
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idioma", languageToUse);
        String url = buildUrl("avisos", params);
        return getJsonResponseAsync(url, AvisosResponse.class);
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
     * @return A {@link CompletableFuture} that resolves to a {@link ExpedicionesParadaItinerarioResponse} representing the API response.
     */
    public CompletableFuture<ExpedicionesParadaItinerarioResponse> expedicionesParadaItinerarioAsync(int idItinerario, int idParada, LocalDate fecha, LocalTime hora, String idioma) {
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
        return getJsonResponseAsync(url, ExpedicionesParadaItinerarioResponse.class);
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
     * @return A {@link CompletableFuture} that resolves to a {@link ExpedicionesParadaSentidoResponse} representing the API response.
     */
    public CompletableFuture<ExpedicionesParadaSentidoResponse> expedicionesParadaSentidoAsync(int idSentido, int idParada, LocalDate fecha, LocalTime hora, String idioma) {
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
        return getJsonResponseAsync(url, ExpedicionesParadaSentidoResponse.class);
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
     * @return A {@link CompletableFuture} that resolves to a {@link ItinerariosLineaResponse} representing the API response.
     */
    public CompletableFuture<ItinerariosLineaResponse> itinerariosLineaAsync(int idLinea, String idioma) {
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        params.put("idioma", languageToUse);
        String url = buildUrl("itinerariosLinea", params);
        return getJsonResponseAsync(url, ItinerariosLineaResponse.class);
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
     * @return A {@link CompletableFuture} that resolves to a {@link SentidosLineaResponse} representing the API response.
     */
    public CompletableFuture<SentidosLineaResponse> sentidosLineaAsync(int idLinea, String idioma) {
        String languageToUse = getLanguageOrDefault(idioma);
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        params.put("idioma", languageToUse);
        String url = buildUrl("sentidosLinea", params);
        return getJsonResponseAsync(url, SentidosLineaResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves trips for a specific itinerary, stop, day type, and time range.
     *  Endpoint: {@code expedicionesItinerario?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s&idItinerario=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type // NO HAY INFORMACION PERO SE USA "H" SI NO SE METE NINGUN VALOR.
     * @param hInicio Start time in HHmm format.
     * @param hFin End time in HHmm format.
     * @param idItinerario The ID of the itinerary.
     * @return A {@link CompletableFuture} that resolves to a {@link ExpedicionesItinerarioResponse} representing the API response.
     */
    public CompletableFuture<ExpedicionesItinerarioResponse> expedicionesItinerarioAsync(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin, int idItinerario) {
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
        return getJsonResponseAsync(url, ExpedicionesItinerarioResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves lines serving a specific stop for a given day type and time range.
     *  Endpoint: {@code lineasParada?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type (LAB, SAB, DOM).
     * @param hInicio Start time in HHmm format.
     * @param hFin End time in HHmm format.
     * @return A {@link CompletableFuture} that resolves to a {@link LineasParadaResponse} representing the API response.
     */
    public CompletableFuture<LineasParadaResponse> lineasParadaAsync(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        String hInicioStr = hInicio.format(timeFormatter);
        String hFinStr = hFin.format(timeFormatter);

        Map<String, String> params = new HashMap<>();
        params.put("idParada", String.valueOf(idParada));
        params.put("tipoDia", tipoDia);
        params.put("hInicio", hInicioStr);
        params.put("hFin", hFinStr);
        String url = buildUrl("lineasParada", params);
        return getJsonResponseAsync(url, LineasParadaResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves stops for a specific itinerary.
     *  Endpoint: {@code paradasItinerario?idItinerario=%s}
     * </p>
     * @param idItinerario The ID of the itinerary.
     * @return A {@link CompletableFuture} that resolves to a {@link ParadasItinerarioResponse} representing the API response.
     */
    public CompletableFuture<ParadasItinerarioResponse> paradasItinerarioAsync(int idItinerario) {
        Map<String, String> params = new HashMap<>();
        params.put("idItinerario", String.valueOf(idItinerario));
        String url = buildUrl("paradasItinerario", params);
        return getJsonResponseAsync(url, ParadasItinerarioResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves stops for a specific direction (sense).
     *  Endpoint: {@code paradasSentido?idSentido=%s}
     * </p>
     * @param idSentido The ID of the direction (sense).
     * @return A {@link CompletableFuture} that resolves to a {@link ParadasSentidoResponse} representing the API response.
     */
    public CompletableFuture<ParadasSentidoResponse> paradasSentidoAsync(int idSentido) {
        Map<String, String> params = new HashMap<>();
        params.put("idSentido", String.valueOf(idSentido));
        String url = buildUrl("paradasSentido", params);
        return getJsonResponseAsync(url, ParadasSentidoResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves the route path (geometry) for a specific line.
     *  Endpoint: {@code recorridoLinea?idLinea=%s}
     * </p>
     * @param idLinea The ID of the bus line.
     * @return A {@link CompletableFuture} that resolves to a {@link RecorridoLineaResponse} representing the API response.
     */
    public CompletableFuture<RecorridoLineaResponse> recorridoLineaAsync(int idLinea) {
        Map<String, String> params = new HashMap<>();
        params.put("idLinea", String.valueOf(idLinea));
        String url = buildUrl("recorridoLinea", params);
        return getJsonResponseAsync(url, RecorridoLineaResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves a list of all bus lines.
     *  Endpoint: {@code listadoLineas}
     * </p>
     * @return A {@link CompletableFuture} that resolves to a {@link ListadoLineasResponse} representing the API response.
     */
    public CompletableFuture<ListadoLineasResponse> listadoLineasAsync() {
        String url = buildUrl("listadoLineas", null); // No params for this endpoint
        return getJsonResponseAsync(url, ListadoLineasResponse.class);
    }

    /**
     * <p>
     *  Asynchronously retrieves a list of all stop points.
     *  Endpoint: {@code puntosParada}
     * </p>
     * @return A {@link CompletableFuture} that resolves to a {@link PuntosParadaResponse} representing the API response.
     */
    public CompletableFuture<PuntosParadaResponse> puntosParadaAsync() {
        String url = buildUrl("puntosParada", null);
        return getJsonResponseAsync(url, PuntosParadaResponse.class);
    }


    // --- API Endpoint Methods (Synchronous) ---

    /**
     * <p>
     *  Synchronously retrieves estimated arrival times for buses at a specific stop.
     *  Endpoint: {@code tiemposParada?codParada=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A {@link TiemposParadaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public TiemposParadaResponse tiemposParada(int codParada, String idioma) throws ApiException {
        return tiemposParadaAsync(codParada, idioma).join();
    }

    /**
     * <p>
     *  Synchronously retrieves estimated arrival times for a specific bus at a specific stop.
     *  Endpoint: {@code tiemposParadaBus?codParada=%s&codVehiculo=%s&idioma=%s}
     * </p>
     * @param codParada The code of the bus stop.
     * @param codVehiculo The code of the bus vehicle.
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A {@link TiemposParadaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public TiemposParadaResponse tiemposParadaBus(int codParada, int codVehiculo, String idioma) throws ApiException {
        return tiemposParadaBusAsync(codParada, codVehiculo, idioma).join();
    }

    /**
     * <p>
     *  Synchronously retrieves data for a specific bus vehicle.
     *  Endpoint: {@code datosVehiculo?codVehiculo=%s&codEmpresa=1&petItinerario=%s}
     * </p>
     * @param codVehiculo The code of the bus vehicle.
     * @param petItinerario Boolean value (true/false) as String to request itinerary information.
     * @return A {@link DatosVehiculoResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public DatosVehiculoResponse datosVehiculo(int codVehiculo, boolean petItinerario) throws ApiException {
        return datosVehiculoAsync(codVehiculo, petItinerario).join();
    }

    /**
     * <p>
     *  Synchronously retrieves general alerts or notices from the dBUS service.
     *  Endpoint: {@code avisos?idioma=%s}
     * </p>
     * @param idioma The language for the response (es, eu, en, fr).
     * @return A {@link AvisosResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public AvisosResponse avisos(String idioma) throws ApiException {
        return avisosAsync(idioma).join();
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
     * @return A {@link ExpedicionesParadaItinerarioResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public ExpedicionesParadaItinerarioResponse expedicionesParadaItinerario(int idItinerario, int idParada, LocalDate fecha, LocalTime hora, String idioma) throws ApiException {
        return expedicionesParadaItinerarioAsync(idItinerario, idParada, fecha, hora, idioma).join();
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
     * @return A {@link ExpedicionesParadaSentidoResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public ExpedicionesParadaSentidoResponse expedicionesParadaSentido(int idSentido, int idParada, LocalDate fecha, LocalTime hora, String idioma) throws ApiException {
        return expedicionesParadaSentidoAsync(idSentido, idParada, fecha, hora, idioma).join();
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
     * @return A {@link ItinerariosLineaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public ItinerariosLineaResponse itinerariosLinea(int idLinea, String idioma) throws ApiException {
        return itinerariosLineaAsync(idLinea, idioma).join();
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
     * @return A {@link SentidosLineaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     * @throws IllegalArgumentException if the language code is invalid.
     */
    public SentidosLineaResponse sentidosLinea(int idLinea, String idioma) throws ApiException {
        return sentidosLineaAsync(idLinea, idioma).join();
    }

    /**
     * <p>
     *  Synchronously retrieves trips for a specific itinerary, stop, day type, and time range.
     *  Endpoint: {@code expedicionesItinerario?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s&idItinerario=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type // NO HAY INFORMACION PERO SE USA "H" SI NO SE METE NINGUN VALOR.
     * @param hInicio Start time in HHmm format.
     * @param hFin End time in HHmm format.
     * @param idItinerario The ID of the itinerary.
     * @return A {@link ExpedicionesItinerarioResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public ExpedicionesItinerarioResponse expedicionesItinerario(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin, int idItinerario) throws ApiException {
        return expedicionesItinerarioAsync(idParada, tipoDia, hInicio, hFin, idItinerario).join();
    }

    /**
     * <p>
     *  Synchronously retrieves lines serving a specific stop for a given day type and time range.
     *  Endpoint: {@code lineasParada?idParada=%s&tipoDia=%s&hInicio=%s&hFin=%s}
     * </p>
     * @param idParada The ID of the bus stop.
     * @param tipoDia Day type (LAB, SAB, DOM).
     * @param hInicio Start time in HHmm format.
     * @param hFin End time in HHmm format.
     * @return A {@link LineasParadaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public LineasParadaResponse lineasParada(int idParada, String tipoDia, LocalTime hInicio, LocalTime hFin) throws ApiException {
        return lineasParadaAsync(idParada, tipoDia, hInicio, hFin).join();
    }

    /**
     * <p>
     *  Synchronously retrieves stops for a specific itinerary.
     *  Endpoint: {@code paradasItinerario?idItinerario=%s}
     * </p>
     * @param idItinerario The ID of the itinerary.
     * @return A {@link ParadasItinerarioResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public ParadasItinerarioResponse paradasItinerario(int idItinerario) throws ApiException {
        return paradasItinerarioAsync(idItinerario).join();
    }

    /**
     * <p>
     *  Synchronously retrieves stops for a specific direction (sense).
     *  Endpoint: {@code paradasSentido?idSentido=%s}
     * </p>
     * @param idSentido The ID of the direction (sense).
     * @return A {@link ParadasSentidoResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public ParadasSentidoResponse paradasSentido(int idSentido) throws ApiException {
        return paradasSentidoAsync(idSentido).join();
    }

    /**
     * <p>
     *  Synchronously retrieves the route path (geometry) for a specific line.
     *  Endpoint: {@code recorridoLinea?idLinea=%s}
     * </p>
     * @param idLinea The ID of the bus line.
     * @return A {@link RecorridoLineaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public RecorridoLineaResponse recorridoLinea(int idLinea) throws ApiException {
        return recorridoLineaAsync(idLinea).join();
    }

    /**
     * <p>
     *  Synchronously retrieves a list of all bus lines.
     *  Endpoint: {@code listadoLineas}
     * </p>
     * @return A {@link ListadoLineasResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public ListadoLineasResponse listadoLineas() throws ApiException {
        return listadoLineasAsync().join();
    }

    /**
     * <p>
     *  Synchronously retrieves a list of all stop points.
     *  Endpoint: {@code puntosParada}
     * </p>
     * @return A {@link PuntosParadaResponse} representing the API response.
     * @throws ApiException If the API request fails.
     */
    public PuntosParadaResponse puntosParada() throws ApiException {
        return puntosParadaAsync().join();
    }
}