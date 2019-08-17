package com.matthewsyren.seriessearcher.network;

import android.os.AsyncTask;

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
    public IApiConnectionResponse mApiConnectionResponse = null;

    /**
     * Retrieves the JSON returned from the API
     */
    protected String doInBackground(String... urls) {
        //Initialises variable
        HttpURLConnection urlConnection = null;

        try {
            //Initialises variables
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = new StringBuilder();

            //Loops through the given URLs and appends the contents of the page that each URL points to to the stringBuilder variable
            for(int i = 0; i < urls.length; i++){
                //Fetches the next URL
                URL url = new URL(urls[i]);

                //Opens a connection to the URL and reads its contents
                urlConnection = (HttpURLConnection) url.openConnection();
                bufferedReader  = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;

                //Appends each line from the bufferedReader to the stringBuilder
                while ((line = bufferedReader.readLine()) != null) {
                    if(urls.length > 1 && i < urls.length - 1){
                        //Appends a comma before the \n character at the end of the stringBuilder (as there is another URL to follow)
                        stringBuilder.append(line).append(",\n");
                    }
                    else{
                        //Appends a \n character to the stringBuilder as it is the last URL
                        stringBuilder.append(line).append("\n");
                    }
                }
            }

            //Closes the BufferedReader
            if(bufferedReader != null){
                bufferedReader.close();
            }

            //Returns the full response
            return stringBuilder.toString();
        }
        catch(Exception e) {
            return null;
        }
        finally{
            //Disconnects the HttpURLConnection
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Passes the JSON back to the Main thread (to the point from which this class was instantiated)
     * @param response The response returned from the API
     */
    protected void onPostExecute(String response) {
        if(mApiConnectionResponse != null){
            mApiConnectionResponse.onJsonResponseRetrieved(response);
        }
    }

    /**
     * Setter method
     * @param iApiConnectionResponse The instance of the IApiConnectionResponse class to send the response to
     */
    public void setApiConnectionResponse(IApiConnectionResponse iApiConnectionResponse){
        mApiConnectionResponse = iApiConnectionResponse;
    }

    /**
     * Interface is used to pass API related data to the class that implements this interface
     */
    public interface IApiConnectionResponse {
        /**
         * Used to send JSON data that was retrieved from an API to the appropriate class. The class that needs the data will implement this interface, and the ApiConnection class sends the data to the method once it has fetched the data
         * @param response The response that was retrieved from the API
         */
        void onJsonResponseRetrieved(String response);
    }
}