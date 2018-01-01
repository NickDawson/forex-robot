package com.trading.forex.exceptions;

public class RobotTechnicalException extends RuntimeException {

    public RobotTechnicalException() {
        super();
    }

    public RobotTechnicalException(String message) {
        super(message);
    }
    public RobotTechnicalException(Throwable message) {
        super(message);
    }

}

