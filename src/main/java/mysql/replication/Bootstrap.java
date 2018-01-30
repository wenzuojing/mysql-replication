package mysql.replication;

import mysql.replication.canal.ControllerService;
import mysql.replication.config.DestinationConfigManager;
import mysql.replication.web.WebConsole;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

/**
 * Created by wens on 15-10-14.
 */
public class Bootstrap {

    private static final Logger logger = LoggerFactory.getLogger();

    public static void main(String[] args) throws Throwable {
        try {

            LogbackConfigLoader.load();
            Conf conf = Conf.getInstance();
            ZookeeperUtils.init(conf);
            final DestinationConfigManager destinationConfigManager = new DestinationConfigManager();
            final ControllerService controllerService = new ControllerService(conf);
            controllerService.setDestinationConfigManager(destinationConfigManager);
            final WebConsole webConsole = new WebConsole(conf, controllerService, destinationConfigManager, coordinatorController);

            controllerService.start();

            logger.info("## start the controller service success.");
            webConsole.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        webConsole.stop();
                        controllerService.stop();
                        logger.info("## stop the controller service success");
                    } catch (Throwable e) {
                        logger.warn("##something goes wrong when stopping canal Server:\n{}",
                                ExceptionUtils.getFullStackTrace(e));
                    } finally {
                        logger.info("## canal server is down.");
                    }

                }
            });

            logger.info("## All Component is ready.");

        } catch (Throwable e) {
            logger.error("## Something goes wrong when starting up the canal Server:\n{}",
                    ExceptionUtils.getFullStackTrace(e));
            System.exit(0);
        }
    }

}
