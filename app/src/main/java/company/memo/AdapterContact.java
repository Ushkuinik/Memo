package company.memo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *
 */
public class AdapterContact extends ArrayAdapter<Contact> {
    private final String LOG_TAG = this.getClass().toString();
    private LayoutInflater     inflater;
    private ArrayList<Contact> mContacts;
    private Context            mContext;


    AdapterContact(final Context _context, final ArrayList<Contact> _contacts) {
        super(_context, R.layout.list_item_contact, _contacts);

        mContext = _context;
        mContacts = _contacts;
        this.inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return this.mContacts.size();
    }


    @Override
    public Contact getItem(final int _position) {
        return this.mContacts.get(_position);
    }


    @Override
    public long getItemId(final int position) {
        return position;
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        View view = convertView;
        if(view == null) {
            view = this.inflater.inflate(R.layout.list_item_contact, parent, false);
        }

        Contact contact = this.getContact(position);

        //Log.d(this.LOG_TAG, ": getView: pos: " + position + ", name: " + contact.getIncomingNumber() + ", count: " + contact.getMemoCount());

        view.setTag(contact.getIncomingNumber());

        String name = contact.getName();
        if(name == null)
            name = mContext.getString(R.string.unknown_contact) + " (" + contact.getIncomingNumber() + ")";
        ((TextView) view.findViewById(R.id.name)).setText(name);

        //((TextView) view.findViewById(R.id.count)).setText(Integer.toString(contact.getMemoCount()));

        Bitmap bm;
        if(contact.getMemoCount() == 1) {
            bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_memos1).copy(Bitmap.Config.ARGB_8888, true);
        }
        else {
            bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_memos3).copy(Bitmap.Config.ARGB_8888, true);
        }
//        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_note_stroke2).copy(Bitmap.Config.ARGB_8888, true);

        String text = Integer.toString(contact.getMemoCount());
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#888888"));
        ApplicationMemo app = ((ApplicationMemo)mContext.getApplicationContext());
        paint.setTextSize(app.Dp2Pixel(14));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, (bm.getWidth() - bounds.width()) / 2 - app.Dp2Pixel(4), (bm.getHeight() + bounds.height() + app.Dp2Pixel(16)) / 2, paint);

        ((ImageView) view.findViewById(R.id.memos)).setImageBitmap(bm);

        if(contact.getPhotoUri() != null) {
            ((ImageView) view.findViewById(R.id.photo)).setImageURI(contact.getPhotoUri());
        }

        return view;
    }


    Contact getContact(final int position) {
        return (this.getItem(position));
    }
}

