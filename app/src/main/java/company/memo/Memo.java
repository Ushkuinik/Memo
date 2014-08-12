package company.memo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class Memo {
    private long mId;
    private String mNumber;
    private String mBody;
    private Date mDate = null;
    private Calendar mCalendar = Calendar.getInstance();


    Memo(long _id, String _number, String _body, String _timestamp) {
        this.mId = _id;
        this.mNumber = _number;
        this.mBody = _body;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            this.mDate = format.parse(_timestamp);
            this.mCalendar.setTime(mDate);

        }
        catch(ParseException e) {
            e.printStackTrace();
        }
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
        return mCalendar.get(Calendar.DAY_OF_MONTH) + " / " + mCalendar.get(Calendar.MONTH);
    }
    public String getTime() {
        return mCalendar.get(Calendar.HOUR_OF_DAY) + ":" + mCalendar.get(Calendar.MINUTE);
    }


    public String getBody() {
        return mBody;
    }


    public String getNumber() {
        return mNumber;
    }
}
