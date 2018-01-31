package mysql.replication;

import org.I0Itec.zkclient.ZkClient;

/**
 * Created by wens on 15-12-4.
 */
public class ZkPathUtils {

    private static String ROOT;

    public static void init(String root, ZkClient zkClient) {
        ROOT = root;

        if (!zkClient.exists(getDestinationConfigPath())) {
            zkClient.createPersistent(getDestinationConfigPath(), true);
        }

    }

    public static String getDestinationConfigPath() {
        return ROOT + "/destinations";
    }

    public static String getDestinationConfigPath(String destination) {
        return getDestinationConfigPath() + "/" + destination;
    }


}
