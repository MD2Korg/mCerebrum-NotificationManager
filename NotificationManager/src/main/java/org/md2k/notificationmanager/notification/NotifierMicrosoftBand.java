package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.os.Handler;

import com.google.gson.Gson;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.datatype.DataTypeString;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.time.DateTime;
import org.md2k.notificationmanager.configuration.NotificationOption;
import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.Notification;

import java.util.ArrayList;

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
public class NotifierMicrosoftBand extends Notifier {
    private static final String TAG = NotifierMicrosoftBand.class.getSimpleName() ;
    Context context;
    DataSourceClient dataSourceClient;
    Handler handler;
    int curVibrateCount;

    NotifierMicrosoftBand(Context context, NotificationOption notificationOption, DataSourceClient dataSourceClient){
        super(context, notificationOption);
        curVibrateCount=0;
        handler=new Handler();
        this.dataSourceClient=dataSourceClient;
    }
    boolean isAvailable(){
        //TODO: check whether MSBand available
        return true;
/*        if(dataKitHandler.isConnected()){
            DataSourceBuilder dataSourceBuilder=new DataSourceBuilder().setType(DataSourceType.BAND_CONTACT);
            ArrayList<DataSourceClient> dataSourceClientArrayList=dataKitHandler.find(dataSourceBuilder);
            for(int i=0;i<dataSourceClientArrayList.size();i++){
                if(dataSourceClientArrayList.get(i).getStatus().getStatusCode()== StatusCodes.DATASOURCE_ACTIVE){
                    return true;
                }
            }
        }
        return false;
*/    }
    int gerVibrationType(String vibrationType){
        switch(vibrationType){
            case "NOTIFICATION_ONE_TONE": return Notification.VIBRATION.NOTIFICATION_ONE_TONE;
            case "NOTIFICATION_TWO_TONE": return Notification.VIBRATION.NOTIFICATION_TWO_TONE;
            case "NOTIFICATION_ALARM": return Notification.VIBRATION.NOTIFICATION_ALARM;
            case "NOTIFICATION_TIMER": return Notification.VIBRATION.NOTIFICATION_TIMER;
            case "ONE_TONE_HIGH": return Notification.VIBRATION.ONE_TONE_HIGH;
            case "TWO_TONE_HIGH": return Notification.VIBRATION.TWO_TONE_HIGH;
            case "THREE_TONE_HIGH": return Notification.VIBRATION.THREE_TONE_HIGH;
            case "RAMP_UP": return Notification.VIBRATION.RAMP_UP;
            case "RAMP_DOWN": return Notification.VIBRATION.RAMP_DOWN;
            default:return 0;
        }
    }
    private Runnable setAlarmVibrate = new Runnable() {
        public void run() {
            if (curVibrateCount <= notificationOption.getNotification().getVibrate_count()) {
                curVibrateCount++;
                vibrate();
                handler.postDelayed(this, notificationOption.getNotification().getVibrate_interval() * 1000);
            }
        }
    };
    void vibrate(){
        Gson gson=new Gson();
        Notification notification=new Notification();
        notification.setDataSource(notificationOption.getNotification().getDataSource());
        notification.setOperation(Notification.OPERATION.SEND);
        notification.setNotification_type(notificationOption.getNotification().getType());
        notification.setMessage(notificationOption.getNotification().getMessage());
        notification.setVibration_type(gerVibrationType(notificationOption.getNotification().getVibrate_type()));
        String str=gson.toJson(notification);
        Log.d(TAG, "alert()... MicrosoftBand=" + str);
        DataTypeString dataTypeString=new DataTypeString(DateTime.getDateTime(),str);
        DataKitAPI.getInstance(context).insert(dataSourceClient,dataTypeString);
    }

    @Override
    void alert() {
        handler.post(setAlarmVibrate);
    }

    @Override
    void cancelAlert() {
        handler.removeCallbacks(setAlarmVibrate);
    }

    @Override
    public int compareTo(Notifier notifier) {
        return 0;
    }
}
