package ru.liveproduction.tasker;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class ApplicationLL extends Application {

    public static final String LOG_TAG = "LIVeProduction: Tasker";
    public static final String PREFERENCE_FILE_NAME = "ru.liveproduction.tasker.TasksDB.v.0.1.alpha";
    public static final String NOTIFICATION_CHANEL = "ru.liveproduction.tasker.notification.canal.1";
    public static final String GROUP_ID = "ru.liveproduction.tasker.notification.group.1";
    public static final TaskLLManager manager = new TaskLLManager();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ApplicationLL.LOG_TAG, "ApplicationLL -> onCreate()");
        manager.load(this);
        startService(new Intent(this, AlarmManagerService.class));
    }

    @Override
    public void onTerminate() {
        manager.save(this);
        Log.d(ApplicationLL.LOG_TAG, "ApplicationLL -> onTerminate()");
        super.onTerminate();
    }

}
