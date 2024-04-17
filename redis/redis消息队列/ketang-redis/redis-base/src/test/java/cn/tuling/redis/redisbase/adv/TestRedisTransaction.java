package cn.tuling.redis.redisbase.adv;

import cn.tuling.redis.adv.RedisTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestRedisTransaction {

    @Autowired
    private RedisTransaction redisTransaction;

    @Test
    public void testTransaction() {
        redisTransaction.transaction();
    }

}
