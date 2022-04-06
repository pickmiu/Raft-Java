import config.Config;
import lombok.extern.slf4j.Slf4j;
import network.HttpServer;
import raft.Follower;
import raft.RaftClientProvider;

import java.io.IOException;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class MainApplication {
    public static void main(String[] args) {
        int port = Config.port;
        try {
            new HttpServer(port);
        } catch (IOException e) {
            log.info("[op:main] port {} is already in use.", port);
        }
        // 启动服务
        RaftClientProvider.switchRaftClient(new Follower());
    }
}