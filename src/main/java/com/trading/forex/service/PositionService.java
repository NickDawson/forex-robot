package com.trading.forex.service;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.position.Position;

import java.util.List;

/**
 * Created by wf on 10/21/2017.
 */
public interface PositionService {
    List<Position> getOpenedPositions() throws RequestException, ExecuteException;

    Double  getProfitOpenedPositions() throws RequestException, ExecuteException;

    Boolean closeOpenedPosition(Position position);

    Boolean  closeOpenedPosition() ;
}
