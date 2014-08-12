package company.memo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * The Window, which will float over Call screen when call incomes.
 * It will be shown and closed by CallDetectService.
 * The window will contain a list of known activity related to calling contact such as call history
 * and calendar appointments
 *
 * @see StandOutWindow
 */
public class TopWindow extends StandOutWindow {

    private final String LOG_TAG = this.getClass().toString();

    public enum SizeState {
        SIZE_STATE_HIDDEN,      // hidden to notification area
        SIZE_STATE_MINIMIZED,   // only toolbar is visible
        SIZE_STATE_NORMAL       // expanded to normal size (toolbar + memo area)
    }



    private View view = null;
    private int             mWindowId;    // window mWindowId
    private SizeState       mSizeState;
    private AdapterDatabase mAdapterDatabase;
    private String          mPhoneNumber;
    private long mMemoId = 0;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;


    private Point getWindowSize() {
        Log.d(this.LOG_TAG, "getWindowSize");
        int w = 100, h = 100;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
//        Point p = new Point(dm.widthPixels, dm.heightPixels / 2);
        switch(mSizeState) {
            case SIZE_STATE_HIDDEN:
                break;
            case SIZE_STATE_MINIMIZED:
                w = (int) Math.ceil(96 * dm.density);
                h = (int) Math.ceil(48 * dm.density);
                break;
            case SIZE_STATE_NORMAL:
                w = (int) Math.ceil(250 * dm.density);
                h = (int) Math.ceil(300 * dm.density);
                break;
        }

        return new Point(w, h);
    }


    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(this.LOG_TAG, "onCreate");

