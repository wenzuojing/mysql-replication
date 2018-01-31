package mysql.replication.web;

import com.alibaba.otter.canal.common.utils.JsonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mysql.replication.CoordinatorController;
import mysql.replication.ZkPathUtils;
import mysql.replication.ZookeeperUtils;
import mysql.replication.config.DestinationConfig;
import mysql.replication.config.DestinationConfigManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wens on 15-11-16.
 */
public class AllDestinationServlet extends HttpServlet {


    private DestinationConfigManager destinationConfigManager;


    public AllDestinationServlet(DestinationConfigManager destinationConfigManager) {
        this.destinationConfigManager = destinationConfigManager;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Set<String> allDestination = destinationConfigManager.getAllDestination();
        List<Map<String, Object>> retList = Lists.newArrayList();

        for (String dest : allDestination) {

            DestinationConfig config = destinationConfigManager.getDestinationConfig(dest);
            String monitorUrl = String.format("%s?destination=%s&mysqlAddress=%s&mysqlUser=%s&mysqlPassword=%s","/monitor", config.getDestination(), config.getDbAddress(), config.getDbUser(), config.getDbPassword());;

            HashMap<String, Object> map = Maps.newHashMap();
            map.put("destination", dest);
            map.put("monitorUrl", monitorUrl);
            map.put("stopped", config.isStopped());
            retList.add(map);
        }
        response.getWriter().write(JsonUtils.marshalToString(retList));
    }


}
