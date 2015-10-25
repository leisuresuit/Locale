package android.app;

/**
 * "Fake" interface to call Android internal class w/o using reflection
 */
public abstract class ActivityManagerNative implements IActivityManager {
    public static IActivityManager getDefault(){
        return null;
    }
}