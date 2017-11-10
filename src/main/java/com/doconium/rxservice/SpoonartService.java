package com.doconium.rxservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.doconium.bus.RxBus;
import com.doconium.event.base.BaseEventUI;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Doconium on 17/07/2017.
 */

public abstract class SpoonartService<BEU extends BaseEventUI> extends Service {

    private RxBus rxBus;

    private final IBinder binder = new IBinderStub.Stub(){

        @Override
        public int getPid(int pid) throws RemoteException {
            return android.os.Process.myPid();
        }

        @Override
        public void onBind() throws RemoteException {
            initBus();
        }

        @Override
        public void onUnbind() throws RemoteException {
            rxBus = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if(!(binder instanceof IBinderStub))
        {
            throw new Error("binder must be instance of IBinderStub ");
        }
    }

    private void initBus(){
        rxBus = ServiceManager.getBus();
        rxBus.toObserverable()
                .ofType(getEventTypeClass())
                .observeOn(Schedulers.io()).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {
                onServiceReceiveEvent((BEU) o);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public abstract Class getEventTypeClass();

    public void onServiceReceiveEvent(BEU data) {

    }

    @SuppressWarnings("unused")
    public final static boolean isRunning(Context context, Class yourServiceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (yourServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart();
        return Service.START_STICKY;
    }

    /*
    * called inside onStartCommand
    */
    public abstract void onStart();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
