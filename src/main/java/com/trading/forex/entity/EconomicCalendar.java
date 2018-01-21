package com.trading.forex.entity;

import com.trading.forex.model.Currency;
import com.trading.forex.model.Importance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by hsouidi on 11/19/2017.
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class EconomicCalendar {

    @EmbeddedId
    private EconomicCalendarID economicCalendarID;

    @Enumerated(EnumType.STRING)
    private Currency currency;
    private Double actual;
    private Double forecast;
    private Double previous;
    @Enumerated(EnumType.STRING)
    private Importance importance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class EconomicCalendarID implements Serializable {
        private Date eventDate;
        private String event;
    }
}


