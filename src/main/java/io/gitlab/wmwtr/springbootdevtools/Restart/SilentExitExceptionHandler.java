package io.gitlab.wmwtr.springbootdevtools.Restart;

import java.util.Arrays;

class SilentExitExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler delegate;

    SilentExitExceptionHandler(Thread.UncaughtExceptionHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        if (exception instanceof SilentExitExceptionHandler.SilentExitException) {
            if (this.isJvmExiting(thread)) {
                this.preventNonZeroExitCode();
            }

        } else {
            if (this.delegate != null) {
                this.delegate.uncaughtException(thread, exception);
            }

        }
    }

    private boolean isJvmExiting(Thread exceptionThread) {
        Thread[] var2 = this.getAllThreads();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Thread thread = var2[var4];
            if (thread != exceptionThread && thread.isAlive() && !thread.isDaemon()) {
                return false;
            }
        }

        return true;
    }

    protected Thread[] getAllThreads() {
        ThreadGroup rootThreadGroup = this.getRootThreadGroup();
        Thread[] threads = new Thread[32];

        int count;
        for(count = rootThreadGroup.enumerate(threads); count == threads.length; count = rootThreadGroup.enumerate(threads)) {
            threads = new Thread[threads.length * 2];
        }

        return (Thread[]) Arrays.copyOf(threads, count);
    }

    private ThreadGroup getRootThreadGroup() {
        ThreadGroup candidate;
        for(candidate = Thread.currentThread().getThreadGroup(); candidate.getParent() != null; candidate = candidate.getParent()) {
            ;
        }

        return candidate;
    }

    protected void preventNonZeroExitCode() {
        System.exit(0);
    }

    static void setup(Thread thread) {
        Thread.UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
        if (!(handler instanceof SilentExitExceptionHandler)) {
            Thread.UncaughtExceptionHandler handler2 = new SilentExitExceptionHandler(handler);
            thread.setUncaughtExceptionHandler(handler2);
        }

    }

    static void exitCurrentThread() {
        throw new SilentExitExceptionHandler.SilentExitException();
    }

    private static class SilentExitException extends RuntimeException {
        private SilentExitException() {
        }
    }
}