package com.zenghm.reactor.core;

import com.zenghm.reactor.core.samples.MyEventListener;
import com.zenghm.reactor.core.samples.MyEventProcessor;
import com.zenghm.reactor.core.samples.SampleSubscriber;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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

    @Test
    void generate() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    //????????????
                    if (state == 10) sink.complete();
                    //???????????????????????????????????????????????????
                    return state + 1;
                });
        flux.subscribe(System.out::println);
    }

    /**
     * ???????????????
     */
    @Test
    void atomicLongGenerate() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                });
        flux.subscribe(System.out::println);
    }

    /**
     * ??? consumer
     */
    @Test
    void consumerGenerate() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, (state) -> System.out.println("state: " + state));
        flux.subscribe();
    }


    @Test
    void create() {
        MyEventProcessor myEventProcessor = new MyEventProcessor();
        Flux<String> bridge = Flux.create(sink -> {
            myEventProcessor.register(
                    new MyEventListener<String>() {

                        public void onDataChunk(List<String> chunk) {
                            for (String s : chunk) {
                                sink.next(s);
                            }
                        }

                        public void processComplete() {
                            sink.complete();
                        }
                    });
        });
        bridge.subscribe(System.out::println);
    }

    @Test
    void onDisposeAndOnCancel() {
        //Flux<String> bridge = Flux.create(sink -> {
        //    sink.onRequest(n -> channel.poll(n))
        //            .onCancel(() -> channel.cancel())
        //            .onDispose(() -> channel.close())
        //});
    }

    public String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 26) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }

    /**
     * handle???????????????????????????????????????
     */
    @Test
    void handle() {
        Flux<String> alphabet = Flux.just(-1, 30, 13, 9, 20)
                .handle((i, sink) -> {
                    String letter = alphabet(i);
                    if (letter != null)
                        sink.next(letter);
                });

        alphabet.subscribe(System.out::println);
    }

    /**
     * Mono?????????????????????
     */
    @Test
    void runNewThread() {
        final Mono<String> mono = Mono.just("hello ");
        //print main thread name
        System.out.println(Thread.currentThread().getName());
        Thread t = new Thread(() -> mono
                .map(msg -> msg + "thread ")
                .subscribe(v ->
                        System.out.println(v + Thread.currentThread().getName())
                )
        );
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void schedulersPublishOn() throws InterruptedException {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);
        final Flux<String> flux = Flux
                .range(1, 2)
                // ?????????????????????
                .map(i -> {
                    System.out.println(Thread.currentThread().getName());
                    return 10 + i;
                })
                // ??????????????????
                .publishOn(s)
                // ????????? parallel-scheduler ??????
                .map(i -> {
                    System.out.println(Thread.currentThread().getName());
                    return "value " + i;
                });
        Thread t = new Thread(() -> flux.subscribe(System.out::println));
        t.start();
        t.join();
    }

    @Test
    void schedulersSubscribeOn() throws InterruptedException {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);
        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> {
                    System.out.println("map1-"+Thread.currentThread().getName());
                    return 10 + i;
                })
                //subscribeOn???????????????????????????????????????????????????
                .subscribeOn(s)
                //?????????map ????????????????????????
                .map(i -> {
                    System.out.println("map2-"+Thread.currentThread().getName());
                    return "value " + i;
                });
        Thread t = new Thread(() -> flux.subscribe(System.out::println));
        t.start();
        t.join();
    }

    @Test
    void onError(){
        Flux<String> flux = Flux.just(1, 2, 0)
                .map(i -> "100 / " + i + " = " + (100 / i)); //this triggers an error with 0
                //?????? .onErrorReturn("Divided by zero :("); // error handling example
        flux.subscribe(System.out::println, v->{
            System.out.println("Divided by zero :(");
        });
    }

    /**
     * ????????????
     */
    @Test
    void onErrorCondition(){
        Flux<String> flux = Flux.just(1, 2, 0)
                .map(i -> "100 / " + i + " = " + (100 / i)) //this triggers an error with 0
        .onErrorReturn(e->e.getMessage().equals("/ by zero"),"null"); // error handling example
        flux.subscribe(System.out::println);
    }
}