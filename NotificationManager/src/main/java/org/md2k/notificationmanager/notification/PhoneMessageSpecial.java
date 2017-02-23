package org.md2k.notificationmanager.notification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.md2k.utilities.Report.Log;
import org.md2k.utilities.data_format.notification.NotificationRequest;
import org.md2k.utilities.data_format.notification.NotificationResponse;

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
    android.app.AlertDialog alertDialog;
    int selected;
    //private MaterialDialog materialDialog;

    PhoneMessageSpecial(Context context, Callback1 callback) {
        super(context, callback);
        handler = new Handler();
    }

    private void showAlertDialog() {
        Log.d(TAG, "show_alert_dialog...");
        String[] options = new String[notificationRequest.getMessage().length - 3];
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


        alertDialogSingleChoice(context, notificationRequest.getMessage()[1], options, 0, "Ok", null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==-1)
                    callback.onResponse(notificationRequest, null);
                else callback.onResponse(notificationRequest, notificationRequest.getMessage()[which+3]);
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
    public void alertDialogSingleChoice(final Context context, String title, String[] strings, int curSelected, String positive, String negative,final DialogInterface.OnClickListener onClickListener){
        selected=curSelected;
        alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, org.md2k.utilities.R.style.app_theme_teal_light_dialog))
                .setTitle(title)
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog,-1);
                    }
                })
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setSingleChoiceItems(strings, curSelected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected=which;
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickListener.onClick(null, selected);
                if(alertDialog!=null) alertDialog.dismiss();
            }
        });
        AlertDialogStyle(context, alertDialog);
    }
    private void AlertDialogStyle(Context context, android.app.AlertDialog alertDialog){
        int titleDividerId = alertDialog.getContext().getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = alertDialog.getWindow().getDecorView().findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(ContextCompat.getColor(context, org.md2k.utilities.R.color.deeporange_100));
            titleDivider.setVisibility(View.VISIBLE);
        }
        int textViewId = alertDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) alertDialog.findViewById(textViewId);
        tv.setTextColor(ContextCompat.getColor(context, org.md2k.utilities.R.color.teal_700));

        int textViewMsgId = alertDialog.getContext().getResources().getIdentifier("android:id/message", null, null);
        TextView tvMsg = (TextView) alertDialog.findViewById(textViewMsgId);
        if(tvMsg!=null) {
            tvMsg.setTextSize(16);
            tvMsg.setGravity(Gravity.CENTER);
            tvMsg.setTextColor(ContextCompat.getColor(context, org.md2k.utilities.R.color.black));
        }
        Button b = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if(b != null){
            b.setTextColor(ContextCompat.getColor(context, org.md2k.utilities.R.color.teal_700));
            b.setTypeface(null, Typeface.BOLD);
        }
        b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(b != null){
            b.setTextColor(ContextCompat.getColor(context, org.md2k.utilities.R.color.teal_700));
            b.setTypeface(null, Typeface.BOLD);
        }
        b = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if(b != null){
            b.setTextColor(ContextCompat.getColor(context, org.md2k.utilities.R.color.teal_700));
            b.setTypeface(null, Typeface.BOLD);
        }

    }

}
