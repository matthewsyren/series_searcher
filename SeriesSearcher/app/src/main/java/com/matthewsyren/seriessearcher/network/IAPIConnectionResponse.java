package com.matthewsyren.seriessearcher.network;

/**
 * Created by matthew on 2017/02/03.
 * This interface is used to pass data to the class that implements this interface
 */

@SuppressWarnings("WeakerAccess")
public interface IAPIConnectionResponse {
    /**
     * Used to parse JSON data that was retrieved from an API. The class that needs the data will implement this interface, and the APIConnection class sends the data to the method once it has fetched the data
     */
    void parseJsonResponse(String response);
}