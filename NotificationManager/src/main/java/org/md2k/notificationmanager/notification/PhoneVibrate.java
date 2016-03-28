package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;

import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationRequest;

/**
 * Created by monowar on 3/13/16.
 */
public class PhoneVibrate extends Notification {
    private static final String TAG = PhoneVibrate.class.getSimpleName();
    Handler handler;
    int count;

    PhoneVibrate(Context context, Callback1 callback) {
        super(context, callback);
        handler = new Handler();
    }

    @Override
    public void start(NotificationRequest notificationRequest) {
        Log.d(TAG, "phoneVibrate...stop()");
        count = 0;
        this.notificationRequest=notificationRequest;
        handler.post(runnableVibrate);

    }

    Runnable runnableVibrate = new Runnable() {
        @Override
        public void run() {
            if (count < notificationRequest.getRepeat()) {
                count++;
                vibrate();
                handler.postDelayed(this, notificationRequest.getDuration() / notificationRequest.getRepeat());
            }
        }
    };

    @Override
    public void stop() {
        Log.d(TAG, "phoneVibrate...stop()");
        handler.removeCallbacks(runnableVibrate);
    }

    private void vibrate() {
        Vibrator vibrator;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

}
