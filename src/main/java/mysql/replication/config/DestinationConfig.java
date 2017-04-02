package mysql.replication.config;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wens on 15-10-14.
 */
public class DestinationConfig implements Serializable {

    private String destination;

    private String dbAddress;

    private String dbUser;

    private String dbPassword;

    private String runOn;

    private boolean runFail;

    private List<TableConfig> tableConfigs;

    private boolean stopped = true;


    public List<TableConfig> getTableConfigs() {
        return tableConfigs;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public void setDbAddress(String dbAddress) {
        this.dbAddress = dbAddress;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public String getRunOn() {
        return runOn;
    }

    public void setRunOn(String runOn) {
        this.runOn = runOn;
    }

    public boolean isRunFail() {
        return runFail;
    }

    public void setRunFail(boolean runFail) {
        this.runFail = runFail;
    }

    public static class TableConfig {

        private String tableName;

        private String topic ;

        private String redisHost;

        private String redisPassword;

        private Integer redisPort ;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getRedisHost() {
            return redisHost;
        }

        public void setRedisHost(String redisHost) {
            this.redisHost = redisHost;
        }

        public String getRedisPassword() {
            return redisPassword;
        }

        public void setRedisPassword(String redisPassword) {
            this.redisPassword = redisPassword;
        }

        public Integer getRedisPort() {
            return redisPort;
        }

        public void setRedisPort(Integer redisPort) {
            this.redisPort = redisPort;
        }
    }

    public void setTableConfigs(List<TableConfig> tableConfigs) {
        this.tableConfigs = tableConfigs;
    }
}
