package syrenware.seriessearcher;

import android.graphics.Bitmap;

/**
 * Created by matthew on 2017/02/11.
 * Class is used to retrieve the image from the API for a specific Show
 */

public interface IAPIImage {
    public void getJsonImage(Bitmap bitmap, int position);
}
