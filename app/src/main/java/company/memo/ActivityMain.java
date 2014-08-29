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
import android.content.Context;
import android.content.Intent;
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

    private final String LOG_TAG = "ActivityMain";

    private AdapterDatabase mAdapterDatabase;
    private ArrayList<Contact> mContacts = new ArrayList<Contact>();
    private AdapterContact mAdapterContact;


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
        this.listContacts();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapterDatabase.close();
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
                bundle.putString("phoneNumber", "1234");
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


    /**
     * Looks for ...
     *
     */
    private void listContacts() {

        Log.d(this.LOG_TAG, "listContacts");

        mContacts = mAdapterDatabase.selectContacts();
        this.mAdapterContact = new AdapterContact(this, mContacts);
        ListView listContacts = (ListView)findViewById(R.id.listView);
        listContacts.setAdapter(this.mAdapterContact);
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
