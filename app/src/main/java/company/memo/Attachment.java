package company.memo;

/**
 *
 *
 */
public class Attachment {
    public static int ATTACHMENT_UNDEFINED = 0;
    public static int ATTACHMENT_IMAGE     = 1;
    public static int ATTACHMENT_AUDIO     = 2;
    private long   mId;
    private int    mType;
    private long   mMemoId;
    private String mPath;


    Attachment(long _id, int _type, long _memoId, String _path) {
        mId = _id;
        mType = _type;
        mMemoId = _memoId;
        mPath = _path;
    }


    public long getId() {
        return mId;
    }


    public long getMemoId() {
        return mMemoId;
    }


    public int getType() {
        return mType;
    }


    public String getPath() {
        return mPath;
    }
}
