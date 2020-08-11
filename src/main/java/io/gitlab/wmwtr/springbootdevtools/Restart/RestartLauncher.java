package io.gitlab.wmwtr.springbootdevtools.Restart;

import java.lang.reflect.Method;

public class RestartLauncher extends Thread{
    private final String mainClassName;
    private final String[] args;
    RestartLauncher(ClassLoader classLoader, String mainClassName, String[] args) {
        this.mainClassName = mainClassName;
        this.args = args;
        this.setName("restartedMain");
        this.setDaemon(false);
        this.setContextClassLoader(classLoader);
    }

    @Override
    public void run() {
        try {
            Class<?> mainClass = Class.forName(this.mainClassName, false, this.getContextClassLoader());
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            mainMethod.invoke((Object)null,  (Object) this.args);
        } catch (Throwable var3) {
            this.getUncaughtExceptionHandler().uncaughtException(this, var3);
        }

    }

}
