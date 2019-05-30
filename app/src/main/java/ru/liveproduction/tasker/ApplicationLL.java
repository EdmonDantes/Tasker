package ru.liveproduction.tasker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ApplicationLL extends Application {

    public static final String PREFERENCE_FILE_NAME = "ru.liveproduction.tasker.TasksDB.v.0.1.alpha";
    public static final String NOTIFICATION_CHANEL = "ru.liveproduction.tasker.notification.canal.1";
    public static final TaskLLManager manager = new TaskLLManager();

    @Override
    public void onCreate() {
        super.onCreate();
        manager.load(this);
    }

    @Override
    public void onTerminate() {
        manager.save(this);
        super.onTerminate();
    }

    public static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
    }

}
