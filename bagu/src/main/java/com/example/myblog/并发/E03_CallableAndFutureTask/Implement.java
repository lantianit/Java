package com.example.myblog.并发.E03_CallableAndFutureTask;

import java.util.concurrent.locks.LockSupport;

@FunctionalInterface
interface MyCallable<T> {
    public T call() throws Exception;
}

interface MyFutureTask<T> {
    public T get() throws Exception;
    boolean isDone();
}

class MyFutureTaskImpl<T> implements MyFutureTask<T>, Runnable {

    private final MyCallable<T> callable;
    private T result;
    private Thread waiter;
    private volatile boolean isDone = false;

    MyFutureTaskImpl(MyCallable<T> callable) {
        this.callable = callable;
    }


    @Override
    public T get() throws Exception {
        if (!isDone()) {
            waiter = Thread.currentThread();
            LockSupport.park(waiter);
        }
        return result;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void run() {
        try {
            result = callable.call();
            isDone = true;
            LockSupport.unpark(waiter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}