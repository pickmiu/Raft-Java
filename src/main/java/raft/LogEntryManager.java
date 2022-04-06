package raft;

import config.Config;
import lombok.extern.slf4j.Slf4j;
import pojo.Command;
import pojo.Entry;
import util.EntryUtil;

import java.util.List;
import java.util.Vector;

import static config.Config.logEntryManager;

/**
 * notice : index 属性默认为 第一个元素为 1 不是0 ！！！
 *  ? 并发安全问题 并发安全优化
 *
 * @author Tangliyi (2238192070@qq.com)
 */
@Slf4j
public class LogEntryManager {

    private final List<Entry> entries = new Vector<>(1024);

    public synchronized void addEntry(Entry entry) {
        entry.setIndex(entries.size() + 1);
        entries.add(entry);
        log.info("[op:addEntry] entries={}", entries);
    }

    public synchronized void addEntry(Command command) {
        Entry newEntry = new Entry();
        newEntry.setTerm(Config.currentTerm.get());
        newEntry.setCommand(command);
        addEntry(newEntry);
    }

    public List<Entry> getEnties() {
        return entries;
    }

    public Entry getEntryByIndex(int index) {
        index--;
        if (index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index);
    }

    public void applyEntries(int prevLogIndex, List<Entry> newEntries) {
        if (newEntries == null || newEntries.isEmpty()) {
            // 删除 prevLogIndex(exclude) 之后的所有日志
            logEntryManager.remove(prevLogIndex + 1);
            return;
        }

        for (int i = 0; i < newEntries.size(); i++) {
            Entry newEntry = newEntries.get(i);
            if (getLastEntryIndex() < newEntry.getIndex()) {
                // 说明之后的都插入进来 不需要覆盖
                entries.addAll(newEntries.subList(i, newEntries.size()));
                break;
            }

            if (notMatch(newEntry.getIndex(), newEntry.getTerm())) {
                // 覆盖旧的entry
                Entry oldEntry = getEntryByIndex(newEntry.getIndex());
                oldEntry.setTerm(newEntry.getTerm());
                oldEntry.setCommand(newEntry.getCommand());
            }
        }

        int newEntryLastIndex = EntryUtil.getLastEntryIndex(newEntries);
        if (getLastEntryIndex() > newEntryLastIndex) {
            // follower上多的日志需要清除掉
            remove(newEntryLastIndex + 1, getLastEntryIndex());
        }
        log.info("[op:applyEntries] entries={}", entries);
    }

    /**
     * @param fromIndex include
     * @param toIndex   include
     */
    public void remove(int fromIndex, int toIndex) {
        if (toIndex >= fromIndex) {
            if (fromIndex < 1) {
                fromIndex = 1;
            }
            entries.subList(fromIndex - 1, toIndex).clear();
        }
        log.info("[op:remove] entries={}", entries);
    }

    public void remove(int fromIndex) {
        remove(fromIndex, entries.size());
    }

    public Entry getLastEntry() {
        if (entries.isEmpty()) {
            return null;
        } else {
            return entries.get(entries.size() - 1);
        }
    }

    public List<Entry> subEntries(int fromIndex) {
        if (fromIndex > entries.size()) {
            return null;
        }
        if (fromIndex < 1) {
            fromIndex = 1;
        }
        return entries.subList(fromIndex - 1, entries.size());
    }

    /**
     * -1 为存在
     *  notice: 不可能返回0
     * @return
     */
    public int getLastEntryIndex() {
        Entry lastEntry = getLastEntry();
        if (lastEntry == null) {
            return -1;
        } else {
            return lastEntry.getIndex();
        }
    }

    public boolean match(int index, int term) {
        if (index <= 0 && term == -1) {
            // 特殊情况： leader服务器刚启动entries中没有entry
            return true;
        }
        Entry entry = entries.get(index - 1);
        if (entry == null) {
            return false;
        }
        return entry.getTerm() == term;
    }

    public boolean notMatch(int index, int term) {
        return !match(index, term);
    }

    public int getTermFirstOccurIndex(int term) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).getTerm() == term && i - 1 >= 0 && entries.get(i - 1).getTerm() != term) {
                return entries.get(i).getIndex();
            }
        }
        return -1;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public boolean notEmpty() {
        return !isEmpty();
    }

}