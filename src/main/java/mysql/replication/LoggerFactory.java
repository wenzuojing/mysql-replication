package mysql.replication;

import org.slf4j.Logger;

/**
 * Created by wens on 15-10-29.
 */
public class LoggerFactory {

    public static final String LOG_NAME = "mysql-replication";

    public static Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger(LoggerFactory.LOG_NAME);
    }
}
