package org.md2k.notificationmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class ActivityInterventionApps extends Activity {
    List<ItemObject> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervention_apps);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        List<ItemObject> allItems = getAllItemObject();
        CustomAdapterInterventionApps customAdapterInterventionApps = new CustomAdapterInterventionApps(this, allItems);
        gridview.setAdapter(customAdapterInterventionApps);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(items.get(position).getPackageName());
                startActivity(launchIntent);
//                Toast.makeText(MainActivity.this, "Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<ItemObject> getAllItemObject() {

        items.add(new ItemObject("Mood Surfing", "org.md2k.moodsurfing"));
        items.add(new ItemObject("Thought Shakeup", "org.md2k.thoughtshakeup"));
        items.add(new ItemObject("Head Space", "com.getsomeheadspace.android"));
   //     items.add(new ItemObject("Aeon", null));
        return items;
    }
}
