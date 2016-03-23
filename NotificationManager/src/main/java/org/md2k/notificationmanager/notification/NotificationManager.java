package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.application.Application;
import org.md2k.datakitapi.source.application.ApplicationBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.NotificationAcknowledge;
import org.md2k.utilities.data_format.NotificationRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

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
public class NotificationManager {
    private static final String TAG = NotificationManager.class.getSimpleName();
    Context context;
    private static NotificationManager instance = null;
    HashMap<String, Notification> notificationHashMap;
    DataSourceClient dataSourceClientAcknowledge;
    ArrayList<DataSourceClient> dataSourceClientRequests;

    public static NotificationManager getInstance(Context context) {
        if (instance == null) instance = new NotificationManager(context);
        return instance;
    }


    void prepareNotificationHashMap() {
        notificationHashMap = new HashMap<>();
        notificationHashMap.put(MicrosoftBandMessage.class.getSimpleName(), new MicrosoftBandMessage(context, callback));
        notificationHashMap.put(MicrosoftBandVibrate.class.getSimpleName(), new MicrosoftBandVibrate(context, callback));
        notificationHashMap.put(PhoneTone.class.getSimpleName(), new PhoneTone(context, callback));
        notificationHashMap.put(PhoneVibrate.class.getSimpleName(), new PhoneVibrate(context, callback));
        notificationHashMap.put(PhoneScreen.class.getSimpleName(), new PhoneScreen(context, callback));
        notificationHashMap.put(PhoneMessage.class.getSimpleName(), new PhoneMessage(context, callback));
        notificationHashMap.put(PhoneNotification.class.getSimpleName(), new PhoneNotification(context, callback));
    }

    Callback callback = new Callback() {
        @Override
        public void onResponse(NotificationRequest notificationRequest, String status) {
            Log.d(TAG, "onResponse=" + status);
            stopAll();
            if (status.equals(NotificationAcknowledge.DELAY)) {
                notificationHashMap.get(PhoneNotification.class.getSimpleName()).start(notificationRequest);
            }
            insertToDataKit(notificationRequest, status);
        }
    };

    void insertToDataKit(NotificationRequest notificationRequest, String status) {
        Gson gson = new Gson();
        NotificationAcknowledge notificationAcknowledge = new NotificationAcknowledge();
        notificationAcknowledge.setNotificationRequest(notificationRequest);
        notificationAcknowledge.setStatus(status);
        DataTypeString dataTypeString = new DataTypeString(DateTime.getDateTime(), gson.toJson(notificationAcknowledge));
        DataKitAPI.getInstance(context).insert(dataSourceClientAcknowledge, dataTypeString);
    }


    public void stopAll() {
        for (HashMap.Entry<String, Notification> entry : notificationHashMap.entrySet()) {
            entry.getValue().stop();
        }
    }

