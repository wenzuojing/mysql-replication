package mysql.replication;

import com.alibaba.otter.canal.common.utils.AddressUtils;
import mysql.replication.canal.ControllerService;
import mysql.replication.config.DestinationConfig;
import mysql.replication.config.DestinationConfigManager;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by wens on 15/12/5.
 */
public class CoordinatorController implements Lifecycle {

    private final Logger logger = LoggerFactory.getLogger();

    private ControllerService controllerService;

    private DestinationConfigManager destinationConfigManager;


    public CoordinatorController(ControllerService controllerService ,DestinationConfigManager destinationConfigManager) {
        this.controllerService = controllerService;
        this.destinationConfigManager = destinationConfigManager;
    }


    public boolean stopDestination(String destination) {
        DestinationConfig destinationConfig = destinationConfigManager.getDestinationConfig(destination);
        doStopDestination(destination);
        destinationConfig.setStopped(true);
        destinationConfigManager.saveOrUpdate(destinationConfig);
        return true ;

    }

    private void doStopDestination(String destination) {
        controllerService.stopTask(destination);
    }

    public boolean startDestination(String destination) {

        DestinationConfig destinationConfig = destinationConfigManager.getDestinationConfig(destination);
        if (!destinationConfig.isStopped()) {
            doStopDestination(destination);
        }
        doStartDestination(destination);
        destinationConfig.setStopped(false);
        destinationConfigManager.saveOrUpdate(destinationConfig);
        return true ;

    }

    private void doStartDestination(String destination) {
        controllerService.startTask(destination);
    }

    public void deleteDestination(String destination) {
        DestinationConfig destinationConfig = destinationConfigManager.getDestinationConfig(destination);
        if (!destinationConfig.isStopped()) {
            doStopDestination(destination);
        }
        destinationConfigManager.delete(destination);
    }

    @Override
    public void start() {
        Set<String> all = destinationConfigManager.getAllDestination();
        for(String dest : all ){
            DestinationConfig destinationConfig = destinationConfigManager.getDestinationConfig(dest);
            if(!destinationConfig.isStopped()){
                doStartDestination(dest);
            }

        }
    }

    @Override
    public void stop() {
        Set<String> all = destinationConfigManager.getAllDestination();
        for(String dest : all ){
            DestinationConfig destinationConfig = destinationConfigManager.getDestinationConfig(dest);
            if(!destinationConfig.isStopped()){
                doStopDestination(dest);
            }
        }
    }
}
