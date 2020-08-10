package com.github.wmwtr.springbootdevtools.FileWatch;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;

public class ChangedFile {
    private final File sourceDir;
    private ChangedFile.FileType fileType;
    private final ChangedFile.ChangedType changedType;
    private final File file;

    public ChangedFile(ChangedFile.FileType fileType, File file, ChangedFile.ChangedType type, File sourceDir) {
        Assert.notNull(fileType, "FileType must not be null");
        Assert.notNull(file, "File must not be null");
        Assert.notNull(type, "Type must not be null");
        Assert.notNull(sourceDir, "sourceDir must not be null");
        this.fileType = fileType;
        this.file = file;
        this.changedType = type;
        this.sourceDir = sourceDir;
    }

    public String getRelativeName() {
        File directory = sourceDir.getAbsoluteFile();
        File file = this.file.getAbsoluteFile();
        String directoryName = StringUtils.cleanPath(directory.getPath());
        String fileName = StringUtils.cleanPath(file.getPath());
        Assert.state(fileName.startsWith(directoryName), () -> {
            return "The file " + fileName + " is not contained in the source directory " + directoryName;
        });
        return fileName.substring(directoryName.length() + 1);
    }
    public File getFile() {
        return this.file;
    }

    public ChangedFile.ChangedType getChangedType() {
        return this.changedType;
    }
    public ChangedFile.FileType getFileType(){
        return this.fileType;
    }


    public static enum  FileType{
        DIR,
        JAVA,
        OTHER;
        private FileType(){}
    }
    public static enum ChangedType{
        ADD,
        MODIFY,
        DELETE;
        private ChangedType(){}
    }
}
