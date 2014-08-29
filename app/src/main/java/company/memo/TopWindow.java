package company.memo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
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

    public enum WindowState {
        STATE_NO_MEMO,
        STATE_MEMO_NO_SELECT,
        STATE_HIDDEN, STATE_MEMO_SELECTED
    }


    // Window part's sizes in dp
    final int TITLE_HEIGHT = 52;
    final int MEMO_LIST_HEIGHT_FULL = 100;
    final int MEMO_LIST_HEIGHT_MIN  = 40;
    final int WINDOW_WIDTH_MIN = 96;
    final int WINDOW_WIDTH_NORMAL = 250;
    final int WINDOW_HEIGHT_LIST = TITLE_HEIGHT + MEMO_LIST_HEIGHT_FULL;
    final int WINDOW_HEIGHT_VIEW = TITLE_HEIGHT + MEMO_LIST_HEIGHT_MIN + 250;

    private View mView = null;
    private int                   mWindowId;    // window mWindowId
    private WindowState           mWindowState;
    private AdapterDatabase       mAdapterDatabase;
    private AdapterMemoHorizontal mAdapterMemo;
    private HorizontalListView    mListMemos;
    private String                mPhoneNumber;
    private long mMemoId = 0;
    private LineEditText mMemoText;
    private Memo         mPrevMemo;
    private float        mDensity;

//    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
//    private Uri fileUri;


    private Point getWindowSize() {
//        Log.d(this.LOG_TAG, "getWindowSize");
        int w = 0, h = 0;
//        Point p = new Point(dm.widthPixels, dm.heightPixels / 2);
        switch(mWindowState) {
            case STATE_HIDDEN:
                hide(mWindowId);
                break;
            case STATE_MEMO_NO_SELECT:
                w = Dp2Pixel(WINDOW_WIDTH_NORMAL);
                h = Dp2Pixel(WINDOW_HEIGHT_LIST);
                break;

            case STATE_MEMO_SELECTED:
                w = Dp2Pixel(WINDOW_WIDTH_NORMAL);
                h = Dp2Pixel(WINDOW_HEIGHT_VIEW);
                break;
            case STATE_NO_MEMO:
            default:
                w = Dp2Pixel(WINDOW_WIDTH_MIN);
                h = Dp2Pixel(TITLE_HEIGHT);
                break;
        }

        return new Point(w, h);
    }


    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(this.LOG_TAG, "onCreate");

        mWindowState = WindowState.STATE_NO_MEMO;

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
        mWindowState = WindowState.STATE_NO_MEMO;
        mMemoId = 0;

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

        if(mPhoneNumber != null) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final String photoPath = preferences.getString("photoPath", null);
            String thumbPath = preferences.getString("thumbPath", null);
            if(photoPath == null) {
                setState(WindowState.STATE_MEMO_NO_SELECT);
                Log.d(this.LOG_TAG, "State: STATE_MEMO_NO_SELECT");
            }
            else {
                setState(WindowState.STATE_MEMO_SELECTED);
                Log.d(this.LOG_TAG, "State: STATE_MEMO_SELECTED");

                mMemoText.insertImage(thumbPath);

                SharedPreferences.Editor editor = preferences.edit();
                //TODO: declare pref keys as resource strings
                editor.remove("photoPath");
                editor.remove("thumbPath");
                editor.commit();
            }
        }
        else {
            setState(WindowState.STATE_NO_MEMO);
            Log.d(this.LOG_TAG, "State: STATE_NO_MEMO");
        }

        return super.onShow(id, window);
    }


    /**
     * Implement this callback to be alerted when a window corresponding to the
     * id is about to be hidden. This callback will occur before the view is
     * removed from the window manager and {@link #getHiddenNotification(int)}
     * is called.
     *
     * @param id     The id of the view, provided as a courtesy.
     * @param window
     * @return Return true to cancel the view from being hidden, or false to
     * continue.
     * @see #hide(int)
     */
    @Override
    public boolean onHide(int id, Window window) {
        Log.d(this.LOG_TAG, "onHide");
        return super.onHide(id, window);
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

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        this.mView = layoutInflater.inflate(R.layout.top_window, frame, true);
        this.mWindowId = id;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("windowId", this.mWindowId);
        editor.commit();

/*
        if(adapter != null)
            adapter.clear();
*/
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        mDensity = dm.density;

        mListMemos = (HorizontalListView)mView.findViewById(R.id.hsvMemos);
        mMemoText = (LineEditText)mView.findViewById(R.id.body);

        View btnQuit = mView.findViewById(R.id.btnQuit);
        btnQuit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                StandOutWindow.closeAll(TopWindow.this, TopWindow.class);
            }
        });

