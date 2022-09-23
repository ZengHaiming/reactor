package com.zenghm.reactor.core.samples;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Airlen
 * @date 2022/9/23
 * @description xxx
 */
public class MyEventProcessor {
    public static final CopyOnWriteArrayList<MyEventListener> COPY_ON_WRITE_ARRAY_LIST = new CopyOnWriteArrayList<>();
    public void register(MyEventListener myEventListener){
        COPY_ON_WRITE_ARRAY_LIST.add(myEventListener);
    }
}
