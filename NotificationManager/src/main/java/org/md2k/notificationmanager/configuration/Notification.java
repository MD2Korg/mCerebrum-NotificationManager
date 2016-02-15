package org.md2k.notificationmanager.configuration;

import android.os.Parcel;
import android.os.Parcelable;

import org.md2k.datakitapi.source.datasource.DataSource;

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
public class Notification implements Parcelable {
    String type;
    DataSource datasource;
    String vibrate_type;
    int vibrate_count;
    int vibrate_interval;
    String tone_type;
    int tone_count;
    int tone_interval;
    String[] message;


    protected Notification(Parcel in) {
        type = in.readString();
        datasource = in.readParcelable(DataSource.class.getClassLoader());
        vibrate_type = in.readString();
        vibrate_count = in.readInt();
        vibrate_interval = in.readInt();
        tone_type = in.readString();
        tone_count = in.readInt();
        tone_interval = in.readInt();
        message = in.createStringArray();
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public DataSource getDataSource() {
        return datasource;
    }

    public String getVibrate_type() {
        return vibrate_type;
    }

    public int getVibrate_count() {
        return vibrate_count;
    }

    public String getTone_type() {
        return tone_type;
    }

    public int getTone_count() {
        return tone_count;
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public int getVibrate_interval() {
        return vibrate_interval;
    }

    public int getTone_interval() {
        return tone_interval;
    }

    public String[] getMessage() {
        return message;
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
        dest.writeString(type);
        dest.writeParcelable(datasource, flags);
        dest.writeString(vibrate_type);
        dest.writeInt(vibrate_count);
        dest.writeInt(vibrate_interval);
        dest.writeString(tone_type);
        dest.writeInt(tone_count);
        dest.writeInt(tone_interval);
        dest.writeStringArray(message);
    }
}
