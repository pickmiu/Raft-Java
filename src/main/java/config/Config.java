package config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import raft.LogEntryManager;
import util.RandomUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class Config {
    // ps: JAVA静态变量和静态代码块的执行顺序和代码排版的顺序一致

    public static List<String> otherServerUrl = new Vector<>();

    public static volatile int port = 8084;

    /**
     * 选举超时时间 单位:ms
     */
    public static volatile int electionTimeoutMilliSeconds = 10000;

    public static volatile int conflictTimeoutMin = 5000;
    public static volatile int conflictTimeoutMax = 10000;

    public static String clientId = "";

    public static volatile int leaderHeartBeatSendInterval = 5000;

    public static volatile AtomicInteger currentTerm = new AtomicInteger(0);

    public static volatile String votedFor;

    public static volatile AtomicInteger commitIndex = new AtomicInteger(0);

    public static okhttp3.OkHttpClient OkHttpClient = new OkHttpClient.Builder().build();

    public static final okhttp3.MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static ExecutorService executorService = new ThreadPoolExecutor(6, 12, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static LogEntryManager logEntryManager = new LogEntryManager();

    static {
        if (log.isDebugEnabled()) {
            Config.clientId = port+"";
        } else {
            Config.clientId = System.currentTimeMillis() + RandomUtil.generateFixLengthRandom(0, 9999);
        }

        log.info("[op:instance initializer] clientId={}", Config.clientId);

        List<Integer> ports = Arrays.asList(8081, 8082, 8083, 8084, 8085);
        String urlPrefix = "http://127.0.0.1:";
        for (Integer port: ports) {
            if (!port.equals(Config.port)) {
                Config.otherServerUrl.add(urlPrefix + port);
            }
        }
        log.info("[op:instance initializer] otherServerUrl={}", Config.otherServerUrl);
    }
}