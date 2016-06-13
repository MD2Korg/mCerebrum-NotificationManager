package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import org.md2k.notificationmanager.Constants;
import org.md2k.utilities.data_format.notification.NotificationRequest;

import java.io.IOException;

/**
 * Created by monowar on 3/13/16.
 */
public class PhoneTone extends Notification {
    Handler handler;
    int count;
    MediaPlayer mPlayer;

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
                play();
                handler.postDelayed(this, notificationRequest.getDuration() / notificationRequest.getRepeat());
            }
        }
    };

    @Override
    public void stop() {
        try {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
            }
            mPlayer=null;
        }catch (Exception ignored){

        }
        handler.removeCallbacks(runnableTone);
    }
    private void play(){
        try {
            mPlayer = new MediaPlayer();
            Uri myUri = Uri.parse(Constants.CONFIG_DIRECTORY+"tone.mp3");
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(context, myUri);
            mPlayer.prepare();
            mPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

/*    private void tone() {
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
*/
}
