package ru.liveproduction.tasker;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Map;
import java.util.TreeMap;

public class TaskLLManager {

    private TreeMap<String, TaskLL> data = new TreeMap<>();
    private boolean saved = false;

    public TaskLLManager(){}

    public TaskLL get(String name) {
        return data.get(name.toLowerCase());
    }

    public void update(TaskLL taskLL) {
        data.put(taskLL.getName().toLowerCase(), taskLL);
        saved = false;
    }

    public void remove(TaskLL taskLL){
        data.remove(taskLL.getName().toLowerCase());
        saved = false;
    }

    public void remove(String name) {
        data.remove(name);
        saved = false;
    }

    public Map<String, TaskLL> getAll(){
        return data;
    }

    public void save(Context context) {
        if (!saved) {
            SharedPreferences.Editor pref = context.getSharedPreferences(ApplicationLL.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE).edit();
            Gson gson = new Gson();
            pref.clear();
            for (TreeMap.Entry<String, TaskLL> tmp : data.entrySet()) {
                pref.putString(tmp.getKey(), gson.toJson(tmp.getValue()));
            }
            pref.apply();
        }
        saved = true;
    }

    public void load(Context context) {
        SharedPreferences pref = context.getSharedPreferences(ApplicationLL.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        for (Map.Entry<String, ?> tmp : pref.getAll().entrySet()) {
            data.put(tmp.getKey().toLowerCase(), gson.fromJson((String) tmp.getValue(), TaskLL.class));
        }

    }

}
