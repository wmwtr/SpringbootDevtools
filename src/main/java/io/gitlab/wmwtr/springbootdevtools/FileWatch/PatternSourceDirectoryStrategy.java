package io.gitlab.wmwtr.springbootdevtools.FileWatch;

import java.io.File;
import java.util.Collection;

public interface PatternSourceDirectoryStrategy {
    Collection<File> getSourceDir(Thread var1);
}
