package com.trading.forex.controller;

/**
 * Created by wf on 10/20/2017.
 */

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.trading.forex.exceptions.RobotTechnicalException;
import com.trading.forex.model.Status;
import com.trading.forex.schedule.ScheduleBookingService;
import com.trading.forex.service.BalanceService;
import com.trading.forex.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
public class RobotRestController {


    @Autowired
    private ScheduleBookingService scheduleBookingService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private PositionService positionService;

    @RequestMapping(value = "action/{action}", method = RequestMethod.GET)
    public String action(@PathVariable String action) {
        log.info("Execute Action :" + action);
        switch (action) {
            case "limit":
                break;
            case "stop":
                scheduleBookingService.setRunBooking(false);
                break;
            case "start":
                scheduleBookingService.setRunBooking(true);
                break;
            case "close":
                positionService.closeOpenedPosition();
                break;
            default :
                throw new RobotTechnicalException("Undefined action "+action);

        }
        return null;
    }

    @RequestMapping(value = "payout/{payout}", method = RequestMethod.POST)
    public void setPayout(@PathVariable Double  payout) {
        log.info("Set Unit :" + payout);
        scheduleBookingService.setUnit(payout.intValue());
    }

    @RequestMapping(value = "status", method = RequestMethod.GET)
    @ResponseBody
    public Status status() throws ExecuteException, RequestException {
        log.info("Execute Action : status");
        return Status.builder().inverse(false)
                .maxLoss(Optional.ofNullable(balanceService.getMaxloss()).orElse(0.0))
                .maxProfit(Optional.ofNullable(balanceService.getMaxProfit()).orElse(0.0))
                .mode(scheduleBookingService.getMode())
                .solde(Optional.ofNullable(balanceService.getSolde()).orElse(0.0))
                .serverStatus(Optional.ofNullable(scheduleBookingService.getRunBooking()).orElse(Boolean.FALSE))
                .strategy("Investing")
                .limit(false)
                .nbOpenedTransaction(positionService.getOpenedPositions().size())
                .payout(Double.valueOf(scheduleBookingService.getUnit()))
                .positionProfit(positionService.getProfitOpenedPositions())
                .coverage(false)
                .build();

    }

    @RequestMapping(value = "solde/reset", method = RequestMethod.POST)
    public void reset() {
        log.info("Execute Action : reset");
        balanceService.reset();
    }
}