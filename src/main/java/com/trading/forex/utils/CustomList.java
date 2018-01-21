package com.trading.forex.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by hsouidi on 06/19/2017.
 */
public class CustomList<T> extends CopyOnWriteArrayList<T> {

    public CustomList(List<T> list) {
        super(list);
    }

    public CustomList() {
        super();
    }

    public T getLast(){
        if(this.isEmpty()){
            return null;
        }
        return (T) get(this.size()-1);
    }

    public T getFirst(){

        return (T) get(0);
    }


    public T getLastMinus(int minus){

        return (T) get(this.size()-minus-1);
    }
}
