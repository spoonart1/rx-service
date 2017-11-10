package com.doconium.bus;


import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Doconium on 10/07/2017.
 */

public class RxBus {

    private static RxBus rxBus;
    /// / If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final PublishSubject<Object> _bus = PublishSubject.create();

    public void send(Object o) {
        _bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return _bus;
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }

    public static RxBus get(){
        if(rxBus == null){
            rxBus = new RxBus();
        }
        return rxBus;
    }
}
