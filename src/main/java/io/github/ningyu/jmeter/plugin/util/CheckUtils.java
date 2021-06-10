package io.github.ningyu.jmeter.plugin.util;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.retry.RetryNTimes;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.utils.ConfigValidationUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.util.List;

public class CheckUtils {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static void checkZookeeper(ReferenceConfig<?> reference) throws Exception {
        if (!Constants.REGISTRY_ZOOKEEPER.equals(reference.getRegistry().getProtocol())) return;

        log.info("check zookeeper connect");
        List<URL> urls = ConfigValidationUtils.loadRegistries(reference, false);
        if (urls.size() > 0) {
            URL url = urls.get(0);
            try (CuratorZookeeperClient client = new CuratorZookeeperClient(url.getBackupAddress(), 60000, 60000,
                    null, new RetryNTimes(0, 1000));) {
                client.start();
                if (!client.blockUntilConnectedOrTimedOut()) {
                    client.close();
                    log.error("zookeeper not connected");
                    throw new IllegalStateException("zookeeper not connected");
                }
            }
        }
    }
}
