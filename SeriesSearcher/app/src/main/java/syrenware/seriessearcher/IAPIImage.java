package syrenware.seriessearcher;

import android.graphics.Bitmap;

/**
 * Created by matthew on 2017/02/11.
 * Class is used to retrieve the image from the API for a specific Show
 */

public interface IAPIImage {
    //Method is used to send the image and its position in an array to be stored in to the class that implements this interface
    public void getJsonImage(Bitmap bitmap, int position);
}
