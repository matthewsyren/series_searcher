package com.matthewsyren.seriessearcher.network;

/**
 * This interface is used to pass data to the class that implements this interface
 */

@SuppressWarnings("WeakerAccess")
public interface IApiConnectionResponse {
    /**
     * Used to parse JSON data that was retrieved from an API. The class that needs the data will implement this interface, and the ApiConnection class sends the data to the method once it has fetched the data
     */
    void parseJsonResponse(String response);
}