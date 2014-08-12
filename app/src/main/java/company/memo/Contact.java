package company.memo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Detailed information about contact
 */
public class Contact {

    private final String LOG_TAG = this.getClass().toString();

    private final int INDEX_INCOMING_NUMBER = 0;
    /**
     * Phone number
     */
    ArrayList<String> numbers;
    /**
     * eMails
     */
    ArrayList<String> emails;
    private Context mContext;
    private String  mName;
    private long    mId;
    private int     mMemoCount;
    private Uri     mPhotoUri;


    /**
     * @param _number incoming number. It will always be the first in the array
     */
    Contact(Context _context, String _number) {
        // TODO: If _number is invalid, throw exception
        mContext = _context;
        numbers = new ArrayList<String>();
        emails = new ArrayList<String>();
        numbers.add(_number);
        getContactInfo(_number);
    }


    public String getName() {
        return mName;
    }


    public void setName(String _name) {
        mName = _name;
    }


    public long getId() {
        return mId;
    }


    public void setId(long _id) {
        mId = _id;
    }


    public int getMemoCount() {
        return mMemoCount;
    }


    public void setMemoCount(int _count) {
        mMemoCount = _count;
    }


    public Uri getPhotoUri() {
        return mPhotoUri;
    }


    public String getIncomingNumber() {
        return numbers.get(INDEX_INCOMING_NUMBER);
    }


    /**
     * Looks for contact info by phone number
     *
     * @param _number phone number of incoming call
     * @return filled Contact object in contact is in phone book, otherwise <tt>null</tt>
     * @see android.provider.ContactsContract
     * @see Contact
     */
    private void getContactInfo(final String _number) {

        //Log.d(this.LOG_TAG, "getContactInfo");

        // 1. Get contact RecordId & Name
        String[] projection = new String[] {
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
//                ContactsContract.PhoneLookup.PHOTO_FILE_ID,
                ContactsContract.PhoneLookup.PHOTO_ID,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
                ContactsContract.PhoneLookup.PHOTO_URI
        };

        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(_number));
        ContentResolver contentResolver = mContext.getContentResolver();

        Cursor cursor = contentResolver.query(contactUri, projection, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {

                this.mId = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                this.mName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                if((this.mName == null) || (this.mName == ""))
                    this.mName = _number;
                this.mPhotoUri = Uri.parse(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)));

                Log.d(this.LOG_TAG, "Contact found. Id: " + mId);

                // 2. Get all phones and emails, related to this contact mRecordId
                String[] projection2 = new String[] {
                        ContactsContract.Data._ID,
                        ContactsContract.Data.DATA1,
                        ContactsContract.Data.MIMETYPE
                };

                String select2 = ContactsContract.Data.CONTACT_ID + "=?" + " AND ("
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' OR "
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "')";

                String[] selectArgs2 = new String[] {Long.toString(this.mId)};

                Cursor cursor2 = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection2,
                        select2,
                        selectArgs2,
                        null);

                if(cursor2 != null) {
                    if(cursor2.moveToFirst()) {
                        do {
                            String data = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.DATA1));
                            String mime = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Data.MIMETYPE));

                            //Log.d(this.LOG_TAG, "Data: data: " + data + ", mime = " + mime);

                            if(mime.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                                // TODO Need to define number normalization rules (stripe ' ', '-', '+', '(', ')', etc.)
                                data = data.replace(" ", "");
                                data = data.replace("-", "");
                                data = data.replace("+7", "");
                                data = data.replace("(", "");
                                data = data.replace(")", "");
                                if(data.equals(this.getIncomingNumber())) {
//                                    Log.d(this.LOG_TAG, "Skipped number. it is same as incomingNumber");
                                }
                                else {
//                                    Log.d(this.LOG_TAG, "Added number " + data);
                                    this.numbers.add(data);
                                }
                            }
                            else if(mime.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                                this.emails.add(data.toLowerCase());
                            }
                        } while(cursor2.moveToNext());
                    }
                    else {
//                        Log.d(this.LOG_TAG, "Contact not found");
                    }
                    cursor2.close();
                }
            }
            else {
//                Log.d(this.LOG_TAG, "Contact not found");
                this.mName = _number;
            }
            cursor.close();
        }
    }


}
