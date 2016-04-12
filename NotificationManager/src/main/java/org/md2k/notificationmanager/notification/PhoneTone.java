package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;

import org.md2k.utilities.data_format.NotificationRequest;

/**
 * Created by monowar on 3/13/16.
 */
public class PhoneTone extends Notification {
    Handler handler;
    int count;

    PhoneTone(Context context, Callback1 callback) {
        super(context, callback);
        handler = new Handler();
    }

    @Override
    public void start(NotificationRequest notificationRequest) {
        count = 0;
        this.notificationRequest=notificationRequest;
        handler.post(runnableTone);

    }

    Runnable runnableTone = new Runnable() {
        @Override
        public void run() {
            if (count < notificationRequest.getRepeat()) {
                count++;
                tone();
                handler.postDelayed(this, notificationRequest.getDuration() / notificationRequest.getRepeat());
            }
        }
    };

    @Override
    public void stop() {
        handler.removeCallbacks(runnableTone);
    }

    private void tone() {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        am.setStreamVolume(
                AudioManager.STREAM_DTMF,
                am.getStreamMaxVolume(AudioManager.STREAM_DTMF),
                0);
        ToneGenerator tone = new ToneGenerator(android.media.AudioManager.STREAM_DTMF, 100);
        tone.startTone(ToneGenerator.TONE_PROP_BEEP);
        tone.release();
    }

}
