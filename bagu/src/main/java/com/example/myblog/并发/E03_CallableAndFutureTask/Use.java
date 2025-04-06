package com.example.myblog.并发.E03_CallableAndFutureTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class Mycallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        Thread.sleep(3000);
        return "返回值";
    }
}

public class Use {
    public static void main(String[] args) {
        Mycallable mycallable = new Mycallable();
        FutureTask<String> futureTask = new FutureTask<>(mycallable);
        new Thread(futureTask).start();
        try {
            String s = futureTask.get();
//            System.out.println(s);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
