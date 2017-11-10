# Service Example

## Communication between Service and UI using RXJava & RxAndroid

### NO NEED SENDBROADCAST/BROADCAST_RECEIVER
by using rxjava/rxandroid we can pass our data type like String, Int, Boolean, YourModel, YourPojo, even more.
there is no need to send over intent with bundle extra or somelike that.
there is no need to listen from receiver by listening intent.getAction() and do your stuff inside the `if condition`


#### How to Use ??
clone and use as library project (by importing library/module) my repo [here](https://github.com/spoonart1/rxservicemodule)

#### Requirements
Java 8 (JDK 1.8)

#########

this repo contains some example about how to use [rx_service](https://github.com/spoonart1/rxservicemodule)

NOTE : important file to use!!
1. a Service class of course :) -> must inherits from [rx service](https://github.com/spoonart1/rxservicemodule/blob/master/src/main/java/com/spoonart/service/rxservice/SpoonartService.java)
, for example : [see here](https://github.com/spoonart1/rxme/blob/master/app/src/main/java/com/example/lafran/testdoang/service/ServiceExample.java)
2. a class inherits from BaseUIEvent -> this class will helps you to pass data from Service to UI.
3. a class inherits from BaseEvent -> this class will helps you pass data from UI to Service.
4. a ServiceReceiverListener must be implemented to your UI class. see [ui class here](https://github.com/spoonart1/rxme/blob/master/app/src/main/java/com/example/lafran/testdoang/MainActivity.java).
5. an Instance variable ServiceManager, this class helps you to configure your service.


#### Implementing UI (Activity, Fragment, DialogFragment) with ServiceListener
```
public class MainActivity extends AppCompatActivity implements ServiceReceiverListener<YourServiceBaseEvent>{

//define service manager variable
private ServiceManager<YourServiceBaseEvent> serviceManager;
}

//how to initialize
serviceManager = new ServiceManager<ServiceEvent>(this)
                .setProperties(ServiceExample.class, ServiceEvent.class)
                .setListener(this)
                .startRunning(true);
```

#### Extending ServiceClass (SponnartService)
```
//set generic type class
public class ServiceExample extends SpoonartService<UIEvent>{
  //set yourBaseUIEvent class
  @Override
  public Class getEventTypeClass() {
     return yourBaseUIEvent.class;
  }
}
```

#### Sending/Passing Data From UI to Service
```
//using service manager
serviceManager.sendToService(new UIEvent(etMessage.getText().toString()));
```

#### Receiving Passed Data From UI to Service
```
// your inherited serviceClass must override this method to listen incoming data from UI
@Override
public void onServiceReceiveEvent(YourBaseUIEvent data) {
  
}
```

#### Sending/Passing data from service to UI
```
//use the static method from ServiceManager.class
ServiceManager.sendEventToUI(new YourBaseEvent("hello from service"));
```

#### Receiving data from service to UI
```
//consider this method was overridden in your MainActivity.class
@Override
public void onReceiveUpdate(ServiceEvent data) {
  tvReceivedFromService.setText(String.valueOf(data.tag));
}
```



### Please
i will try to keep updating this repo 
contributes or questions  -> email me on (myspoonart@gmail.com).


### Bug Reports
`nothing to show`
