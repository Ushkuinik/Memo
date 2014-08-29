package company.memo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ActivityEditMemo extends ActionBarActivity {

    private AdapterDatabase        mAdapterDatabase;
    private AdapterMemoHorizontal  mAdapterMemo;
    private HorizontalListView     mListMemos;
    private String                 mPhoneNumber;
    private long                   mMemoId;
    private TopWindow.LineEditText mMemoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_edit_memo);

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();

        mMemoText = (TopWindow.LineEditText) findViewById(R.id.body);

        Intent intent = getIntent();
        mMemoId = intent.getLongExtra("memoId", 0);
        mPhoneNumber = intent.getStringExtra("phoneNumber");
        if(mMemoId != 0) {
            Memo memo = mAdapterDatabase.getMemo(mMemoId);
            String text = memo.getBody();
            mMemoId = memo.getId();
            mPhoneNumber = memo.getNumber();
            mMemoText.setHtmlText(text);
        }
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
        getMenuInflater().inflate(R.menu.activity_edit_memo, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {

            case R.id.action_settings:
                break;

            case R.id.action_save:
                String text = mMemoText.getHtmlText();
                if(mMemoId == 0) {
                    mMemoId = mAdapterDatabase.createMemo(mPhoneNumber, text);
                }
                else {
                    mAdapterDatabase.updateMemo(mMemoId, mPhoneNumber, text);
                }
                Toast.makeText(getApplicationContext(), "Save memo: " + mMemoId, Toast.LENGTH_LONG).show();
                finish();
                break;
            case R.id.action_photo:
                Toast.makeText(getApplicationContext(), "New photo", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), ActivityCameraDummy.class);
                startActivity(intent);
                break;
            case R.id.action_calendar:
                break;
            case R.id.action_audio:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String photoPath = preferences.getString("photoPath", null);
        String thumbPath = preferences.getString("thumbPath", null);
        if(thumbPath != null) {

            mMemoText.insertImage(thumbPath);

            SharedPreferences.Editor editor = preferences.edit();
            //TODO: declare pref keys as resource strings
            editor.remove("photoPath");
            editor.remove("thumbPath");
            editor.commit();
        }

    }
}
