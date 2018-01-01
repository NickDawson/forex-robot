package com.trading.forex.model;

import com.trading.forex.exceptions.RobotTechnicalException;
import lombok.Builder;
import lombok.Data;

/**
 * Created by wf on 04/23/2017.
 */
@Data
@Builder
public class InvestingData {

    private Decision cinqMinute;
    private Decision quinzeMinute;
    private Decision heure;
    private Decision jour;
    private Decision mensuel;

    public Integer getWeight() {
        return getWeightByDecision(cinqMinute) + getWeightByDecision(quinzeMinute)
                + getWeightByDecision(heure) + getWeightByDecision(jour);
    }

    private int getWeightByDecision(Decision decision) {
        switch (decision) {
            case BUY:
                return 5;
            case STRONG_BUY:
                return 10;
            case SELL:
                return -5;
            case STRONG_SELL:
                return -10;
            case NEUTRAL:
                return 0;
            default:
                throw new RobotTechnicalException("Unkown decision ");
        }

    }
}
