package kstr14.tipper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Kristine on 17-05-2015.
 */
public class BitmapHelper {

    public static Bitmap decodeBitmapFromResource(Resources res, int id, int width, int height) {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, id, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, id, options);
    }


    public static int calculateSize(BitmapFactory.Options options, int width, int height) {
        // Raw height and width of image
        int raw_height = options.outHeight;
        int raw_width = options.outWidth;
        int size = 1;

        if (raw_height > height || raw_width > width) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) raw_height / (float) raw_height);
            final int widthRatio = Math.round((float) raw_width / (float) raw_width);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            size = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return size;
    }


}
