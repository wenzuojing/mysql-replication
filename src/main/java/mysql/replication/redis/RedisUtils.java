package mysql.replication.redis;

import com.github.wens.mq.RedisMessageQueue;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

/**
 * Created by wens on 2017/3/27.
 */
public class RedisUtils {

    public static RedisMessageQueue createRedisMessageQueue(String host , int port ,String password ){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(5);
        config.setMaxTotal(5);
        config.setMinIdle(2);
        return new RedisMessageQueue( new JedisPool(config,host ,port ,6000 ,password ) );
    }

    public static void main(String[] args) {
        RedisMessageQueue redisMessageQueue = createRedisMessageQueue("118.89.27.94", 12430, "8#tgU9UTmg");
        redisMessageQueue.publish("test","test".getBytes());
        System.out.println("OK");
    }


}
