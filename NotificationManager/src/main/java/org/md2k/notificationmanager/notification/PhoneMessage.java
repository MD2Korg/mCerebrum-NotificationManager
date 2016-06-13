package org.md2k.notificationmanager.notification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.view.WindowManager;

import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequest;
import org.md2k.utilities.data_format.notification.NotificationResponse;

/**
 * Created by monowar on 3/13/16.
 */
public class PhoneMessage extends Notification {
    private static final String TAG = PhoneMessage.class.getSimpleName();
    int count;
    Handler handler;
    AlertDialog alertDialog;
    AlertDialog alertDialogCancel;
    AlertDialog alertDialogDelayOptions;

    PhoneMessage(Context context, Callback1 callback) {
        super(context, callback);
        handler = new Handler();
    }

    void showAlertDialogCancel(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Cancel " + msg);
        alertDialogBuilder.setMessage("Are you sure you want to cancel?");
        alertDialogBuilder.setIcon(ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_error_red_50dp));
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onResponse(notificationRequest, NotificationResponse.CANCEL);
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showAlertDialog();
                alertDialogCancel.dismiss();
            }
        });
        alertDialogCancel = alertDialogBuilder.create();
        alertDialogCancel.setCancelable(false);
        alertDialogCancel.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialogCancel.show();
    }

    void showAlertDialog() {
        int msgNo = 0;
        Log.d(TAG,"show_alert_dialog...");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
        alertDialogBuilder.setTitle(notificationRequest.getMessage()[msgNo++]).setMessage(notificationRequest.getMessage()[msgNo++]);
        alertDialogBuilder.setIcon(ContextCompat.getDrawable(context, org.md2k.utilities.R.drawable.ic_warning_grey_50dp));

        if (notificationRequest.getResponse_option().isOk()) {
            alertDialogBuilder.setNegativeButton(notificationRequest.getMessage()[msgNo++], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    callback.onResponse(notificationRequest, NotificationResponse.OK);
                }
            });
        }
        if (notificationRequest.getResponse_option().isCancel()) {
            alertDialogBuilder.setNeutralButton(notificationRequest.getMessage()[msgNo++], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAlertDialogCancel(notificationRequest.getMessage()[0]);
                }
            });
        }

        if (notificationRequest.getResponse_option().isDelay()) {
            alertDialogBuilder.setPositiveButton(notificationRequest.getMessage()[msgNo], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (notificationRequest.getDuration() == -1) {
                        showAlertDialogDelayOption();
                    } else
                        callback.onResponse(notificationRequest, NotificationResponse.DELAY);
                }
            });
        }
        alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    void showAlertDialogDelayOption() {
        final CharSequence[] items = {"15 Minutes", "30 Minutes", "1 Hour", "2 Hours"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Delay Option");
        alertDialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        notificationRequest.setDuration(900000);
                        callback.onResponse(notificationRequest, NotificationResponse.DELAY);
                        break;
                    case 1:
                        notificationRequest.setDuration(1800000);
                        callback.onResponse(notificationRequest, NotificationResponse.DELAY);
                        break;
                    case 2:
                        notificationRequest.setDuration(3600000);
                        callback.onResponse(notificationRequest, NotificationResponse.DELAY);
                        break;
                    case 3:
                        notificationRequest.setDuration(7200000);
                        callback.onResponse(notificationRequest, NotificationResponse.DELAY);
                        break;
                }
                alertDialogDelayOptions.dismiss();
            }
        });
        alertDialogDelayOptions = alertDialogBuilder.create();
        alertDialogDelayOptions.setCancelable(false);
        alertDialogDelayOptions.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialogDelayOptions.show();
    }

    @Override
    public void start(NotificationRequest notificationRequest) {
        count = 0;
        this.notificationRequest = notificationRequest;
        showAlertDialog();
        handler.postDelayed(runnableStop, notificationRequest.getDuration());
    }

    Runnable runnableStop = new Runnable() {
        @Override
        public void run() {
            callback.onResponse(notificationRequest, NotificationResponse.TIMEOUT);
            stop();
        }
    };

    @Override
    public void stop() {
        Log.d(TAG, "PhoneMessage..stop()");
        if (alertDialog != null)
            alertDialog.dismiss();
        if (alertDialogCancel != null)
            alertDialogCancel.dismiss();
        handler.removeCallbacks(runnableStop);
    }
}
