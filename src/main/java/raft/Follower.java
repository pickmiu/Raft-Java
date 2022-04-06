package raft;

import config.Config;
import enums.EventEnum;
import lombok.extern.slf4j.Slf4j;
import pojo.AppendEntriesRPCRequest;
import pojo.AppendEntriesRPCResponse;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class Follower extends RaftClient {

    private volatile AtomicBoolean anyHeartbeatReceived = new AtomicBoolean(false);

    @Override
    public void run() {
        log.info("[op:run] follower thread start.");
        // 持续监听心跳，维持election-timeout
        while (true) {
            try {
                Thread.sleep(Config.electionTimeoutMilliSeconds);
                if (!checkAnyHeartbeatReceived()) {
                    break;
                }
            } catch (InterruptedException e) {
                log.error("[op:run] catch-exception", e);
            }
        }
        log.info("[op:run] follower thread stop.");
    }

    private boolean checkAnyHeartbeatReceived() {
        if (anyHeartbeatReceived.get()) {
            anyHeartbeatReceived.compareAndSet(true, false);
            log.info("[op:checkAnyHeartbeatReceived] election timeout reset");
            return true;
        } else {
            // 如果没有收到 发送election timeout事件
            eventHandle(EventEnum.ElectionTimeOut);
            return false;
        }
    }

    /**
     * follower 重写checkterm 需要在checkTerm期间刷新心跳
     * @param appendEntriesRPCRequest
     * @return
     */
    @Override
    protected AppendEntriesRPCResponse checkTerm(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        AppendEntriesRPCResponse appendEntriesRPCResponse = super.checkTerm(appendEntriesRPCRequest);
        if (appendEntriesRPCResponse == null) {
            // 刷新心跳
            anyHeartbeatReceived.compareAndSet(false, true);
        }
        return appendEntriesRPCResponse;
    }

    @Override
    public void eventHandle(EventEnum eventEnum) {
        log.info("[op:eventHandle] event={}", eventEnum);
        switch (eventEnum) {
            case ElectionTimeOut:
                RaftClientProvider.switchRaftClient(new Candidate());
                break;
            default:
        }
    }
}