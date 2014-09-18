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

    public static final  String KEY_ID          = "id";
    public static final  String KEY_NUMBER      = "number";
    public static final  String KEY_DATE        = "date";
    public static final  String KEY_TITLE       = "title";
    public static final  String KEY_BODY        = "body";
    public static final  String KEY_TYPE        = "type";
    public static final  String KEY_MEMO_ID     = "memo_id";
    public static final  String KEY_PATH        = "path";
    private static final String DATABASE_NAME   = "memo_db";
    private static final String DATABASE_TABLE1 = "memo";
    private static final String TABLE_CREATE1   =
            "CREATE TABLE " + DATABASE_TABLE1 + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NUMBER + " TEXT NOT NULL, "
                    + KEY_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + KEY_TITLE + " TEXT NOT NULL, "
                    + KEY_BODY + " TEXT NOT NULL);";
    private static final String DATABASE_TABLE2 = "attachment";
    private static final String TABLE_CREATE2   =
            "CREATE TABLE " + DATABASE_TABLE2 + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_TYPE + " INTEGER, "
                    + KEY_MEMO_ID + " INTEGER, "
                    + KEY_PATH + " TEXT NOT NULL);";

    private static final int    DATABASE_VERSION = 4;
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


    public long createMemo(String _number, String _title, String _body) {
        Log.d(this.LOG_TAG, "createMemo");
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NUMBER, _number);
        initialValues.put(KEY_TITLE, _title);
        initialValues.put(KEY_BODY, _body);

        return m_db.insert(DATABASE_TABLE1, null, initialValues);
    }


    public boolean deleteMemo(long _id) {
        return m_db.delete(DATABASE_TABLE1, KEY_ID + "=" + _id, null) > 0;
    }


    public Cursor fetchAllMemos() {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_DATE, KEY_TITLE, KEY_BODY};
        return m_db.query(DATABASE_TABLE1, columns, null, null, null, null, null);
    }


    public ArrayList<Contact> selectContacts() {
        String[] columns = new String[] {KEY_NUMBER, "COUNT(*) AS count"};
        Cursor cursor = m_db.query(DATABASE_TABLE1, columns, null, null, KEY_NUMBER, null, null);

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


/*
    public void syncMemoCount(Contact _contact) {
        String number = _contact.getIncomingNumber();
        String[] columns = new String[] {KEY_NUMBER, "COUNT(*) AS count"};
        String selection = KEY_NUMBER + "=?";
        String[] selectionArgs = {number};
        Cursor cursor = m_db.query(DATABASE_TABLE1, columns, selection, selectionArgs, null, null, null);
        if(cursor != null) {

        }

    }
*/


    public Cursor fetchMemo(long _id) throws SQLException {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_TITLE, KEY_DATE, KEY_BODY};
        String selection = KEY_ID + "=" + _id;
        Cursor mCursor = m_db.query(DATABASE_TABLE1, columns, selection, null, null, null, null);

        if(mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    public ArrayList<Memo> getMemos(String _number) throws SQLException {
        Log.d(this.LOG_TAG, "getMemos for number: [" + _number + "]");

        if(_number == null) {
            return null;
        }

        ArrayList<Memo> memos = new ArrayList<Memo>();
        String sql = "SELECT m.id, m.number, m.date, m.title, m.body, COUNT(a.memo_id) as count FROM memo m LEFT OUTER JOIN attachment a ON a.memo_id=m.id GROUP BY a.memo_id, m.id ORDER BY m.id DESC;";
        Cursor cursor1 = m_db.rawQuery(sql, null);
        if(cursor1 != null) {
            while(cursor1.moveToNext()) {
                Log.d(this.LOG_TAG, "Cursor[5]: " + cursor1.getString(5));

                String id = cursor1.getString(cursor1.getColumnIndex(AdapterDatabase.KEY_ID));
                String number = cursor1.getString(cursor1.getColumnIndex(AdapterDatabase.KEY_NUMBER));
                String title = cursor1.getString(cursor1.getColumnIndex(AdapterDatabase.KEY_TITLE));
                String body = cursor1.getString(cursor1.getColumnIndex(AdapterDatabase.KEY_BODY));
                String timestamp = cursor1.getString(cursor1.getColumnIndex(AdapterDatabase.KEY_DATE));
                String count = cursor1.getString(cursor1.getColumnIndex("count"));
                Memo newMemo = new Memo(Long.parseLong(id), _number, title, body, timestamp, Integer.parseInt(count));
                memos.add(newMemo);
            }
        }
        cursor1.close();

        return memos;
    }


    public void logMemos() throws SQLException {
        String[] columns = new String[] {KEY_ID, KEY_NUMBER, KEY_DATE, KEY_TITLE, KEY_BODY};
        Cursor cursor = m_db.query(true, DATABASE_TABLE1, columns, null, null, null, null, null, null);

        if(cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_ID));
                String number = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_NUMBER));
                String title = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_TITLE));
                String body = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_BODY));
                String timestamp = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_DATE));

                Log.d(this.LOG_TAG, "id: " + id + " number: " + number + " title: " + title + " body: " + body + " time: " + timestamp);
            }
        }
    }


    public boolean updateMemo(long _id, String _number, String _title, String _body) {
        ContentValues args = new ContentValues();
        args.put(KEY_NUMBER, _number);
        args.put(KEY_TITLE, _title);
        args.put(KEY_BODY, _body);
        String selection = KEY_ID + "=" + _id;

        return m_db.update(DATABASE_TABLE1, args, selection, null) > 0;
    }


    public Memo getMemo(long _id) {
        Memo memo = null;

        Cursor cursor = this.fetchMemo(_id);

        if(cursor != null) {
            String number = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_NUMBER));
            String title = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_TITLE));
            String body = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_BODY));
            String timestamp = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_DATE));
            memo = new Memo(_id, number, title, body, timestamp, 99);
        }

        return memo;
    }


    public ArrayList<Attachment> getAttachmentByMemoId(long _id) throws SQLException {
//        Log.d(this.LOG_TAG, "getAttachmentByMemoId: " + _id);

        if(_id == 0) {
            return null;
        }
        String[] columns = new String[] {KEY_ID, KEY_TYPE, KEY_MEMO_ID, KEY_PATH};
        String selection = KEY_MEMO_ID + "=?";
        String[] selectionArgs = {String.valueOf(_id)};
        Cursor cursor = m_db.query(DATABASE_TABLE2, columns, selection, selectionArgs, null, null, null);

        ArrayList<Attachment> attachments = new ArrayList<Attachment>();

//        Log.d(this.LOG_TAG, "getAttachmentByMemoId found " + cursor.getCount() + " attachments for memo: " + _id);

        if(cursor != null) {
            while(cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_ID));
                String type = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_TYPE));
                String path = cursor.getString(cursor.getColumnIndex(AdapterDatabase.KEY_PATH));
                Attachment newAttachment = new Attachment(Long.parseLong(id), Integer.parseInt(type), _id, path);
                attachments.add(newAttachment);
            }
        }
        return attachments;
    }


    public long createAttachment(int _type, long _memo_id, String _path) {
        Log.d(this.LOG_TAG, "createAttachment");
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, _type);
        initialValues.put(KEY_MEMO_ID, _memo_id);
        initialValues.put(KEY_PATH, _path);

        return m_db.insert(DATABASE_TABLE2, null, initialValues);
    }


    public boolean deleteAttachment(long _id) {
        Log.d(this.LOG_TAG, "deleteAttachment");
        return m_db.delete(DATABASE_TABLE2, KEY_ID + "=" + _id, null) > 0;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String LOG_TAG = "DatabaseHelper";


        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(TABLE_CREATE1);
            db.execSQL(TABLE_CREATE2);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE1);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE2);
            onCreate(db);
        }
    }
}
