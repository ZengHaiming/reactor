package com.zenghm.reactor.core.samples;

import java.util.List;

/**
 * @author Airlen
 * @date 2022/9/23
 * @description xxx
 */
public interface MyEventListener<T> {
    void onDataChunk(List<T> chunk);
    void processComplete();
}
