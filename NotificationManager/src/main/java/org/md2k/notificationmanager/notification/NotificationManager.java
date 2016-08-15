package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataType;
import org.md2k.datakitapi.datatype.DataTypeJSONObject;
import org.md2k.datakitapi.exception.DataKitException;
import org.md2k.datakitapi.messagehandler.OnReceiveListener;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.notificationmanager.ServiceNotificationManager;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequest;
import org.md2k.utilities.data_format.notification.NotificationRequests;
import org.md2k.utilities.data_format.notification.NotificationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
    HashMap<String, Notification> notificationHashMap;
    HashSet<Integer> hashSetSubscribed;
    DataSourceClient dataSourceClientAcknowledge;
    DataSourceClient dataSourceClientResponse;
    ArrayList<DataSourceClient> dataSourceClientRequests;
    Handler handlerSubscribe;
    Callback1 callback = new Callback1() {
        @Override
        public void onResponse(NotificationRequest notificationRequest, String status) {
            try {
                Log.d(TAG, "onResponse=" + status);
                stopAll();
                if (status.equals(NotificationResponse.DELAY)) {
                    Log.d(TAG, "notification ack=DELAY");
                    if (notificationRequest.getResponse_action().getType().equals(NotificationRequest.MESSAGE))
                        notificationHashMap.get(PhoneMessage.class.getSimpleName()).start(notificationRequest.getResponse_action());
//                    if (notificationRequest.getResponse_action().getType().equals("NOTIFICATION"))
                    notificationHashMap.get(PhoneNotification.class.getSimpleName()).start(notificationRequest);

                }
                insertToDataKit(notificationRequest, status);
            } catch (DataKitException e) {
                stopService();
            }
        }
    };
    int RETRY = 60;
    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            try {
                Log.d(TAG, "notification request...onReceive...()..");
                DataTypeJSONObject dataTypeJSONObject = message.getData().getParcelable(DataTypeJSONObject.class.getSimpleName());
                DataKitAPI.getInstance(context).insert(dataSourceClientAcknowledge, dataTypeJSONObject);
                stopAll();
                Gson gson = new Gson();
                assert dataTypeJSONObject != null;
                NotificationRequests notificationRequests = gson.fromJson(dataTypeJSONObject.getSample().toString(), NotificationRequests.class);

                for (int i = 0; i < notificationRequests.getNotification_option().size(); i++) {
                    String notificationString = getNotificationString(notificationRequests.getNotification_option().get(i));
                    if (notificationString != null) {
                        notificationHashMap.get(notificationString).start(notificationRequests.getNotification_option().get(i));
                    }
                }
            } catch (DataKitException e) {
                Log.e(TAG, "Exception: DataKitException..." + e.getMessage());
                stopService();
            }

        }
    };
    Runnable runnableSubscribe = new Runnable() {
        @Override
        public void run() {
            try {
                DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_REQUEST);
                dataSourceClientRequests = DataKitAPI.getInstance(context).find(dataSourceBuilder);
                for (int i = 0; i < dataSourceClientRequests.size(); i++) {
                    if (dataSourceClientRequests.get(i).getDataSource().getApplication().getId().equals("org.md2k.notificationmanager"))
                        continue;
                    int ds_id = dataSourceClientRequests.get(i).getDs_id();
                    if (hashSetSubscribed.contains(ds_id)) continue;
                    hashSetSubscribed.add(ds_id);
                    DataKitAPI.getInstance(context).subscribe(dataSourceClientRequests.get(i), new OnReceiveListener() {
                        @Override
                        public void onReceived(final DataType dataType) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "notification request...received...");
                                    DataTypeJSONObject dataTypeJSONObject = (DataTypeJSONObject) dataType;
                                    Message msg = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable(DataTypeJSONObject.class.getSimpleName(), dataTypeJSONObject);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                }
                            }).start();
                        }
                    });
                }
                if (RETRY > 0) {
                    RETRY--;
                    handlerSubscribe.postDelayed(this, 5000);
                }
            } catch (DataKitException e) {
                stopService();
            }
        }
    };

    public NotificationManager(Context context) {
        try {
            Log.d(TAG, "Constructor()");
            this.context = context;
            prepareNotificationHashMap();
            hashSetSubscribed = new HashSet<>();
            DataSourceBuilder dataSourceBuilderA = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_ACKNOWLEDGE);
            dataSourceClientAcknowledge = DataKitAPI.getInstance(context).register(dataSourceBuilderA);
            DataSourceBuilder dataSourceBuilderR = new DataSourceBuilder().setType(DataSourceType.NOTIFICATION_RESPONSE);
            dataSourceClientResponse = DataKitAPI.getInstance(context).register(dataSourceBuilderR);
            handlerSubscribe = new Handler();
            handlerSubscribe.post(runnableSubscribe);
        } catch (DataKitException e) {
            stopService();
        }
    }

    void prepareNotificationHashMap() throws DataKitException {
        notificationHashMap = new HashMap<>();
        notificationHashMap.put(MicrosoftBandMessage.class.getSimpleName(), new MicrosoftBandMessage(context, callback));
        notificationHashMap.put(MicrosoftBandVibrate.class.getSimpleName(), new MicrosoftBandVibrate(context, callback));
        notificationHashMap.put(PhoneTone.class.getSimpleName(), new PhoneTone(context, callback));
        notificationHashMap.put(PhoneVibrate.class.getSimpleName(), new PhoneVibrate(context, callback));
        notificationHashMap.put(PhoneScreen.class.getSimpleName(), new PhoneScreen(context, callback));
        notificationHashMap.put(PhoneMessage.class.getSimpleName(), new PhoneMessage(context, callback));
        notificationHashMap.put(PhoneNotification.class.getSimpleName(), new PhoneNotification(context, callback));
    }

    void insertToDataKit(NotificationRequest notificationRequest, String status) throws DataKitException {
        Gson gson = new Gson();
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setNotificationRequest(notificationRequest);
        notificationResponse.setStatus(status);
        JsonObject sample = new JsonParser().parse(gson.toJson(notificationResponse)).getAsJsonObject();
        DataTypeJSONObject dataTypeJSONObject = new DataTypeJSONObject(DateTime.getDateTime(), sample);
        DataKitAPI.getInstance(context).insert(dataSourceClientResponse, dataTypeJSONObject);
    }

    public void stopAll() {
        for (HashMap.Entry<String, Notification> entry : notificationHashMap.entrySet()) {
            entry.getValue().stop();
        }
    }

    void stopService() {
        Intent intent = new Intent(ServiceNotificationManager.INTENT_NAME);
        // You can also include some extra data.
        intent.putExtra(ServiceNotificationManager.STATUS, "STOP");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

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
        try {
            stopAll();
            handlerSubscribe.removeCallbacks(runnableSubscribe);
            for (int i = 0; dataSourceClientRequests != null && i < dataSourceClientRequests.size(); i++)
                DataKitAPI.getInstance(context).unsubscribe(dataSourceClientRequests.get(i));
            if (dataSourceClientAcknowledge != null)
                DataKitAPI.getInstance(context).unregister(dataSourceClientAcknowledge);
            if (dataSourceClientResponse != null)
                DataKitAPI.getInstance(context).unregister(dataSourceClientResponse);
            hashSetSubscribed.clear();
        } catch (Exception ignored) {
        }
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
