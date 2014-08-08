package company.memo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import wei.mark.standout.StandOutWindow;


public class ActivityMain extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                bundle.putString("phoneNumber", "123");
                StandOutWindow.sendData(this, TopWindow.class, StandOutWindow.DEFAULT_ID, CallDetectService.GOT_PHONE_NUMBER, bundle, null, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
