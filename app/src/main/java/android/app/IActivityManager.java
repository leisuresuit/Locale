package android.app;

import android.content.res.Configuration;
import android.os.RemoteException;

/**
 * "Fake" interface to call Android internal class w/o using reflection
 */
public interface IActivityManager {
    public abstract Configuration getConfiguration () throws RemoteException;
    public abstract void updateConfiguration (Configuration configuration) throws RemoteException;
}