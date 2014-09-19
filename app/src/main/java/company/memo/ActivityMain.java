//TODO: Create Memo edit activity
//TODO: Create memo preview thumbnails
//TODO: Crop photo
//TODO: Memo preview in memo list
//TODO: Picture preview on click
//TODO: Add calendar support
//TODO: Add voice recording
//TODO: Memo deleting on swipe should be reworked. Disabled for now


package company.memo;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import wei.mark.standout.StandOutWindow;


public class ActivityMain extends ActionBarActivity {

    private final       String LOG_TAG                   = "ActivityMain";
    public static final String CUSTOM_MEMO_EVENT         = "company.memo.CUSTOM_MEMO_EVENT";
    public static final int    ACTION_MEMO_UNDEFINED     = 0;
    public static final int    ACTION_MEMO_ADDED         = 1;
    public static final int    ACTION_MEMO_DELETED       = 2;
    public static final int    ACTION_MEMO_CHANGED       = 3;
    public static final int    ACTION_ATTACHMENT_ADDED   = 4;
    public static final int    ACTION_ATTACHMENT_DELETED = 5;

    private AdapterDatabase mAdapterDatabase;
    private ArrayList<Contact> mContacts = new ArrayList<Contact>();
    private AdapterContact mAdapterContact;
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(CUSTOM_MEMO_EVENT)) {
                Log.d("BroadcastReceiver", "Update list");
                String number = intent.getStringExtra("phoneNumber");
                Log.d("BroadcastReceiver", "Number: " + number);

                int action = intent.getIntExtra("action", ACTION_MEMO_UNDEFINED);

                Contact contact = getContactByNumber(number);

                switch(action) {
                    case ACTION_MEMO_ADDED:
                        Log.d(LOG_TAG, "ACTION_MEMO_ADDED");
                        if(contact != null)
                            contact.setMemoCount(contact.getMemoCount() + 1);
                        else {
                            contact = new Contact(getApplicationContext(), number);
                            contact.setMemoCount(1);
                            mContacts.add(contact);
                        }
                        break;

                    case ACTION_MEMO_DELETED:
                        Log.d(LOG_TAG, "ACTION_MEMO_DELETED");
                        contact.setMemoCount(contact.getMemoCount() - 1);
                        if(contact.getMemoCount() >= 0)
                            mContacts.remove(contact);
                        break;

                    default:
                        return;
                }

                mAdapterContact.notifyDataSetChanged();
            }
        }
    };


    private Contact getContactByNumber(String _number){
        for(Contact c : mContacts) {
            if(c.getIncomingNumber().equals(_number)) {
                return c;
            }
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listContacts = (ListView) findViewById(R.id.listView);
        listContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String number = view.getTag().toString();
                Toast.makeText(getApplicationContext(), "Position in view: " + position + "; list id: " + id + "; tag: " + number, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), ActivityMemoList.class);
                intent.putExtra("phoneNumber", number);
                startActivity(intent);
            }
        });

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();

        mContacts = mAdapterDatabase.selectContacts();
        this.mAdapterContact = new AdapterContact(this, mContacts);
        listContacts.setAdapter(this.mAdapterContact);

        //mReceiver = new IncomingReceiver();

        IntentFilter filter = new IntentFilter(CUSTOM_MEMO_EVENT);
        registerReceiver(mReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapterDatabase.close();
        unregisterReceiver(mReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem menuPower = (MenuItem)menu.findItem(R.id.action_power);
        menuPower.setChecked(isServiceRunning(CallDetectService.class));

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
                bundle.putString("phoneNumber", "12345689");
                StandOutWindow.sendData(this, TopWindow.class, StandOutWindow.DEFAULT_ID, CallDetectService.GOT_PHONE_NUMBER, bundle, null, 0);
                break;

            case R.id.action_power:
                item.setChecked(!item.isChecked()); // change checked state

                Intent intent = new Intent(ActivityMain.this, CallDetectService.class);
                if (item.isChecked()) {
                    startService(intent);
                    Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
                }
                else {
                    stopService(intent);
                    Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

