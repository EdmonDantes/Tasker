package ru.liveproduction.tasker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ApplicationLL.LOG_TAG, "MainActivity -> onCreate()");

        startService(new Intent(this, AlarmManagerService.class));
        setContentView(R.layout.activity_main);

        final Context context = this;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onUpdateItem(intent.getStringExtra("objectName"));
            }
        };


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if ( ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).getNotificationChannel(ApplicationLL.NOTIFICATION_CHANEL) == null) {
                NotificationChannel channel = channel = new NotificationChannel(ApplicationLL.NOTIFICATION_CHANEL, getResources().getString(R.string.textNotificationChanelName), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(getResources().getString(R.string.textNotificationChanelDescription));
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{200});
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            }
        }

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new CustomAdapterLL(this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TaskLL taskLL = (TaskLL) adapterView.getAdapter().getItem(i);
                Intent intent = new Intent(context, EditTaskActivity.class);
                intent.putExtra("objectName", taskLL.getName().toLowerCase());
                intent.putExtra("startMain", true);
                ((MainActivity) context).startActivityForResult(intent, 0);
            }
        });

    }

    public void onUpdateItem(String name) {
        if (name != null) {
            TaskLL taskLL = ApplicationLL.manager.use(name);
            if (taskLL != null)
                ((CustomAdapterLL) ((ListView) findViewById(R.id.listView)).getAdapter()).update(taskLL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(ApplicationLL.LOG_TAG, "MainActivity -> onStart()");
        if (broadcastReceiver != null)
            registerReceiver(broadcastReceiver, new IntentFilter("ru.liveproduction.tasker.update.intent.UpdateTask"));

        for (String tmp : ApplicationLL.manager.getNeedToUpdate()) {
            onUpdateItem(tmp);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(ApplicationLL.LOG_TAG, "MainActivity -> onStop()");
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addTaskButton) {
            Intent in = new Intent(this, EditTaskActivity.class);
            startActivityForResult(in, 0);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case 0:
                    ((CustomAdapterLL) ((ListView) findViewById(R.id.listView)).getAdapter()).update(ApplicationLL.manager.get(data.getStringExtra("objectName")));
                    break;
            }
        } else if (resultCode == -2) {
            ((CustomAdapterLL) ((ListView) findViewById(R.id.listView)).getAdapter()).remove(data.getStringExtra("objectName"));
        } else if (resultCode == -3){
            onUpdateItem(data.getStringExtra("objectName"));
        }
    }

    @Override
    protected void onDestroy() {
        ApplicationLL.manager.save(this);
        Log.d(ApplicationLL.LOG_TAG, "MainActivity -> onDestroy()");
        super.onDestroy();
    }
}
