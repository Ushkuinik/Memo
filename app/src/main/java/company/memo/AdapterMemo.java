package company.memo;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *
 */
public class AdapterMemo extends ArrayAdapter<Memo> {
    private final String LOG_TAG = this.getClass().toString();
    private LayoutInflater  inflater;
    private ArrayList<Memo> mMemos;
    private Context         mContext;


    AdapterMemo(final Context _context, final ArrayList<Memo> _memos) {
        super(_context, R.layout.list_item_memo, _memos);

        mContext = _context;
        mMemos = _memos;
        this.inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if(view == null) {
            view = this.inflater.inflate(R.layout.list_item_memo, parent, false);
        }

        Memo memo = this.getMemo(position);
        Log.d(this.LOG_TAG, ": getView: pos: " + position + ", time: " + memo.getTime() + ", body: " + memo.getBody());

        ((TextView) view.findViewById(R.id.textYear)).setText(memo.getYear());
        ((TextView) view.findViewById(R.id.textDayMonth)).setText(memo.getDayMonth());
        ((TextView) view.findViewById(R.id.textTime)).setText(memo.getTime());
        ((TextView) view.findViewById(R.id.textBody)).setText(memo.getBody());
        TextView title = ((TextView) view.findViewById(R.id.textTitle));
        if(memo.getTitle().isEmpty()) {
            title.setText(mContext.getResources().getText(R.string.memo_title_empty));
            title.setTextColor(Color.parseColor("#888888"));
        }
        else {
            title.setText(memo.getTitle());
            title.setTextColor(Color.parseColor("#aaaaaa"));
        }

        view.setTag(memo.getId());

        return view;
    }


    @Override
    public boolean hasStableIds() {
        return true;
    }


    Memo getMemo(final int position) {
        return (this.getItem(position));
    }
}
