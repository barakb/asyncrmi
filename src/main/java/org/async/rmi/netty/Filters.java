package org.async.rmi.netty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * 12/6/14.
 */
public class Filters {
    private static final int MASK = 0x0001;

    public static int encode(List<String> filters) {
        int res = 0;
        if (filters.contains("drop")) {
            res = res | MASK;
        }
        if (filters.contains("compress")) {
            res = res | (MASK << 1);
        }
        if (filters.contains("encrypt")) {
            res = res | (MASK << 2);
        }
        return res;
    }

    public static boolean hasDrop(int filters){
        return 0 < (filters & MASK);
    }
    public static boolean hasCompress(int filters){
        return 0 < (filters & (MASK << 1));
    }
    public static boolean hasEncrypt(int filters){
        return 0 < (filters & (MASK << 2));
    }

    public static List<String> decode(int filters) {
        List<String> res = new ArrayList<>();
        if (hasDrop(filters)) {
            res.add("drop");
        }
        if (hasCompress(filters)) {
            res.add("compress");
        }
        if (hasEncrypt(filters)) {
            res.add("encrypt");
        }
        return res;
    }

}
