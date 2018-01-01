package com.trading.forex.schedule.impl;

import com.trading.forex.repository.EconomicCalendarRepository;
import com.trading.forex.schedule.EconomicEventSchedule;
import com.trading.forex.service.IndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by wf on 11/21/2017.
 */
@Service
public class EconomicEventScheduleImpl implements EconomicEventSchedule {


    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private EconomicCalendarRepository economicCalendarRepository;

    @Override
    @Scheduled(fixedDelay = 15000)
    public void extractEvent() {
        economicCalendarRepository.save(indicatorService.getEconomicCalendarData(null));
    }
}
