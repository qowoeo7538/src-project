package org.lucas.disruptor;

/**
 * @Author: shaw
 * @Date: 2019/5/17 12:32
 */
public interface EventSink<E> {

    /**
     * 将事件发布到缓冲区
     *
     * @param translator
     */
    void publishEvent(EventTranslator<E> translator);

    boolean tryPublishEvent(EventTranslator<E> translator);

    <A> void publishEvent(EventTranslatorOneArg<E, A> translator, A arg0);

    <A> boolean tryPublishEvent(EventTranslatorOneArg<E, A> translator, A arg0);

    <A, B> void publishEvent(EventTranslatorTwoArg<E, A, B> translator, A arg0, B arg1);

    <A, B> boolean tryPublishEvent(EventTranslatorTwoArg<E, A, B> translator, A arg0, B arg1);

    <A, B, C> void publishEvent(EventTranslatorThreeArg<E, A, B, C> translator, A arg0, B arg1, C arg2);

    <A, B, C> boolean tryPublishEvent(EventTranslatorThreeArg<E, A, B, C> translator, A arg0, B arg1, C arg2);

    void publishEvent(EventTranslatorVararg<E> translator, Object... args);

    boolean tryPublishEvent(EventTranslatorVararg<E> translator, Object... args);

    void publishEvents(EventTranslator<E>[] translators);

    void publishEvents(EventTranslator<E>[] translators, int batchStartsAt, int batchSize);

    boolean tryPublishEvents(EventTranslator<E>[] translators);

    boolean tryPublishEvents(EventTranslator<E>[] translators, int batchStartsAt, int batchSize);

    <A> void publishEvents(EventTranslatorOneArg<E, A> translator, A[] arg0);

    <A> void publishEvents(EventTranslatorOneArg<E, A> translator, int batchStartsAt, int batchSize, A[] arg0);

    <A> boolean tryPublishEvents(EventTranslatorOneArg<E, A> translator, A[] arg0);

    <A> boolean tryPublishEvents(EventTranslatorOneArg<E, A> translator, int batchStartsAt, int batchSize, A[] arg0);

    <A, B> void publishEvents(EventTranslatorTwoArg<E, A, B> translator, A[] arg0, B[] arg1);

    <A, B> void publishEvents(
            EventTranslatorTwoArg<E, A, B> translator, int batchStartsAt, int batchSize, A[] arg0,
            B[] arg1);

    <A, B> boolean tryPublishEvents(EventTranslatorTwoArg<E, A, B> translator, A[] arg0, B[] arg1);

    <A, B> boolean tryPublishEvents(
            EventTranslatorTwoArg<E, A, B> translator, int batchStartsAt, int batchSize,
            A[] arg0, B[] arg1);

    <A, B, C> void publishEvents(EventTranslatorThreeArg<E, A, B, C> translator, A[] arg0, B[] arg1, C[] arg2);

    <A, B, C> void publishEvents(
            EventTranslatorThreeArg<E, A, B, C> translator, int batchStartsAt, int batchSize,
            A[] arg0, B[] arg1, C[] arg2);

    <A, B, C> boolean tryPublishEvents(EventTranslatorThreeArg<E, A, B, C> translator, A[] arg0, B[] arg1, C[] arg2);

    <A, B, C> boolean tryPublishEvents(
            EventTranslatorThreeArg<E, A, B, C> translator, int batchStartsAt,
            int batchSize, A[] arg0, B[] arg1, C[] arg2);

    void publishEvents(EventTranslatorVararg<E> translator, Object[]... args);

    void publishEvents(EventTranslatorVararg<E> translator, int batchStartsAt, int batchSize, Object[]... args);

    boolean tryPublishEvents(EventTranslatorVararg<E> translator, Object[]... args);

    boolean tryPublishEvents(EventTranslatorVararg<E> translator, int batchStartsAt, int batchSize, Object[]... args);

}
