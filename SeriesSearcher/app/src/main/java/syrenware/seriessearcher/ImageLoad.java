package syrenware.seriessearcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by matthew on 2017/02/03.
 * Class is used to load and display an image in a specific View
 */

@SuppressWarnings("WeakerAccess")
class ImageLoad extends AsyncTask<Void, Void, Bitmap> {
    //Declarations
    private String url;
    private ImageView imageView;
    private Context context;
    private int showID;
    private boolean saveImages;
    private Uri uri;

    //Constructor (accepts URL of image, the ImageView to display the image, and the position to store the image in (incrementer)
    public ImageLoad(String url, ImageView imageView, Context context, int showID, boolean saveImages) {
        this.url = url;
        this.imageView = imageView;
        this.context = context;
        this.showID = showID;
        this.saveImages = saveImages;
    }

    //Method fetches the image from the URL specified in the constructor
    @Override
    protected Bitmap doInBackground(Void... params) {
        try
        {
            if(checkForFile()){
                //Loops through all files and displays the file that matches the filename for the appropriate image
                File[] files = context.getFilesDir().listFiles();
                String fileName = showID + ".png";
                for(int i = 0; i < files.length; i++){
                    String name = files[i].getName();
                    if(fileName.equals(name)){
                        //Sets the content for the ImageView to the appropriate image
                        uri = Uri.parse(files[i].getAbsolutePath());

                        return null;
                    }
                }
            }
            else{
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();

                //Specifies that you want to read the response from the URL and connects to the URL
                connection.setDoInput(true);
                connection.connect();

                //Fetches the input stream from the API and decodes it into a Bitmap
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    //Method sets the image in the ImageView and saves the image in internal storage
    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        //The result variable will be null when the image has been found in internal storage
        if(result != null){
            if(saveImages){
                saveFile(result);
            }
            imageView.setImageBitmap(result);
        }
        else{
            imageView.setImageURI(uri);
        }
    }

    //Method checks to see if the image has already been downloaded
    public boolean checkForFile(){
        File[] files = context.getFilesDir().listFiles();
        boolean fileDownloaded = false;

        //Loops through downloaded files to see if the image has already been downloaded
        for(int i = 0; i < files.length; i++){
            if(files[i].getName().equals(showID + ".png")){
                fileDownloaded = true;
                break;
            }
        }

        return fileDownloaded;
    }

    //Method saves the image to internal storage in order to lessen future data usage
    public void saveFile(Bitmap bitmap){
        try{
            File file = new File(context.getFilesDir(), showID + ".png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();
        }
        catch(IOException ioe){
            Toast.makeText(context, ioe.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
