package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Vibrator;

import org.md2k.notificationmanager.configuration.NotificationOption;
import org.md2k.utilities.Report.Log;

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
public class NotifierPhone extends Notifier {
    private static final String TAG = NotifierPhone.class.getSimpleName() ;
    Handler handler;
    int curToneCount;
    int curVibrateCount;

    NotifierPhone(Context context, NotificationOption notificationOption) {
        super(context, notificationOption);
        handler = new Handler();
        curToneCount = 0;
        curVibrateCount=0;
    }

    public boolean isAvailable() {
        return true;
    }

    private Runnable setAlarmTone = new Runnable() {
        public void run() {
            if (curToneCount <= notificationOption.getNotification().getTone_count()) {
                curToneCount++;
                tone();
                handler.postDelayed(this, notificationOption.getNotification().getTone_interval() * 1000);
            }
        }
    };
    private Runnable setAlarmVibrate = new Runnable() {
        public void run() {
            if (curVibrateCount <= notificationOption.getNotification().getVibrate_count()) {
                curVibrateCount++;
                vibrate();
                handler.postDelayed(this, notificationOption.getNotification().getVibrate_interval() * 1000);
            }
        }
    };

    private void vibrate() {
        Vibrator vibrator;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    private void tone() {
        ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_DTMF, 100);
        tone.startTone(ToneGenerator.TONE_PROP_PROMPT);
        tone.release();
    }

    public void alert() {
        handler.post(setAlarmTone);
        handler.post(setAlarmVibrate);
    }

    public void cancelAlert() {
        Log.d(TAG, "cancelAlert()");
        handler.removeCallbacks(setAlarmVibrate);
        handler.removeCallbacks(setAlarmTone);
    }

    @Override
    public int compareTo(Notifier notifier) {
        return 0;
    }

}
