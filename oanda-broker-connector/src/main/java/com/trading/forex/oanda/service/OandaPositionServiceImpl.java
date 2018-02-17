package com.trading.forex.oanda.service;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.position.PositionCloseRequest;
import com.oanda.v20.position.PositionListResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.trading.forex.common.exceptions.RobotTechnicalException;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.connector.exceptions.ConnectorTechnicalException;
import com.trading.forex.connector.model.Position;
import com.trading.forex.connector.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wf on 10/21/2017.
 */
@Service
public class OandaPositionServiceImpl implements PositionService {

    @Autowired
    public Context context;

    @Autowired
    public AccountID accountID;

    @Override
    public List<Position> getOpenedPositions() {
        PositionListResponse positionListResponse = null;
        try {
            positionListResponse = listPosition(context);
        } catch (ExecuteException | RequestException e) {
            throw new ConnectorTechnicalException(e);
        }
        return positionListResponse.getPositions().stream().filter(position ->
                position.getLong().getUnits().doubleValue() != 0D
                        || position.getShort().getUnits().doubleValue() != 0D)
                .map(position -> toPosition(position))
                .collect(Collectors.toList());
    }

    private Symbol fromIntrumentName(String instrumentName){
        return Symbol.valueOf(instrumentName);
    }

    private Position toPosition(com.oanda.v20.position.Position position) {
        return Position.builder()
                .pl(position.getPl().doubleValue())
                .unrealizedPL(position.getUnrealizedPL().doubleValue())
                .resettablePL(position.getResettablePL().doubleValue())
                .commission(position.getCommission().doubleValue())
                .longValue(position.getLong().getAveragePrice()!=null?position.getLong().getAveragePrice().doubleValue():null)
                .shortValue(position.getShort().getAveragePrice()!=null?position.getShort().getAveragePrice().doubleValue():null)
                .symbol(fromIntrumentName(position.getInstrument().toString()))
                .build();
    }

    @Override
    public Double getProfitOpenedPositions() {
        return getOpenedPositions().stream().mapToDouble(value -> value.getUnrealizedPL().doubleValue()).sum();
    }

    @Override
    public Boolean closeOpenedPosition(Position position) {
        PositionCloseRequest positionCloseRequest = new PositionCloseRequest(accountID, new InstrumentName(position.getSymbol().name()));
        Double shortUnit = position.getShortValue();
        Double longUnit = position.getLongValue();
        if (shortUnit < 0) {
            positionCloseRequest.setShortUnits(String.valueOf(Math.abs(shortUnit)));
        }
        if (longUnit > 0) {
            positionCloseRequest.setLongUnits(String.valueOf(Math.abs(longUnit)));
        }
        try {
            context.position.close(positionCloseRequest);
        } catch (Exception e) {
            throw new RobotTechnicalException(e);
        }

        return true;
    }

    @Override
    public Boolean closeOpenedPosition() {
        try {
            getOpenedPositions().stream().forEach(position -> {
                closeOpenedPosition(position);
            });
        } catch (Exception e) {
            throw new ConnectorTechnicalException(e);
        }
        return true;
    }


    private PositionListResponse listPosition(Context ctx) throws ExecuteException, RequestException {
        return ctx.position.list(accountID);
    }
}