/*
        mView.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //TODO: hide Window and start ActivityEditMemo
                if(mPrevMemo != null) {
//                    setMemoListHeight(MEMO_LIST_HEIGHT_MIN, true);

                    mPrevMemo.setSelected(false);
                    mAdapterMemo.notifyDataSetChanged();
                    mMemoId = 0;
                    mMemoText.setHtmlText("");
                    Toast.makeText(getApplicationContext(), "New memo selected", Toast.LENGTH_SHORT).show();

                    setState(WindowState.STATE_HIDDEN);
                }
            }
        });
*/

        mView.findViewById(R.id.btnCollapse).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setState(WindowState.STATE_MEMO_NO_SELECT);

                if(mPrevMemo != null) {
                    mPrevMemo.setSelected(false);
                    mAdapterMemo.notifyDataSetChanged();
                }

            }
        });

/*
        final ImageButton buttonAttachment = (ImageButton)mView.findViewById(R.id.btnAttachment);
        buttonAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow dropDown = TopWindow.this.getDropDown(id);
                if(dropDown != null) {
                    dropDown.showAsDropDown(buttonAttachment);
                }
            }
        });

        final ImageButton buttonSave = (ImageButton)mView.findViewById(R.id.btnSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mMemoText.getHtmlText();
                if(mMemoId == 0)
                    mMemoId = mAdapterDatabase.createMemo(mPhoneNumber, text);
                else
                    mAdapterDatabase.updateMemo(mMemoId, mPhoneNumber, text);
                Toast.makeText(getApplicationContext(), "Save memo: " + mMemoId, Toast.LENGTH_LONG).show();
            }
        });
*/

