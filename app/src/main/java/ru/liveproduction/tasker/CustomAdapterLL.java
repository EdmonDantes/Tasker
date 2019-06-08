package ru.liveproduction.tasker;

import android.content.Context;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class CustomAdapterLL extends BaseAdapter {

    private ArrayList<Pair<String, TaskLL>> list = new ArrayList<Pair<String, TaskLL>>();
    private Context context;

    public CustomAdapterLL(Context context){
        this.context = context;

        ApplicationLL.manager.forEach(new TaskLLManager.OnGetTask() {
            @Override
            public void onGet(TaskLL tmp) {
                list.add(new Pair<String, TaskLL>(tmp.getName().toLowerCase(),tmp));
            }
        });

        Collections.sort(list, new Comparator<Pair<String, TaskLL>>() {
            @Override
            public int compare(Pair<String, TaskLL> obj0, Pair<String, TaskLL> obj1) {
                return -obj0.second.getStartTime().compareTo(obj1.second.getStartTime());
            }
        });
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i).second;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void add(TaskLL task) {
        list.add(new Pair<String, TaskLL>(task.getName().toLowerCase(), task));
        Collections.sort(list, new Comparator<Pair<String, TaskLL>>() {
            @Override
            public int compare(Pair<String, TaskLL> obj0, Pair<String, TaskLL> obj1) {
                return -obj0.second.getStartTime().compareTo(obj1.second.getStartTime());
            }
        });
        notifyDataSetChanged();
    }

    public void update(TaskLL taskLL) {
        _remove(taskLL.getName().toLowerCase());
        add(taskLL);
    }

    public void remove(TaskLL taskLL) {
        _remove(taskLL.getName().toLowerCase());
        notifyDataSetChanged();
    }

    public void remove(String name){
        if (name != null) {
            _remove(name.toLowerCase());
            notifyDataSetChanged();
        }
    }

    private void _remove(String name){
        for (Pair<String, TaskLL> tmp : list) {
            if (tmp.first.equals(name)) {
                list.remove(tmp);
                return;
            }
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View result = inflater.inflate(R.layout.adapter_list_of_tasks, null);

        TaskLL taskLL = list.get(i).second;

        if (taskLL != null) {
            ((TextView) result.findViewById(R.id.nameOfTask)).setText(taskLL.getName());
            ((TextView) result.findViewById(R.id.count)).setText(String.valueOf(taskLL.getCount()));
            if (taskLL.isRepeat()) {

                int id = -1;
                switch (taskLL.getTypeOfRepeat()) {
                    case TaskLL.INTERVALS_SECONDS:
                        id = R.plurals.second;
                        break;
                    case TaskLL.INTERVALS_MINUTE:
                        id = R.plurals.minute;
                        break;
                    case TaskLL.INTERVALS_HOUR:
                        id = R.plurals.hour;
                        break;
                    case TaskLL.INTERVALS_DAY:
                        id = R.plurals.day;
                        break;
                    case TaskLL.INTERVALS_WEEK:
                        id = R.plurals.week;
                        break;
                    case TaskLL.INTERVALS_MONTH:
                        id = R.plurals.month;
                        break;
                    case TaskLL.INTERVALS_YEAR:
                        id = R.plurals.year;
                        break;
                }
                ((TextView) result.findViewById(R.id.every)).setText(context.getResources().getString(R.string.custom_adapter_every_fmt, id > 0 ? context.getResources().getQuantityString(id, taskLL.getCountOfRepeat(), taskLL.getCountOfRepeat()).toLowerCase() : "NONE"));
                if (taskLL.isCreateNotification()) {
                    ((TextView) result.findViewById(R.id.alarm)).setText(R.string.custom_adapter_alarm_turn_on);
                } else {
                    result.findViewById(R.id.alarm).setVisibility(View.GONE);
                }
            } else {
                result.findViewById(R.id.every).setVisibility(View.GONE);
                result.findViewById(R.id.alarm).setVisibility(View.GONE);
            }

            if (taskLL.getMaxCount() > 0) {
                ((TextView) result.findViewById(R.id.max)).setText(context.getResources().getString(R.string.custom_adapter_max_count_fmt, taskLL.getMaxCount()));
            } else
                result.findViewById(R.id.max).setVisibility(View.GONE);

        }

        return result;
    }
}