    Handler handlerSubscribe;
    Runnable runnableSubscribe = new Runnable() {
        @Override
        public void run() {
            Application application = new ApplicationBuilder().setId("org.md2k.ema_scheduler").build();
            DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST).setApplication(application);
            dataSourceClientRequests = DataKitAPI.getInstance(context).find(dataSourceBuilder);
            Log.d(TAG, "DataSourceClients...size=" + dataSourceClientRequests.size());
            if (dataSourceClientRequests.size() == 0) {
                handlerSubscribe.postDelayed(this, 1000);
            } else {
                for (int i = 0; i < dataSourceClientRequests.size(); i++) {
                    DataKitAPI.getInstance(context).subscribe(dataSourceClientRequests.get(i), new OnReceiveListener() {
                        @Override
                        public void onReceived(DataType dataType) {
                            DataTypeString dataTypeString = (DataTypeString) dataType;
                            Log.d(TAG, "dataTypeString=" + dataTypeString.getSample());
                            Gson gson = new Gson();
                            Type collectionType = new TypeToken<NotificationRequest>() {
                            }.getType();
                            NotificationRequest notificationRequest = gson.fromJson(dataTypeString.getSample(), collectionType);
                            String notificationString = getNotificationString(notificationRequest);
                            if (notificationString != null) {
                                notificationHashMap.get(notificationString).start(notificationRequest);
                            }
                        }
                    });
                }

            }
        }
    };

    NotificationManager(Context context) {
        Log.d(TAG, "Constructor()");
        this.context = context;
        prepareNotificationHashMap();
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_ACKNOWLEDGE);
        dataSourceClientAcknowledge = DataKitAPI.getInstance(context).register(dataSourceBuilder);
        handlerSubscribe = new Handler();
        handlerSubscribe.post(runnableSubscribe);
    }

    String getNotificationString(NotificationRequest notificationRequest) {
        switch (notificationRequest.getDatasource().getPlatform().getType()) {
            case PlatformType.MICROSOFT_BAND:
                switch (notificationRequest.getType()) {
                    case NotificationRequest.VIBRATION:
                        return MicrosoftBandVibrate.class.getSimpleName();
                    case NotificationRequest.MESSAGE:
                        return MicrosoftBandMessage.class.getSimpleName();
                    default:
                        return null;
                }
            case PlatformType.PHONE:
                switch (notificationRequest.getType()) {
                    case NotificationRequest.VIBRATION:
                        return PhoneVibrate.class.getSimpleName();
                    case NotificationRequest.TONE:
                        return PhoneTone.class.getSimpleName();
                    case NotificationRequest.SCREEN:
                        return PhoneScreen.class.getSimpleName();
                    case NotificationRequest.MESSAGE:
                        return PhoneMessage.class.getSimpleName();
                    case NotificationRequest.NOTIFICATION:
                        return PhoneNotification.class.getSimpleName();
                    default:
                        return null;
                }
        }
        return null;
    }

    public void clear() {
//        cancelAlert();
//        notifications.clear();
        handlerSubscribe.removeCallbacks(runnableSubscribe);
        for (int i = 0; i < dataSourceClientRequests.size(); i++)
            DataKitAPI.getInstance(context).unsubscribe(dataSourceClientRequests.get(i));
        DataKitAPI.getInstance(context).unregister(dataSourceClientAcknowledge);
        instance = null;
    }
 /*   public void setNotificationManager(NotificationConfig notificationConfig){
        clear();
        this.notificationConfig=notificationConfig;
        for(int i=0;i<notificationConfig.getNotification_Request().size();i++){
            addNotifier(notificationConfig.getNotification_Request().get(i));
        }
    }
    private void addNotifier(NotificationRequest notificationRequest){
        switch(notificationRequest.getNotification().getDataSource().getPlatform().getType()){
            case PlatformType.PHONE:
                NotifierPhone notifierPhone=new NotifierPhone(context,notificationRequest);
                notifications.add(notifierPhone);
                break;
            case PlatformType.MICROSOFT_BAND:
                MicrosoftBandVibrate microsoftBandVibrate =new MicrosoftBandVibrate(context, notificationRequest,dataSourceClient);
                notifications.add(microsoftBandVibrate);
                break;
        }
    }
    int getPriorityAvailable(){
        Collections.sort(notifications, Notifier.Comparators.PRIORITY);
        for(int i=0;i< notifications.size();i++)
            if(notifications.get(i).isAvailable()){
                return notifications.get(i).notificationRequest.getPriority();
            }
        return -1;
    }
    public void alert(){
        int priority=getPriorityAvailable();
        for(int i=0;i< notifications.size();i++)
            if(notifications.get(i).isAvailable() && notifications.get(i).notificationRequest.getPriority()==priority){
//                Log.d(TAG,"send notification: "+notifications.get(i).notificationRequest.getNotification().getSource().getPlatform_type()+" "+notifications.get(i).notificationRequest.getNotification().getSource().getLocation());
                notifications.get(i).alert();
            }
    }
    public void cancelAlert(){
        Log.d(TAG, "CancelAlert()");
        int priority=getPriorityAvailable();
        for(int i=0;i< notifications.size();i++)
            if(notifications.get(i).isAvailable() && notifications.get(i).notificationRequest.getPriority()==priority){
                notifications.get(i).cancelAlert();
            }

    }
    */
}
