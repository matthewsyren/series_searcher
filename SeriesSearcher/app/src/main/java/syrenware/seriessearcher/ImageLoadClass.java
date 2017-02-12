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

class ImageLoadClass extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;
    public IAPIImage delegate = null;
    int position;

    public ImageLoadClass(String url, ImageView imageView, int position) {
        this.url = url;
        this.imageView = imageView;
        this.position = position;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try
        {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
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

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imageView.setImageBitmap(result);
        delegate.getJsonImage(result, position);
    }
}
