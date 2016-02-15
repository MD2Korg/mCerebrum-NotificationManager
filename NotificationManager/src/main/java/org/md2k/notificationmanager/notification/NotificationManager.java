package org.md2k.notificationmanager.notification;

import android.content.Context;

import org.md2k.datakitapi.DataKitAPI;
import org.md2k.datakitapi.source.datasource.DataSourceBuilder;
import org.md2k.datakitapi.source.datasource.DataSourceClient;
import org.md2k.datakitapi.source.datasource.DataSourceType;
import org.md2k.datakitapi.source.platform.PlatformType;
import org.md2k.notificationmanager.configuration.NotificationConfig;
import org.md2k.notificationmanager.configuration.NotificationOption;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.Collections;

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
    ArrayList<Notifier> notifiers;
    NotificationConfig notificationConfig;
    Context context;
    DataSourceClient dataSourceClient;
    private static NotificationManager instance=null;
    public static NotificationManager getInstance(Context context){
        if(instance==null) instance=new NotificationManager(context);
        return instance;
    }
    NotificationManager(Context context){
        Log.d(TAG,"Constructor()");
        this.context=context;
        notifiers=new ArrayList<>();
        DataSourceBuilder dataSourceBuilder=new DataSourceBuilder().setType(DataSourceType.NOTIFICATION);
        dataSourceClient= DataKitAPI.getInstance(context).register(dataSourceBuilder);
        Log.d(TAG,"ds_id="+dataSourceClient.getDs_id());
    }
    public void clear(){
        cancelAlert();
        notifiers.clear();
    }
    public void setNotificationManager(NotificationConfig notificationConfig){
        clear();
        this.notificationConfig=notificationConfig;
        for(int i=0;i<notificationConfig.getNotification_option().size();i++){
            addNotifier(notificationConfig.getNotification_option().get(i));
        }
    }
    private void addNotifier(NotificationOption notificationOption){
        switch(notificationOption.getNotification().getDataSource().getPlatform().getType()){
            case PlatformType.PHONE:
                NotifierPhone notifierPhone=new NotifierPhone(context,notificationOption);
                notifiers.add(notifierPhone);
                break;
            case PlatformType.MICROSOFT_BAND:
                NotifierMicrosoftBand notifierMicrosoftBand=new NotifierMicrosoftBand(context, notificationOption,dataSourceClient);
                notifiers.add(notifierMicrosoftBand);
                break;
        }
    }
    int getPriorityAvailable(){
        Collections.sort(notifiers, Notifier.Comparators.PRIORITY);
        for(int i=0;i<notifiers.size();i++)
            if(notifiers.get(i).isAvailable()){
                return notifiers.get(i).notificationOption.getPriority();
            }
        return -1;
    }
    public void alert(){
        int priority=getPriorityAvailable();
        for(int i=0;i<notifiers.size();i++)
            if(notifiers.get(i).isAvailable() && notifiers.get(i).notificationOption.getPriority()==priority){
//                Log.d(TAG,"send notification: "+notifiers.get(i).notificationOption.getNotification().getSource().getPlatform_type()+" "+notifiers.get(i).notificationOption.getNotification().getSource().getLocation());
                notifiers.get(i).alert();
            }
    }
    public void cancelAlert(){
        Log.d(TAG, "CancelAlert()");
        int priority=getPriorityAvailable();
        for(int i=0;i<notifiers.size();i++)
            if(notifiers.get(i).isAvailable() && notifiers.get(i).notificationOption.getPriority()==priority){
                notifiers.get(i).cancelAlert();
            }

    }
}
