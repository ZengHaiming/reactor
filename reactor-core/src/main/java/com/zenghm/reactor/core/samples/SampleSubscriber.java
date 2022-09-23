package com.zenghm.reactor.core.samples;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

/**
 * @author Airlen
 * @date 2022/9/23
 * 当您需要自定义请求数量时，扩展BaseSubscriber会更有用。
 */
public class SampleSubscriber<T> extends BaseSubscriber<T> {
    public void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        request(1);
    }

    public void hookOnNext(T value) {
        System.out.println(value);
        request(1);
    }
}
