package net.xy.codebase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ustomized threadlocal object
 * 
 * @author Xyan
 * 
 */
public class ThreadLocal {
    /**
     * store thread value tables
     */
    private static final Map THREADMAP = Collections.synchronizedMap(new HashMap());

    /**
     * sets value to actual thread
     * 
     * @param obj
     */
    public static void set(final Object obj) {
        THREADMAP.put(Thread.currentThread().hashCode(), obj);
    }

    /**
     * gets value from actual thread
     * 
     * @param obj
     * @return
     */
    public static Object get() {
        return THREADMAP.get(Thread.currentThread().hashCode());
    }

    /**
     * sets value to given thread
     * 
     * @param obj
     */
    public static void set(final Object obj, final Thread th) {
        THREADMAP.put(th.hashCode(), obj);
    }

    /**
     * gets value from given thread
     * 
     * @param obj
     * @return
     */
    public static Object get(final Thread th) {
        return THREADMAP.get(th.hashCode());
    }
}
