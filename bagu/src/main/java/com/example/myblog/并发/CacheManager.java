package com.example.myblog.并发;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

// 缓存项类，实现 Delayed 接口
class CacheItem implements Delayed {
    private String key;
    private long expireTime;

    public CacheItem(String key, long delayTime) {
        this.key = key;
        // 计算缓存项的过期时间
        this.expireTime = System.currentTimeMillis() + delayTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        // 计算剩余延迟时间
        return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        // 定义排序规则，根据延迟时间排序
        return Long.compare(this.expireTime, ((CacheItem) o).expireTime);
    }

    public String getKey() {
        return key;
    }
}

// 缓存管理类
public class CacheManager {
    private Map<String, Object> cache = new HashMap<>();
    private DelayQueue<CacheItem> delayQueue = new DelayQueue<>();

    // 添加缓存项
    public void put(String key, Object value, long delayTime) {
        cache.put(key, value);
        delayQueue.put(new CacheItem(key, delayTime));
    }

    // 获取缓存项
    public Object get(String key) {
        return cache.get(key);
    }

    // 启动缓存清理线程
    public void startCleaning() {
        new Thread(() -> {
            try {
                while (true) {
                    // 从队列中取出到期的缓存项
                    CacheItem item = delayQueue.take();
                    // 从缓存中移除到期的缓存项
                    cache.remove(item.getKey());
                    System.out.println("缓存项 " + item.getKey() + " 已清理");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public static void main(String[] args) {
        CacheManager manager = new CacheManager();
        // 添加缓存项
        manager.put("key1", "value1", 5 * 1000);
        // 启动缓存清理线程
        manager.startCleaning();
    }
}