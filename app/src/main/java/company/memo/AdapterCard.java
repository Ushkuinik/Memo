package company.memo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 */
public class AdapterCard extends ArrayAdapter<Memo> {

    private final Context   mContext;
    private ArrayList<Memo> mMemos;


    AdapterCard(final Context _context, final ArrayList<Memo> _memos) {
        super(_context, R.layout.list_item_memo, _memos);

        mContext = _context;
        mMemos = _memos;
    }


    @Override
    public int getCount() {
        return this.mMemos.size();
    }


    @Override
    public Memo getItem(final int _position) {
        return this.mMemos.get(_position);
    }


    @Override
    public long getItemId(final int position) {
        return position;
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_memo_card, parent, false);
        }

        Memo memo = this.getItem(position);

        ((TextView) view.findViewById(R.id.card_time)).setText(memo.getDateFormatted("HH:mm  d MMM, yyyy"));
        ((TextView) view.findViewById(R.id.card_body)).setText(memo.getBody());
        TextView title = ((TextView) view.findViewById(R.id.card_title));
        if(memo.getTitle().isEmpty()) {
            title.setText(mContext.getResources().getText(R.string.memo_title_empty));
            title.setTextColor(Color.parseColor("#888888"));
        }
        else {
            title.setText(memo.getTitle());
            title.setTextColor(Color.parseColor("#aaaaaa"));
        }


        ApplicationMemo app = (ApplicationMemo)mContext.getApplicationContext();
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_attachments1).copy(Bitmap.Config.ARGB_8888, true);

        String text = Integer.toString(memo.getAttachmentCount());
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#666666"));
        paint.setTextSize(app.Dp2Pixel(14));
        paint.setTypeface(Typefaces.get("sans-serif-light", Typeface.NORMAL));
        paint.setAntiAlias(true);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Canvas canvas = new Canvas(bitmap);
        Paint paint_fill = new Paint();
        paint_fill.setColor(Color.parseColor("#aaffffff"));
        int h = bounds.height() + app.Dp2Pixel(7);
        int w = Math.max(bounds.width() + app.Dp2Pixel(7), h);

        float r = (float)h / 2;
        RectF rectF = new RectF(0, 0, w, h);
        rectF.offsetTo((bitmap.getWidth() - rectF.width())/ 2, (bitmap.getHeight() - rectF.height()) / 2);

        canvas.drawRoundRect(rectF, r, r, paint_fill);
        canvas.drawText(text, (bitmap.getWidth() - bounds.width())/ 2, (bitmap.getHeight() + bounds.height()) / 2, paint);

        ((ImageView) view.findViewById(R.id.card_attachments)).setImageBitmap(bitmap);


        view.setTag(memo.getId());

        return view;
    }

    public static class Typefaces {
        private static final String TAG = "Typefaces";

        private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();


        public static Typeface get(String familyName, int style) {
            synchronized(cache) {
                if(!cache.containsKey(familyName)) {
                    try {
                        Typeface t = Typeface.create(familyName, style);
                        cache.put(familyName, t);
                    }
                    catch(Exception e) {
                        Log.e(TAG, "Could not get typeface '" + familyName
                                + "' because " + e.getMessage());
                        return null;
                    }
                }
                return cache.get(familyName);
            }
        }
    }
}