package kstr14.tipper;

import android.content.Context;
import android.content.CursorLoader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class containing helper methods for dealing with images
 */
public class ImageHelper {

    public static int COMPRESSION_QUALITY = 100;
    public static final int IMAGE_SIZE = 256;


    /**
     * Convert uri into absolute path
     * @param uri
     * @return the absolute path
     */
    public static String getRealPathFromURI(Context context, Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Rotates a Bitmap in the specified angle
     * @param source
     * @param angle
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Decodes image specified by Uri
     * @param context
     * @param uri
     * @param size
     * @return
     * @throws IOException
     */
    public static Bitmap decodeBitmapFromUri(Context context, Uri uri, int size) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);

        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    /**
     * Decoding of images from resources
     * @param res
     * @param id
     * @param width
     * @param height
     * @return
     */
    public static Bitmap decodeBitmapFromResource(Resources res, int id, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // avoid memory allocation while decoding image
        BitmapFactory.decodeResource(res, id, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, id, options);
    }

    /**
     * Calculates the size of the image
     * @param options
     * @param width
     * @param height
     * @return
     */
    public static int calculateSize(BitmapFactory.Options options, int width, int height) {
        // Raw height and width of image
        int raw_height = options.outHeight;
        int raw_width = options.outWidth;
        int size = 1;

        if (raw_height > height || raw_width > width) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) raw_height / (float) raw_height);
            final int widthRatio = Math.round((float) raw_width / (float) raw_width);
            size = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return size;
    }
}
