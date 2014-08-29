package company.memo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityMemoList extends ActionBarActivity {

    private final String LOG_TAG = "ActivityMemoList";

    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION  = 300;

    private AdapterDatabase mAdapterDatabase;
    private AdapterMemo     mAdapterMemo;
    private HashMap<Long, Integer> mItemIdTopMap  = new HashMap<Long, Integer>();

    private boolean mSwiping = false;
    private boolean mItemPressed = false;


    /*
    private float mDownX;
    private float mDownY;
    private final float SCROLL_THRESHOLD = 10;
    private boolean isOnClick;


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                isOnClick = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isOnClick) {
                    Log.i(LOG_TAG, "onClick ");
                    Toast.makeText(getApplicationContext(), "click detected", Toast.LENGTH_LONG).show();
                    //TODO onClick code
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isOnClick && (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD)) {
                    Log.i(LOG_TAG, "movement detected");
                    Toast.makeText(getApplicationContext(), "movement detected", Toast.LENGTH_LONG).show();
                    isOnClick = false;
                }
                break;
            default:
                break;
        }
        return mGestureDetector.onTouchEvent(ev);
    }
*/
    private ListView mMemoView;
    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        float mDownX;
        private int mSwipeSlop = -1;


        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if(mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(ActivityMemoList.this).
                        getScaledTouchSlop();
            }
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(mItemPressed) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    mItemPressed = true;
                    mDownX = event.getX();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    mItemPressed = false;
                    break;
                case MotionEvent.ACTION_MOVE: {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    if(!mSwiping) {
                        if(deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            mMemoView.requestDisallowInterceptTouchEvent(true);
//                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if(mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                }
                break;
                case MotionEvent.ACTION_UP: {
                    // User let go - figure out whether to animate the view out, or back into place
                    if(mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if(deltaXAbs > v.getWidth() / 4) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            remove = true;

                            long memo_id = ((Long) v.getTag()).longValue();
                            mAdapterDatabase.deleteMemo(memo_id);
                        }
                        else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                            remove = false;
                        }
                        // Animate position and alpha of swiped item
                        // NOTE: This is a simplified version of swipe behavior, for the
                        // purposes of this demo about animation. A real version should use
                        // velocity (via the VelocityTracker class) to send the item off or
                        // back at an appropriate speed.
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        mMemoView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                 withEndAction(new Runnable() {
                                     @Override
                                     public void run() {
                                         // Restore animated values
                                         v.setAlpha(1);
                                         v.setTranslationX(0);
                                         if(remove) {
                                             animateRemoval(mMemoView, v);
                                         }
                                         else {
//                                             mBackgroundContainer.hideBackground();
                                             mSwiping = false;
                                             mMemoView.setEnabled(true);
                                         }
                                     }
                                 });
                    }
                }
                mItemPressed = false;
                break;
                default:
                    return false;
            }
            return true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_list);
        Intent intent = getIntent();
        String number = intent.getStringExtra("phoneNumber");

        mAdapterDatabase = new AdapterDatabase(this);
        mAdapterDatabase.open();
//        mAdapterDatabase.logMemos();

        ArrayList<Memo> memos = mAdapterDatabase.getMemos(number);
//        mAdapterMemo = new AdapterMemo(this, memos, mTouchListener);
        mAdapterMemo = new AdapterMemo(this, memos, null);

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
    }


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
        if(id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when a context menu for the {@code view} is about to be shown.
     * Unlike {@link #onCreateOptionsMenu(android.view.Menu)}, this will be called every
     * time the context menu is about to be shown and should be populated for
     * the view (or item inside the view for {@link android.widget.AdapterView} subclasses,
     * this can be found in the {@code menuInfo})).
     * <p/>
     * Use {@link #onContextItemSelected(android.view.MenuItem)} to know when an
     * item has been selected.
     * <p/>
     * It is not safe to hold onto the context menu after this method returns.
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_memo, menu);
    }


    /**
     * This hook is called whenever an item in a context menu is selected. The
     * default implementation simply returns false to have the normal processing
     * happen (calling the item's Runnable or sending a message to its Handler
     * as appropriate). You can use this method for any items for which you
     * would like to do processing without those other facilities.
     * <p/>
     * Use {@link android.view.MenuItem#getMenuInfo()} to get extra information set by the
     * View that added this menu item.
     * <p/>
     * Derived classes should call through to the base class for it to perform
     * the default menu handling.
     *
     * @param item The context menu item that was selected.
     * @return boolean Return false to allow normal context menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                Log.d(LOG_TAG, "Delete item, " + " id: " + info.id);// + " tag: " + view.getTag().toString());
                int position = (int)info.id;
                Memo memo = (Memo)mMemoView.getItemAtPosition(position);
//                long memo_id = memo.getId();
//                Log.d(LOG_TAG, "Delete item, " + " id: " + memo_id);// + " tag: " + view.getTag().toString());
//                Memo m = mAdapterMemo.getMemo(position);
//                Log.d(LOG_TAG, "Delete item, " + " id: " + m.getId());// + " tag: " + view.getTag().toString());
                Toast.makeText(this, "Deleted item: " + memo.getId(), Toast.LENGTH_SHORT).show();
                mAdapterDatabase.deleteMemo(memo.getId());
                mAdapterMemo.remove(memo);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for(int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if(child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = mAdapterMemo.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = mMemoView.getPositionForView(viewToRemove);
        mAdapterMemo.remove(mAdapterMemo.getItem(position));

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for(int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapterMemo.getItemId(position);
                    Integer startTop = mItemIdTopMap.get(itemId);
                    int top = child.getTop();
                    if(startTop != null) {
                        if(startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if(firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
//                                        mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        mMemoView.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    }
                    else {
                        // Animate new views along with the others. The catch is that they did not
                        // exist in the start state, so we must calculate their starting position
                        // based on neighboring views.
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        startTop = top + (i > 0 ? childHeight : -childHeight);
                        int delta = startTop - top;
                        child.setTranslationY(delta);
                        child.animate().setDuration(MOVE_DURATION).translationY(0);
                        if(firstAnimation) {
                            child.animate().withEndAction(new Runnable() {
                                public void run() {
//                                    mBackgroundContainer.hideBackground();
                                    mSwiping = false;
                                    mMemoView.setEnabled(true);
                                }
                            });
                            firstAnimation = false;
                        }
                    }
                }
                mItemIdTopMap.clear();
                return true;
            }
        });
    }
}