//        mMemoText.setHint("New memo");
        //FIXME: Long press allows to past text. It should be disabled
        mMemoText.setFocusable(false);
        mMemoText.setClickable(true);
    }



    private void setMemoListHeight(int _height, boolean  _isAnimate) {
        Animation ani = new ShowAnim(mListMemos, Dp2Pixel(_height), new IDataChangeListener() {
            @Override
            public void onEvent() {
                mAdapterMemo.notifyDataSetChanged();
            }
        });
        ani.setDuration(400);
        mListMemos.startAnimation(ani);
    }


    private void setState(WindowState _mode) {
        mWindowState = _mode;
        switch(mWindowState) {
            case STATE_HIDDEN:
                Log.d(this.LOG_TAG, "setState: STATE_HIDDEN");
                break;
            case STATE_MEMO_NO_SELECT:
                Log.d(this.LOG_TAG, "setState: STATE_MEMO_NO_SELECT");
                Log.d(this.LOG_TAG, "MEMO_LIST_HEIGHT_FULL: " + MEMO_LIST_HEIGHT_FULL);
                setMemoListHeight(MEMO_LIST_HEIGHT_FULL, true);
                mView.findViewById(R.id.btnCollapse).setVisibility(View.GONE);
//                mView.findViewById(R.id.btnAttachment).setVisibility(View.GONE);
                mView.findViewById(R.id.btnEdit).setVisibility(View.GONE);
                break;
            case STATE_MEMO_SELECTED:
                Log.d(this.LOG_TAG, "setState: STATE_MEMO_SELECTED");
                Log.d(this.LOG_TAG, "MEMO_LIST_HEIGHT_MIN: " + MEMO_LIST_HEIGHT_MIN);
                setMemoListHeight(MEMO_LIST_HEIGHT_MIN, true);
                mView.findViewById(R.id.btnCollapse).setVisibility(View.VISIBLE);
//                mView.findViewById(R.id.btnAttachment).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.btnEdit).setVisibility(View.VISIBLE);
                break;
            case STATE_NO_MEMO:
                Log.d(this.LOG_TAG, "setState: STATE_NO_MEMO");
            default:
                mView.findViewById(R.id.btnCollapse).setVisibility(View.GONE);
//                mView.findViewById(R.id.btnAttachment).setVisibility(View.GONE);
                mView.findViewById(R.id.btnSave).setVisibility(View.GONE);

/*
                if(mPhoneNumber != null) {
                    if(mAdapterMemo.getCount() > 0) {
                        w = Dp2Pixel(250);
                        h = Dp2Pixel(152);
                    }
                    else {
                        w = Dp2Pixel(96);
                        h = Dp2Pixel(48);
                    }
                }
                else {
                    w = Dp2Pixel(96);
                    h = Dp2Pixel(48);
                }
*/
                break;
        }
        resizeWindow();

//        switch(mWindowState) {
//            case STATE_NO_MEMO:
//                mView.findViewById(R.id.btnExpand).setVisibility(View.VISIBLE);
//                mView.findViewById(R.id.btnCollapse).setVisibility(View.GONE);
//                mView.findViewById(R.id.btnAttachment).setVisibility(View.GONE);
//                mView.findViewById(R.id.btnSave).setVisibility(View.GONE);
//                break;

//            case STATE_MEMO_NO_SELECT:
//                mView.findViewById(R.id.btnExpand).setVisibility(View.GONE);
//                mView.findViewById(R.id.btnCollapse).setVisibility(View.VISIBLE);
//                mView.findViewById(R.id.btnAttachment).setVisibility(View.VISIBLE);
//                mView.findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
//                break;

//            default:
//                break;
//        }
    }

    private void resizeWindow() {

        Window window = getWindow(mWindowId);
        if(window != null) {
            Point p = getWindowSize();
            window.edit().setSize(p.x, p.y).commit();
        }

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
                | StandOutFlags.FLAG_FIX_COMPATIBILITY_ALL_DISABLE
                | StandOutFlags.FLAG_ADD_FUNCTIONALITY_ALL_DISABLE
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
                //mMemoText.setText(number);
                /**
                 * Populate horizontal list
                 */
                ArrayList<Memo> memos = mAdapterDatabase.getMemos(mPhoneNumber);
                mAdapterMemo = new AdapterMemoHorizontal(this, memos);
                mListMemos.setAdapter(mAdapterMemo);
                mListMemos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                        setMemoListHeight(MEMO_LIST_HEIGHT_FULL, true);

                        String s = "Click: " + view.getTag().toString();
                        Toast.makeText(TopWindow.this, s, Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, s);

                        Memo memo = mAdapterMemo.getItem(position);
                        String text = memo.getBody();
                        mMemoId = memo.getId();
                        mMemoText.setHtmlText(text);
                        mMemoText.setHint("Enter memo (update)");

                        if(mPrevMemo != null)
                            mPrevMemo.setSelected(false);
                        memo.setSelected(true);
                        mPrevMemo = memo;

                        mAdapterMemo.notifyDataSetChanged();

/*
                        ViewGroup.LayoutParams params = listMemos.getLayoutParams();
                        params.height = 120;
                        listMemos.setLayoutParams(params);
*/
                        setState(WindowState.STATE_MEMO_SELECTED);
                    }
                });
                mListMemos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        String s = "Long click: " + view.getTag().toString();
                        Log.d(LOG_TAG, s);
                        Toast.makeText(TopWindow.this, s, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });

                if(mAdapterMemo.getCount() > 0)
                    setState(WindowState.STATE_MEMO_NO_SELECT);
                else
                    setState(WindowState.STATE_NO_MEMO);

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



    public static class LineEditText extends EditText {
        private Rect   mRect;
        private Paint  mPaint;
        private String mHtmlText = "";
        private SpannableStringBuilder ssb;
        Context mContext;


        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setColor(Color.LTGRAY);
            mContext = context;
        }


        @Override
        protected void onDraw(Canvas canvas) {

/*
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
*/
            super.onDraw(canvas);
        }


        public void setHtmlText(String _htmlText) {
//            Log.d("TopWindow", "setHtmlText() html text: " + _htmlText);
            mHtmlText = _htmlText;
            ThumbImageGetter getter = new ThumbImageGetter();
            Spanned htmlSpan = Html.fromHtml(_htmlText, getter, null);
            this.setText(htmlSpan);
        }

        public String getHtmlText() {
            mHtmlText = Html.toHtml(this.getText());
//            Log.d("TopWindow", "getHtmlText() html text: " + mHtmlText);
            return mHtmlText;
        }


        public void insertImage(String _path) {
            //Uri uri = Uri.fromFile(new File(_path));
            Drawable d = Drawable.createFromPath(_path);
            ImageSpan imageSpan = new ImageSpan(d, _path);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(this.getText());

            final int OBJ_CHAR_CODE = 0xFFFC;
            String imgId = String.format("%c", OBJ_CHAR_CODE);
            int selection_start = this.getSelectionStart();
            int selection_end = this.getSelectionEnd();
            builder.replace(this.getSelectionStart(), this.getSelectionEnd(), imgId);

            builder.setSpan(imageSpan, selection_start, selection_start + imgId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            int extra = 0;
            if(selection_start != 0) {
                // add '\n' before image
                builder.replace(selection_start, selection_start, "\n");
//                Log.d("TopWindow", "Added before NL at " + selection_start);
                extra++;
            }
            Log.d("TopWindow", "length: " + this.getText().toString().length() + " selection: " + selection_start);

            if(selection_end < this.getText().toString().length()) {
                // add '\n' after image
                int after = selection_start + imgId.length() + extra;
                builder.replace(after, after, "\n");
//                Log.d("TopWindow", "Added after NL  " + mHtmlText);
            }

//            this.setText(builder);
            mHtmlText = Html.toHtml(builder);
            this.setHtmlText(mHtmlText);

/*
//            String src = "<img src=\"" + _path + "\" />";
//            Editable htmlSpan = this.getText();
            builder.setSpan();
            String s = htmlSpan.toString();
            Editable htmlSpan2 = htmlSpan.insert(this.getSelectionStart(), src, 0, src.length());
            Editable htmlSpan2 = htmlSpan.insert(this.getSelectionStart(), src, 0, src.length());
            mHtmlText = Html.toHtml(htmlSpan);
            int selection_start = this.getSelectionStart();
            int selection_end = this.getSelectionEnd();
            //CharSequence part1 = htmlSpan.subSequence(0, selection_start);
            Spanned spans[] = htmlSpan.getSpans(0, htmlSpan.length(), Spanned.class);
            CharSequence part2 = htmlSpan.subSequence(selection_end, htmlSpan.length());
            //mHtmlText = part1 + src + part2;
            this.setHtmlText(mHtmlText);
*/

//            ssb = new SpannableStringBuilder();
//            ssb.replace(this.getSelectionStart(), this.getSelectionStart(), src, 0, src.length() - 1);
        }


        public class ThumbImageGetter implements Html.ImageGetter {

            public Drawable getDrawable(String source) {
                Bitmap bitmap = BitmapFactory.decodeFile(source);
                Drawable d = Drawable.createFromPath(source);
                d.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
//                Log.d("TopWindow", "w:  " + bitmap.getWidth() + " h: " + bitmap.getHeight());
//                Rect r = d.getBounds();
//                Log.d("TopWindow", "rect:  " + r.toString());
                return d;
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
        }
        ));

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
            }
            ));
        }

        items.add(new DropDownListItem(R.drawable.ic_action_mic_holo_dark,
                                       getResources().getString(R.string.menu_audio), new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "New audio record", Toast.LENGTH_SHORT).show();
            }
        }
        ));
        return items;
    }


    public PopupWindow getDropDown(final int id) {
        final List<DropDownListItem> items;

        List<DropDownListItem> dropDownListItems = this.getDropDownItems();
        if(dropDownListItems != null) {
            items = dropDownListItems;
        }
        else {
            items = new ArrayList<StandOutWindow.DropDownListItem>();
        }

        // turn item list into views in PopupWindow
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        final PopupWindow dropDown = new PopupWindow(list,
                                                     ViewGroup.LayoutParams.WRAP_CONTENT,
                                                     ViewGroup.LayoutParams.WRAP_CONTENT, true);

        for(final DropDownListItem item : items) {
            ViewGroup listItem = (ViewGroup) mView.inflate(this, R.layout.list_item_drop_down, null);
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
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        }
        else {
            // no camera on this device
            return false;
        }
    }
    public class ShowAnim extends Animation {
        int targetHeight;
        int initialHeight;
        View view;
        protected IDataChangeListener mDataChangeListener;

        public ShowAnim(View view, int targetHeight, IDataChangeListener _listener) {
            this.view = view;
            this.targetHeight = targetHeight;
            mDataChangeListener = _listener;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            //view.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
            float k = (targetHeight - initialHeight);
            view.getLayoutParams().height = (int) (k * interpolatedTime + initialHeight);
//            view.requestLayout();
            mDataChangeListener.onEvent();
//            Log.d("Animation", "height: " + view.getLayoutParams().height + " t: " + interpolatedTime);
        }

        @Override
        public void initialize(int width, int height, int parentWidth,
                               int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            initialHeight = height;
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public void onClickAddMemo(View view) {
        Intent intent = new Intent(getBaseContext(), ActivityEditMemo.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("phoneNumber", mPhoneNumber);
        startActivity(intent);
        close(mWindowId);
    }

    public void onClickEditMemo(View view) {
        Intent intent = new Intent(getBaseContext(), ActivityEditMemo.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("memoId", mMemoId);
        startActivity(intent);
        close(mWindowId);
    }


    public interface IDataChangeListener
    {
        public void onEvent();
    }

    public int Dp2Pixel(int _dp) {
        return Math.round(_dp * mDensity);
    }
}

