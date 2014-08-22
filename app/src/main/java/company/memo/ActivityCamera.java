package company.memo;


import android.app.Activity;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityCamera extends Activity {
    private final String LOG_TAG = this.getClass().toString();

    private Camera        mCamera;
    private CameraPreview mPreview;
    private Camera cameraInstance;
    private CameraOverlay mOverlay;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // set Camera parameters
        Camera.Parameters params = mCamera.getParameters();

        if (params.getMaxNumMeteringAreas() > 0){ // check that metering areas are supported
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            meteringAreas.add(new Camera.Area(new Rect(-100, -100, 100, 100), 1000));
            params.setMeteringAreas(meteringAreas);
        }

        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
        Log.d(LOG_TAG, "Sizes: " + sizes.size());
        Camera.Size size = sizes.get(0);
        Log.d(LOG_TAG, "w: " + size.width + " h: " + size.height);
        //params.setPictureSize(size.width, size.height);

        mCamera.setParameters(params);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
//        mOverlay = new CameraOverlay(this);
        final FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.addView(mPreview);
//        ((FrameLayout) findViewById(R.id.camera_overlay)).addView(mOverlay);
//        addContentView(mOverlay, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(LOG_TAG, "View click: " + motionEvent.getX() + " : " + motionEvent.getY());
                focusOnTouch(motionEvent);
                return false;
            }
        });

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
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


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();
            if(pictureFile == null) {
                Log.d(LOG_TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Log.d(LOG_TAG, "file: " + pictureFile.getPath());
                Log.d(LOG_TAG, "size: " + data.length);

                fos.write(data);
                fos.close();

                final int THUMBSIZE = 100;
                String thumbName = removeExtension(pictureFile.getName()) + "_thumb.jpg";
                String thumbPath = pictureFile.getParent() + "/" + thumbName;
                Log.d(LOG_TAG, "thumb path: " + thumbPath);
                fos = new FileOutputStream(thumbPath);

                Bitmap srcBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(srcBitmap, THUMBSIZE, THUMBSIZE);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
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
        }
    };


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch(Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


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


    protected void focusOnTouch(MotionEvent event) {
        if(mCamera != null) {

            mCamera.cancelAutoFocus();
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
            Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);
            Log.d(LOG_TAG, "focusRect: " + focusRect.toString());
            Log.d(LOG_TAG, "meteringRect: " + meteringRect.toString());

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            //Rect areaRect1 = new Rect(-100, -100, 100, 100);    // specify an area in center of image
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            //focusList.add(new Camera.Area(areaRect1, 600)); // set weight to 60%
            focusList.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(focusList);


            if(parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringList = new ArrayList<Camera.Area>();
                meteringList.add(new Camera.Area(meteringRect, 1000));
                parameters.setMeteringAreas(meteringList);
            }


            mCamera.setParameters(parameters);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    Log.d(LOG_TAG, "Auto focus done");
                }
            });
        }
    }


    private Rect calculateTapArea(float x, float y, float coefficient) {
        int focusAreaSize = 100;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        View surface = findViewById(R.id.camera_preview);
        int left = clamp((int) x - areaSize / 2, 0, surface.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, surface.getHeight() - areaSize);

        RectF r1 = new RectF(left, top, left + areaSize, top + areaSize);
        float k1 = 2000.0f / (float) surface.getWidth();
        float k2 = 2000.0f / (float) surface.getHeight();
        RectF rectF = new RectF((r1.left * k1 - 1000), (r1.top * k2 - 1000), (r1.right * k1 - 1000), (r1.bottom * k2 - 1000));

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }


    private int clamp(int x, int min, int max) {
        if(x > max) {
            return max;
        }
        if(x < min) {
            return min;
        }
        return x;
    }
}