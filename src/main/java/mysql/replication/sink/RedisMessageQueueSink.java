package mysql.replication.sink;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.github.wens.mq.RedisMessageQueue;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mysql.replication.LoggerFactory;
import mysql.replication.canal.AbstractSink;
import mysql.replication.config.DestinationConfig;

import mysql.replication.redis.RedisUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by wens on 15-12-3.
 */
public class RedisMessageQueueSink extends AbstractSink {

    private final static Logger logger = LoggerFactory.getLogger();




    public RedisMessageQueueSink(DestinationConfig destinationConfig ) {
        super(destinationConfig);
    }

    @Override
    public void start() {

    }

    @Override
    protected AbstractSink.SinkWorker createSinkWorker(DestinationConfig.TableConfig tableConfig) {
        return new AbstractSink.SinkWorker(tableConfig) {


            private  RedisMessageQueue redisMessageQueue = RedisUtils.createRedisMessageQueue(tableConfig.getRedisHost(),tableConfig.getRedisPort(),tableConfig.getRedisPassword() );


            @Override
            protected void handleInsert(List<CanalEntry.RowData> rowDatasList) {

                List<Map<String, String>> rowList  = new ArrayList<>(rowDatasList.size()) ;
                for (CanalEntry.RowData rowData : rowDatasList) {
                    List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();

                    if (logger.isDebugEnabled()) {
                        logger.debug("{} receive insert data:\n{}", tableConfig.getTableName(), toString(afterColumnsList));
                    }
                    Map<String, String> row = Maps.newHashMap();
                    for (CanalEntry.Column c : afterColumnsList) {
                        row.put(c.getName(), c.getValue());
                    }
                    rowList.add(row);
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableName" , tableConfig.getTableName()) ;
                jsonObject.put("event" , "insert") ;
                jsonObject.put("rowList" , rowList );
                byte[] bytes = jsonObject.toJSONString().getBytes(Charsets.UTF_8);
                redisMessageQueue.publish(tableConfig.getTopic() ,bytes );


            }


            @Override
            protected void handleUpdate(List<CanalEntry.RowData> rowDatasList) {

                List<Map<String, String>> rowList  = new ArrayList<>(rowDatasList.size()) ;
                Set<String> updateColumns = Sets.newHashSet();
                for (CanalEntry.RowData rowData : rowDatasList) {
                    List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} receive update data:\n{}", tableConfig.getTableName(), toString(afterColumnsList));
                    }
                    boolean drop = true;
                    Map<String, String> row = Maps.newHashMap();

                    for (CanalEntry.Column c : afterColumnsList) {

                        if (c.getUpdated()) {
                            updateColumns.add(c.getName());
                            drop = false;
                        }

                        row.put(c.getName(), c.getValue());
                    }
                    if (!drop) {
                        rowList.add(row);
                    }
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableName" , tableConfig.getTableName()) ;
                jsonObject.put("event" , "update") ;
                jsonObject.put("rowList" , rowList );
                jsonObject.put("updateColumns" , updateColumns );
                byte[] bytes = jsonObject.toJSONString().getBytes(Charsets.UTF_8);
                redisMessageQueue.publish(tableConfig.getTopic() ,bytes );
            }

            @Override
            protected void handleDelete(List<CanalEntry.RowData> rowDatasList) {
                List<Map<String, String>> rowList  = new ArrayList<>(rowDatasList.size()) ;
                for (CanalEntry.RowData rowData : rowDatasList) {
                    List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} receive delete data :\n {}", tableConfig.getTableName(), toString(beforeColumnsList));
                    }
                    Map<String, String> row = Maps.newHashMap();
                    for (CanalEntry.Column c : beforeColumnsList) {
                        if (c.getIsKey()) {
                            row.put(c.getName(), c.getValue());
                        }
                    }
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableName" , tableConfig.getTableName()) ;
                jsonObject.put("event" , "delete") ;
                jsonObject.put("rowList" , rowList );
                byte[] bytes = jsonObject.toJSONString().getBytes(Charsets.UTF_8);
                redisMessageQueue.publish(tableConfig.getTopic() ,bytes );
            }

            @Override
            public void stop() {
                super.stop();
                this.redisMessageQueue.close();
            }
        };
    }

}
