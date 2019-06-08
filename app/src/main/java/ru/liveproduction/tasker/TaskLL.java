package ru.liveproduction.tasker;

import java.io.Serializable;

import lombok.Data;

@Data
public class TaskLL implements Serializable, Cloneable {

    //It will can delete if this be too much load
    public static final int INTERVALS_SECONDS = 0;
    public static final int INTERVALS_MINUTE = 1;
    public static final int INTERVALS_HOUR = 2;
    public static final int INTERVALS_DAY = 3;
    public static final int INTERVALS_WEEK = 4;
    public static final int INTERVALS_MONTH = 5;
    public static final int INTERVALS_YEAR = 6;




    private final String name;
    private final long count;
    private final long maxCount;
    private final boolean repeat;
    private final int countOfRepeat;
    private final int typeOfRepeat;
    private final long countOfAction;
    private final ClockLL startTime;
    private final boolean createNotification;

    public TaskLL(String name, long count, long maxCount, boolean repeat, int countOfRepeat, int typeOfRepeat, long countOfAction, boolean createNotification, ClockLL startTime) {
        this.name = name;
        this.count = count;
        this.maxCount = maxCount;
        this.repeat = repeat;
        this.countOfRepeat = countOfRepeat;
        this.typeOfRepeat = typeOfRepeat;
        this.countOfAction = countOfAction;
        this.createNotification = createNotification;
        this.startTime = startTime;


    }

    public TaskLL(String name, long count) {
        this(name, count, 0, false, -1, -1, 0, false, new ClockLL());
    }

    public TaskLL setCount(long count) {
        return new TaskLL(name, count, maxCount, repeat, countOfRepeat, typeOfRepeat, countOfAction, createNotification, startTime);
    }

    public long getTimeToCloserRepeatInMs() {
        return getTimeToCloserRepeatInMs(new ClockLL());
    }

    public long getTimeToCloserRepeatInMs(ClockLL now) {
        return getCloserTime(now).toMilliseconds() - now.toMilliseconds();
    }

    public ClockLL getCloserTime() {
        return getCloserTime(new ClockLL());
    }

    public ClockLL getCloserTime(ClockLL now) {
        ClockLL dif = now.sub(startTime);
        switch (typeOfRepeat) {
            case INTERVALS_SECONDS:
                return new ClockLL(now.getYear(), now.getMonth(), now.getDay(), now.getHour(), now.getMinutes(), getField(startTime, now, countOfRepeat, 0), startTime.getMilliseconds());
            case INTERVALS_MINUTE:
                return new ClockLL(now.getYear(), now.getMonth(), now.getDay(), now.getHour(), getField(startTime, now, countOfRepeat, 1), startTime.getSeconds(), startTime.getMilliseconds());
            case INTERVALS_HOUR:
                return new ClockLL(now.getYear(), now.getMonth(), now.getDay(), getField(startTime, now, countOfRepeat, 2), startTime.getMinutes(), startTime.getSeconds(), startTime.getMilliseconds());
            case INTERVALS_DAY:
                return new ClockLL(now.getYear(), now.getMonth(), getField(startTime, now, countOfRepeat, 3), startTime.getHour(), startTime.getMinutes(), startTime.getSeconds(), startTime.getMilliseconds());
            case INTERVALS_WEEK:
                ClockLL result = startTime;
                ClockLL inc = ClockLL.createClockWithZeroValues(0,0, 7 * countOfRepeat, 0, 0, 0, 0);

                while (result.compareTo(now) < 1) {
                    result = result.add(inc);
                }

                return result;
            case INTERVALS_MONTH:
                return new ClockLL(now.getYear(),  getField(startTime, now, countOfRepeat, 5), startTime.getDay() % ClockLL.getDaysOfMonth(now.getYear(), getField(startTime, now, countOfRepeat, 5)), startTime.getHour(), startTime.getMinutes(), startTime.getSeconds(), startTime.getMilliseconds());
            case INTERVALS_YEAR:
                return new ClockLL(startTime.getYear() + (dif.getYear() / countOfRepeat + 1) * countOfRepeat, startTime.getMonth(), startTime.getDay(), startTime.getHour(), startTime.getMinutes(), startTime.getSeconds(), startTime.getMilliseconds());
        }
        return null;
    }

    private static long getField(ClockLL startTime, ClockLL now, long count, int field) {
        long first = 0;
        long second = 0;
        long mod = 0;
        ClockLL tmp = now.sub(startTime);
        long dif = 0;
        switch (field) {
            case 0:
                first = startTime.getSeconds();
                dif = tmp.getSeconds();
                mod = 60;
                break;
            case 1:
                first = startTime.getMinutes();
                dif = tmp.getMinutes();
                mod = 60;
                break;
            case 2:
                first = startTime.getHour();
                dif = tmp.getHour();
                mod = 24;
                break;
            case 3:
                first = startTime.getDay();
                dif = tmp.getDay();
                mod = ClockLL.getDaysOfMonth(now.getYear(), now.getMonth());
                break;
            case 5:
                first = startTime.getMonth();
                dif = tmp.getMonth();
                mod = 12;
                break;
        }
        return (first + ((dif / count + 1) * count)) % mod;
    }
}
