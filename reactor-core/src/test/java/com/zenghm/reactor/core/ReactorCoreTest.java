package com.zenghm.reactor.core;

import com.zenghm.reactor.core.samples.SampleSubscriber;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

class ReactorCoreTest {

    @org.junit.jupiter.api.Test
    void main() {
    }

    @Test
    void flux() {
        Flux<String> seq1 = Flux.just("foo", "bar", "foobar");
        List<String> iterable = Arrays.asList("foo", "bar", "foobar");
        Flux<String> seq2 = Flux.fromIterable(iterable);
    }

    @Test
    void mono() {
        Mono<String> noData = Mono.empty();
        Mono<String> data = Mono.just("foo");
        Flux<Integer> numbersFromFiveToSeven = Flux.range(5, 3);
    }

    @Test
    void subscribe() {
        Flux<Integer> ints = Flux.range(1, 3);
        ints.subscribe(System.out::println);
    }

    @Test
    void subscribeError() {
        Flux<Integer> ints = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    throw new RuntimeException("Got to 4");
                });
        ints.subscribe(System.out::println,
                error -> System.err.println("Error: " + error));
    }

    @Test
    void subscribeDone() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(System.out::println,
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"));
    }

    @Test
    void sampleSubscriber() {
        SampleSubscriber<Integer> ss = new SampleSubscriber<Integer>();
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(ss);
    }

    @Test
    void baseSubscriberHookOnSubscribe() {
        Flux.range(1, 10)
                .doOnRequest(r -> System.out.println("request of " + r))
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }
                    @Override
                    public void hookOnNext(Integer integer) {
                        System.out.println("Cancelling after having received " + integer);
                        cancel();
                    }
                });
    }

}