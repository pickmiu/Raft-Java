package raft;

import config.Config;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class RaftClientProvider {

    private static volatile RaftClient raftClient;

    public static synchronized void switchRaftClient(RaftClient raftClient) {
        RaftClientProvider.raftClient = raftClient;
        Config.executorService.submit(RaftClientProvider.raftClient);
    }

    public static RaftClient getRaftClient() {
        return raftClient;
    }
}