package ru.liveproduction.tasker;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmManagerLL extends BroadcastReceiver {

    private static final String GROUP_ID = "ru.liveproduction.tasker.notification.group.1";

    public static PendingIntent createIntentForNotification(Context context, TaskLL task){
        Intent intent = new Intent(context, AlarmManagerLL.class);
        intent.putExtra("objectName", task.getName().toLowerCase());
        return PendingIntent.getBroadcast(context, task.getName().toLowerCase().hashCode(), intent, 0);
    }

    public static Intent createIntentForUpdate(String name){
        Intent intent = new Intent("ru.liveproduction.tasker.update.intent.UpdateTask");
        intent.putExtra("objectName", name);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("objectName");
        if (name != null) {
            TaskLL task = ApplicationLL.manager.get(name);
            if (task != null) {
                if (task.isRepeat()) {
                    long result = task.getCount();
                    if (task.isUsePlus())
                        result += task.getCountOfAction();
                    else
                        result -= task.getCountOfAction();

                    if (task.getMaxCount() > 0 && result >= task.getMaxCount()) {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getName().toLowerCase().hashCode(), intent, 0);
                        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
                        result = task.getMaxCount();
                    }

                    task.setCount(result);

                    ApplicationLL.manager.update(task);

                    if (task.isCreateNotification()) {

                        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(GROUP_ID.hashCode(), new NotificationCompat.Builder(context, ApplicationLL.NOTIFICATION_CHANEL)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setGroup(GROUP_ID)
                                .setGroupSummary(true)
                                .setAutoCancel(true)
                                .build());

                        Intent intent1 = new Intent(context, EditTaskActivity.class);
                        intent1.putExtra("objectName", task.getName());
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, task.getName().hashCode(), intent1, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ApplicationLL.NOTIFICATION_CHANEL);
                        builder
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(context.getResources().getString(R.string.textSeeName_notification) + " " + task.getName())
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getResources().getString(R.string.textSeeCount_notification) + " " + task.getCount()))
                                .setContentIntent(pendingIntent)
                                .setGroup(GROUP_ID)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true);

                        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(task.getName().toLowerCase().hashCode(), builder.build());
                    }

                    context.sendBroadcast(createIntentForUpdate(task.getName()));
                } else {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getName().toLowerCase().hashCode(), intent, 0);
                    ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
                }
            } else {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, intent.getStringExtra("objectName").hashCode(), intent, 0);
                ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
            }
        }
    }

}
