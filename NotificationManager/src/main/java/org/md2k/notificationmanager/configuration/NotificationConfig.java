package org.md2k.notificationmanager.configuration;


import android.os.Parcel;
import android.os.Parcelable;

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
public class NotificationConfig implements Parcelable{
    String name;
    String type;
    String display_name;
    String package_name;
    String file_name;
    Timeout timeout;

    ArrayList<NotificationOption> notification_option;


    protected NotificationConfig(Parcel in) {
        name = in.readString();
        type = in.readString();
        display_name = in.readString();
        package_name = in.readString();
        file_name = in.readString();
        timeout = in.readParcelable(Timeout.class.getClassLoader());
        notification_option = in.createTypedArrayList(NotificationOption.CREATOR);
    }

    public static final Creator<NotificationConfig> CREATOR = new Creator<NotificationConfig>() {
        @Override
        public NotificationConfig createFromParcel(Parcel in) {
            return new NotificationConfig(in);
        }

        @Override
        public NotificationConfig[] newArray(int size) {
            return new NotificationConfig[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public ArrayList<NotificationOption> getNotification_option() {
        return notification_option;
    }

    public String getFile_name() {
        return file_name;
    }


    public String getType() {
        return type;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(display_name);
        dest.writeString(package_name);
        dest.writeString(file_name);
        dest.writeParcelable(timeout, flags);
        dest.writeTypedList(notification_option);
    }
}
