package org.async.rmi;

import java.util.concurrent.TimeUnit;

/**
 * Created by Barak Bar Orion
 * 01/11/14.
 */
public class TimeSpan {
    private final long time;
    private final TimeUnit timeUnit;


    public TimeSpan(long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public long getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long asMilliseconds(){
        return TimeUnit.MILLISECONDS.convert(time, timeUnit);
    }

    @Override
    public String toString() {
        return "TimeSpan{" +
                "time=" + time +
                ", timeUnit=" + timeUnit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSpan timeSpan = (TimeSpan) o;

        return time == timeSpan.time && timeUnit == timeSpan.timeUnit;

    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + timeUnit.hashCode();
        return result;
    }
}
