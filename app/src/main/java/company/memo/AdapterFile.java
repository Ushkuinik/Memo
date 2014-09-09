package company.memo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 *
 */
public class AdapterFile {
    static private final String LOG_TAG = "AdapterFile";
    static private final String IMAGE_DIR = "Memo";
    static private final String IMAGE_DIR_PHOTO = "Photo";
    static private final String IMAGE_DIR_THUMB = "Thumb";
    static private final String IMAGE_DIR_SCREENSHOT = "Screenshot";


    static private File getPathToDir(String _dir) {
        File filePictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File fileMemo = new File(filePictures, IMAGE_DIR);
        File fileResult = new File(fileMemo, _dir);
        if(!fileResult.exists()) {
            if(!fileResult.mkdirs()) {
                Log.d(LOG_TAG, "failed to create directory " + _dir);
                return null;
            }
        }
        return fileResult;
    }

    static private boolean createDirectories() {
        boolean result = true;
        return result;
    }

    /**
     * Create a File for saving a photo
     * @param _filename thumbnail filename
     */
    static public File getFilePhoto(String _filename) {
        Log.i(LOG_TAG, "getFilePhoto");

        File dir = getPathToDir(IMAGE_DIR_PHOTO);
        File mediaFile = new File(dir, _filename);

        return mediaFile;
    }


    /**
     * Create a File for saving a thumbnail
     * @param _filename thumbnail filename (same as original file's name)
     */
    static public File getFileThumb(String _filename) {
        Log.i(LOG_TAG, "getFileThumb");

        File dir = getPathToDir(IMAGE_DIR_THUMB);
        File fileThumbnail = new File(dir, _filename);

        return fileThumbnail;
    }


    /**
     *
     * @param _path full path to original file
     * @return
     */
    static public File createThumbnail(String _path) {
        File filePathSource = new File(_path);
        return createThumbnail(filePathSource);
    }


    static public File createThumbnail(File _source) {
        String filename = _source.getName(); // get filename from full path
        File fileThumbnail = getFileThumb(filename); // create full path to thumbnail

        // if no thumbnail exists, create it
        if(!fileThumbnail.exists())
            if(_source.exists())
                fileThumbnail = createFileThumbnail(fileThumbnail, _source);
        return fileThumbnail;
    }


    static public String createFileName(String _number) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());
        return (_number + "_" + timeStamp + ".jpg");
    }


/*
    static public String ____createThumbnail(File _source) {
        String thumbPath = null;

        try {
            ExifInterface exif = new ExifInterface(_source.getPath());

            Bitmap bitmap;
            if(exif.hasThumbnail()) {
                byte[] thumbByteArray = exif.getThumbnail();
                bitmap = BitmapFactory.decodeByteArray(thumbByteArray, 0, thumbByteArray.length);
            }
            else {
                final int THUMB_WIDTH = 512;
                final int THUMB_HEIGHT = 288;
                Bitmap srcBitmap = BitmapFactory.decodeFile(_source.getPath());
                bitmap = ThumbnailUtils.extractThumbnail(srcBitmap, THUMB_WIDTH, THUMB_HEIGHT);
            }
            Matrix matrix = new Matrix();

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            String thumbName = removeExtension(_source.getName()) + "_thumb.jpg";
            thumbPath = _source.getParent() + "/" + thumbName;
            Log.d(LOG_TAG, "thumb path: " + thumbPath);

            FileOutputStream fos = new FileOutputStream(thumbPath);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return thumbPath;
    }
*/


    /**
     *
     * @param _fileThumbnail new thumbnail file to be created
     * @param _fileSource original image file
     * @return created thumbnail file
     */
    static private File createFileThumbnail(File _fileThumbnail, File _fileSource) {
        Bitmap bitmap = createBitmapThumbnail(_fileSource, 200, 200); //FIXME: pixels in dimension
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(_fileThumbnail);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return _fileThumbnail;
    }


    static public Bitmap createBitmapThumbnail(File _source, int _width, int _height) {
        Bitmap result_bitmap = null;

        try {
            ExifInterface exif = new ExifInterface(_source.getPath());

            Bitmap srcBitmap = BitmapFactory.decodeFile(_source.getPath());
            //Bitmap bitmap = ThumbnailUtils.extractThumbnail(srcBitmap, _width, _height); // get cropped square thumbnail
            //Bitmap bitmap = Bitmap.createScaledBitmap(srcBitmap, _width, _height, false); // get resized square thumbnail
            Bitmap bitmap = resizeImage(srcBitmap, Math.max(_width, _height)); // get proportional thumbnail

            Matrix matrix = new Matrix();
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
            result_bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return result_bitmap;
    }


    static private Bitmap resizeImage(Bitmap bitmap, int newSize){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = 0;
        int newHeight = 0;

        if(width > height){
            newWidth = newSize;
            newHeight = (newSize * height)/width;
        } else if(width < height){
            newHeight = newSize;
            newWidth = (newSize * width)/height;
        } else if (width == height){
            newHeight = newSize;
            newWidth = newSize;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                                   width, height, matrix, true);

        return resizedBitmap;
    }


    static private String removeExtension(String _filename) {
        String filename = null;
        int pos = _filename.lastIndexOf(".");
        if (pos > 0) {
            filename = _filename.substring(0, pos);
        }
        return filename;
    }


    public static File getOriginalFile(String _thumbPath) {
        File f = new File(_thumbPath);
        String path = f.getParent();
        String name = f.getName();
        int pos = name.lastIndexOf("_thumb");
        String filename = name.substring(0, pos) + ".jpg";

        return new File(path + File.separator + filename);
    }
}
