package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Vibrator;

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
    Handler handler;
    int beepCount;
    int totalBeepCount;
    int totalPromptTime;
    int promptTime;
    public static final int PROMPT_INTERVAL=500;

    NotifierPhone(Context context, int totalBeepCount, int totalPromptTime) {
        super(context);
        this.totalBeepCount=totalBeepCount;
        this.totalPromptTime=totalPromptTime;
        handler = new Handler();
        beepCount = 0;
    }
    public boolean isAvailable() {
        return true;
    }

    private Runnable setAlarm = new Runnable() {
        public void run() {
            handler.post(promptUser);
            promptTime =0;
            beepCount++;
            if (beepCount < notification.duration/notification.interval)
                handler.postDelayed(this, notification.interval * 1000);
        }
    };
    private Runnable promptUser = new Runnable() {
        public void run() {
            if(promptTime<totalPromptTime){
                promptTime++;
                prompt();
                handler.postDelayed(this, PROMPT_INTERVAL);
            }
        }
    };
    private void prompt() {
        Vibrator vibrator;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_DTMF, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 500);
        tone.release();
    }
    public void alert() {
        handler.post(setAlarm);
    }
    public void cancelAlert(){
        handler.removeCallbacks(promptUser);
        handler.removeCallbacks(setAlarm);
    }
}
