package syrenware.seriessearcher;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by matthew on 2017/02/03.
 * Class fetches JSON returned from the TVMaze API in an AsyncTask
 */

public class APIConnection extends AsyncTask<String, Void, String> {

    //Declares an object of the IAPIConnectionResponse interface, which will be used to send the JSON back to the  thread
    public IAPIConnectionResponse delegate = null;

    protected void onPreExecute() {
        //progressBar.setVisibility(View.VISIBLE);
        //responseView.setText("");
    }

    //Method retrieves the JSON returned from the API
    protected String doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    //Method passes the JSON back to the Main thread (to the point from which this class was instantiated)
    protected void onPostExecute(String response) {
        if (response == null) {
            response = "THERE WAS AN ERROR";
        }
        if(delegate != null){
            delegate.getJsonResponse(response);
        }
        Log.i("INFO", response);
    }
}