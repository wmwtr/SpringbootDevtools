package io.gitlab.wmwtr.springbootdevtools.Restart;

import java.net.URL;

/**
 * @author wmwtr on 2020/8/13
 */
public interface PatternClassPathStrategy {
    URL[] getClassPathDirs(Thread var1);
}
