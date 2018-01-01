package com.trading.forex.schedule;

/**
 * Created by wf on 10/19/2017.
 */
public interface ScheduleBookingService {

    Boolean getRunBooking();

    void setRunBooking(Boolean runBooking);

    public void runStrategy() throws Exception;

    String getMode();

    double getUnit();

    void setUnit(double unit);
}
