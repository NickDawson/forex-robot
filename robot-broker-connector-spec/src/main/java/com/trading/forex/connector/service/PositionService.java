package com.trading.forex.connector.service;


import com.trading.forex.connector.model.Position;

import java.util.List;

/**
 * Created by hsouidi on 10/21/2017.
 */
public interface PositionService {
    List<Position> getOpenedPositions();

    Double  getProfitOpenedPositions();

    Boolean closeOpenedPosition(Position position);

    Boolean closeOpenedPosition() ;
}
