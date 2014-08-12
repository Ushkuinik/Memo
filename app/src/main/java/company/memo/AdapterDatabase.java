package company.memo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 */
public class AdapterDatabase {

    public static final  String KEY_ID           = "id";
    public static final  String KEY_NUMBER       = "number";
    public static final  String KEY_DATE         = "date";
    public static final  String KEY_BODY         = "body";
    private static final String DATABASE_NAME    = "memo_db";
    private static final String DATABASE_TABLE   = "memo";
    private static final String DATABASE_CREATE  =
            "CREATE TABLE " + DATABASE_TABLE + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NUMBER + " TEXT NOT NULL, "
                    + KEY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + KEY_BODY + " TEXT NOT NULL);";
    private static final int    DATABASE_VERSION = 1;
    private final        String LOG_TAG          = this.getClass().toString();
    private final Context        mContext;
    private       DatabaseHelper m_dbHelper;
    private       SQLiteDatabase m_db;


    public AdapterDatabase(Context _context) {
        mContext = _context;
    }


    public AdapterDatabase open() throws SQLException {
        m_dbHelper = new DatabaseHelper(mContext);
        m_db = m_dbHelper.getWritableDatabase();
        return this;
    }


    public void close() {
        m_dbHelper.close();
    }


    public long createMemo(String _number, String _body) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NUMBER, _number);
        initialValues.put(KEY_BODY, _body);

        return m_db.insert(DATABASE_TABLE, null, initialValues);
    }


    public boolean deleteMemo(long _id) {
        return m_db.delete(DATABASE_TABLE, KEY_ID + "=" + _id, null) > 0;
    }


    public Cursor fetchAllMemos() {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_DATE, KEY_BODY};
        return m_db.query(DATABASE_TABLE, columns, null, null, null, null, null);
    }


    public ArrayList<Contact> selectContacts() {
        String[] columns = new String[] {KEY_NUMBER, "COUNT(*) AS count"};
        Cursor cursor = m_db.query(DATABASE_TABLE, columns, null, null, KEY_NUMBER, null, null);

        ArrayList<Contact> contacts = new ArrayList<Contact>();

        Log.d(this.LOG_TAG, "selectContacts found " + cursor.getCount() + " contacts");
        while(cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_NUMBER));
            String count = cursor.getString(cursor.getColumnIndex("count"));
            Contact newContact = new Contact(mContext, number);
            newContact.setMemoCount(Integer.parseInt(count));
            contacts.add(newContact);
        }

        return contacts;
    }


    public Cursor fetchMemo(long _id) throws SQLException {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_DATE, KEY_BODY};
        String selection = KEY_ID + "=" + _id;
        Cursor mCursor = m_db.query(DATABASE_TABLE, columns, selection, null, null, null, null);

        if(mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public ArrayList<Memo> getMemos(String _number) throws SQLException {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_DATE, KEY_BODY};
        String selection = KEY_NUMBER + "=?";
        String[] selectionArgs = {_number};
        Cursor cursor = m_db.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, null);

        ArrayList<Memo> memos = new ArrayList<Memo>();

        Log.d(this.LOG_TAG, "getMemos found " + cursor.getCount() + " memos for number: [" + _number + "]");

        if(cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_ID));
                String body = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_BODY));
                String timestamp = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_DATE));
                Memo newMemo = new Memo(Long.parseLong(id), _number, body, timestamp);
                memos.add(newMemo);
            }
        }
        return memos;
    }


    public void logMemos() throws SQLException {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_DATE, KEY_BODY};
        Cursor cursor = m_db.query(true, DATABASE_TABLE, columns, null, null, null, null, null, null);

        if(cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_ID));
                String number = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_NUMBER));
                String body = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_BODY));
                String timestamp = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_DATE));

                Log.d(this.LOG_TAG, "id: " + id + " number: " + number + " body: " + body + " time: " + timestamp);
            }
        }
    }

    public boolean updateMemo(long _id, String _number, String _body) {
        ContentValues args = new ContentValues();
        args.put(KEY_NUMBER, _number);
        args.put(KEY_BODY, _body);
        String selection = KEY_ID + "=" + _id;

        return m_db.update(DATABASE_TABLE, args, selection, null) > 0;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String LOG_TAG = "DatabaseHelper";


        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }
}
