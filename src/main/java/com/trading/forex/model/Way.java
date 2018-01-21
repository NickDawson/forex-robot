package com.trading.forex.model;

import lombok.Getter;

/**
 * Created by hsouidi on 05/08/2017.
 */
@Getter
public enum Way {
    PUT(-1), CALL(1),NEUTRE(0);
    private int value;


    Way(int value) {
        this.value = value;
    }



    public Way inverse() {
        return this.equals(PUT) ? CALL : PUT;
    }
}
