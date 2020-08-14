package io.gitlab.wmwtr.springbootdevtools.FileWatch;

import java.io.File;
import java.util.Collection;

/**
 * @author wmwtr on 2020/8/13
 */
public interface PatternSourceDirectoryStrategy {
    Collection<File> getSourceDir(Thread var1);
}