        mSizeState = SizeState.SIZE_STATE_MINIMIZED;

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();
    }


    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be closed. This callback will occur before the view is
     * removed from the window manager.
     *
     * @param id     The id of the view, provided as a courtesy.
     * @param window
     * @return Return true to cancel the view from being closed, or false to
     * continue.
     * @see #close(int)
     */
    @Override
    public boolean onClose(int id, Window window) {
        Log.d(this.LOG_TAG, "onClose");
        mSizeState = SizeState.SIZE_STATE_MINIMIZED;

        return super.onClose(id, window);
    }


    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be shown. This callback will occur before the view is
     * added to the window manager.
     *
     * @param id     The id of the view, provided as a courtesy.
     * @param window
     * @return Return true to cancel the view from being shown, or false to
     * continue.
     * @see #show(int)
     */
    @Override
    public boolean onShow(int id, Window window) {
        Log.d(this.LOG_TAG, "onShow");
        mSizeState = SizeState.SIZE_STATE_MINIMIZED;
        return super.onShow(id, window);
    }


    @Override
    public void onDestroy() {
        Log.d(this.LOG_TAG, "onDestroy");
        super.onDestroy();
        mAdapterDatabase.close();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("windowId");
        editor.commit();
    }


    @Override
    public String getAppName() {

        return getResources().getString(R.string.app_name);
    }


    @Override
    public int getAppIcon() {

        return R.drawable.ic_action_remove_holo_dark;
    }


    /**
     * @param id    The mWindowId representing the window.
     * @param frame - body frame to be filled by custom content
     */
    @Override
    public void createAndAttachView(final int id, final FrameLayout frame) {

        Log.d(this.LOG_TAG, "createAndAttachView");

        mSizeState = SizeState.SIZE_STATE_MINIMIZED;

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        this.view = layoutInflater.inflate(R.layout.top_window, frame, true);
        this.mWindowId = id;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("windowId", this.mWindowId);
        editor.commit();


/*
        if(adapter != null)
            adapter.clear();
*/

        View btnQuit = view.findViewById(R.id.btnQuit);
        btnQuit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            StandOutWindow.closeAll(TopWindow.this, TopWindow.class);
            }
        });

        view.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mSizeState == SizeState.SIZE_STATE_MINIMIZED) {
                    mSizeState = SizeState.SIZE_STATE_NORMAL;
                    view.findViewById(R.id.btnAttachment).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
                }
                else if(mSizeState == SizeState.SIZE_STATE_NORMAL) {
                    mSizeState = SizeState.SIZE_STATE_MINIMIZED;
                    view.findViewById(R.id.btnSave).setVisibility(View.GONE);
                }

                resizeWindow();
            }
        });

        final ImageButton buttonAttachment = (ImageButton)view.findViewById(R.id.btnAttachment);
        buttonAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow dropDown = TopWindow.this.getDropDown(id);
                if(dropDown != null) {
                    dropDown.showAsDropDown(buttonAttachment);
                }
            }
        });

        final ImageButton buttonSave = (ImageButton)view.findViewById(R.id.btnSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((EditText)view.findViewById(R.id.body)).getText().toString();
                if(mMemoId == 0)
                    mMemoId = mAdapterDatabase.createMemo(mPhoneNumber, text);
                else
                    mAdapterDatabase.updateMemo(mMemoId, mPhoneNumber, text);
                Toast.makeText(getApplicationContext(), "Save memo: " + mMemoId, Toast.LENGTH_LONG).show();
            }
        });

        long msTime = System.currentTimeMillis();
        Date curDateTime = new Date(msTime);

        SimpleDateFormat formatter = new SimpleDateFormat("d'/'M'/'y");
        ((EditText)view.findViewById(R.id.body)).setText(formatter.format(curDateTime));
    }


    private void resizeWindow() {
        Window window = getWindow(mWindowId);
        Point p = getWindowSize();
        int width = p.x;
        int height = p.y;

        window.edit().setSize(width, height).commit();
    }


    @Override
    public StandOutLayoutParams getParams(final int id, final wei.mark.standout.ui.Window window) {
        Log.d(this.LOG_TAG, "getParams");

        Point p = getWindowSize();
        int width = p.x;
        int height = p.y;

        return new StandOutLayoutParams(
                id,
                width,    // width
                height,    // height
                StandOutLayoutParams.TOP,   // xpos
                StandOutLayoutParams.LEFT);   // ypos
    }


    /**
     * The window should not be resizable and movable. It should be always on top until closed by
     * CallDetectService.
     *
     * @param id The mWindowId of the window.
     * @return Flags with window parameters
     */
    @Override
    public int getFlags(final int id) {
        Log.d(this.LOG_TAG, "getFlags");

        int flags = StandOutFlags.FLAG_DECORATION_CLOSE_DISABLE
                | StandOutFlags.FLAG_DECORATION_RESIZE_DISABLE
                | StandOutFlags.FLAG_DECORATION_MAXIMIZE_DISABLE
                | StandOutFlags.FLAG_DECORATION_MOVE_DISABLE
                | StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
                | StandOutFlags.FLAG_BODY_MOVE_ENABLE
//                | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH
//                | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP
//                | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE // StandOutWindow incorrectly checks screen boundaries after phone rotated
                | StandOutFlags.FLAG_FIX_COMPATIBILITY_ALL_DISABLE
                | StandOutFlags.FLAG_ADD_FUNCTIONALITY_ALL_DISABLE
//                | StandOutFlags.FLAG_ADD_FUNCTIONALITY_RESIZE_DISABLE
                | StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE;

        flags &= ~StandOutFlags.FLAG_DECORATION_SYSTEM; // switch off DECORATION_SYSTEM to disable window title
        Log.d(this.LOG_TAG, "getFlags: " + flags);
        return flags;
    }


    @Override
    public int getHiddenIcon() {

        return android.R.drawable.ic_menu_info_details;
    }


    // return an Intent that restores the MultiWindow
    @Override
    public Intent getHiddenNotificationIntent(int id) {
        return StandOutWindow.getShowIntent(this, getClass(), id);
    }


    /**
     * Receives a phone number of incoming call from CallDetectService and starts ListView
     * populating.
     *
     * @param id          The mWindowId of your receiving window.
     * @param requestCode The sending window provided this request code to declare what
     *                    kind of data is being sent.
     * @param data        A bundle of parceleable data that was sent to your receiving
     *                    window.
     * @param fromCls     The sending window's class. Provided if the sender wants a
     *                    result.
     * @param fromId      The sending window's mWindowId. Provided if the sender wants a
     */
    @Override
    public void onReceiveData(final int id, final int requestCode, final Bundle data,
                              final Class<? extends StandOutWindow> fromCls, final int fromId) {

        Log.d(this.LOG_TAG, "onReceiveData");

        switch(requestCode) {
            case CallDetectService.GOT_PHONE_NUMBER:
                Window window = getWindow(id);
                if(window == null) {
                    String errorText = String.format(Locale.US,
                                                     "%s received data but TopWindow id: %d is not open.",
                                                     getAppName(), id);
                    Log.d(this.LOG_TAG, errorText);
                          //Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
                    return;
                }
                mPhoneNumber = data.getString("phoneNumber");
                mPhoneNumber = mPhoneNumber.replace("-", "");
                Log.d(this.LOG_TAG, "onReceiveData(phoneNumber = " + mPhoneNumber + ")");
                //((LineEditText)(view.findViewById(R.mWindowId.body))).setText(number);
                break;

            default:
                Log.d("TopWindow", "Unexpected data received.");
                break;
        }
    }




    public void setTitle(final String _title) {
/*
        ((TextView) view.findViewById(R.mWindowId.txtTitle)).setText(_title);
*/
    }






    /**
     *
     */

    /*
    private class PhoneDataRetriever extends EventRetriever {
        private final String LOG_TAG = this.getClass().toString();


        public PhoneDataRetriever(final Context c) {

            super(c);
            Log.d(this.LOG_TAG, "PhoneDataRetriever");
        }


        @Override
        void onNewEventFound(final Event event) {

            TopWindow.this.adapter.add(event);
            ListView listRecords = (ListView) TopWindow.this.view.findViewById(R.mWindowId.listView);
            listRecords.setSelection(TopWindow.this.adapter.getCount() - 1);
//          listRecords.smoothScrollToPosition(TopWindow.this.adapter.getCount() - 1);
        }


        @Override
        void onSearchStarted() {
            // TODO Show infinite progress...
        }


        @Override
        void onSearchFinished() {
            // TODO Hide infinite progress
            // Close window if no events were found
            if(TopWindow.this.adapter.getCount() == 0) {
                close(TopWindow.this.mWindowId);
            }
        }
    }
*/

    public static class LineEditText extends EditText {
        private Rect  mRect;
        private Paint mPaint;


        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setColor(Color.LTGRAY);
        }


        @Override
        protected void onDraw(Canvas canvas) {

            int height = getHeight();
            int line_height = getLineHeight();

            int count = height / line_height;

            if(getLineCount() > count) {
                count = getLineCount();
            }

            Rect r = mRect;
            Paint paint = mPaint;
            int baseline = getLineBounds(0, r);

            for(int i = 0; i < count; i++) {

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
                baseline += getLineHeight();

                super.onDraw(canvas);
            }
        }
    }


    public List<DropDownListItem> getDropDownItems() {
        List<DropDownListItem> items = new ArrayList<DropDownListItem>();
        items.add(new DropDownListItem(R.drawable.ic_action_calendar_holo_dark,
                                       getResources().getString(R.string.menu_calendar), new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "New calendar event", Toast.LENGTH_SHORT).show();
            }
        }));

        if(checkCameraHardware(this)) {
            items.add(new DropDownListItem(R.drawable.ic_action_camera_holo_dark,
                                           getResources().getString(R.string.menu_photo), new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "New photo", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), ActivityCameraDummy.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    hide(mWindowId);
                    startActivity(intent);
                }
            }));
        }

        items.add(new DropDownListItem(R.drawable.ic_action_mic_holo_dark,
                                       getResources().getString(R.string.menu_audio), new Runnable() {
           @Override
            public void run() {
               Toast.makeText(getApplicationContext(), "New audio record", Toast.LENGTH_SHORT).show();
            }
        }));
        return items;
    }


    public PopupWindow getDropDown(final int id) {
        final List<DropDownListItem> items;

        List<DropDownListItem> dropDownListItems = this.getDropDownItems();
        if (dropDownListItems != null) {
            items = dropDownListItems;
        } else {
            items = new ArrayList<StandOutWindow.DropDownListItem>();
        }

        // turn item list into views in PopupWindow
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        final PopupWindow dropDown = new PopupWindow(list,
                                                     ViewGroup.LayoutParams.WRAP_CONTENT,
                                                     ViewGroup.LayoutParams.WRAP_CONTENT, true);

        for (final DropDownListItem item : items) {
            ViewGroup listItem = (ViewGroup) view.inflate(this, R.layout.list_item_drop_down, null);
            list.addView(listItem);

            ImageView icon = (ImageView) listItem.findViewById(R.id.icon);
            icon.setImageResource(item.icon);

            TextView description = (TextView) listItem.findViewById(R.id.description);
            description.setText(item.description);

            listItem.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    item.action.run();
                    dropDown.dismiss();
                }
            });
        }

        Drawable background = getResources().getDrawable(
                android.R.drawable.editbox_dropdown_dark_frame);
        dropDown.setBackgroundDrawable(background);
        return dropDown;
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}
