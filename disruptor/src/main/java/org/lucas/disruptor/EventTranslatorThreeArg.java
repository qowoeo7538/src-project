package org.lucas.disruptor;

/**
 * @Author: shaw
 * @Date: 2019/5/17 12:48
 */
@FunctionalInterface
public interface EventTranslatorThreeArg<T, A, B, C> {

    void translateTo(T event, long sequence, A arg0, B arg1, C arg2);

}
