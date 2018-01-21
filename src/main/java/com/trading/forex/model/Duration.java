package com.trading.forex.model;

import lombok.Getter;

/**
 * Created by hsouidi on 05/14/2017.
 */
@Getter
public enum Duration {

    ONE_MIN(1,DurationUnit.MINUTE), TWO_MIN(2,DurationUnit.MINUTE),FIVE_MIN(5,DurationUnit.MINUTE),TEN_MIN(10,DurationUnit.MINUTE),FIFTEEN_MIN(15,DurationUnit.MINUTE),THIRTY_MIN(30,DurationUnit.MINUTE),ONE_HOUR(60,DurationUnit.MINUTE);
    private int duration;
    private DurationUnit duration_unit;

    Duration(int duration,DurationUnit duration_unit){
        this.duration=duration;
        this.duration_unit=duration_unit;
    }
}
