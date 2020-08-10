package com.github.wmwtr.springbootdevtools.AutoCompile;

import com.github.wmwtr.springbootdevtools.FileWatch.ChangedFile;
import com.github.wmwtr.springbootdevtools.FileWatch.ChangedFileRepository;
import com.github.wmwtr.springbootdevtools.Restart.Restarter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AutoCompiler {
    private static final Log logger = LogFactory.getLog(AutoCompiler.class);
    private final BlockingQueue<Runnable> workQueue;
    private final ProcessClassPathFiles processor;
    private final ChangedFileRepository repository;
    private final AutoCompiler.AutoCompilerThread execThread;
    private boolean onTriggerEnter = false;

    public AutoCompiler(ChangedFileRepository repository, File classPathDir) {
        this.workQueue = new LinkedBlockingQueue<>();
        this.repository = repository;
        this.execThread = new AutoCompilerThread();
        this.processor = new ProcessClassPathFiles(classPathDir);
    }
    public void start(){
        logger.info("[" + repository.getSourceDir() + "]" + " has started to monitor");
        execThread.start();
    }

    public Thread getExecThread(){
        return this.execThread;
    }
    private void convertToWorkQueue(){
        workQueue.add(new Runnable() {
            @Override
            public void run() {
                try{
                    MessageProcess();
                }catch (Exception e){
                    logger.error("Fail to process files", e);
                }
            }
        });
    }
    private void MessageProcess() throws IOException{
        ChangedFile[] changedFiles = repository.pollAll();

        for (ChangedFile file:changedFiles
             ) {
            if (file.getChangedType() == ChangedFile.ChangedType.ADD){
                if (file.getFileType() == ChangedFile.FileType.DIR ) processor.createDir(file.getRelativeName());
                else if (file.getFileType() == ChangedFile.FileType.JAVA) processor.createClassFile(file.getFile());
                else if (file.getFileType() == ChangedFile.FileType.OTHER) processor.createOtherFile(file.getRelativeName(), file.getFile());
            }
            else if (file.getChangedType() == ChangedFile.ChangedType.DELETE){
                if (file.getFileType() == ChangedFile.FileType.DIR ) processor.delDir(file.getRelativeName());
                else if (file.getFileType() == ChangedFile.FileType.JAVA) processor.delClassFile(file.getRelativeName());
                else if (file.getFileType() == ChangedFile.FileType.OTHER) processor.delOtherFile(file.getRelativeName());
            }
            else if (file.getChangedType() == ChangedFile.ChangedType.MODIFY){
                if (file.getFileType() == ChangedFile.FileType.JAVA){
                    processor.delClassFile(file.getRelativeName());
                    processor.createClassFile(file.getFile());
                }else if(file.getFileType() == ChangedFile.FileType.OTHER){
                    processor.delOtherFile(file.getRelativeName());
                    processor.createOtherFile(file.getRelativeName(), file.getFile());
                }
            }
        }
    }
    private synchronized void setTrueOnTriggerEnter(){
        this.onTriggerEnter = true;
    }
    public void trigger(){
        setTrueOnTriggerEnter();
        synchronized (this.execThread){
            this.execThread.notify();
        }
    }
    private class AutoCompilerThread extends Thread{
        public AutoCompilerThread(){
            this.setName("AutoCompiler");
        }
        @Override
        public void run() {
            try{
                while(true){
                    synchronized (this){
                        if (!onTriggerEnter){
                            this.wait();
                        }else {
                            synchronized (AutoCompiler.this){
                                AutoCompiler.this.convertToWorkQueue();
                                Runnable r = workQueue.poll();
                                if (r != null){
                                    r.run();
                                }
                                onTriggerEnter = false;
                            }
                            Restarter.getInstance().restart();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}

