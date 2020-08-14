package io.gitlab.wmwtr.springbootdevtools.FileWatch;

import io.gitlab.wmwtr.springbootdevtools.AutoCompile.AutoCompiler;
import io.gitlab.wmwtr.springbootdevtools.Restart.PatternClassPathStrategy;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author wmwtr on 2020/8/13
 */
public class FileWatcher {
    private static final Log logger = LogFactory.getLog(FileWatcher.class);
    private static FileWatcher instance;
    private final HashSet<FileAlterationObserver> observers;
    private final long interval;
    private final FileAlterationMonitor fileMonitor;
    private FileWatcher(long interval, PatternSourceDirectoryStrategy strategy, PatternClassPathStrategy cstrategy){
        this.interval = interval;
        this.observers = new HashSet<>();
        Collection<File> watcheDir = strategy.getSourceDir(Thread.currentThread());
        URL[] var5 = cstrategy.getClassPathDirs(Thread.currentThread());
        int var6 = 0;
        for (File var1:watcheDir
             ) {
            FileAlterationObserver var2 = new FileAlterationObserver(var1);
            ChangedFileRepository var3 = new ChangedFileRepository(true, var2.getDirectory());
            AutoCompiler var4 = new AutoCompiler(var3, new File(var5[getIndex(var5, var6)].getPath()));
            var3.addAutoCompiler(var4);
            var4.start();
            var2.addListener(new FileWatchListener(var3));
            this.observers.add(var2);
            var6++;
        }
        fileMonitor = new FileAlterationMonitor(interval, this.observers.toArray(new FileAlterationObserver[0]));
    }
    private int getIndex(URL[] urls, int index){
        return urls != null && urls.length != 0 ? index % urls.length : -1;
    }
    public static void initialize(long interval, PatternSourceDirectoryStrategy strategy, PatternClassPathStrategy cstrategy){
        if (instance == null){
            instance = new FileWatcher(interval, strategy, cstrategy);
            instance.start();
        }
    }
    public static FileWatcher getInstance(){
        Assert.state(instance != null, "FileWatcher has not been initialized");
        return instance;
    }
    private void start(){
        try{
            this.fileMonitor.start();
            logger.info("FileWatcher has been started");
        }catch (Exception e){
            logger.error("FileWatcher fail to start", e);
        }

    }
}
