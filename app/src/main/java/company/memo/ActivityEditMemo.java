package company.memo;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class ActivityEditMemo extends ActionBarActivity {

    private static final int SELECT_IMAGE  = 100;
    private static final int CAPTURE_IMAGE = 200;

    private final String LOG_TAG = "ActivityEditMemo";
    private Vibrator mVibrator;

    private AdapterDatabase        mAdapterDatabase;
    private String                 mPhoneNumber;
    private long                   mMemoId;
    private EditText               mMemoTitle;
    private TopWindow.LineEditText mMemoText;
    private ArrayList<Attachment>  mAttachments;
    private File                   mPhotoFile;
    private enum    MAGIC_STATE {
        UNDEFINED,
        COLLAPSED,
        COLLAPSING,
        EXPANDED,
        EXPANDING
    }
    private MAGIC_STATE mMagicState = MAGIC_STATE.COLLAPSED;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);
        Log.d(LOG_TAG, "OnCreate");

        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        //TODO: Move AdapterDatabase to ApplicationMemo
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

            mMemoText.setText(memo.getBody());
            mMemoText.setHint(R.string.memo_body_hint);

            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutAttachmentsMagic);

/*
            LayoutTransition transition = layout.getLayoutTransition();
            //transition.enableTransitionType(LayoutTransition.CHANGING);
            transition.setDuration(2000);
            transition.addTransitionListener(new LayoutTransition.TransitionListener() {
                @Override
                public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {

                }


                @Override
                public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                    Log.d(LOG_TAG, "endTransition view.id:" + view.getId() + " type:" + transitionType + " R.id.layoutAttachmentsMagic: " + R.id.layoutAttachmentsMagic);
                    if(view.getId() == -1) {
*/
/*
                        findViewById(R.id.scrollViewMagic).getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        findViewById(R.id.scrollViewMagic).requestLayout();
*//*

                    }
                }
            });
*/

            mAttachments = mAdapterDatabase.getAttachmentByMemoId(mMemoId);
            for(Attachment a : mAttachments) {
                ImageView icon = getAttachmentIcon(a);
                layout.addView(icon);
            }

            ImageView magicButton = (ImageView) findViewById(R.id.magicButton);
            View magic = findViewById(R.id.scrollViewMagic);

            magic.setVisibility(View.INVISIBLE);
            mMagicState = MAGIC_STATE.COLLAPSED;
            magic.getLayoutParams().width = 1;
            magicButton.setImageResource(R.drawable.ic_attachments1);

            if(mAttachments.size() == 0) {
                magicButton.setVisibility(View.INVISIBLE);
            }

        }
        else {
            Toast.makeText(this, "Memo was not specified", Toast.LENGTH_SHORT).show();
            finish();
        }

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    private Attachment getAttachmentById(long _id) {
        Log.d(LOG_TAG, "getAttachmentById");
        for(Attachment a : mAttachments) {
            if(a.getId() == _id) {
                return a;
            }
        }
        return null;
    }


    /**
     * Creates ImageView for attachment. Icon depends on attachments content. Photos and pictures
     *   have preview thumbnails as icons, audio files are marked as special icons
     * @param _attachment Attachment object
     * @return ImageView of attachment icon
     */
    public ImageView getAttachmentIcon(Attachment _attachment) {
        Log.d(LOG_TAG, "getAttachmentIcon");

        ImageView icon = new ImageView(getApplicationContext());
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        File fileThumbnail = AdapterFile.createThumbnail(this, _attachment.getPath());
        if(fileThumbnail.exists()) { // if thumbnail exists, use it
            Bitmap bitmap = BitmapFactory.decodeFile(fileThumbnail.getPath());
            icon.setImageBitmap(bitmap);
        }
        else {
            // if thumbnail cannot be created use predefined icon
            icon.setImageResource(R.drawable.ic_sheet);
        }
        icon.setId((int) _attachment.getId());

        // set layout params
        ApplicationMemo app = (ApplicationMemo) getApplicationContext();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(0, 0, 0, 0);
        icon.setLayoutParams(params);

        // set listeners
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Icon clicked [" + v.getId() + "]");

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
                Log.d(LOG_TAG, "Icon long clicked [" + v.getId() + "]");

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEditMemo.this);
                builder.setMessage(R.string.dialog_delete_image_message);
                builder.setTitle(R.string.dialog_delete_image_title);
                builder.setIcon(R.drawable.ic_dialog_alert_holo_light);
                builder.setPositiveButton(R.string.dialog_button_yes,
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                                  Log.d(LOG_TAG, "Delete item [" + v.getId() + "]");
                                                  if(mAdapterDatabase.deleteAttachment(v.getId())) {
                                                      mAttachments.remove(getAttachmentById(v.getId()));
                                                      //TODO: Probably we should delete file here as well

                                                      LinearLayout layout = (LinearLayout) findViewById(R.id.layoutAttachmentsMagic);
                                                      layout.removeView(v);
                                                      findViewById(R.id.scrollViewMagic).getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                                                      if(mAttachments.size() == 0)
                                                          findViewById(R.id.magicButton).setVisibility(View.INVISIBLE);
                                                  }
                                              }
                                          }
                );
                builder.setNegativeButton(R.string.dialog_button_no,
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int id) {
                                                  Log.d(LOG_TAG, "Deletion cancelled");
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
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        mAdapterDatabase.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.activity_edit_memo, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");

        Intent intent;
        switch(item.getItemId()) {

            case R.id.action_settings:
                break;

            case R.id.action_photo:
                Log.i(LOG_TAG, "Action [Photo]");

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mPhotoFile = AdapterFile.getFilePhoto(AdapterFile.createFileName(mPhoneNumber));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile((mPhotoFile))); // set the image file name
                startActivityForResult(intent, CAPTURE_IMAGE);
                break;

            case R.id.action_picture:
                Log.i(LOG_TAG, "Action [Picture]");

                intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE);
                break;

            case R.id.action_calendar:
                Log.i(LOG_TAG, "Action [Calendar]");

                Contact contact = getContactInfo(mPhoneNumber);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("prefPhoneNumber", contact.getIncomingNumber());
                editor.putString("prefContactName", contact.getName()); // rewrite with null if no name
                if(contact.emails.size() > 0)
                    editor.putString("prefContactEmail", contact.emails.get(0)); // rewrite with null if no emails
                editor.commit();

                Intent i = new Intent(getBaseContext(), PreferenceActivityNewEvent.class);
                startActivity(i);
                break;

            case R.id.action_audio:
                Log.i(LOG_TAG, "Action [Audio]");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.d(LOG_TAG, "onActivityResult");

        switch(requestCode) {
            case SELECT_IMAGE:
                if((resultCode == RESULT_OK) && (imageReturnedIntent.getData() != null)) {

                    Uri selectedImage = imageReturnedIntent.getData();
                    Log.i(LOG_TAG, "Media selected [" + selectedImage.getPath() + "]");

                    // Retrieve file path by media path
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String path = cursor.getString(columnIndex);
                    cursor.close();

                    Log.i(LOG_TAG, "Image found [" + path + "]");

                    addAttachment(new File(path));
                }
                break;
            case CAPTURE_IMAGE:
                if(resultCode == RESULT_OK) {
                    Log.i(LOG_TAG, "Picture taken [" + mPhotoFile.getPath() + "]");

                    addAttachment(mPhotoFile);
                }
                break;
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, @NotNull KeyEvent event) {
        Log.d(LOG_TAG, "onKeyUp");

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
        Log.d(LOG_TAG, "addAttachment");

        AdapterFile.createThumbnail(this, _path); // create thumbnail for selected file

        long id = mAdapterDatabase.createAttachment(Attachment.ATTACHMENT_IMAGE, mMemoId, _path.getPath());
        Attachment a = new Attachment(id, Attachment.ATTACHMENT_IMAGE, mMemoId, _path.getPath());
        mAttachments.add(a);

        LinearLayout layout = (LinearLayout)findViewById(R.id.layoutAttachmentsMagic);

        ImageView icon = getAttachmentIcon(a);

        layout.addView(icon);

        if(mAttachments.size() == 1) {
            ImageView magicButton = (ImageView) findViewById(R.id.magicButton);
            magicButton.setVisibility(View.VISIBLE);

//            findViewById(R.id.scrollViewMagic).setVisibility(View.VISIBLE);
//            mMagicState = MAGIC_STATE.EXPANDED;
/*
            findViewById(R.id.scrollViewMagic).getLayoutParams().width = 20;
            findViewById(R.id.scrollViewMagic).setVisibility(View.VISIBLE);
            magicButton.performClick();
*/
        }
        else {
            findViewById(R.id.scrollViewMagic).getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }


//        magicScrollToRight();
    }


    public void onClickMagicButton(View _v) {
        Log.d(this.LOG_TAG, "MagicButton");

        //mVibrator.vibrate(20);

        View magic = findViewById(R.id.scrollViewMagic);
        View layout = findViewById(R.id.layoutAttachmentsMagic);
        View button = findViewById(R.id.magicButton);

        ApplicationMemo app = (ApplicationMemo) getApplicationContext();
        app.update();

        if((mMagicState == MAGIC_STATE.EXPANDED) || (mMagicState == MAGIC_STATE.EXPANDING)) {
            Log.d(this.LOG_TAG, "Collapsing");
            mMagicState = MAGIC_STATE.COLLAPSING;
            magicAnimate(magic, 1); // 0 makes view to expand for full width
            ((ImageView) _v).setImageResource(R.drawable.ic_attachments1);
        }
        else if((mMagicState == MAGIC_STATE.COLLAPSED) || (mMagicState == MAGIC_STATE.COLLAPSING)){
            Log.d(this.LOG_TAG, "Expanding");
            magic.setVisibility(View.VISIBLE);
            mMagicState = MAGIC_STATE.EXPANDING;
            magicAnimate(magic, Math.min(app.mScreenWidth - button.getWidth(), layout.getWidth()));
            ((ImageView) _v).setImageResource(R.drawable.ic_attachments2);
        }
    }


    public void magicAnimate(final View _view, int _width) {
        final int initialWidth = _view.getWidth();
        final int delta = _width - initialWidth;
        Log.d(LOG_TAG, "d: " + delta + " init: " + initialWidth + " new_w: " + _width);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                _view.getLayoutParams().width = initialWidth + (int) (delta * interpolatedTime);
                _view.requestLayout();
                Log.d(LOG_TAG, "w: " + _view.getLayoutParams().width + " t: " + interpolatedTime);
            }


            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(600);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }


            @Override
            public void onAnimationEnd(Animation animation) {
//                View layout = findViewById(R.id.layoutAttachmentsMagic);
                switch(mMagicState) {
                    case EXPANDING:
                        mMagicState = MAGIC_STATE.EXPANDED;
//                        magicScrollToRight();
                        break;

                    case COLLAPSING:
                        mMagicState = MAGIC_STATE.COLLAPSED;
                        _view.setVisibility(View.INVISIBLE);
                        break;

                    case EXPANDED:
                    case COLLAPSED:
                    case UNDEFINED:
                    default:
                        break;
                }
            }


            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        _view.startAnimation(a);
    }


    private void magicScrollToRight() {
        final HorizontalScrollView magic = (HorizontalScrollView) findViewById(R.id.scrollViewMagic);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                magic.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);
    }


    /**
     * Looks for contact info by phone number
     *
     * @param _number
     *              phone number of incoming call
     * @return      filled Contact object in contact is in phone book, otherwise <tt>null</tt>
     * @see android.provider.ContactsContract
     * @see Contact
     */
    private Contact getContactInfo(final String _number) {
        Log.d(this.LOG_TAG, "getContactInfo");

        Contact contact = new Contact(this, _number);

        // 1. Get contact id
        String[] projection = new String[] {
                ContactsContract.PhoneLookup._ID,
        };

        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(_number));

        Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                contact.setId(Long.parseLong(id));

                Log.d(this.LOG_TAG, "Contact found. Id: " + id);

                // 2. Get all phones and emails, related to this contact id
                String[] projection2 = new String[] {
                        ContactsContract.Data._ID,
                        ContactsContract.Data.DATA1,
                        ContactsContract.Data.MIMETYPE

                };

                String select2 = ContactsContract.Data.CONTACT_ID + "=?" + " AND ("
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' OR "
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "')";

                String [] selectArgs2 = new String[] {String.valueOf(contact.getId())};

                Cursor cursor2 = getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        projection2,
                        select2,
                        selectArgs2,
                        null);

                if(cursor2 != null) {
                    if (cursor2.moveToFirst()) {
                        do {
                            String data = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.DATA1));
                            String mime = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.MIMETYPE));

                            Log.d(this.LOG_TAG, "Data: data: " + data + ", mime = " + mime);

                            if(mime.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                // TODO Need to define number normalization rules (stripe ' ', '-', '+', '(', ')', etc.)
                                data = data.replace(" ", "");
                                data = data.replace("-", "");
                                data = data.replace("+7", "");
                                data = data.replace("(", "");
                                data = data.replace(")", "");
                                if(data.equals(contact.getIncomingNumber())) {
                                    Log.d(this.LOG_TAG, "Skipped number. it is same as incomingNumber");
                                }
                                else {
                                    Log.d(this.LOG_TAG, "Added number " + data);
                                    contact.numbers.add(data);
                                }
                            }
                            else if(mime.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                                contact.emails.add(data.toLowerCase());
                            }
                        } while(cursor2.moveToNext());
                    }
                    else {
                        Log.d(this.LOG_TAG, "Contact not found");
                    }
                    cursor2.close();
                }
            }
            else {
                Log.d(this.LOG_TAG, "Contact not found");
            }
            cursor.close();
        }

        return contact;
    }
}
