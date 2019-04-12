package com.matthewsyren.seriessearcher.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class fetches JSON returned from the TVMaze API in an AsyncTask
 */

@SuppressWarnings("WeakerAccess")
public class ApiConnection
        extends AsyncTask<String, Void, String> {
    //Declares an object of the IApiConnectionResponse interface, which will be used to send the JSON back to the  thread
    public IApiConnectionResponse delegate = null;

    /**
     * Retrieves the JSON returned from the API
     */
    protected String doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        try {
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = new StringBuilder();

            for(int i = 0; i < urls.length; i++){
                URL url = new URL(urls[i]);
                urlConnection = (HttpURLConnection) url.openConnection();
                bufferedReader  = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(urls.length > 1 && i < urls.length - 1){
                        stringBuilder.append(line).append(",\n");
                    }
                    else{
                        stringBuilder.append(line).append("\n");
                    }
                }
            }

            if(bufferedReader != null){
                bufferedReader.close();
            }

            return stringBuilder.toString();
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
        finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Passes the JSON back to the Main thread (to the point from which this class was instantiated)
     */
    protected void onPostExecute(String response) {
        if(delegate != null){
            delegate.parseJsonResponse(response);
        }
    }

    /**
     * Interface is used to pass API related data to the class that implements this interface
     */
    @SuppressWarnings("WeakerAccess")
    public interface IApiConnectionResponse {
        /**
         * Used to parse JSON data that was retrieved from an API. The class that needs the data will implement this interface, and the ApiConnection class sends the data to the method once it has fetched the data
         */
        void parseJsonResponse(String response);
    }
}