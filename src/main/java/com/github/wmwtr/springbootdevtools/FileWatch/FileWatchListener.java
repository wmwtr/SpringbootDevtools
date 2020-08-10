package com.github.wmwtr.springbootdevtools.FileWatch;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

public class FileWatchListener extends FileAlterationListenerAdaptor{
    private final ChangedFileRepository repository;
    public FileWatchListener(ChangedFileRepository repository){
        this.repository = repository;
    }
    @Override
    public void onDirectoryChange(File directory) {
        //repository.put(new ChangedFile(ChangedFile.FileType.DIR, directory, ChangedFile.ChangedType.MODIFY, repository.getSourceDir()));
    }

    @Override
    public void onDirectoryDelete(File directory) {
        repository.put(new ChangedFile(ChangedFile.FileType.DIR, directory, ChangedFile.ChangedType.DELETE, repository.getSourceDir()));
    }

    @Override
    public void onDirectoryCreate(File directory) {
        repository.put(new ChangedFile(ChangedFile.FileType.DIR, directory, ChangedFile.ChangedType.ADD, repository.getSourceDir()));
    }

    @Override
    public void onFileChange(File file) {
        if(file.getName().endsWith(".java")){
            repository.put(new ChangedFile(ChangedFile.FileType.JAVA, file, ChangedFile.ChangedType.MODIFY, repository.getSourceDir()));
        }else{
            repository.put(new ChangedFile(ChangedFile.FileType.OTHER, file, ChangedFile.ChangedType.MODIFY, repository.getSourceDir()));
        }

    }

    @Override
    public void onFileDelete(File file) {
        if(file.getName().endsWith(".java")){
            repository.put(new ChangedFile(ChangedFile.FileType.JAVA, file, ChangedFile.ChangedType.DELETE, repository.getSourceDir()));
        }else{
            repository.put(new ChangedFile(ChangedFile.FileType.OTHER, file, ChangedFile.ChangedType.DELETE, repository.getSourceDir()));
        }
    }

    @Override
    public void onFileCreate(File file) {
        if(file.getName().endsWith(".java")){
            repository.put(new ChangedFile(ChangedFile.FileType.JAVA, file, ChangedFile.ChangedType.ADD, repository.getSourceDir()));
        }else{
            repository.put(new ChangedFile(ChangedFile.FileType.OTHER, file, ChangedFile.ChangedType.ADD, repository.getSourceDir()));
        }
    }
}
