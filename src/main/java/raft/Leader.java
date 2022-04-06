package raft;

import config.Config;
import enums.EventEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.RequestBody;
import okhttp3.Response;
import pojo.AppendEntriesRPCRequest;
import pojo.AppendEntriesRPCResponse;
import pojo.Entry;
import pojo.ServerInfo;
import util.CalculateUtil;
import util.EntryUtil;
import util.OkHttpUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static config.Config.*;
import static enums.EventEnum.LeaderReceivedResponseClaimTermBigger;
import static enums.EventEnum.LeaderReceivedValidAppendEntriesRPC;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class Leader extends RaftClient {
    /**
     * 在当前status已经不是Candidate 转换成了其他状态(flollower)的情况下,因为Candidate的主线程不能立即结束需要通过一个变量判断是否需要执行下去 让它及时结束
     */
    private volatile boolean isStop = false;

    /**
     * otherServerInfoMap : 存储其他服务器上的信息
     * matchIndex: the log is all the same from matchIndex (include) to 0 in leader and follower
     * nextIndex: leader maintains nextIndex for each follower. which is the index of the next log entry the leader will send to
     * the follower
     */
    private final Map<String, ServerInfo> otherServerInfoMap = new ConcurrentHashMap(Config.otherServerUrl.size());

    {
        int nextIndex = Config.logEntryManager.getLastEntryIndex() + 1;
        for (String url : Config.otherServerUrl) {
            otherServerInfoMap.put(url, new ServerInfo(nextIndex, 0));
        }
        // leader 开始需要将自身commitIndex置0
        Config.commitIndex.set(0);
    }

    @Override
    public void run() {
        // 定时向所有客户端发送心跳
        while (!isStop) {
            List<String> otherServerUrl = Config.otherServerUrl;

            AppendEntriesRPCRequest appendEntriesRPCRequest = new AppendEntriesRPCRequest();

            for (String targetUrl : otherServerUrl) {
                build(appendEntriesRPCRequest, targetUrl);
                RequestBody requestBody = OkHttpUtil.buildRequestBody(appendEntriesRPCRequest);
                Call call = OkHttpUtil.getCall(targetUrl + "/appendEntriesRpc", requestBody);
                Config.executorService.submit(() -> {
                    if (isStop) {
                        return;
                    }
                    try {
                        Response response = call.execute();
                        if (response.isSuccessful()) {
                            AppendEntriesRPCResponse appendEntriesRPCResponse = com.alibaba.fastjson.JSON.parseObject(response.body().string(), AppendEntriesRPCResponse.class);
                            ServerInfo targetServerInfo = otherServerInfoMap.get(targetUrl);
                            if (appendEntriesRPCResponse.isSuccess()) {
                                // appendEntriesRPC成功
                                // 设置 nextIndex
                                setNextIndex(targetServerInfo, appendEntriesRPCRequest);
                                // 设置 matchIndex
                                setMatchIndex(targetServerInfo, getMatchIndex(appendEntriesRPCRequest));
                            } else {
                                // 1. response中要求更新term
                                if (appendEntriesRPCResponse.getTerm() > currentTerm.get()) {
                                    eventHandle(LeaderReceivedResponseClaimTermBigger);
                                }
                                // 2. 日志缺失或冲突 设置nextIndex
                                if (appendEntriesRPCResponse.getConflictTerm() == -1) {
                                    // 缺失
                                    targetServerInfo.setNextIndex(appendEntriesRPCResponse.getNextIndexSuggest());
                                } else {
                                    // 冲突 该情况下follower 返回的 nextIndexSuggest为follower在conflict-term下的第一个entry的index
                                    Entry entry = logEntryManager.getEntryByIndex(appendEntriesRPCResponse.getNextIndexSuggest());
                                    // 冲突情况讨论
                                    if (entry.getTerm() == appendEntriesRPCResponse.getConflictTerm()) {
                                        // 相等
                                        targetServerInfo.setNextIndex(appendEntriesRPCResponse.getNextIndexSuggest() + 1);
                                    } else if (entry.getTerm() > appendEntriesRPCResponse.getConflictTerm()) {
                                        // leader对应index的term更大
                                        // 继续向前搜索 比conflict-term大的 term 全部排除
                                        List<Entry> entries = logEntryManager.getEnties();
                                        for (int i = appendEntriesRPCResponse.getNextIndexSuggest() - 2; i > 0; i--) {
                                            if (entries.get(i).getTerm() < appendEntriesRPCResponse.getTerm()) {
                                                targetServerInfo.setNextIndex(entries.get(i).getIndex() + 1);
                                                break;
                                            }
                                        }
                                    } else {
                                        // follower对应的term更大
                                        targetServerInfo.setNextIndex(appendEntriesRPCResponse.getNextIndexSuggest());
                                    }
                                }

                            }
                            log.info("[op:run] appendEntriesRPCResponse={}", appendEntriesRPCResponse);
                        } else {
                            log.info("[op:run] hearbeat send fail url={} response data={}", targetUrl, response.body().string());
                        }
                    } catch (ConnectException e) {
                        log.info("[op:run] {}", e.getMessage());
                    } catch (IOException e) {
                        log.error("[op:run] catch-exception", e);
                    }
                });
            }

            try {
                Thread.sleep(Config.leaderHeartBeatSendInterval);
            } catch (InterruptedException e) {
                log.error("[op:run] catch-exception", e);
            }
        }
    }

    @Override
    protected AppendEntriesRPCResponse checkTerm(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        AppendEntriesRPCResponse appendEntriesRPCResponse = super.checkTerm(appendEntriesRPCRequest);
        if (appendEntriesRPCResponse == null) {
            eventHandle(LeaderReceivedValidAppendEntriesRPC);
        }
        return appendEntriesRPCResponse;
    }

    private void build(AppendEntriesRPCRequest appendEntriesRPCRequest, String targetUrl) {
        appendEntriesRPCRequest.setTerm(currentTerm.get());
        appendEntriesRPCRequest.setLeaderId(Config.clientId);
        int nextIndex = otherServerInfoMap.get(targetUrl).getNextIndex();
        log.info("[op:build] targetUrl={} nextIndex={}", targetUrl, nextIndex);
        appendEntriesRPCRequest.setEntries(logEntryManager.subEntries(nextIndex));
        Entry prevEntry = Config.logEntryManager.getEntryByIndex(nextIndex - 1);
        // notice: 需要处理entries为空的情况 prevEntry == null
        appendEntriesRPCRequest.setPrevLogIndex(prevEntry == null ? -1 : prevEntry.getIndex());
        appendEntriesRPCRequest.setPrevLogTerm(prevEntry == null ? -1 : prevEntry.getTerm());
        appendEntriesRPCRequest.setLeaderCommit(Config.commitIndex.get());
    }

    @Override
    public void eventHandle(EventEnum eventEnum) {
        log.info("[op:eventHandle] event={}", eventEnum);
        switch (eventEnum) {
            case LeaderReceivedResponseClaimTermBigger:
            case LeaderReceivedValidAppendEntriesRPC:
                isStop = true;
                RaftClientProvider.switchRaftClient(new Follower());
                break;
            default:
        }
    }

    private int getMatchIndex(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        if (appendEntriesRPCRequest.getEntries() == null || appendEntriesRPCRequest.getEntries().isEmpty()) {
            return appendEntriesRPCRequest.getPrevLogIndex();
        } else {
            List<Entry> entries = appendEntriesRPCRequest.getEntries();
            return entries.get(entries.size() - 1).getIndex();
        }
    }

    private void setMatchIndex(ServerInfo serverInfo, int newMatchIndex) {
        int oldMatchIndex = serverInfo.getMatchIndex();
        if (newMatchIndex != oldMatchIndex) {
            serverInfo.setMatchIndex(newMatchIndex);
            // 触发commitIndex check
            checkCommitIndex();
        }
    }

    private void checkCommitIndex() {
        Set<String> keySet = otherServerInfoMap.keySet();
        int total = keySet.size();
        int biggerCount = 0;
        boolean needUpdate = false;
        int majority = CalculateUtil.divideRoundsUp(total, 2);
        PriorityQueue<Integer> biggerMatchIndexPriorityQueue = new PriorityQueue<>();
        for (String key : keySet) {
            ServerInfo serverInfo = otherServerInfoMap.get(key);
            if (serverInfo.getMatchIndex() > commitIndex.get()) {
                biggerCount++;
                biggerMatchIndexPriorityQueue.offer(serverInfo.getMatchIndex());
                if (!needUpdate && biggerCount >= majority) {
                    // 大多数已同步 更新commitIndex
                    needUpdate = true;
                }
            }
        }
        if (needUpdate) {
            // 更新算法：
            // 取biggerMatchIndex中第 majority 大的 (即第 size - majority 小的)
            for (int i = 0; i < biggerMatchIndexPriorityQueue.size() - majority - 1; i++) {
                biggerMatchIndexPriorityQueue.poll();
            }
            commitIndex.set(biggerMatchIndexPriorityQueue.poll());
            log.info("[op:checkCommitIndex] commitIndex={}", commitIndex.get());
        }
    }

    private void setNextIndex(ServerInfo serverInfo, AppendEntriesRPCRequest appendEntriesRPCRequest) {
        List<Entry> sendEntries = appendEntriesRPCRequest.getEntries();
        if (sendEntries == null || sendEntries.isEmpty()) {
            return;
        }
        serverInfo.setNextIndex(EntryUtil.getLastEntryIndex(sendEntries) + 1);
    }
}