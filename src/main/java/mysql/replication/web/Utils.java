package mysql.replication.web;

import com.alibaba.fastjson.JSONObject;
import com.github.wens.mq.RedisMessageQueue;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import mysql.replication.config.DestinationConfig;
import mysql.replication.redis.RedisUtils;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by wens on 15-11-16.
 */
public class Utils {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Load mysql driver fail.");
        }
    }

    public static String testConfig(DestinationConfig destinationConfig) throws SQLException {
        Connection conn = null;
        try {
            StringBuilder sb = new StringBuilder();
            conn = DriverManager.getConnection(String.format("jdbc:mysql://%s?connectTimeout=4000", destinationConfig.getDbAddress()), destinationConfig.getDbUser(), destinationConfig.getDbPassword());
            for (DestinationConfig.TableConfig tableConfig : destinationConfig.getTableConfigs()) {

                PreparedStatement statement = conn.prepareStatement("select * from  " + tableConfig.getTableName() + "  limit 1 ");
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    Map<String, String> row = Maps.newHashMap();
                    int columnCount = resultSet.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = resultSet.getMetaData().getColumnName(i);
                        row.put(columnName, resultSet.getString(columnName));
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("event" , "insert") ;
                    jsonObject.put("rowList" , Arrays.asList(row));
                    byte[] bytes = jsonObject.toJSONString().getBytes(Charsets.UTF_8);
                    RedisMessageQueue redisMessageQueue = RedisUtils.createRedisMessageQueue(tableConfig.getRedisHost(), tableConfig.getRedisPort(),tableConfig.getRedisPassword());
                    redisMessageQueue.publish(tableConfig.getTopic() ,bytes );
                    redisMessageQueue.close();
                    sb.append(tableConfig.getTableName() + " is  ok\r\n");
                }
                resultSet.close();
                statement.close();
            }
            return sb.toString();
        }catch (Exception e ){
            return e.getMessage() ;
        }finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

}
