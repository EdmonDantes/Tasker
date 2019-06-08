package ru.liveproduction.tasker;

import android.os.Build;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class ClockLL implements Comparable<ClockLL>, Cloneable {

    private final long year, month, day, hour, minutes, seconds, ms;
    private final boolean canTransform;

    public ClockLL(){
        GregorianCalendar calendar = new GregorianCalendar();
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minutes = calendar.get(Calendar.MINUTE);
        this.seconds = calendar.get(Calendar.SECOND);
        this.ms = calendar.get(Calendar.MILLISECOND);
        this.canTransform = true;
    }

    private ClockLL(long year, long month, long day, long hour, long minutes, long seconds, long ms, boolean useZero) {
        seconds += ms / 1000;
        if (ms < 0){
            seconds--;
            ms = 1000 + (ms % 1000);
        } else
            ms %= 1000;

        minutes += seconds / 60;
        if (seconds < 0) {
            minutes--;
            seconds = 60 + (seconds % 60);
        } else
            seconds %= 60;

        hour += minutes / 60;
        if (minutes < 0) {
            hour--;
            minutes = 60 + (minutes % 60);
        } else
            minutes %= 60;

        day += hour / 24;
        if (hour < 0) {
            day--;
            hour = 24 + (hour % 24);
        } else
            hour %= 24;

        while (month > 13) {
            year++;
            month -= 12;
        }

        while (month < (useZero ? 0 : 1)) {
            year--;
            month += 12;
        }

        while (day < (useZero ? 0 : 1)) {
            day += getDaysOfMonth(year, month--);
            if (month < 1){
                year--;
                month += 12;
            }
        }

        while (day > getDaysOfMonth(year, month)){
            day -= getDaysOfMonth(year, month++);
            if (month > 12){
                year++;
                month -= 12;
            }
        }

        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minutes = minutes;
        this.seconds = seconds;
        this.ms = ms;
        this.canTransform = !useZero;
    }

    public ClockLL(long year, long month, long day, long hour, long minutes, long seconds, long ms) {
        this(year, month, day, hour, minutes, seconds, ms, false);
    }

    public ClockLL(long year, long month, long day) {
        this(year, month, day, 0, 0, 0, 0);
    }

    public ClockLL(long hour, long minutes, long seconds, long ms) {
        this(0, 0, 0, hour, minutes, seconds, ms);
    }

    public ClockLL getDate() {
        return new ClockLL(this.year, this.month, this.day, 0, 0, 0, 0);
    }

    public ClockLL getTime() {
        return new ClockLL(0, 0, 0, this.hour, this.minutes, this.seconds, this.ms);
    }

    public long getYear() {
        return year;
    }

    public long getDaysOfYear(){
        return getDaysOfYear(this.year);
    }

    public long getMonth() {
        return month;
    }

    public long getDaysOfMonth(){
        return getDaysOfMonth(this.year, this.month);
    }

    public long getWeekOfYear(){
        return getWeekOfYear(this.year, this.month, this.day);
    }

    public long getWeekOfMonth(){
        return getWeekOfMonth(this.year, this.month, this.day);
    }

    public long getDayOfYear(){
        return getDayOfYear(this.year, this.month, this.day);
    }

    public long getDayOfWeek(){
        return getDayOfWeek(this.year, this.month, this.day);
    }

    public long getDay() {
        return day;
    }

    public long getHour() {
        return hour;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMilliseconds() {
        return ms;
    }

    public ClockLL add(ClockLL inc) {
        return new ClockLL(this.year + inc.year, this.month + inc.month, this.day + inc.day, this.hour + inc.hour, this.minutes + inc.minutes, this.seconds + inc.seconds, this.ms + inc.ms);
    }

    public ClockLL sub(ClockLL dec) {
        return new ClockLL(this.year - dec.year, this.month - dec.month, this.day - dec.day, this.hour - dec.hour, this.minutes - dec.minutes, this.seconds - dec.seconds, this.ms - dec.ms, true);
    }

    public static long getDaysOfYear(long year) {
        return year % 400 == 0 || (year % 100 != 0 && year % 4 == 0) ? 366 : 365;
    }

    public static long getDaysOfMonth(long year, long month){
        while (month > 12) {
            month -= 12;
            year++;
        }

        switch ((int) month) {
            case 2:
                return getDaysOfYear(year) == 366 ? 29 : 28;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            default:
                return 30;
        }
    }

    public static long getWeekOfYear(long year, long month, long day) {
        long tmp0 = getDayOfYear(year, month, day);
        long tmp1 = 8 - getDayOfWeek(year, 1, 1);
        tmp0 = Math.abs(tmp0 - tmp1);
        return tmp0 / 7 + (tmp0 % 7 == 0 ? 0 : 1) + 1;
    }

    public static long getWeekOfMonth(long year, long month, long day) {
        long tmp1 = 8 - getDayOfWeek(year, month, 1);
        long tmp0 = Math.abs(day - tmp1);
        return tmp0 / 7 + (tmp0 % 7 == 0 ? 0 : 1) + 1;
    }

    public static long getDayOfYear(long year, long month, long day) {
        long tmp0 = 1;
        long result = 0;
        while (tmp0 < month) {
            result += getDaysOfMonth(year, tmp0++);
        }
        result += day;
        return result;
    }

    public static long getDayOfWeek(long year, long month, long day){
        long result = 1;
        long tmp0 = 1900;
        long tmp1 = 1;

        while (tmp0 < year) {
            if (getDaysOfYear(tmp0++) == 365)
                result = (result + 1) % 7;
            else
                result = (result + 2) % 7;
        }

        while (tmp1 < month) {
            result = ((getDaysOfMonth(tmp0, tmp1++) - (8 - result)) % 7) + 1;
        }
        result = (result + (day - 1)) % 7;
        return result == 0 ? 7 : result;
    }

    public static ClockLL createClockWithZeroValues(long year, long month, long day, long hour, long minutes, long seconds, long ms) {
        return new ClockLL(year, month, day, hour, minutes, seconds, ms, true);
    }

    @Override
    public String toString() {
        return String.format("%02d.%02d.%04d %02d:%02d:%02d:%03d", day, month, year, hour, minutes, seconds, ms);
    }

    public ClockLL normalizeOrNull() {
        if (canTransform)
            return null;
        else
            return new ClockLL(year, month, day, hour, minutes, seconds, ms);
    }

    public Date toDate(){
        if (canTransform) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Date.from(Instant.from(LocalDateTime.of((int) year, (int) (month - 1), (int) day, (int) hour, (int) minutes, (int) seconds, (int) (ms * 1000000))));
            } else {
                return new Date((int) year, (int) month, (int) day, (int) hour, (int) minutes, (int) seconds);
            }
        } else
            return null;
    }

    public Calendar toCalendar(){
        if (canTransform) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return new GregorianCalendar.Builder().setDate((int) year, (int) month, (int) day).setTimeOfDay((int) hour, (int) minutes, (int) seconds, (int) ms).build();
            } else {
                return new GregorianCalendar((int) year, (int) month, (int) day, (int) hour, (int) minutes, (int) seconds);
            }
        } else
            return null;
    }

    public long toMilliseconds(){
        if (canTransform) {
            return toCalendar().getTimeInMillis();
        } else
            return -1;
    }

    @Override
    public int compareTo(ClockLL clockLL) {
        int c0 = Long.compare(this.year, clockLL.year);
        if (c0 == 0) {
            c0 = Long.compare(this.month, clockLL.month);
            if (c0 == 0) {
                c0 = Long.compare(this.day, clockLL.day);
                if (c0 == 0) {
                    c0 = Long.compare(this.hour, clockLL.hour);
                    if (c0 == 0) {
                        c0 = Long.compare(this.minutes, clockLL.minutes);
                        if (c0 == 0) {
                            c0 = Long.compare(this.seconds, clockLL.seconds);
                            if (c0 == 0) {
                                c0 = Long.compare(this.ms, clockLL.ms);
                            }
                        }
                    }
                }
            }
        }


        return c0;
    }
}