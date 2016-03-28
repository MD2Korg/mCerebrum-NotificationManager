package org.md2k.notificationmanager.notification;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationAcknowledge;
import org.md2k.utilities.data_format.NotificationRequest;

/**
 * Created by monowar on 3/13/16.
 */
public class PhoneNotification extends Notification {
    private static final String TAG = PhoneNotification.class.getSimpleName();
    public static final String ACTION_CLICKED="ACTION_CLICKED";
    int count;
    Handler handler;
    boolean isRegistered=false;
    android.app.NotificationManager mNotificationManager = null;

    PhoneNotification(Context context, Callback1 callback) {
        super(context, callback);
        handler = new Handler();
        isRegistered=false;
    }

    void showNotification() {
        Intent intent = new Intent(ACTION_CLICKED);
        intent.setAction(ACTION_CLICKED);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(org.md2k.utilities.R.drawable.ic_notification_deeporange_48dp)
                        .setContentTitle(notificationRequest.getMessage()[0])
                        .setContentText(notificationRequest.getMessage()[1]+"  (Click to start)")
                        .setContentIntent(pIntent)
                        .setOngoing(true)
                        .setAutoCancel(true);
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(9999, mBuilder.build());
    }

    @Override
    public void start(NotificationRequest notificationRequest) {
        count = 0;
        this.notificationRequest = notificationRequest;
        context.registerReceiver(receiverStartNow, new IntentFilter(ACTION_CLICKED));
        isRegistered=true;
        showNotification();
        Log.d(TAG,"duration="+notificationRequest.getDuration());
        handler.postDelayed(runnableStop, notificationRequest.getResponse_option().getDelay_time());
    }
    private BroadcastReceiver receiverStartNow = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d(TAG,"delay stopped");
            callback.onResponse(notificationRequest, NotificationAcknowledge.DELAY_CANCEL);


//            if(action.equals(NOTIFICATION_MEDIA_CHANGE_NEXT))
//                playNextSong();
//            else if(action.equals(NOTIFICATION_MEDIA_CHANGE_BACK))
//                playPreviousSong();
        }
    };
    Runnable runnableStop = new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    @Override
    public void stop() {
        Log.d(TAG, "PhoneMessage..stop()");
        if(isRegistered)
            context.unregisterReceiver(receiverStartNow);
        isRegistered=false;
        if (mNotificationManager != null)
            mNotificationManager.cancel(9999);
        mNotificationManager = null;
        handler.removeCallbacks(runnableStop);
    }
}
