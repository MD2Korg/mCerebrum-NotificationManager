package org.md2k.notificationmanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnConnectionListener;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.notificationmanager.notification.NotificationManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.Report.LogStorage;

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
    public static final String STATUS = "STATUS";
    public static final String INTENT_NAME = ServiceNotificationManager.class.getSimpleName();
    private static final String TAG = ServiceNotificationManager.class.getSimpleName();
    DataKitAPI dataKitAPI;
    NotificationManager notificationManager;
    private boolean isStopping;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(STATUS);
            if (message.equals("STOP")) {
                Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",broadcast_receiver_stop_service");
                clear();
                stopSelf();
            }
            Log.d("receiver", "Got message: " + message);
        }
    };

    public void onCreate() {
        super.onCreate();
        isStopping = false;
        LogStorage.startLogFileStorageProcess(getApplicationContext().getPackageName());
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_start");
        Log.d(TAG, "onCreate()");
        try {
            notificationManager=null;
            connectDataKit();
        } catch (DataKitException e) {
            clear();
            stopSelf();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(INTENT_NAME));
    }

    private synchronized void connectDataKit() throws DataKitException {
        Log.d(TAG, "connectDataKit()...");
        dataKitAPI = DataKitAPI.getInstance(this);
        dataKitAPI.connect(new OnConnectionListener() {
            @Override
            public void onConnected() {
//                Toast.makeText(getApplicationContext(), "Notification Manager started Successfully", Toast.LENGTH_LONG).show();
                notificationManager = new NotificationManager(ServiceNotificationManager.this);
            }
        });
    }

    private synchronized void clear() {
        if (isStopping) return;
        isStopping = true;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Log.w(TAG, "time=" + DateTime.convertTimeStampToDateTime(DateTime.getDateTime()) + ",timestamp=" + DateTime.getDateTime() + ",service_stop");
        Log.d(TAG, "onDestroy()...");
        if(notificationManager!=null)
            notificationManager.clear();
        if (dataKitAPI != null && dataKitAPI.isConnected()) dataKitAPI.disconnect();

    }

    @Override
    public void onDestroy() {
        clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
