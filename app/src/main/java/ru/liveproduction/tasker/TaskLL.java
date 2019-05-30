package ru.liveproduction.tasker;

import android.os.Build;

import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

public class TaskLL implements Serializable {

    public enum REPEATED_INTERVALS implements Serializable{
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }


    public TaskLL(String name, long count){
        this.name = name;
        this.count = count;
    }

    public static int indexOfUnit(REPEATED_INTERVALS unit) {
        switch (unit) {
            case SECOND: return 0;
            case MINUTE: return 1;
            case HOUR: return 2;
            case DAY: return 3;
            case WEEK: return 4;
            case MONTH: return 5;
            case YEAR: return 6;
        }
        return -1;
    }


    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private long count;


    @Getter
    private long maxCount = -1;



    @Getter
    @Setter
    private boolean repeat = false;

    @Getter
    @Setter
    private int countOfRepeat = -1;

    @Getter
    @Setter
    private REPEATED_INTERVALS typeOfRepeat = null;

    @Getter
    @Setter
    private boolean usePlus = true;

    @Getter
    @Setter
    private long countOfAction = 0;

    @Getter
    @Setter
    private long startTime = System.currentTimeMillis();



    @Getter
    @Setter
    private boolean createNotification = false;

    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
        if (maxCount > 0)
        this.count = Math.min(count, maxCount);
    }

    public long getTimeToCloserRepeatInMs() {
        return getTimeForRepeatInMs() - ((System.currentTimeMillis() - startTime) % getTimeForRepeatInMs());
    }

    public long getTimeForRepeatInMs() {
        if (repeat && countOfAction > 0) {
            switch (this.typeOfRepeat) {
                case SECOND:
                    return countOfRepeat * 1000;
                case MINUTE:
                    return countOfRepeat * 1000 * 60;
                case HOUR:
                    return countOfRepeat * 1000 * 60 * 60;
                case DAY:
                    return countOfRepeat * 1000 * 60 * 60 * 24;
                case WEEK:
                    return countOfRepeat * 1000 * 60 * 60 * 24 * 7;
                case MONTH:
                    return countOfRepeat * 1000 * 60 * 60 * 24 * 30;
                case YEAR:
                    return countOfRepeat * 1000 * 60 * 60 * 24 * 365;
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskLL) {
            return ((TaskLL) obj).name.equals(this.name);
        }
        return false;
    }
}
