package mysql.replication.canal;

import com.alibaba.otter.canal.common.zookeeper.ZookeeperPathUtils;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import mysql.replication.Conf;
import mysql.replication.LoggerFactory;
import mysql.replication.ZookeeperUtils;
import mysql.replication.config.DestinationConfig;
import mysql.replication.config.DestinationConfigManager;
import mysql.replication.sink.RocketMqSink;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ControllerService {

    private final Logger logger = LoggerFactory.getLogger();

    private final ConcurrentHashMap<String, MessagePuller> runningTasks = new ConcurrentHashMap<>();
    private DestinationConfigManager destinationConfigManager;

    private final Conf conf;

    private CanalServerWithEmbedded server;

    private ScheduledExecutorService scheduledExecutorService;

    public ControllerService(Conf conf) throws IOException {
        this.conf = conf;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void setDestinationConfigManager(DestinationConfigManager destinationConfigManager) {
        this.destinationConfigManager = destinationConfigManager;
    }

    public void start() {
        server = new CanalServerWithEmbedded();
        server.setCanalInstanceGenerator(new CanalInstanceGenerator() {

            public CanalInstance generate(String destination) {
                return new CanalInstanceWithManager(buildCanal(destination), buildFilterTable(destination));
            }


        });
        server.start();
    }

    public void stopTask(String destination) {
        MessagePuller task = runningTasks.get(destination);
        if (task != null) {
            task.safeStop();
            runningTasks.remove(destination);
        }

        if (server.isStart(destination)) {
            server.stop(destination);
        }
        logger.info("## Stopped destination task:" + destination);
    }

    public void startTask(String destination) {
        DestinationConfig destinationConfig = getDestinationConfig(destination);
        MessagePuller task = runningTasks.get(destination);
        if (task != null) {
            task.safeStop();
        }
        server.start(destination);
        MessagePuller puller = new MessagePuller(conf.getCanalBatchSize(), destination, server, new RocketMqSink(destinationConfig), scheduledExecutorService);
        puller.start();
        runningTasks.put(destination, puller);
        logger.info("## Started destination task:" + destinationConfig.getDestination());
    }


    public void stop() {
        for (String dest : runningTasks.keySet()) {
            stopTask(dest);
        }
        server.stop();
        scheduledExecutorService.shutdownNow();
    }


    private String buildFilterTable(String destination) {
        DestinationConfig destinationConfig = getDestinationConfig(destination);
        if (destinationConfig.getTableConfigs() == null || destinationConfig.getTableConfigs().size() == 0) {
            return "not-exist-table";
        }

        StringBuilder sb = new StringBuilder(100 * destinationConfig.getTableConfigs().size());

        for (DestinationConfig.TableConfig tableConfig : destinationConfig.getTableConfigs()) {
            sb.append(tableConfig.getTableName().replaceFirst("\\.", "\\\\."));
            sb.append(",");
        }
        sb.append("not-exist-table");
        return sb.toString();
    }


    private Canal buildCanal(String destination) {
        DestinationConfig destinationConfig = getDestinationConfig(destination);

        recovery(destinationConfig);

        Canal canal = new Canal();
        canal.setId(1L);
        canal.setName(destination);

        CanalParameter parameter = new CanalParameter();

        parameter.setZkClusters(Arrays.asList(conf.getZookeeperServer().split(",")));

        parameter.setMetaMode(CanalParameter.MetaMode.ZOOKEEPER);
        parameter.setHaMode(CanalParameter.HAMode.HEARTBEAT);
        parameter.setIndexMode(CanalParameter.IndexMode.MEMORY_META_FAILBACK);

        parameter.setStorageMode(CanalParameter.StorageMode.MEMORY);
        parameter.setMemoryStorageBufferSize(32 * 1024);

        parameter.setSourcingType(CanalParameter.SourcingType.MYSQL);

        String[] dbAddresses = destinationConfig.getDbAddress().split(",");

        List<InetSocketAddress> dbAddressList = new ArrayList<>(dbAddresses.length);

        for (String address : dbAddresses) {
            String[] strings = address.split(":");
            String ip = strings[0];
            int port = 3306;
            if (strings.length == 2) {
                port = Integer.parseInt(strings[1]);
            }
            dbAddressList.add(new InetSocketAddress(ip, port));
        }

        parameter.setDbAddresses(dbAddressList);
        parameter.setDbUsername(destinationConfig.getDbUser());
        parameter.setDbPassword(destinationConfig.getDbPassword());

        parameter.setSlaveId(1888L);

        parameter.setDefaultConnectionTimeoutInSeconds(30);
        parameter.setConnectionCharset("UTF-8");
        parameter.setConnectionCharsetNumber((byte) 33);
        parameter.setReceiveBufferSize(8 * 1024);
        parameter.setSendBufferSize(8 * 1024);

        parameter.setDetectingEnable(false);


        canal.setCanalParameter(parameter);
        return canal;
    }

    private void recovery(DestinationConfig destinationConfig) {
        ZookeeperUtils.deleteRecursive(ZookeeperPathUtils.getBatchMarkPath(destinationConfig.getDestination(), (short) 1));
    }

    public DestinationConfig getDestinationConfig(String destination) {
        return destinationConfigManager.getDestinationConfig(destination);
    }


}
