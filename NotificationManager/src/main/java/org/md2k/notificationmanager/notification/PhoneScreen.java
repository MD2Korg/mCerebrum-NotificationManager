package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;

import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequest;

/**
 * Created by monowar on 3/13/16.
 */
public class PhoneScreen extends Notification {
    private static final String TAG = PhoneScreen.class.getSimpleName();
    int count;
    Handler handler;
    PowerManager.WakeLock wl=null;

    PhoneScreen(Context context, Callback1 callback) {
        super(context, callback);
        handler=new Handler();
    }

    @Override
    public void start(NotificationRequest notificationRequest) {
        count = 0;
        this.notificationRequest=notificationRequest;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();
        Log.d(TAG, "phonescreen...duration="+notificationRequest.getDuration());
        handler.postDelayed(runnableStop, notificationRequest.getDuration());
    }
    Runnable runnableStop=new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    @Override
    public void stop() {
        Log.d(TAG,"PhoneScreen..stop()");
        if(wl!=null)
            wl.release();
        wl=null;
        handler.removeCallbacks(runnableStop);
    }
}
