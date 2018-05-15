package mysql.replication.sink;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mysql.replication.EmailUtil;
import mysql.replication.LoggerFactory;
import mysql.replication.canal.AbstractSink;
import mysql.replication.config.DestinationConfig;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by wens on 15-12-3.
 */
public class RocketMqSink extends AbstractSink {

    private final static Logger logger = LoggerFactory.getLogger();


    private DefaultMQProducer producer ;


    public RocketMqSink(DestinationConfig destinationConfig ) {
        super(destinationConfig);
    }

    @Override
    public void start() {
        producer = new DefaultMQProducer("mysql-binlog");
        producer.setNamesrvAddr(this.destinationConfig.getMqNamesrvAddr());
        producer.setRetryTimesWhenSendFailed(10);
        producer.setSendMsgTimeout(60000);
        try {
            producer.start();
        } catch (MQClientException e) {
            EmailUtil.sendMail("【MySQL数据复制服务】","RocketMq启动失败");
            throw new RuntimeException("Start rocket mq fail", e );

        }

    }

    @Override
    public void stop() {
        super.stop();
        producer.shutdown();
    }

    @Override
    protected AbstractSink.SinkWorker createSinkWorker(DestinationConfig.TableConfig tableConfig) {



        return new AbstractSink.SinkWorker(tableConfig) {

            @Override
            public void start() {
                super.start();

            }

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

                Message msg = new Message(tableConfig.getTopic() ,  tableConfig.getTableName() ,bytes);
                try {
                    producer.send(msg, new SelectMessageQueueByHash(), tableConfig.getTableName() );
                } catch (Exception e) {
                    handleSendFail(jsonObject, e);
                }
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
                Message msg = new Message(tableConfig.getTopic() , tableConfig.getTableName() ,bytes);
                try {
                    producer.send(msg, new SelectMessageQueueByHash(), tableConfig.getTableName() );
                } catch (Exception e) {
                    handleSendFail(jsonObject, e);
                }
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
                    rowList.add(row);
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableName" , tableConfig.getTableName()) ;
                jsonObject.put("event" , "delete") ;
                jsonObject.put("rowList" , rowList );
                byte[] bytes = jsonObject.toJSONString().getBytes(Charsets.UTF_8);
                Message msg = new Message(tableConfig.getTopic() , tableConfig.getTableName() ,bytes);
                try {
                    producer.send(msg, new SelectMessageQueueByHash(), tableConfig.getTableName() );
                } catch (Exception e) {
                    handleSendFail(jsonObject, e);
                }
            }

            @Override
            public void stop() {
                super.stop();
            }
        };
    }

    private void handleSendFail(JSONObject jsonObject, Exception e) {
        EmailUtil.sendMail("【MySQL数据复制服务】","发送消息导 RocketMq 失败");
        logger.error("[Send rocket mq msg fail] data = " + jsonObject.toJSONString(),e);
    }

}
