package com.example.myblog.并发;

import java.util.concurrent.*;

class MyCallable implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += i;
        }
        return sum;
    }
}

public class E02_CallableExample {
    public static void main(String[] args) {
        // 创建实现 Callable 接口的对象
        MyCallable myCallable = new MyCallable();
        // 创建 FutureTask 对象，并将 MyCallable 对象作为参数传递
        FutureTask<Integer> futureTask = new FutureTask<>(myCallable);
        // 创建 Thread 对象，并将 FutureTask 对象作为参数传递
        Thread thread = new Thread(futureTask);
        // 启动线程
        thread.start();
        try {
            // 获取线程执行结果
            Integer result = futureTask.get();
            System.out.println("线程执行结果: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}