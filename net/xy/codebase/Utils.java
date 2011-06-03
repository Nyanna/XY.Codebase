package net.xy.codebase;

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
}
