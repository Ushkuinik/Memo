package company.memo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *
 */
public class AdapterMemoHorizontal extends ArrayAdapter<Memo> {
    private final String LOG_TAG = this.getClass().toString();
    private LayoutInflater  inflater;
    private ArrayList<Memo> mMemos;
    private Context         mContext;

    AdapterMemoHorizontal(final Context _context, final ArrayList<Memo> _memos) {
        super(_context, R.layout.list_item_memo_horizontal, _memos);

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
            view = this.inflater.inflate(R.layout.list_item_memo_horizontal, parent, false);
        }

        Memo memo = this.getMemo(position);
        Log.d(this.LOG_TAG, ": getView: pos: " + position + ", time: " + memo.getTime() + ", body: " + memo.getBody());

        ((TextView) view.findViewById(R.id.textTimestamp)).setText(memo.getTime());
        ((ImageView) view.findViewById(R.id.imageThumbnail)).setImageResource(R.drawable.ic_sheet);

        view.setTag(memo.getId());

        return view;
    }


    Memo getMemo(final int position) {
        return (this.getItem(position));
    }
}
