package com.trading.forex.service.impl;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.position.Position;
import com.oanda.v20.position.PositionCloseRequest;
import com.oanda.v20.position.PositionListResponse;
import com.trading.forex.exceptions.RobotTechnicalException;
import com.trading.forex.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wf on 10/21/2017.
 */
@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    public Context context;

    @Autowired
    public AccountID accountID;

    @Override
    public List<Position> getOpenedPositions() throws RequestException, ExecuteException {
        PositionListResponse positionListResponse = listPosition(context);
        return positionListResponse.getPositions().stream().filter(position ->
                position.getLong().getUnits().doubleValue() != 0D
                        || position.getShort().getUnits().doubleValue() != 0D).collect(Collectors.toList());
    }

    @Override
    public Double getProfitOpenedPositions() throws RequestException, ExecuteException {
        return getOpenedPositions().stream().mapToDouble(value -> value.getUnrealizedPL().doubleValue()).sum();
    }

    @Override
    public Boolean closeOpenedPosition(Position position) {
        PositionCloseRequest positionCloseRequest = new PositionCloseRequest(accountID, position.getInstrument());
        Double shortUnit=position.getShort().getUnits().doubleValue();
        Double longUnit=position.getLong().getUnits().doubleValue();
        if(shortUnit<0){
            positionCloseRequest.setShortUnits(String.valueOf(Math.abs(shortUnit)));
        }
        if(longUnit>0){
            positionCloseRequest.setLongUnits(String.valueOf(Math.abs(longUnit)));
        }
        try {
            context.position.close(positionCloseRequest);
        } catch (Exception e) {
            throw new RobotTechnicalException(e);
        }

        return  true;
    }

    @Override
    public Boolean closeOpenedPosition() {
        try {
            getOpenedPositions().stream().forEach(position -> {
                closeOpenedPosition(position);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }


    private PositionListResponse listPosition(Context ctx) throws ExecuteException, RequestException {
        return ctx.position.list(accountID);
    }
}
