package org.md2k.notificationmanager.notification;

import android.content.Context;

import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.utilities.data_format.NotificationRequest;


/**
 * Created by monowar on 3/13/16.
 */
public abstract class Notification {
    NotificationRequest notificationRequest;
    Context context;
    Callback1 callback;
    Notification(Context context, Callback1 callback){
        this.context=context;this.callback = callback;
    }
    public abstract void start(NotificationRequest notificationRequest) throws DataKitException;
    public abstract void stop();
}
