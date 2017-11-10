package com.doconium.rxservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.doconium.bus.RxBus;
import com.doconium.event.base.BaseEvent;
import com.doconium.event.base.BaseEventUI;
import com.doconium.listener.ServiceListener;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Doconium on 17/07/2017.
 */

public class ServiceManager<SE extends BaseEvent> {

    public static final String SERVICE_NAME = "spoonart_service";

    private final BehaviorSubject<IBinderStub> serviceSubject = BehaviorSubject.create();
    private final Context context;
    private Disposable subscription;
    private ServiceListener<SE> listener;

    private Class mService;
    private Class mEvent;

    private ServiceConnection serviceConnection;
    private Intent serviceIntent;

    public ServiceManager(Context context) {
        this.context = context;
    }

    public ServiceManager setProperties(Class service, Class busEventClass) {
        this.mEvent = busEventClass;
        this.mService = service;
        Intent serviceIntent = new Intent(context, mService);
        serviceIntent.setAction(SERVICE_NAME);
        this.serviceIntent = serviceIntent;
        initBus();
        return this;
    }

    public ServiceManager setListener(ServiceListener<SE> listener) {
        this.listener = listener;
        return this;
    }

    private void initBus() {
        getBus().toObserverable()
                .ofType(mEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Object o) {
                        if (listener != null) {
                            listener.onReceiveUpdate((SE) o);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public static RxBus getBus() {
        return RxBus.get();
    }

    public ServiceManager startRunning(boolean stayAlive) {
        startLive(stayAlive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<IBinderStub>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        subscription = d;
                    }

                    @Override
                    public void onNext(IBinderStub iBinderStub) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return this;
    }

    public void unsubscribe() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    private Observable<IBinderStub> startLive(boolean stayAlive) {
        return executeService(stayAlive)
                .flatMap(new Function<Boolean, ObservableSource<IBinderStub>>() {
                    @Override
                    public ObservableSource<IBinderStub> apply(Boolean aBoolean) throws Exception {
                        return connectService();
                    }
                });
    }

    private Observable<Boolean> executeService(final boolean stayAlive) {

        return Observable.defer(new Callable<ObservableSource<? extends Boolean>>() {
            @Override
            public ObservableSource<? extends Boolean> call() throws Exception {
                if (stayAlive && !SpoonartService.isRunning(context, mService)) {
                    System.out.println("Start service to stay alive -> " + stayAlive);
                    context.startService(serviceIntent);
                }
                return Observable.just(SpoonartService.isRunning(context, mService));
            }
        });
    }

    private Observable<IBinderStub> connectService() {
        return Observable.create(new ObservableOnSubscribe<IBinderStub>() {
            @Override
            public void subscribe(ObservableEmitter<IBinderStub> e) throws Exception {
                synchronized (ServiceManager.this) {
                    if (serviceConnection == null) {
                        System.out.println("disconnecting service...");
                        context.unbindService(serviceConnection);
                        serviceConnection = null;

                        serviceConnection = getServiceConnection();
                        System.out.println("connecting service...");
                        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                    }
                }
            }
        });
    }

    private ServiceConnection getServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                System.out.println("service connected!");
                IBinderStub binder = IBinderStub.Stub.asInterface(service);
                serviceBinder(binder);

                if (listener != null) {
                    listener.onStartService();
                }
                serviceSubject.onNext(binder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                System.out.println("service disconnected!");
                serviceSubject.onComplete();
            }
        };
    }

    private void serviceBinder(final IBinderStub binderStub) {
        try {
            binderStub.onUnbind();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            binderStub.onBind();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //UTILITY
    public static <T extends BaseEvent> void sendEventToUI(T serviceEvent) {
        ServiceManager.getBus().send(serviceEvent);
    }

    public static <T extends BaseEventUI> void sendEventToService(T uiEvent) {
        ServiceManager.getBus().send(uiEvent);
    }
}
