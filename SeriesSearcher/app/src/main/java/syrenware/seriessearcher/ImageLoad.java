package syrenware.seriessearcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by matthew on 2017/02/03.
 */

class ImageLoad extends AsyncTask<Void, Void, Bitmap> {
    //Declarations
    private String url;
    public IAPIImage delegate = null;
    int position;

    //Constructor (accepts URL of image, the ImageView to display the image, and the position to store the image in (incrementer)
    public ImageLoad(String url, int position) {
        this.url = url;
        this.position = position;
    }

    //Method fetches the image from the URL specified in the constructor
    @Override
    protected Bitmap doInBackground(Void... params) {
        try
        {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();

            //Specifies that you want to read the response from the URL and connects to the URL
            connection.setDoInput(true);
            connection.connect();

            //Fetches the input stream from the API and decodes it into a Bitmap
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    //Method sets the image in the ImageView and sends the image and its position to the class that implemented the IAPIImage class
    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        //imageView.setImageBitmap(result);
        delegate.getJsonImage(result, position);
    }
}