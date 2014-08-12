package company.memo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class ActivityMemoList extends ActionBarActivity {

    private AdapterDatabase mAdapterDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);
        Intent intent = getIntent();
        String number = intent.getStringExtra("phoneNumber");

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();
        mAdapterDatabase.logMemos();

        ArrayList<Memo> memos = mAdapterDatabase.getMemos(number);
        AdapterMemo adapterMemo = new AdapterMemo(this, memos);
        ListView listContacts = (ListView)findViewById(R.id.listView);
        listContacts.setAdapter(adapterMemo);

    }


    /**
     * Destroy all fragments and loaders.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapterDatabase.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_memo_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
