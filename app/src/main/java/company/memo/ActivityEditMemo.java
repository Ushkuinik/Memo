package company.memo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ActivityEditMemo extends ActionBarActivity {

    private static final int    SELECT_IMAGE            = 100;
    private static final int    CAPTURE_IMAGE           = 200;

    private final String LOG_TAG = "ActivityEditMemo";
    private AdapterDatabase        mAdapterDatabase;
    private String                 mPhoneNumber;
    private long                   mMemoId;
    private EditText               mMemoTitle;
    private TopWindow.LineEditText mMemoText;
    private ArrayList<Attachment>  mAttachments;
    private File                   mPhotoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();

        mMemoTitle = (EditText) findViewById(R.id.title);
        mMemoText = (TopWindow.LineEditText) findViewById(R.id.body);

        Intent intent = getIntent();
        mMemoId = intent.getLongExtra("memoId", 0);
        if(mMemoId != 0) {
            Memo memo = mAdapterDatabase.getMemo(mMemoId);
            mPhoneNumber = memo.getNumber();
            mMemoTitle.setText(memo.getTitle());
            mMemoTitle.setHint(R.string.memo_title_hint);
/*
            mMemoTitle.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mMemoTitle.getWindowToken(), 0);
*/
            mMemoText.setText(memo.getBody());
            mMemoText.setHint(R.string.memo_body_hint);

//            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutAttachments);
            LinearLayout layout2 = (LinearLayout)findViewById(R.id.layoutAttachmentsMagic);
            mAttachments = mAdapterDatabase.getAttachmentByMemoId(mMemoId);
            for(Attachment a : mAttachments) {
//                ImageView icon = getAttachmentIcon(a);
//                layout.addView(icon);

                ImageView icon2 = getAttachmentIcon(a);
                layout2.addView(icon2);
            }

            ImageView magicButton = (ImageView)findViewById(R.id.magicButton);
            View magic = findViewById(R.id.scrollViewMagic);
            if(mAttachments.size() > 0) {
                magic.getLayoutParams().width = 1;
                magicButton.setImageResource(R.drawable.ic_sheet);
            }
            else {
                magicButton.setVisibility(View.GONE);
            }

        }
        else {
            Toast.makeText(this, "Memo was not specified", Toast.LENGTH_SHORT).show();
            finish();
        }
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private Attachment getAttachmentById(long _id)
    {
        for(Attachment a : mAttachments) {
            if (a.getId() == _id)
                return a;
        }
        return null;
    }

    public ImageView getAttachmentIcon(Attachment _attachment) {
        ImageView icon = new ImageView(getApplicationContext());
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        File fileThumbnail = AdapterFile.createThumbnail(_attachment.getPath());
        if(fileThumbnail.exists()) { // if thumbnail exists, use it
            Bitmap bitmap = BitmapFactory.decodeFile(fileThumbnail.getPath());
            icon.setImageBitmap(bitmap);

//            icon.setImageURI(Uri.fromFile(fileThumbnail));
        }
        else {
            icon.setImageResource(R.drawable.ic_sheet);
        }
        icon.setId((int)_attachment.getId());

        // set margins
/*
        if(icon.getLayoutParams() instanceof LinearLayout.MarginLayoutParams) {
            LinearLayout.MarginLayoutParams p = (LinearLayout.MarginLayoutParams) icon.getLayoutParams();
            p.setMargins(20, 20, 20, 20);
            icon.requestLayout();
        }
*/

/*
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_sheet).copy(Bitmap.Config.ARGB_8888, true);
        File f = new File(_attachment.getPath());
        if(f.exists()) {
            Bitmap bm2 = AdapterFile.createBitmapThumbnail(f, 140, 140);
            Canvas canvas = new Canvas(bm);
            Paint paint = new Paint();
            canvas.drawBitmap(bm2, 10, 10, paint);
        }
        icon.setImageBitmap(bm);
*/
        ApplicationMemo app = (ApplicationMemo)getApplicationContext();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(app.Dp2Pixel(2), 0, app.Dp2Pixel(2), 0);
        icon.setLayoutParams(params);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Attachment a = getAttachmentById(v.getId());
                File file = new File(a.getPath());
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "image/jpg");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
        icon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Log.d(LOG_TAG, "Long click on image icon");
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEditMemo.this);
                builder.setMessage(R.string.dialog_delete_image_message);
                builder.setTitle(R.string.dialog_delete_image_title);
                builder.setIconAttribute(android.R.attr.alertDialogIcon);
                builder.setPositiveButton(R.string.dialog_button_yes,
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                                  Log.d(LOG_TAG, "Delete item, " + " aid: " + v.getId());
                                                  if(mAdapterDatabase.deleteAttachment(v.getId())) {
                                                      mAttachments.remove(getAttachmentById(v.getId()));
                                                      //TODO: Probably we should delete file here as well

//                                                      LinearLayout layout = (LinearLayout) findViewById(R.id.layoutAttachments);
//                                                      layout.removeView(v);
                                                      LinearLayout layout2 = (LinearLayout) findViewById(R.id.layoutAttachmentsMagic);
                                                      layout2.removeView(v);

                                                      if(mAttachments.size() == 0) {
                                                          findViewById(R.id.scrollViewMagic).getLayoutParams().width = 1;
                                                          findViewById(R.id.magicButton).setVisibility(View.GONE);
                                                      }
/*
                                                      Intent i = new Intent();
                                                      i.setAction(ActivityMain.CUSTOM_MEMO_EVENT);
                                                      i.putExtra("memoId", mMemoId);
                                                      i.putExtra("action", ActivityMain.ACTION_ATTACHMENT_DELETED);
                                                      getApplicationContext().sendBroadcast(i);
*/
                                                  }
                                              }
                                          }
                );
                builder.setNegativeButton(R.string.dialog_button_no,
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                                  Log.d(LOG_TAG, "Cancelled deletion");
                                              }
                                          }
                );
                builder.show();
                return true;
            }
        });
        return icon;
    }


    /**
     * Destroy all fragments and loaders.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapterDatabase.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_edit_memo, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        int id = item.getItemId();
        switch(id) {

            case R.id.action_settings:
                break;

            case R.id.action_photo:
                Intent intent_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mPhotoFile = AdapterFile.getFilePhoto(AdapterFile.createFileName(mPhoneNumber));
                Uri uri = Uri.fromFile((mPhotoFile));
                intent_camera.putExtra(MediaStore.EXTRA_OUTPUT, uri); // set the image file name

                // start the image capture Intent
                Log.d(this.LOG_TAG, "onCreate. Start camera");
                startActivityForResult(intent_camera, CAPTURE_IMAGE);
                break;

            case R.id.action_picture:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE);
                break;

            case R.id.action_calendar:
                break;

            case R.id.action_audio:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_IMAGE:
                if((resultCode == RESULT_OK) && (imageReturnedIntent.getData() != null)) {

                    Uri selectedImage = imageReturnedIntent.getData();
                    Log.d(LOG_TAG, selectedImage.getPath());
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String path = cursor.getString(columnIndex);
                    Log.d(LOG_TAG, "path: " + path);
                    cursor.close();

                    addAttachment(new File(path));
                }
                break;
            case CAPTURE_IMAGE:
                if(resultCode == RESULT_OK) {
                    addAttachment(mPhotoFile);
                }
                break;
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        String title = mMemoTitle.getText().toString();
        String text = mMemoText.getText().toString();
        mAdapterDatabase.updateMemo(mMemoId, mPhoneNumber, title, text);
//        Toast.makeText(getApplicationContext(), "Save memo: " + mMemoId, Toast.LENGTH_SHORT).show();
        Log.d(this.LOG_TAG, "Saved memo: " + mMemoId);

        Intent i = new Intent();
        i.setAction(ActivityMain.CUSTOM_MEMO_EVENT);
        i.putExtra("memoId", mMemoId);
        i.putExtra("memoTitle", title);
        i.putExtra("memoBody", text);
        i.putExtra("action", ActivityMain.ACTION_MEMO_CHANGED);
        getApplicationContext().sendBroadcast(i);

        return super.onKeyUp(keyCode, event);
    }


    private void addAttachment(File _path) {
        AdapterFile.createThumbnail(_path); // create thumbnail for selected file

        long id = mAdapterDatabase.createAttachment(Attachment.ATTACHMENT_IMAGE, mMemoId, _path.getPath());
        Attachment a = new Attachment(id, Attachment.ATTACHMENT_IMAGE, mMemoId, _path.getPath());
        mAttachments.add(a);

/*
        LinearLayout layout = (LinearLayout)findViewById(R.id.layoutAttachments);
        ImageView icon = getAttachmentIcon(a);
        layout.addView(icon);
*/

        final HorizontalScrollView magic = (HorizontalScrollView)findViewById(R.id.scrollViewMagic);
        LinearLayout layout2 = (LinearLayout)findViewById(R.id.layoutAttachmentsMagic);
        ImageView icon2 = getAttachmentIcon(a);
        layout2.addView(icon2);

        if(mAttachments.size() == 1) {
            ImageView magicButton = (ImageView)findViewById(R.id.magicButton);
            magicButton.setVisibility(View.VISIBLE);
            magicButton.setImageResource(R.drawable.ic_launcher);
            magicButton.callOnClick();
        }
        magicScroll();

