package company.memo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        Log.d(this.LOG_TAG, ": getView: pos: " + position + ", name: " + contact.getIncomingNumber() + ", count: " + contact.getMemoCount());

        view.setTag(contact.getIncomingNumber());

        ((TextView) view.findViewById(R.id.name)).setText(contact.getName());

        ((TextView) view.findViewById(R.id.count)).setText(Integer.toString(contact.getMemoCount()));

        if(contact.getPhotoUri() != null) {
            ((ImageView) view.findViewById(R.id.photo)).setImageURI(contact.getPhotoUri());
        }
        else {
//            ((ImageView) view.findViewById(R.id.photo)).setImageResource(R.drawable.ic_contact_picture_2);
        }

        return view;
    }


    Contact getContact(final int position) {
        return (this.getItem(position));
    }
}

