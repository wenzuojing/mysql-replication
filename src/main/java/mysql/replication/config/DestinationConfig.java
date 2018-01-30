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

    private String mqNamesrvAddr ;


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

    public String getMqNamesrvAddr() {
        return mqNamesrvAddr;
    }

    public void setMqNamesrvAddr(String mqNamesrvAddr) {
        this.mqNamesrvAddr = mqNamesrvAddr;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    

    public static class TableConfig {

        private String tableName;

        private String topic ;


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

    }

    public void setTableConfigs(List<TableConfig> tableConfigs) {
        this.tableConfigs = tableConfigs;
    }
}
