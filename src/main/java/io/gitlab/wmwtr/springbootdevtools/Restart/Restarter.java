package io.gitlab.wmwtr.springbootdevtools.Restart;

import io.gitlab.wmwtr.springbootdevtools.FileWatch.DefaultPatternSourceDirectoryStrategy;
import io.gitlab.wmwtr.springbootdevtools.FileWatch.FileWatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.boot.system.JavaVersion;
import org.springframework.cglib.core.ClassNameReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class Restarter {
    private Log logger = DeferredLog.replay(new DeferredLog(), LogFactory.getLog(this.getClass()));
    private static Restarter instance;
    private URL[] baseUrls;
    private final String[] args;
    private final List<ConfigurableApplicationContext> rootContexts = new CopyOnWriteArrayList();
    private final String mainClassName;
    private RestartThread execThread;
    private static final Object INSTANCE_MONITOR = new Object();
    private boolean finished = false;
    private static ClassLoader applicationClassLoader;


    protected Restarter(String[] args, PatternClassPathStrategy initializer, Thread thread){
        SilentExitExceptionHandler.setup(thread);
        this.baseUrls = initializer.getClassPathDirs(thread);
        this.args = args;
        this.mainClassName = getMainClassName(thread);
        this.execThread = new RestartThread();
        applicationClassLoader = thread.getContextClassLoader();
    }
    private String getMainClassName(Thread thread) {
		try {
			return new MainMethod(thread).getDeclaringClassName();
		}
		catch (Exception ex) {
			return null;
		}
	}
    void prepare(ConfigurableApplicationContext applicationContext) {
        if (applicationContext == null || applicationContext.getParent() == null) {
            if (applicationContext instanceof GenericApplicationContext) {
                //this.prepare((GenericApplicationContext)applicationContext);
            }

            this.rootContexts.add(applicationContext);
        }
    }


    void remove(ConfigurableApplicationContext applicationContext) {
        if (applicationContext != null) {
            this.rootContexts.remove(applicationContext);
        }

    }
    public static Restarter getInstance() {
        //Assert.notNull(instance, "Restarter has not been initialized");
        return instance;
    }
    public static void initialize(String[] args, PatternClassPathStrategy initializer){
        Restarter localInstance = null;
        synchronized (INSTANCE_MONITOR){
            if (instance == null) {
                localInstance = new Restarter(args, initializer, Thread.currentThread());
                instance = localInstance;
            }
        }
        if (localInstance != null) {
            try {
                //localInstance.preInitializeLeakyClasses();
                ClassLoader cl = new RestartClassLoader(applicationClassLoader
                        ,localInstance.baseUrls);

                localInstance.execThread.callAndWait(
                        ()-> {
                            instance.relaunch(cl);
                            return null;
                        });
            } catch (Exception var2) {
                var2.printStackTrace();
            }
            SilentExitExceptionHandler.exitCurrentThread();
        }


    }
    protected Throwable relaunch(ClassLoader classLoader) throws Exception {
        RestartLauncher launcher = new RestartLauncher(classLoader, this.mainClassName, this.args);
        launcher.start();
        launcher.join();
        return null;
    }
    private void preInitializeLeakyClasses() {
        try {
            Class<?> readerClass = ClassNameReader.class;
            Field field = readerClass.getDeclaredField("EARLY_EXIT");
            field.setAccessible(true);
            ((Throwable)field.get((Object)null)).fillInStackTrace();
        } catch (Exception var3) {
            this.logger.warn("Unable to pre-initialize classes", var3);
        }

    }
    public void restart() {
        this.logger.info("Restarting application");
        execThread.callAndWait(() -> {
            this.stop();
            this.start();
            return null;
        });
    }
    protected synchronized void stop(){
        this.logger.info("Stopping application");

        try {
            Iterator var1 = this.rootContexts.iterator();
            while(var1.hasNext()) {
                if (!var1.hasNext()) {
                    this.cleanupCaches();
                    break;
                }
                ConfigurableApplicationContext context = (ConfigurableApplicationContext)var1.next();
                context.close();
                this.rootContexts.remove(context);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.gc();
		System.runFinalization();
    }
    private void cleanupCaches() throws Exception {
        Introspector.flushCaches();
        this.cleanupKnownCaches();
    }

    private void cleanupKnownCaches() throws Exception {
        ResolvableType.clearCache();
        this.cleanCachedIntrospectionResultsCache();
        ReflectionUtils.clearCache();
        this.clearAnnotationUtilsCache();
        if (!JavaVersion.getJavaVersion().isEqualOrNewerThan(JavaVersion.NINE)) {
            this.clear("com.sun.naming.internal.ResourceManager", "propertiesCache");
        }

    }

    private void cleanCachedIntrospectionResultsCache() throws Exception {
        this.clear(CachedIntrospectionResults.class, "acceptedClassLoaders");
        this.clear(CachedIntrospectionResults.class, "strongClassCache");
        this.clear(CachedIntrospectionResults.class, "softClassCache");
    }

    private void clearAnnotationUtilsCache() throws Exception {
        try {
            AnnotationUtils.clearCache();
        } catch (Throwable var2) {
            this.clear(AnnotationUtils.class, "findAnnotationCache");
            this.clear(AnnotationUtils.class, "annotatedInterfaceCache");
        }

    }

    private void clear(String className, String fieldName) {
        try {
            this.clear(Class.forName(className), fieldName);
        } catch (Exception var4) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Unable to clear field " + className + " " + fieldName, var4);
            }
        }

    }

    private void clear(Class<?> type, String fieldName) throws Exception {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object instance = field.get((Object)null);
            if (instance instanceof Set) {
                ((Set)instance).clear();
            }

            if (instance instanceof Map) {
                ((Map)instance).keySet().removeIf(this::isFromRestartClassLoader);
            }
        } catch (Exception var5) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Unable to clear field " + type + " " + fieldName, var5);
            }
        }
    }
    private boolean isFromRestartClassLoader(Object object) {
        return object instanceof Class && ((Class)object).getClassLoader() instanceof RestartClassLoader;
    }
    protected Object start() throws Exception{
        this.logger.info("Starting application " + this.mainClassName + " with URLs " + Arrays.asList(baseUrls));
        ClassLoader cl = new RestartClassLoader(applicationClassLoader
                        ,getInstance().baseUrls);
        instance.relaunch(cl);

        return null;
    }

    synchronized boolean isFinished() {
        return finished;
    }

    synchronized void finish() {
        if (!this.isFinished()) {
                this.logger = DeferredLog.replay(this.logger, LogFactory.getLog(this.getClass()));
                this.finished = true;
                FileWatcher.initialize(1000, new DefaultPatternSourceDirectoryStrategy(), new DefaultPatternClassPathStrategy());
            }

    }


    private class RestartThread extends Thread {
        private Callable<?> callable;
        private Object result;

        RestartThread() {
            this.setDaemon(false);
            this.setName("RestartThread");
        }

        void call(Callable<?> callable) {
            this.callable = callable;
            this.start();
        }

        Object callAndWait(Callable callable) {
            //System.out.println("CurrentThread:" + Thread.currentThread().getName() + Thread.currentThread());
            this.callable = callable;
            this.start();

            try {
                this.join();
                return this.result;
            } catch (InterruptedException var3) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(var3);
            }
        }
        @Override
        public void run() {
            try {
                Restarter.this.execThread = Restarter.this.new RestartThread();
                this.result = this.callable.call();
            } catch (Exception var2) {
                var2.printStackTrace();
                System.exit(1);
            }

        }
    }

}
