// --- client package ---
package com.andiri.libs.dbus.api;

/**
 * <p>
 *  {@code DbusApiClientBuilder} class to construct a {@code DbusApiClient} instance.
 *  It allows configuring optional parameters such as default language, connect timeout, and read timeout.
 *  The base URL is fixed and cannot be changed through the builder.
 * </p>
 */
public class DbusApiClientBuilder {
    private String defaultLanguage = "es";
    private int connectTimeoutMillis = 5000;
    private int readTimeoutMillis = 10000;

    /**
     *  Constructs a new {@code DbusApiClientBuilder} instance with default settings.
     *  Base URL is predefined and cannot be set via builder.
     */
    public DbusApiClientBuilder() { // Base URL is fixed, no need to pass in constructor
    }

    /**
     * Sets the default language for API requests.
     * If not set, defaults to "es" (Spanish).
     * @param defaultLanguage the default language code (es, eu, en, fr).
     * @return This {@code DbusApiClientBuilder} instance for method chaining.
     */
    public DbusApiClientBuilder defaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }

    /**
     * Returns the currently configured default language.
     * @return The default language code.
     */
    String defaultLanguage() {
        return defaultLanguage;
    }


    /**
     * Sets the connection timeout for HTTP requests.
     * Defaults to 5000 milliseconds.
     * @param connectTimeoutMillis Timeout in milliseconds for establishing a connection.
     * @return This {@code DbusApiClientBuilder} instance for method chaining.
     */
    public DbusApiClientBuilder connectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    /**
     * Returns the currently configured connect timeout.
     * @return The connect timeout in milliseconds.
     */
    int connectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    /**
     * Sets the read timeout for HTTP requests.
     * Defaults to 10000 milliseconds.
     * @param readTimeoutMillis Timeout in milliseconds for reading data from the connection.
     * @return This {@code DbusApiClientBuilder} instance for method chaining.
     */
    public DbusApiClientBuilder readTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    /**
     * Returns the currently configured read timeout.
     * @return The read timeout in milliseconds.
     */
    int readTimeoutMillis() {
        return readTimeoutMillis;
    }


    /**
     * Builds and returns a new {@code DbusApiClient} instance with the configured parameters.
     * @return A new {@code DbusApiClient} instance.
     */
    public DbusApiClient build() {
        return new DbusApiClient(this);
    }
}