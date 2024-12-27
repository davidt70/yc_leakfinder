package com.yawlconsulting.util.leakfinder;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Title: LeakFinder</p>
 *
 * Description: The LeakFinder class is used to detect whether an associated
 * object - created at the same time as a LeakFinder - was been closed
 * correctly, typically with a call to the object' close method
 *
 * <p>
 *
 * @author David Truffet
 * @version 1.0
 */
public final class LeakFinder {

    private static final Set<IOException> DETECTED_LEAKS = Collections.synchronizedSet(new HashSet<>());
    private static final Logger logger = Logger.getLogger(LeakFinder.class.getName());
    private static volatile Level LEVEL = Level.WARNING;
    private static volatile boolean TRACKING_ENABLED = true;
    private static volatile boolean ONLY_LOG_FIRST=true;

    /**
     * If set to false, stack traces will be logged each time a leak is detected
     * If set to true (or left unchanged) only previously undetected stack 
     * traces are logged.
     * 
     * @param flag 
     */
    public void setOnlyLogFirst(boolean flag) {
        ONLY_LOG_FIRST=flag;
    }
    /**
     * Sets the log level of the LeakFinder
     *
     * @param newLevel
     */
    public static void setLogLevel(Level newLevel) {
        if (newLevel.intValue() <= Level.FINEST.intValue()
                && LEVEL.intValue() > newLevel.intValue()) {
            logger.log(LEVEL, "LeakFinder log level set to Finest");
        }
        LEVEL = newLevel;
    }

    /**
     * clears the history of detected leaks.
     */
    public static void clear() {
        DETECTED_LEAKS.clear();
    }

    
    public static void disable() {
        if (TRACKING_ENABLED) {
            TRACKING_ENABLED = false;
            logger.log(LEVEL, "LeakFinder disabled", new Exception("LeakFinder.disable() called"));
        }
    }

    public static boolean isEnabled() {
        return TRACKING_ENABLED;
    }

    public static void enable() {
        TRACKING_ENABLED = true;
    }

    /**
     * activily try to detect leaks by invoking System.gc(); leaks may or may
     * not be found during normal operation should call getDetectedLeaks()
     * instead.
     *
     * @return
     */
    public static Set<IOException> checkForLeaks() {
        if (TRACKING_ENABLED) {
            System.gc();
            Thread.yield();
            System.gc();
        }
        // best effort
        return getDetectedLeaks();
    }

    /**
     * @return set of detected leaks since last call to clear(). 
     */
    public static Set<IOException> getDetectedLeaks() {
        return Set.copyOf(DETECTED_LEAKS);
    }

    private static final Cleaner cleaner = Cleaner.create();

    private static class State implements Runnable {

        private volatile IOException at;
        private final String request;

        public State(IOException at, String request) {
            this.at = at;
            this.request = request;
        }

        public String getRequest() {
            return request;
        }

        public boolean close() {
            if (this.at != null) {
                this.at = null;
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            if (at != null) {
                if (!ONLY_LOG_FIRST || !DETECTED_LEAKS.contains(at)) {
                    DETECTED_LEAKS.add(at);
                    logger.log(LEVEL, at.getMessage() + "\n Request:\t" + request, at);
                }
                at = null;
            }
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    /**
     * Constructor for the LeakFinder object
     *
     * @param message description to be displayed to user
     * @param request request being performed
     */
    public LeakFinder(String message, String request) {
        if (TRACKING_ENABLED) {
            IOException at = new IOException("Resouce Leak BUG found in code:"
                    + message);
            if (!ONLY_LOG_FIRST || !DETECTED_LEAKS.contains(at)) {
                this.state = new State(at, request);
                this.cleanable = cleaner.register(this, state);
                return;
            }
        }
        this.state = null;
        this.cleanable = null;
    }

    public void close() {
        if (state != null && state.close()) {
            cleanable.clean();
        }
    }
}
// ------------------------------------------
// EOF
