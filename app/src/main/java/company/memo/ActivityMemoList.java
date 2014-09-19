package company.memo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;

import java.util.ArrayList;

public class ActivityMemoList extends ActionBarActivity implements OnDismissCallback {

    private final String LOG_TAG = "ActivityMemoList";

    private AdapterDatabase mAdapterDatabase;
    private ArrayList<Memo> mMemos;
    //private AdapterMemo     mAdapterMemo;
    private AdapterCard     mAdapterCard;

    private ListView mMemoView;
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ActivityMain.CUSTOM_MEMO_EVENT)) {
                long id = intent.getLongExtra("memoId", 0);
                if(id != 0) {
                    Memo memo = getMemoById(id);
                    if(memo != null) {
                        int action = intent.getIntExtra("action", ActivityMain.ACTION_MEMO_UNDEFINED);
                        switch(action) {
                            case ActivityMain.ACTION_MEMO_ADDED:
                                Log.d(LOG_TAG, "ACTION_MEMO_ADDED");
                                mMemos.add(memo);
                                break;
                            case ActivityMain.ACTION_MEMO_DELETED:
                                Log.d(LOG_TAG, "ACTION_MEMO_DELETED");
                                mMemos.remove(memo);
                                break;
                            case ActivityMain.ACTION_MEMO_CHANGED:
                                Log.d(LOG_TAG, "ACTION_MEMO_CHANGED");
                                String body = intent.getStringExtra("memoBody");
                                String title = intent.getStringExtra("memoTitle");
                                memo.setBody(body);
                                memo.setTitle(title);
                                break;
                            case ActivityMain.ACTION_ATTACHMENT_ADDED:
                                Log.d(LOG_TAG, "ACTION_ATTACHMENT_ADDED");
                                memo.setAttachmentCount(memo.getAttachmentCount() + 1);
                                break;
                            case ActivityMain.ACTION_ATTACHMENT_DELETED:
                                Log.d(LOG_TAG, "ACTION_ATTACHMENT_DELETED");
                                memo.setAttachmentCount(memo.getAttachmentCount() - 1);
                                break;
                        }
                        //mAdapterMemo.notifyDataSetChanged();
                        mAdapterCard.notifyDataSetChanged();
                    }
                }
            }
        }
    };


    private Memo getMemoById(long _id) {
        for(Memo a : mMemos) {
            if(a.getId() == _id) {
                return a;
            }
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list2);

        Intent intent = getIntent();
        String number = intent.getStringExtra("phoneNumber");

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();

        mMemos = mAdapterDatabase.getMemos(number);
        mAdapterCard = new AdapterCard(this, mMemos);

        mMemoView = (ListView) findViewById(R.id.activity_googlecards_listview);


        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter =
                new SwingBottomInAnimationAdapter(new SwipeDismissAdapter(mAdapterCard, this));
        swingBottomInAnimationAdapter.setAbsListView(mMemoView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);

        mMemoView.setAdapter(swingBottomInAnimationAdapter);
        mMemoView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(LOG_TAG, "Click detected, position: " + position + " id:" + id + " tag: " + view.getTag().toString());

                long memo_id = ((Long) view.getTag()).longValue();
                Intent intent = new Intent(getBaseContext(), ActivityEditMemo.class);
                intent.putExtra("memoId", memo_id);
                startActivity(intent);
            }
        });

        IntentFilter filter = new IntentFilter(ActivityMain.CUSTOM_MEMO_EVENT);
        registerReceiver(mReceiver, filter);

/*
        setContentView(R.layout.activity_memo_list);
        Intent intent = getIntent();
        String number = intent.getStringExtra("phoneNumber");

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();

        mMemos = mAdapterDatabase.getMemos(number);
        mAdapterMemo = new AdapterMemo(this, mMemos);

        mMemoView = (ListView) findViewById(R.id.listView);
        mMemoView.setAdapter(mAdapterMemo);

        mMemoView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(LOG_TAG, "Click detected, position: " + position + " id:" + id + " tag: " + view.getTag().toString());

                long memo_id = ((Long) view.getTag()).longValue();
                Intent intent = new Intent(getBaseContext(), ActivityEditMemo.class);
                intent.putExtra("memoId", memo_id);
                startActivity(intent);
            }
        });

        registerForContextMenu(mMemoView);
        mMemoView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(LOG_TAG, "Long click detected, position: " + position + " id:" + id + " tag: " + view.getTag().toString());
//                long memo_id = ((Long) view.getTag()).longValue();
//                mAdapterDatabase.deleteMemo(memo_id);
//                mAdapterMemo.remove(mAdapterMemo.getMemo(position));
                return false;
            }
        });

        IntentFilter filter = new IntentFilter(ActivityMain.CUSTOM_MEMO_EVENT);
        registerReceiver(mReceiver, filter);
*/
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
        getMenuInflater().inflate(R.menu.activity_memo_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_memo, menu);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.action_delete:
                Log.d(LOG_TAG, "Delete item, " + " id: " + info.id);// + " tag: " + view.getTag().toString());
                int position = (int) info.id;
                Memo memo = (Memo) mMemoView.getItemAtPosition(position);
//                long memo_id = memo.getId();
//                Log.d(LOG_TAG, "Delete item, " + " id: " + memo_id);// + " tag: " + view.getTag().toString());
//                Memo m = mAdapterMemo.getMemo(position);
//                Log.d(LOG_TAG, "Delete item, " + " id: " + m.getId());// + " tag: " + view.getTag().toString());
                Toast.makeText(this, "Deleted item: " + memo.getId(), Toast.LENGTH_SHORT).show();
                mAdapterDatabase.deleteMemo(memo.getId());
                //mAdapterMemo.remove(memo);
                mAdapterCard.remove(memo);

                Intent i = new Intent();
                i.setAction(ActivityMain.CUSTOM_MEMO_EVENT);
                i.putExtra("phoneNumber", memo.getNumber());
                i.putExtra("action", ActivityMain.ACTION_MEMO_DELETED);
                getApplicationContext().sendBroadcast(i);


                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public void onDismiss(ViewGroup viewGroup, int[] ints) {
        Log.d(LOG_TAG, "ids: " + ints);
        for (int position : ints) {
            Log.d(LOG_TAG, "p: " + position);
            Memo memo = mAdapterCard.getItem(position);
            mAdapterDatabase.deleteMemo(memo.getId());
            mAdapterCard.remove(memo);

            Intent i = new Intent();
            i.setAction(ActivityMain.CUSTOM_MEMO_EVENT);
            i.putExtra("phoneNumber", memo.getNumber());
            i.putExtra("action", ActivityMain.ACTION_MEMO_DELETED);
            getApplicationContext().sendBroadcast(i);
        }
    }
}
