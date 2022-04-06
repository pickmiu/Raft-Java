package util;

import pojo.Entry;

import java.util.List;

/**
 * @author Tangliyi (2238192070@qq.com)
 */
public class EntryUtil {
    public static int getLastEntryIndex(List<Entry> entries) {
        return entries.get(entries.size() - 1).getIndex();
    }
}