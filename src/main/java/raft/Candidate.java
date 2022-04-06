package raft;

import com.alibaba.fastjson.JSON;
import config.Config;
import enums.EventEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.RequestBody;
import okhttp3.Response;
import pojo.*;
import util.OkHttpUtil;
import util.RandomUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import static config.Config.currentTerm;
import static enums.EventEnum.CandidateReceivedValidAppendEntriesRPC;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class Candidate extends RaftClient {

    private List<RequestVoteRPCResponse> electionResultList = new Vector<>(Config.otherServerUrl.size());
    /**
     * 在当前status已经不是Candidate 转换成了其他状态(flollower)的情况下,因为Candidate的主线程不能立即结束需要通过一个变量判断是否需要执行下去 让它及时结束
     */
    private volatile boolean isStop = false;

    @Override
    public void run() {
        while (!isStop) {
            log.info("[op:run] candidate thread start.");
            int currentTerm = Config.currentTerm.incrementAndGet();
            log.info("[op:run] currentTerm={}", currentTerm);
            Config.votedFor = Config.clientId;
            // 清空存储结果的列表
            electionResultList.clear();
            // 向其他服务器发送requestVoteRPC;
            List<String> otherServerUrls = Config.otherServerUrl;

            RequestVoteRPCRequest requestVoteRPCRequest = new RequestVoteRPCRequest();
            build(requestVoteRPCRequest);

            RequestBody requestBody = OkHttpUtil.buildRequestBody(requestVoteRPCRequest);
            CountDownLatch countDownLatch = new CountDownLatch(otherServerUrls.size());
            if (isStop) {
                break;
            }
            for (String url : otherServerUrls) {
                Call call = OkHttpUtil.getCall(url + "/requestVoteRpc", requestBody);
                Config.executorService.submit(() -> {
                    if (isStop) {
                        return;
                    }
                    try {
                        long startTime = System.currentTimeMillis();
                        Response response = call.execute();
                        if (response.isSuccessful()) {
                            log.info("[op:run] one requestVoteRpc takes {} ms", System.currentTimeMillis() - startTime);
                            RequestVoteRPCResponse requestVoteRPCResponse = JSON.parseObject(response.body().string(), RequestVoteRPCResponse.class);
                            electionResultList.add(requestVoteRPCResponse);
                        } else {
                            log.info("[op:run] requestVoteRPC send fail url={} response data={}", url, response.body().string());
                        }
                    } catch (ConnectException e) {
                        log.info("[op:run] {}", e.getMessage());
                    } catch (IOException e) {
                        log.error("[op:run] catch-exception", e);
                    }
                    countDownLatch.countDown();
                    log.info("[op:run] one job has finished. still {} remain", countDownLatch.getCount());
                });
            }

            try {
                log.info("[op:run] countDownLatch start waiting {} job finish.", otherServerUrls.size());
                // 倒计时器等待所有结果返回再继续执行
                countDownLatch.await();
                log.info("[op:run] countDownLatch all job finished");
            } catch (InterruptedException e) {
                log.error("[op:run] catch-exception", e);
            }

            // ps: candidate continues in this status until one of three things happens:
            //     a. it wins the election
            //     b. another server establishes itself as leader
            //     c. a period of time goes by with no winner
            if (isStop) {
                break;
            }
            // 赢得选举
            if (isWin(electionResultList)) {
                // 切换到leader
                eventHandle(EventEnum.WinElection);
                break;
            }
            // timeout
            try {
                int randomTimeout = RandomUtil.generateRandom(Config.conflictTimeoutMin, Config.conflictTimeoutMax);
                log.info("[op:run] randomTimeout={}", randomTimeout);
                Thread.sleep(randomTimeout);
            } catch (InterruptedException e) {
                log.error("[op:run] catch-exception", e);
            }
        }
        log.info("[op:run] candidate thread stop.");
    }

    @Override
    protected AppendEntriesRPCResponse checkTerm(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        AppendEntriesRPCResponse appendEntriesRPCResponse = super.checkTerm(appendEntriesRPCRequest);
        if (appendEntriesRPCResponse == null) {
            eventHandle(CandidateReceivedValidAppendEntriesRPC);
        }
        return appendEntriesRPCResponse;
    }

    @Override
    public void eventHandle(EventEnum eventEnum) {
        log.info("[op:eventHandle] event={}", eventEnum);
        switch (eventEnum) {
            case WinElection:
                isStop = true;
                RaftClientProvider.switchRaftClient(new Leader());
                break;
            case CandidateReceivedValidAppendEntriesRPC:
                isStop = true;
                RaftClientProvider.switchRaftClient(new Follower());
                break;
            default:
        }
    }

    private boolean isWin(List<RequestVoteRPCResponse> requestVoteRPCResponseList) {
        boolean isWin = false;
        int total = requestVoteRPCResponseList.size() + 1;
        int yesVoteNum = 1;
        for (RequestVoteRPCResponse response : requestVoteRPCResponseList) {
            if (response.isVoteGranted()) {
                yesVoteNum++;
            }
            if (yesVoteNum > total / 2) {
                // 大于半数 赢得选举
                isWin = true;
                if (!log.isDebugEnabled()) {
                    // debug模式下追踪票数
                    break;
                }
            }
        }
        log.info("[op:isWin] result: {}/{}, {} election", yesVoteNum, total, isWin ? "win" : "keep on");
        log.info("[op:isWin] Vote detail: {}", requestVoteRPCResponseList);
        return isWin;
    }

    private void build(RequestVoteRPCRequest requestVoteRPCRequest) {
        requestVoteRPCRequest.setCandidateId(Config.clientId);
        requestVoteRPCRequest.setTerm(currentTerm.get());
        Entry lastEntry = Config.logEntryManager.getLastEntry();
        requestVoteRPCRequest.setLastLogIndex(lastEntry == null ? -1 : lastEntry.getIndex());
        requestVoteRPCRequest.setLastLogTerm(lastEntry == null ? -1 : lastEntry.getTerm());
    }
}