package com.example.myblog.并发.E03_CallableAndFutureTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.LockSupport;

@FunctionalInterface
interface MyCallable<T> {
    T call();
}

interface MyFuture<T> {
    boolean isDone();
    T get();
}

class MyFutureTask<T> implements MyFuture<T> , Runnable {

    private final MyCallable<T> callable;
    private T result;
    private volatile boolean isDone = false;
    private Thread waiter;

    public MyFutureTask(MyCallable<T> callable) {
        this.callable = callable;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public T get() {
        if (!isDone()) {
            waiter = Thread.currentThread();
            LockSupport.park(waiter);
        }
        return result;
    }

    @Override
    public void run() {
        result = callable.call();
        isDone = true;
        LockSupport.unpark(waiter);
    }
}

public class Main {
    public static void main(String[] args) {

    }
}