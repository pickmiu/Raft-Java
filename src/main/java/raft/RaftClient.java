package raft;

import config.Config;
import enums.EventEnum;
import lombok.extern.slf4j.Slf4j;
import pojo.*;

import static config.Config.*;
import static config.Config.logEntryManager;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public abstract class RaftClient implements Runnable {

    /**
     * 事件处理器
     * @param eventEnum
     */
    public abstract void eventHandle(EventEnum eventEnum);

    public RequestVoteRPCResponse requestVoteRPCHandle(RequestVoteRPCRequest requestVoteRPCRequest) {
        log.info("[op:requestVoteRPCHandle] RequestVoteRPCRequest={}", requestVoteRPCRequest);
        if (checkVotedForAndTerm(requestVoteRPCRequest) && checkLog(requestVoteRPCRequest)) {
            // 投票
            votedFor = requestVoteRPCRequest.getCandidateId();
            return new RequestVoteRPCResponse(Config.clientId, currentTerm.get(), true);
        } else {
            return new RequestVoteRPCResponse(Config.clientId, currentTerm.get(), false);
        }
    }

    protected boolean checkLog(RequestVoteRPCRequest requestVoteRPCRequest) {
        // first compare term . which term is bigger is more up to date
        Entry lastEntry = Config.logEntryManager.getLastEntry();
        if (lastEntry == null) {
            // enties 为空 直接投赞成票
            return true;
        }
        if (requestVoteRPCRequest.getLastLogTerm() == lastEntry.getTerm()) {
            // second compare index
            if (requestVoteRPCRequest.getLastLogIndex() < lastEntry.getIndex()) {
                return false;
            } else {
                return true;
            }
        } else {
            return requestVoteRPCRequest.getLastLogTerm() > lastEntry.getTerm() ? true : false;
        }
    }

    protected boolean checkVotedForAndTerm(RequestVoteRPCRequest requestVoteRPCRequest) {
        if (requestVoteRPCRequest.getTerm() > currentTerm.get()) {
            int oldTerm = currentTerm.getAndSet(requestVoteRPCRequest.getTerm());
            log.info("[op:checkVotedForAndTerm] update term. oldTerm : {} -> newTerm : {}", oldTerm, currentTerm.get());
            votedFor = null;
            return true;
        } else if (requestVoteRPCRequest.getTerm() == currentTerm.get()) {
            if (votedFor == null || votedFor.equals(requestVoteRPCRequest.getCandidateId())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public AppendEntriesRPCResponse appendEntryRPCHandle(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        log.info("[op:appendEntryRPCHandle] AppendEntriesRPCRequest={}", appendEntriesRPCRequest);
        AppendEntriesRPCResponse appendEntriesRPCResponse = checkTerm(appendEntriesRPCRequest);
        if (appendEntriesRPCResponse != null) {
            return appendEntriesRPCResponse;
        }
        return checkLog(appendEntriesRPCRequest);
    }

    /**
     * 检查日志情况 并组装对应的相应的 AppendEntriesRPCResponse
     * @param appendEntriesRPCRequest
     * @return
     */
    protected AppendEntriesRPCResponse checkLog(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        Entry entry = Config.logEntryManager.getEntryByIndex(appendEntriesRPCRequest.getPrevLogIndex());
        int prevLogIndex = appendEntriesRPCRequest.getPrevLogIndex();
        int prevLogTerm = appendEntriesRPCRequest.getPrevLogTerm();
        if (entry == null && prevLogIndex > 0) {
            // 缺失日志
            return new AppendEntriesRPCResponse(Config.clientId, currentTerm.get(), false, -1, Config.logEntryManager.getLastEntryIndex() + 1);
        } else {
            if (logEntryManager.match(prevLogIndex, prevLogTerm)) {
                // if previous entries is same . apply new log enties
                logEntryManager.applyEntries(prevLogIndex, appendEntriesRPCRequest.getEntries());
                // check leader commit
                Config.commitIndex.set(Math.min(appendEntriesRPCRequest.getLeaderCommit(), logEntryManager.getLastEntryIndex()));
                log.info("[op:checkLog] commitIndex={}", commitIndex.get());
                return new AppendEntriesRPCResponse(Config.clientId, currentTerm.get(), true);
            } else {
                // conflict
                int conflictTerm = logEntryManager.getEntryByIndex(prevLogIndex).getTerm();
                int conflictTermFirstIndex = logEntryManager.getTermFirstOccurIndex(conflictTerm);
                return new AppendEntriesRPCResponse(Config.clientId, currentTerm.get(), false, conflictTerm, conflictTermFirstIndex);
            }
        }
    }

    /**
     * 检查term
     * @param appendEntriesRPCRequest
     * @return 如果没有问题的话 返回null 有问题返回 AppendEntriesRPCResponse
     */
    protected AppendEntriesRPCResponse checkTerm(AppendEntriesRPCRequest appendEntriesRPCRequest) {
        if (appendEntriesRPCRequest.getTerm() < currentTerm.get()) {
            // 小于当前任期的请求拒绝
            return new AppendEntriesRPCResponse(Config.clientId, currentTerm.get(), false);
        }

        // 如果大于当前term update current term
        if (appendEntriesRPCRequest.getTerm() > currentTerm.get()) {
            int oldTerm = currentTerm.getAndSet(appendEntriesRPCRequest.getTerm());
            log.info("[op:checkTerm] update term. oldTerm : {} -> newTerm : {}", oldTerm, currentTerm.get());
        }

        return null;
    }
}