/*
        Intent i = new Intent();
        i.setAction(ActivityMain.CUSTOM_MEMO_EVENT);
        i.putExtra("memoId", mMemoId);
        i.putExtra("action", ActivityMain.ACTION_ATTACHMENT_ADDED);
        getApplicationContext().sendBroadcast(i);
*/

    }

    public void onClickMagicButton(View _v) {
        Log.d(this.LOG_TAG, "MagicButton");
        //Toast.makeText(getApplicationContext(), "MagicButton", Toast.LENGTH_SHORT).show();

        View magic = findViewById(R.id.scrollViewMagic);

        ApplicationMemo app = (ApplicationMemo)getApplicationContext();
        app.update();
        if(magic.getWidth() > app.mScreenWidth / 2) {
            magicAnimate(magic, 1); // 0 makes view to expand for full width
            magic.setVisibility(View.INVISIBLE);
            ((ImageView)_v).setImageResource(R.drawable.ic_sheet);
        }
        else {
            magic.setVisibility(View.VISIBLE);
            magicAnimate(magic, app.mScreenWidth);
            ((ImageView)_v).setImageResource(R.drawable.ic_launcher);
            magicScroll();
        }
    }


    public void magicAnimate(final View _view, int _width) {
        final int targetWidth = _width;
        final int initialWidth = _view.getWidth();
        final int delta = targetWidth - initialWidth;

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                Log.v("Expand", "time " + interpolatedTime + " vis: " + _view.getVisibility() + " w: " + _view.getWidth() + " w2: " + _view.getLayoutParams().width);
                _view.getLayoutParams().width =  initialWidth + (int) (delta * interpolatedTime);
                _view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(600);
        _view.startAnimation(a);
    }

    private void magicScroll() {
       final HorizontalScrollView magic = (HorizontalScrollView)findViewById(R.id.scrollViewMagic);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                magic.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);

    }
}
