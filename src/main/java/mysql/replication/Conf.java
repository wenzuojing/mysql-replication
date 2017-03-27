package mysql.replication;

import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by wens on 15-10-14.
 */
public class Conf {

    private String id;

    private String zookeeperServer;

    private String zookeeperRootPath;

    private int canalBatchSize;

    private int webConsolePort;

    private String httpEndpoin;

    public void setCanalBatchSize(int canalBatchSize) {
        this.canalBatchSize = canalBatchSize;
    }

    public int getWebConsolePort() {
        return webConsolePort;
    }

    public void setWebConsolePort(int webConsolePort) {
        this.webConsolePort = webConsolePort;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZookeeperRootPath() {
        return zookeeperRootPath;
    }

    public void setZookeeperRootPath(String zookeeperRootPath) {
        this.zookeeperRootPath = zookeeperRootPath;
    }

    private static Conf INSTANCE = new Conf();

    public static Conf getInstance() {
        return INSTANCE;
    }

    private Conf() {

        String conf = System.getProperty("canal.conf", "classpath:config.properties");
        Properties properties = new Properties();
        if (conf.startsWith(Constant.CLASSPATH_URL_PREFIX)) {
            conf = StringUtils.substringAfter(conf, Constant.CLASSPATH_URL_PREFIX);
            try {
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(conf));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                properties.load(new FileInputStream(conf));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        init(properties);

    }


    private void init(Properties properties) {
        this.zookeeperServer = properties.getProperty("zk.server");
        this.id = properties.getProperty("id");
        this.zookeeperRootPath = properties.getProperty("zk.root.path", "/mysql-redis-replicate");
        String cbs = properties.getProperty("canal.batch.size");
        this.canalBatchSize = cbs == null || cbs.length() == 0 ? 100 : Integer.parseInt(cbs);
        this.webConsolePort = Integer.parseInt(properties.getProperty("web.console.port"));
        this.httpEndpoin = properties.getProperty("http.endpoint", "http://localhost:" + this.getWebConsolePort() + "/endpoint");
    }

    public String getZookeeperServer() {
        return zookeeperServer == null ? "localhost:2181" : zookeeperServer;
    }

    public void setZookeeperServer(String zookeeperServer) {
        this.zookeeperServer = zookeeperServer;
    }

    public int getCanalBatchSize() {
        return canalBatchSize;
    }

    public String getHttpEndpoin() {
        return httpEndpoin;
    }
}
