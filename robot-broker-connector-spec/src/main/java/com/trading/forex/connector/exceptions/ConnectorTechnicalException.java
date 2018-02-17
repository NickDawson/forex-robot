package com.trading.forex.connector.exceptions;

public class ConnectorTechnicalException extends RuntimeException {

    public ConnectorTechnicalException() {
        super();
    }

    public ConnectorTechnicalException(String message) {
        super(message);
    }
    public ConnectorTechnicalException(Throwable message) {
        super(message);
    }

}

