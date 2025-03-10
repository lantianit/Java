package com.example.consumer.config;

import feign.RetryableException;
import feign.Retryer;

import java.time.LocalDateTime;

/**
 * 自定义超时重传类
 */
public class CustomRetryer implements Retryer {

    private final int maxAttempts;  // 最大尝试次数
    private final long backoff;     // 超时间隔时间
    int attempt; // 当前尝试次数

    public CustomRetryer() {
        this.maxAttempts = 3;
        this.backoff = 1000L;
        this.attempt = 0;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            throw e;
        }
        long interval = this.backoff; // 重试间隔时间
        System.out.println(LocalDateTime.now() + " | 执行一次重试：" + interval);
        try {
            Thread.sleep(interval * attempt);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Retryer clone() {
        return new CustomRetryer();
    }
}
