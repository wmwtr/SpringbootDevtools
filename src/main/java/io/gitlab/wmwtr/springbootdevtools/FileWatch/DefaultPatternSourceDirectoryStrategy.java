package io.gitlab.wmwtr.springbootdevtools.FileWatch;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wmwtr on 2020/8/13
 */
public class DefaultPatternSourceDirectoryStrategy implements PatternSourceDirectoryStrategy{
    private String enctype = "UTF-8";
    public void setEnctype(String enc){
        this.enctype = enc;
    }
    private URL[] getClassPathDirs(Thread var1) {
        List<URL> urls = new ArrayList();
        URL[] var2 = urlsFromClassLoader(var1.getContextClassLoader());
        int var3 = var2.length;
        for(int var4 = 0; var4 < var3; ++var4) {
            if(this.isDirectoryUrl(var2[var4].toString())){
                urls.add(var2[var4]);
            }
        }
        return urls.toArray(new URL[0]);
    }

    @Override
    public Collection<File> getSourceDir(Thread var1){
        URL[] var2 = getClassPathDirs(var1);
        String classPath = getFirstBaseUrls(var2).getPath();
        File parent = new File(classPath).getParentFile().getParentFile();
        File watchFile = new File(parent.getPath() + "//src//main//java");
        File watchFile2 = new File(parent.getPath() + "//src//main//Resources");
        LinkedList<File> var3 = new LinkedList();
        if (watchFile.exists()){
            var3.add(watchFile);
        }
        if (watchFile2.exists()){
            var3.add(watchFile2);
        }
        return var3;
    }
    private URL getFirstBaseUrls(URL[] classPathDirs){
        try{
            return new URL(URLDecoder.decode(classPathDirs[0].toString(), this.enctype));
        }catch (Exception e){
            ;
        }
        return null;
    }

    private static URL[] urlsFromClassLoader(ClassLoader classLoader) {
        return classLoader instanceof URLClassLoader ? ((URLClassLoader)classLoader).getURLs() : new URL[0];
    }
    private boolean isDirectoryUrl(String urlString) {
        return urlString.startsWith("file:") && urlString.endsWith("/");
    }
}
