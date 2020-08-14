package io.gitlab.wmwtr.springbootdevtools.FileWatch;

import io.gitlab.wmwtr.springbootdevtools.AutoCompile.AutoCompiler;
import org.springframework.util.Assert;

import java.io.File;
import java.util.LinkedList;

/**
 * @author wmwtr on 2020/8/13
 */
public class ChangedFileRepository {
    private final LinkedList<ChangedFile> files;
    private final LinkedList<AutoCompiler> compilers;
    private final File sourceDir;
    private boolean isNotify = false;
    public ChangedFileRepository(Boolean isNotify, File sourceDir, AutoCompiler... compilers){
        this.files = new LinkedList<ChangedFile>();
        this.isNotify = isNotify;
        this.compilers = new LinkedList<>();
        this.sourceDir = sourceDir;
        for (AutoCompiler var1:compilers
             ) {
            this.compilers.add(var1);
        }
    }
    public ChangedFileRepository(Boolean isNotify, File sourceDir){
        this.files = new LinkedList<>();
        this.isNotify = isNotify;
        this.compilers = new LinkedList<>();
        this.sourceDir = sourceDir;
    }
    public void addAutoCompiler(AutoCompiler compiler){
        this.compilers.addLast(compiler);
    }
    public File getSourceDir(){return this.sourceDir;}
    public void put(ChangedFile changedFile){
        Assert.notNull(changedFile, "changedFile must not be null");
        if (changedFile != null){
            synchronized (this){
                files.add(changedFile);
            }
            if (isNotify){
                AutoCompiler compiler = compilers.pop();
                compilers.addLast(compiler);
                compiler.trigger();
            }
        }
    }
    public synchronized ChangedFile poll(){
        return files.pollFirst();
    }
    public synchronized ChangedFile[] pollAll(){
        ChangedFile[] changedFiles = files.toArray(new ChangedFile[0]);
        files.clear();
        return changedFiles;

    }
    public synchronized boolean isEmpty(){
        return files.isEmpty();
    }
}
