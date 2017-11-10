package com.doconium.listener;

import com.doconium.event.base.BaseEvent;

/**
 * Created by Lafran on 7/19/17.
 */

public interface ServiceListener<EVT2 extends BaseEvent> {
    void onStartService();
    void onReceiveUpdate(EVT2 data);
}
