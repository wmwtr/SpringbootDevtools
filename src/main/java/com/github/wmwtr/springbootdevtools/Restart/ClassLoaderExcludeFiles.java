package com.github.wmwtr.springbootdevtools.Restart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class ClassLoaderExcludeFiles {
    private final HashSet<File> javaFiles;
    private static ClassLoaderExcludeFiles instance;
    private ClassLoaderExcludeFiles(){
        File[] sourceDirs = new File[]{
        };
        this.javaFiles = new HashSet<>();
        for (File file:sourceDirs
             ) {
            Collection<File> javaFiles = FileUtils.listFiles(file
                , new SuffixFileFilter(".java")
                , TrueFileFilter.TRUE);
            this.javaFiles.addAll(javaFiles);
        }
    }
    public static ClassLoaderExcludeFiles getInstance(){
        if(instance == null){
            instance = new ClassLoaderExcludeFiles();
        }
        return instance;
    }
    public String getPath(String name){
        String filePath;
        String path;
        for (File file:this.javaFiles
             ) {
            filePath = file.getPath();
            path = name.replace(".", "\\") + ".java";
            if (filePath.endsWith(path)){
                return filePath;
            }
        }
        return null;
    }
}
