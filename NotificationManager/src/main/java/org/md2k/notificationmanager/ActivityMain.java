package org.md2k.notificationmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.md2k.notificationmanager.configuration.NotificationConfig;
import org.md2k.notificationmanager.configuration.NotificationConfigManager;
import org.md2k.utilities.Report.Log;

import java.util.ArrayList;
import java.util.UUID;

public class ActivityMain extends Activity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    ArrayList<NotificationConfig> notificationConfigs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationConfigs = NotificationConfigManager.getInstance(this).getNotificationConfig();
        addButtons();
    }
    void addButtons() {
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
                    Intent intent = getPackageManager().getLaunchIntentForPackage(notificationConfigs.get(finalI).getPackage_name());
                    intent.setAction(notificationConfigs.get(finalI).getPackage_name());
                    intent.putExtra("id", UUID.randomUUID().toString());
                    intent.putExtra("name", notificationConfigs.get(finalI).getName());
                    intent.putExtra("display_name", notificationConfigs.get(finalI).getDisplay_name());
                    intent.putExtra("file_name", notificationConfigs.get(finalI).getFile_name());
                    intent.putExtra("timeout", notificationConfigs.get(finalI).getTimeout().getCompletion_timeout());
                    startActivity(intent);
                }
            });
        }
    }

}
