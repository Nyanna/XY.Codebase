package net.xy.codebase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Utils {

    /**
     * convience method for suspending the current thread
     * 
     * @param milliseconds
     */
    public static void sleep(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
        }
    }

    /**
     * convience method for waiting upon an latch
     * 
     * @param latch
     */
    public static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException e) {
        }
    }

    /**
     * gets all declared fields of an class and its super in alphabetical order
     * 
     * @param target
     * @return
     */
    public static List<Field> getFields(final Class<?> target) {
        final List<Field> fields = new ArrayList<Field>();
        final Class<?> cl = target;
        fields.addAll(Arrays.asList(cl.getDeclaredFields()));
        Class<?> pcl = cl.getSuperclass();
        while (pcl != null) {
            fields.addAll(Arrays.asList(pcl.getDeclaredFields()));
            pcl = pcl.getSuperclass(); // next
        }
        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(final Field f1, final Field f2) {
                return f1.getName().compareTo(f1.getName());
            }
        });
        return fields;
    }
}
