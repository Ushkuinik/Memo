package company.memo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import wei.mark.standout.StandOutWindow;

/**
 *
 */
public class ActivityCameraDummy extends Activity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private final String LOG_TAG = this.getClass().toString();
    private File mFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_dummy);

        Log.d(this.LOG_TAG, "onCreate");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        //TODO: declare pref keys as resource strings
        editor.remove("photoPath");
        editor.remove("thumbPath");
        editor.commit();

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mFile = getOutputMediaFile();
        Uri fileUri = Uri.fromFile(mFile); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        Log.d(this.LOG_TAG, "onCreate. Start camera");
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(this.LOG_TAG, "onConfigurationChanged");

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(this.LOG_TAG, "onConfigurationChanged.Landscape");
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d(this.LOG_TAG, "onConfigurationChanged.Portrait");
        }
    }

    private String removeExtension(String _filename) {
        String filename = null;
        int pos = _filename.lastIndexOf(".");
        if (pos > 0) {
            filename = _filename.substring(0, pos);
        }
        return filename;
    }


    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {

        Log.d(this.LOG_TAG, "onActivityResult");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(_requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if(_resultCode == RESULT_OK) {

                String thumbPath = createThumbnail(mFile);

                // Image captured and saved to fileUri specified in the Intent
                Log.i(this.LOG_TAG, "Image saved to " + mFile.getPath());
                Log.i(this.LOG_TAG, "Thumbnail saved to " + thumbPath);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("photoPath", mFile.getPath());
                editor.putString("thumbPath", thumbPath);
                editor.commit();
            }
            else if(_resultCode == RESULT_CANCELED) {
                Log.d(this.LOG_TAG, "Photo cancelled");
            }
            else {
                Log.d(this.LOG_TAG, "Photo failed");
            }
        }

//        int windowId = preferences.getInt("windowId", StandOutWindow.DEFAULT_ID);
//        StandOutWindow.show(this, TopWindow.class, windowId);
//        Log.d(this.LOG_TAG, "restore id: " + windowId);

        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(this.LOG_TAG, "onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(this.LOG_TAG, "onPause");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(this.LOG_TAG, "onStop");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(this.LOG_TAG, "onDestroy");
    }


    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        Log.d(this.LOG_TAG, "getOutputMediaFile");

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                Log.d(this.LOG_TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }


    private String createThumbnail(File _source) {
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
            thumbPath = mFile.getParent() + "/" + thumbName;
            Log.d(LOG_TAG, "thumb path: " + thumbPath);

            FileOutputStream fos = new FileOutputStream(thumbPath);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            fos.write(byteArray);
            fos.close();
        }
        catch(FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        }
        catch(IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }

        return thumbPath;
    }
}
