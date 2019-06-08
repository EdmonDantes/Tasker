package ru.liveproduction.tasker;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class TaskLLManager {

    public static final int UPDATE_POLICY_NOT_CHANGE = 0;
    public static final int UPDATE_POLICY_UPDATE = 1;
    public static final int UPDATE_POLICY_REMOVE = 2;

    private final TreeMap<String, TaskLL> data = new TreeMap<>();
    private final TreeMap<String, Integer> needUpdate = new TreeMap<>();
    private final boolean[] saved = new boolean[]{false};

    public TaskLLManager() {}

    public TaskLL get(String name) {
        if (name != null && name.length() > 0) {
            String id = name.toLowerCase();
            synchronized (data) {
                return data.get(id);
            }
        }
        return null;
    }

    public boolean update(TaskLL taskLL) {
        if (taskLL != null && taskLL.getName() != null && taskLL.getName().length() > 0) {
            String name = taskLL.getName().toLowerCase();
            if (!taskLL.equals(get(name))) {

                synchronized (data) {
                    data.put(name, taskLL);

                    synchronized (needUpdate) {

                        needUpdate.put(name, UPDATE_POLICY_UPDATE);
                        synchronized (saved) {
                            saved[0] = false;
                        }
                        return true;
                    }

                }

            }
        }
        return false;
    }

    public TaskLL use(String name) {
        if (name != null && name.length() > 0) {
            String id = name.toLowerCase();

            Integer updateState = 0;

            synchronized (needUpdate) {
                updateState = needUpdate.get(id);
            }

            switch (updateState) {
                case UPDATE_POLICY_REMOVE:
                    synchronized (needUpdate) {
                        needUpdate.remove(id);
                    }

                    return null;

                default:
                    synchronized (needUpdate) {
                        needUpdate.put(id, UPDATE_POLICY_NOT_CHANGE);
                    }

                    synchronized (data) {
                        return data.get(id);
                    }
            }
        }
        return null;
    }

    public boolean remove(String name) {
        if (name != null && name.length() > 0) {
            String id = name.toLowerCase();
            synchronized (data) {
                data.remove(id);
            }

            synchronized (needUpdate) {
                needUpdate.put(id, UPDATE_POLICY_REMOVE);
            }

            synchronized (saved) {
                saved[0] = false;
            }

            return true;
        }

        return false;
    }

    public LinkedList<String> getNeedToUpdate() {
        LinkedList<String> result = new LinkedList<>();
        Set<Map.Entry<String, Integer>> tmp = null;
        synchronized (needUpdate) {
            tmp = needUpdate.entrySet();
        }

        for (Map.Entry<String, Integer> obj : tmp) {
            if (obj.getValue() > 0)
                result.add(obj.getKey());
        }
        return result;
    }

    public void save(Context context) {

        synchronized (saved) {
            if (!saved[0]) {
                SharedPreferences.Editor pref = context.getSharedPreferences(ApplicationLL.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE).edit();
                Gson gson = new Gson();
                pref.clear();
                synchronized (data) {
                    for (TreeMap.Entry<String, TaskLL> tmp : data.entrySet()) {
                        pref.putString(tmp.getKey(), gson.toJson(tmp.getValue()));
                    }
                }
                pref.apply();
                saved[0] = true;
            }
        }
    }

    public void load(Context context) {
        synchronized (data) {
            SharedPreferences pref = context.getSharedPreferences(ApplicationLL.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            for (Map.Entry<String, ?> tmp : pref.getAll().entrySet()) {
                String id = tmp.getKey().toLowerCase();
                TaskLL task = gson.fromJson((String) tmp.getValue(), TaskLL.class);
                data.put(id, task);
            }
        }
    }

    public interface OnGetTask{
        public void onGet(TaskLL task);
    }

    public void forEach(OnGetTask func) {

        Set<Map.Entry<String, TaskLL>> set = null;
        synchronized (data) {
            set = data.entrySet();
        }

        for (Map.Entry<String, TaskLL> tmp : set) {
            func.onGet(tmp.getValue());
        }
    }

}
