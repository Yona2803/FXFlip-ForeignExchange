package com.currencyApp.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import com.currencyApp.config.Config;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * Handles API responses from an exchange rate service
 * Visual error reporting via dialogs
 */
public class ExchangeRateService {
    private static final String BASE_URL = Config.get("baseUrl");
    private static final String API_KEY = Config.get("apiKey");
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000; // 15 seconds

    /**
     * Custom exception class for API-related errors
     */
    public static class ApiException extends Exception {
        private final int statusCode;

        public ApiException(String message) {
            super(message);
            this.statusCode = 0;
        }

        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * Fetches data from the exchange rate API
     *
     * @param endPoint the API endpoint to call
     * @return JSON response as a string
     * @throws ApiException if there are any issues with the API call
     */
    public static String getResponse(String endPoint) {
        try {
            return fetchDataFromApi(endPoint);
        } catch (ApiException e) {
            showErrorDialog("API Error", "Error calling exchange rate service: " + e.getMessage() +
                    (e.getStatusCode() > 0 ? " (Status code: " + e.getStatusCode() + ")" : ""));
            return null;
        } catch (Exception e) {
            showErrorDialog("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Internal method to fetch data from the API
     */
    private static String fetchDataFromApi(String endPoint) throws ApiException {
        HttpURLConnection conn = null;
        BufferedReader br = null;

        try {
            // Build the URL
            URI uri = new URI(BASE_URL + endPoint + API_KEY);
            URL url = uri.toURL();

            System.out.println("--> the URL : " + url);

            // Create the connection with timeouts
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            // Check for the successful response
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String errorMessage;
                try {
                    // Try to read the error stream for more details
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorMessage = errorResponse.toString();
                } catch (Exception e) {
                    errorMessage = "No additional error details available";
                }

                throw new ApiException("API returned error: " + errorMessage, responseCode);
            }

            // Read successful response
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            return sb.toString();

        } catch (UnknownHostException e) {
            throw new ApiException("Unable to connect to the server. Please check your internet connection.");
        } catch (IOException e) {
            throw new ApiException("Communication error: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new ApiException("Invalid URL format: " + e.getMessage());
        } catch (ApiException e) {
            throw e; // Re-throw ApiExceptions
        } catch (Exception e) {
            throw new ApiException("Unexpected error: " + e.getMessage());
        } finally {
            // Clean up resources
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Shows an error dialog on the EDT
     */
    private static void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
}