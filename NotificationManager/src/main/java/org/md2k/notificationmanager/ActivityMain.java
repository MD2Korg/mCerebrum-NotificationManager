package org.md2k.notificationmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.md2k.notificationmanager.configuration.NotificationConfig;
import org.md2k.notificationmanager.configuration.NotificationConfigManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    ArrayList<NotificationConfig> notificationConfigs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(getApplicationContext(), ServiceNotificationManager.class);
        startService(intent);
        notificationConfigs = NotificationConfigManager.getInstance(getApplicationContext()).getNotificationConfig();
        addButtons();
    }
    void addButtons() {
        if(notificationConfigs==null) return;
        for (int i = 0; i < notificationConfigs.size(); i++) {
            Button myButton = new Button(this);
            myButton.setText(notificationConfigs.get(i).getDisplay_name());
            LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout_buttons);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);
            final int finalI = i;
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ServiceNotificationManager.class);
                    Log.d(TAG, "name=" + NotificationConfig.class.getSimpleName());
                    intent.putExtra(NotificationConfig.class.getSimpleName(),notificationConfigs.get(finalI));
                    startService(intent);
                }
            });
        }
    }
}
