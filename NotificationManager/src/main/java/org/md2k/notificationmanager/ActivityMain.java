package org.md2k.notificationmanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.md2k.datakitapi.time.DateTime;
import org.md2k.utilities.Apps;
import org.md2k.utilities.UI.ActivityAbout;
import org.md2k.utilities.UI.ActivityCopyright;

public class ActivityMain extends AppCompatActivity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button buttonService = (Button) findViewById(R.id.button_app_status);
        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMain.this, ServiceNotificationManager.class);
                if (Apps.isServiceRunning(getBaseContext(), "org.md2k.notificationmanager.ServiceNotificationManager")) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_about:
                intent = new Intent(this, ActivityAbout.class);
                try {
                    intent.putExtra(org.md2k.utilities.Constants.VERSION_CODE, String.valueOf(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode));
                    intent.putExtra(org.md2k.utilities.Constants.VERSION_NAME, this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                break;
            case R.id.action_copyright:
                intent = new Intent(this, ActivityCopyright.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            {
                long time = Apps.serviceRunningTime(ActivityMain.this, "org.md2k.notificationmanager.ServiceNotificationManager");
                if (time < 0) {
                    ((Button) findViewById(R.id.button_app_status)).setText("START");
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_off));

                } else {
                    findViewById(R.id.button_app_status).setBackground(ContextCompat.getDrawable(ActivityMain.this, R.drawable.button_status_on));
                    ((Button) findViewById(R.id.button_app_status)).setText(DateTime.convertTimestampToTimeStr(time));

                }
                mHandler.postDelayed(this, 1000);
            }
        }
    };
    @Override
    public void onResume() {
        mHandler.post(runnable);
        super.onResume();
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(runnable);
        super.onPause();
    }

}
