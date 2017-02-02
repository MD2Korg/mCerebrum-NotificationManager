package org.md2k.notificationmanager.notification;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;

import org.md2k.utilities.Report.Log;
import org.md2k.utilities.UI.AlertDialogs;
import org.md2k.utilities.data_format.notification.NotificationRequest;
import org.md2k.utilities.data_format.notification.NotificationResponse;
import org.md2k.utilities.dialog.Dialog;
import org.md2k.utilities.dialog.DialogCallback;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
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
class PhoneMessageSpecial extends Notification {
    private static final String TAG = PhoneMessageSpecial.class.getSimpleName();
    private Handler handler;
    AlertDialog alertDialog;
    //private MaterialDialog materialDialog;

    PhoneMessageSpecial(Context context, Callback1 callback) {
        super(context, callback);
        handler = new Handler();
    }

    private void showAlertDialog() {
        Log.d(TAG, "show_alert_dialog...");
        final String[] options = new String[notificationRequest.getMessage().length - 3];
        System.arraycopy(notificationRequest.getMessage(), 3, options, 0, notificationRequest.getMessage().length - 3);
/*
        materialDialog = new Dialog().SingleChoice(context.getApplicationContext(), notificationRequest.getMessage()[0], notificationRequest.getMessage()[1], options, options[0], new String[]{"Ok"}, null, new DialogCallback() {
            @Override
            public void onDialogCallback(Dialog.DialogResponse which, String[] result) {
                if (which == Dialog.DialogResponse.POSITIVE)
                    callback.onResponse(notificationRequest, result[0]);
            }
        }).show();
*/

        AlertDialogs.AlertDialogSingleChoice(context, notificationRequest.getMessage()[1], options, 0, "Ok", null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onResponse(notificationRequest, options[which]);
            }
        });
    }

    @Override
    public void start(NotificationRequest notificationRequest) {
        this.notificationRequest = notificationRequest;
        showAlertDialog();
        handler.postDelayed(runnableStop, notificationRequest.getDuration());
    }

    private Runnable runnableStop = new Runnable() {
        @Override
        public void run() {
            callback.onResponse(notificationRequest, NotificationResponse.TIMEOUT);
            stop();
        }
    };

    @Override
    public void stop() {
        Log.d(TAG, "PhoneMessage..stop()");
/*
        if (materialDialog != null) {
            materialDialog.dismiss();
            materialDialog = null;
        }
*/
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        handler.removeCallbacks(runnableStop);
    }
}
