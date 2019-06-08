package ru.liveproduction.tasker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.TreeSet;


public class AlarmManagerService extends Service {

    public static final String COMMAND_ADD = "ru.liveproduction.tasker.AlarmManagerService.addTask";
    public static final String COMMAND_REMOVE = "ru.liveproduction.tasker.AlarmManagerService.removeTask";
    public static final String COMMAND_START = "ru.liveproduction.tasker.AlarmManagerService.start";
    public static final String COMMAND_STOP = "ru.liveproduction.tasker.AlarmManagerService.stop";


    private final Context context = this;
    private final TreeSet<String> map = new TreeSet<>();
    private final boolean[] start = new boolean[]{false};

    private final Thread th = new Thread(new Runnable() {
        @Override
        public void run() {
            boolean innerStart = true;
            while (innerStart) {
                boolean haveItems = false;

                synchronized (map){
                    haveItems = map.size() > 0;
                }

                if (haveItems) {
                    String min = "";

                    synchronized (map) {
                        min = map.first();
                    }

                    ClockLL now = new ClockLL();

                    synchronized (map) {
                        for (String name : map) {
                            if (ApplicationLL.manager.get(min).getTimeToCloserRepeatInMs(now) > ApplicationLL.manager.get(name).getTimeToCloserRepeatInMs(now)) {
                                min = name;
                            }
                        }
                    }

                    if (min != null) {
                        now = new ClockLL();
                        long ms = ApplicationLL.manager.get(min).getCloserTime(now).toMilliseconds() - now.toMilliseconds();
                        while (min != null && ms > 20) {
                            try {
                                Thread.sleep(Math.min(ms, 40000));
                            } catch (InterruptedException ignore) {
                                synchronized (start) {
                                    if (!start[0]) {
                                        Log.e("sjhfashasg", "Kksjhahgasghasga");
                                        return;
                                    }

                                }
                            }

                            synchronized (map) {
                                haveItems = map.size() > 0;
                            }

                            if (haveItems) {
                                synchronized (map) {
                                    for (String name : map) {
                                        TaskLL tmp0 = ApplicationLL.manager.get(min);
                                        TaskLL tmp1 = ApplicationLL.manager.get(name);

                                        if (tmp0 != null && tmp1 != null) {
                                             if (tmp0.getTimeToCloserRepeatInMs(now) > tmp1.getTimeToCloserRepeatInMs(now)) {
                                                 min = name;
                                             }
                                        } else {
                                            if (tmp1 == null)
                                                map.remove(name);

                                            if (tmp0 == null) {
                                                map.remove(min);
                                                min = null;
                                                break;
                                            }

                                        }
                                    }
                                }
                            } else {
                                break;
                            }
                            TaskLL taskLL = ApplicationLL.manager.get(min);
                            if (taskLL != null)
                                ms = taskLL.getCloserTime(now).toMilliseconds() - new ClockLL().toMilliseconds();
                            else break;
                        }
                        TaskLL taskLL = ApplicationLL.manager.get(min);
                        if (taskLL != null) {
                            if (taskLL.isRepeat()) {
                                long count = taskLL.getCount() + taskLL.getCountOfAction();
                                taskLL = taskLL.setCount(Math.max(0, taskLL.getMaxCount() > 0 ? Math.min(count, taskLL.getMaxCount()) : count));
                                ApplicationLL.manager.update(taskLL);

                                if (taskLL.isCreateNotification()) {
                                    ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(ApplicationLL.GROUP_ID.hashCode(), new NotificationCompat.Builder(context, ApplicationLL.NOTIFICATION_CHANEL)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setGroup(ApplicationLL.GROUP_ID)
                                            .setGroupSummary(true)
                                            .setAutoCancel(true)
                                            .build());

                                    Intent onPressNotification = new Intent(context, EditTaskActivity.class);
                                    onPressNotification.putExtra("objectName", min);
                                    PendingIntent onPressNotificationPendingIntent = PendingIntent.getActivity(context, min.hashCode(), onPressNotification, 0);


                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ApplicationLL.NOTIFICATION_CHANEL);
                                    builder
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle(context.getResources().getString(R.string.textSeeName_notification, taskLL.getName()))
                                            .setContentText(context.getResources().getString(R.string.textSeeCount_notification, taskLL.getCount()))
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.textSeeCount_notification, taskLL.getCount())))
                                            .setContentIntent(onPressNotificationPendingIntent)
                                            .setGroup(ApplicationLL.GROUP_ID)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true);

                                    ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(taskLL.getName().toLowerCase().hashCode(), builder.build());
                                }


                                context.sendBroadcast(createIntentForUpdate(taskLL.getName()));
                            } else if (min != null){
                                synchronized (map) {
                                    map.remove(min);
                                }
                            }
                        } else if (min != null) {
                            synchronized (map) {
                                map.remove(min);
                            }
                        }
                    }
                }else {
                    try {
                        Thread.sleep(55000);
                    } catch (InterruptedException ignore) {
                        synchronized (start) {
                            if (!start[0]){
                                return;
                            }

                        }
                    }
                }
                synchronized (start) {
                    innerStart = start[0];
                }
            }
        }
    }, "AlarmManagerServiceThread");

    public static Intent createIntentForUpdate(String name){
        Intent intent = new Intent("ru.liveproduction.tasker.update.intent.UpdateTask");
        intent.putExtra("objectName", name);
        return intent;
    }

    public boolean addTask(String name) {
        if (name != null && name.length() > 0) {
            String id = name.toLowerCase();
            synchronized (map) {
                map.add(id);
            }
            th.interrupt();
            return true;
        }
        return false;
    }

    public void removeTask(String name) {
        if (name != null && name.length() > 0) {
            String id = name.toLowerCase();
            synchronized (map) {
                map.remove(id);
            }
            th.interrupt();
        }
    }

    public boolean start() {
        if (!start[0]) {
            synchronized (start) {
                start[0] = true;
            }
            if (!th.isAlive()) {
                th.start();
            }
            return true;
        }
        return false;
    }

    public void stop() {
        if (start[0]) {
            synchronized (start) {
                start[0] = false;
            }
            th.interrupt();
            while (th.isAlive()) {
                try {
                    th.join();
                } catch (InterruptedException ignore) {}
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ApplicationLL.LOG_TAG, "AlarmManagerService -> onCreate()");
        ApplicationLL.manager.forEach(new TaskLLManager.OnGetTask() {
            @Override
            public void onGet(TaskLL task) {
                addTask(task.getName());
            }
        });
        start();
    }

    @Override
    public void onDestroy() {
        stop();
        ApplicationLL.manager.save(this);
        Log.d(ApplicationLL.LOG_TAG, "AlarmManagerService -> onDestroy()");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(ApplicationLL.LOG_TAG, "AlarmManagerService -> onBind()");
        if (intent.getAction() != null) {

            if (intent.getAction().equals(COMMAND_ADD)) {
                String name = intent.getStringExtra("objectName");
                addTask(name);
            } else if (intent.getAction().equals(COMMAND_REMOVE)) {
                String name = intent.getStringExtra("objectName");
                removeTask(name);
            } else if (intent.getAction().equals(COMMAND_START)) {
                start();
            } else if (intent.getAction().equals(COMMAND_STOP)) {
                stop();
            }
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ApplicationLL.LOG_TAG, "AlarmManagerService -> onStartCommand()");
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                ApplicationLL.manager.load(this);
                ApplicationLL.manager.forEach(new TaskLLManager.OnGetTask() {
                    @Override
                    public void onGet(TaskLL task) {
                        addTask(task.getName());
                    }
                });
            }
            ApplicationLL.manager.forEach(new TaskLLManager.OnGetTask() {
                @Override
                public void onGet(TaskLL task) {
                    addTask(task.getName());
                }
            });
        }
        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        ApplicationLL.manager.save(this);
        return super.stopService(name);
    }
}
