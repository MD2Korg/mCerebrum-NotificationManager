package org.md2k.notificationmanager;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Toast;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.messagehandler.OnExceptionListener;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.status.Status;
import org.md2k.notificationmanager.configuration.NotificationConfig;
import org.md2k.notificationmanager.configuration.NotificationConfigManager;
import org.md2k.notificationmanager.configuration.NotificationOption;
import org.md2k.notificationmanager.notification.NotificationManager;
import org.md2k.utilities.Report.Log;

import java.util.UUID;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ServiceNotificationManager extends Service {
    private static final String TAG = ServiceNotificationManager.class.getSimpleName();
    DataKitAPI dataKitAPI;
    NotificationManager notificationManager;
    NotificationConfigManager notificationConfigManager;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        notificationConfigManager = NotificationConfigManager.getInstance(getApplicationContext());
        if(notificationConfigManager.getNotificationConfig()==null){
            notificationConfigManager.clear();
            Toast.makeText(getApplicationContext(), "!!!Error: Notification Configuration file not available...", Toast.LENGTH_LONG).show();
            stopSelf();
        } else {
            connectDataKit();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()...");
        if (intent!=null && intent.hasExtra(NotificationConfig.class.getSimpleName())) {
            Log.d(TAG, "onStartCommand()...yes");
            NotificationConfig notificationConfig = (NotificationConfig) intent.getParcelableExtra(NotificationConfig.class.getSimpleName());
//            createNotification(notificationConfig);
            notificationManager.setNotificationManager(notificationConfig);
            notificationManager.alert();
            showAlertDialogShowNotification(notificationConfig);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /*    void showAlertDialogConfiguration(final Context context){
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Error: Configuration File")
                    .setIcon(R.drawable.ic_error_outline_white_24dp)
                    .setMessage("Phone Sensor is not configured.\n\n Please go to Menu -> Settings (or, click Settings below)")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(context, ActivityPhoneSensorSettings.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();

            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
    */    //            UIShow.ErrorDialog(getApplicationContext(), "DataKit Error", "DataKit is not available.\n\nPlease Install DataKit");

    private void connectDataKit() {
        Log.d(TAG, "connectDataKit()...");
        dataKitAPI = DataKitAPI.getInstance(getApplicationContext());
        dataKitAPI.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
                Toast.makeText(getApplicationContext(), "Notification Manager started Successfully", Toast.LENGTH_LONG).show();
                notificationManager = NotificationManager.getInstance(ServiceNotificationManager.this);

            }
        }, new OnExceptionListener() {
            @Override
            public void onException(Status status) {
                android.util.Log.d(TAG, "onException...");
                Toast.makeText(ServiceNotificationManager.this, "Notification Managr.. Stopped. Error: " + status.getStatusMessage(), Toast.LENGTH_LONG).show();
                stopSelf();
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()...");
        if (dataKitAPI != null && dataKitAPI.isConnected()) dataKitAPI.disconnect();
        if (dataKitAPI != null)
            dataKitAPI.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
/*
    public void createNotification(NotificationConfig notificationConfig) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = getPackageManager().getLaunchIntentForPackage(notificationConfig.getPackage_name());
        intent.setAction(notificationConfig.getPackage_name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle(notificationConfig.getNotification_option().getAlert_header())
                .setContentText(notificationConfig.getAlert_text()).setSmallIcon(R.drawable.ic_notification_survey)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        noti.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(0, noti);
    }

    public void cancelNotification() {
        android.app.NotificationManager nMgr = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nMgr.cancel(0);
    }
*/
    NotificationOption find(NotificationConfig notificationConfig){
        for(int i=0;i<notificationConfig.getNotification_option().size();i++){
            if(PlatformType.PHONE.equals(notificationConfig.getNotification_option().get(i).getNotification().getDataSource().getPlatform().getType()))
                return notificationConfig.getNotification_option().get(i);
        }
        return null;
    }
    void showAlertDialogShowNotification(final NotificationConfig notificationConfig) {
        NotificationOption notificationOption=find(notificationConfig);
        if(notificationOption==null) return;
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(notificationOption.getNotification().getMessage()[0])
                .setIcon(R.drawable.ic_notification_survey)
                .setMessage(notificationOption.getNotification().getMessage()[1])
                .setPositiveButton("Start Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notificationManager.cancelAlert();
                        Intent intent = getPackageManager().getLaunchIntentForPackage(notificationConfig.getPackage_name());
                        intent.setAction(notificationConfig.getPackage_name());
                        intent.putExtra("id", UUID.randomUUID().toString());
                        intent.putExtra("name", notificationConfig.getName());
                        intent.putExtra("display_name", notificationConfig.getDisplay_name());
                        intent.putExtra("file_name", notificationConfig.getFile_name());
                        intent.putExtra("timeout", notificationConfig.getTimeout().getCompletion_timeout());
                        Log.d(TAG, "name=" + notificationConfig.getName() + " timeout=" + notificationConfig.getTimeout().getCompletion_timeout());
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationManager.getInstance(getBaseContext()).clear();
                    }
                })
                .create();

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
}
