package company.memo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class Memo {
    private long   mId;
    private String mNumber;
    private String mTitle;
    private String mBody;
    private boolean  mSelected = false;
    private Date     mDate     = null;
    private Calendar mCalendar = Calendar.getInstance();


    public void setAttachmentCount(int _count) {
        this.mAttachmentCount = _count;
    }


    private int mAttachmentCount;


    Memo(long _id, String _number, String _title, String _body, String _timestamp, int _count) {
        this.mId = _id;
        this.mNumber = _number;
        this.mTitle = _title;
        this.mBody = _body;
        this.mAttachmentCount = _count;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.mDate = format.parse(_timestamp);
            int tzOffset = TimeZone.getDefault().getRawOffset();
            this.mDate.setTime(this.mDate.getTime() + tzOffset);
            this.mCalendar.setTime(this.mDate);
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
    }


    public int getAttachmentCount() {
        return mAttachmentCount;
    }


    public long getId() {
        return mId;
    }


    public Date getDate() {
        return mDate;
    }


    public String getYear() {
        return mCalendar.get(Calendar.YEAR) + "";
    }


    public String getDayMonth() {
        return mCalendar.get(Calendar.DAY_OF_MONTH) + " / " + (mCalendar.get(Calendar.MONTH) + 1);
    }


    public String getTime() {
        return mCalendar.get(Calendar.HOUR_OF_DAY) + ":" + mCalendar.get(Calendar.MINUTE);
    }


    public String getBody() {
        return mBody;
    }


    public void setBody(String _body) {
        this.mBody = _body;
    }


    public String getTitle() {
        return mTitle;
    }


    public void setTitle(String _title) {
        this.mTitle = _title;
    }


    public String getDateFormatted(String _format) {
        SimpleDateFormat format = new SimpleDateFormat(_format);
        return format.format(mDate);
    }


    public String getNumber() {
        return mNumber;
    }


    public boolean isSelected() {
        return mSelected;
    }


    public void setSelected(boolean _selected) {
        this.mSelected = _selected;
//        Log.d("Memo", "Memo[" + mId + "] is set to " + mSelected);
    }


}
