package company.memo;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import wei.mark.standout.StandOutWindow;


public class ActivityMain extends ActionBarActivity {

    private final String LOG_TAG = this.getClass().toString();

    private DbAdapter mDbAdapter;
    private ArrayList<Contact> mContacts = new ArrayList<Contact>();
    private ContactAdapter mContactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbAdapter = new DbAdapter(this);
        mDbAdapter.open();
        this.listContacts();
    }


    /**
     * Destroy all fragments and loaders.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbAdapter.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;

            case R.id.action_show_window:
                StandOutWindow.closeAll(this, TopWindow.class);
                StandOutWindow.show(this, TopWindow.class, StandOutWindow.DEFAULT_ID);
                Bundle bundle = new Bundle();
                bundle.putString("phoneNumber", "1234");
                StandOutWindow.sendData(this, TopWindow.class, StandOutWindow.DEFAULT_ID, CallDetectService.GOT_PHONE_NUMBER, bundle, null, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Looks for ...
     *
     */
    private void listContacts() {

        Log.d(this.LOG_TAG, "listContacts");

        mContacts = mDbAdapter.selectContacts();

        createContactList(mContacts);

/*
        Contact contact = getContactInfo(number);

        contact.name = getContactName(number);

        if((contact.name != null) && (!contact.name.isEmpty())) {
            setTitle(contact.name);
        }
        else {
            setTitle(number);
        }

        PhoneDataRetriever retriever = new PhoneDataRetriever(this);

        retriever.execute(contact);

        this.createContactList(this.events);
*/
    }
    /**
     * Constructs ListView from Events array. Uses custom EventAdapter
     *
     * @param _contacts array of Events to be converted to ListView
     * @see ContactAdapter
     */

    private void createContactList(final ArrayList<Contact> _contacts) {

        Log.d(this.LOG_TAG, "createContactList");

        ListView listContacts = (ListView)findViewById(R.id.listView);
        this.mContactAdapter = new ContactAdapter(this, _contacts);
        listContacts.setAdapter(this.mContactAdapter);

        // scroll to the bottom
        //listRecords.setSelection(this.adapter.getCount() - 1);
    }

}
