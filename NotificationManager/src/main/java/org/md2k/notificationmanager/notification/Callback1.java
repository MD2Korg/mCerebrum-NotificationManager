package org.md2k.notificationmanager.notification;


import org.md2k.utilities.data_format.notification.NotificationRequest;

import java.io.Serializable;

/**
 * Created by monowar on 3/13/16.
 */
public interface Callback1 extends Serializable{
    void onResponse(NotificationRequest notificationRequest, String status);
}
