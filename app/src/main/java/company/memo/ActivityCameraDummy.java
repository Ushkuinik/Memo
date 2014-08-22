package company.memo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import wei.mark.standout.StandOutWindow;

/**
 *
 */
public class ActivityCameraDummy extends Activity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private final String LOG_TAG = this.getClass().toString();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_dummy);

        Log.d(this.LOG_TAG, "onCreate");

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri fileUri = Uri.fromFile(getOutputMediaFile()); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(this.LOG_TAG, "onActivityResult");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                if(data == null) {
                    Log.d(this.LOG_TAG, "No data!");
                    return;
                }
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(getApplicationContext(), "Image saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("filePath", data.getData().toString());
                editor.commit();

                Log.d(this.LOG_TAG, "OK");
            }
            else if(resultCode == RESULT_CANCELED) {
                Log.d(this.LOG_TAG, "Photo cancelled");
            }
            else {
                Log.d(this.LOG_TAG, "Photo failed");
            }
        }

        int windowId = preferences.getInt("windowId", StandOutWindow.DEFAULT_ID);
        StandOutWindow.show(this, TopWindow.class, windowId);
        Intent i = StandOutWindow.getShowIntent(this, TopWindow.class, windowId);
        Log.d(this.LOG_TAG, "restore id: " + windowId);

        //finish();
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
}
