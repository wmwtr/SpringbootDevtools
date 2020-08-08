package io.gitlab.wmwtr.springbootdevtools.Restart;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class DefaultPatternClassPathStrategy implements PatternClassPathStrategy {
    private String enctype = "UTF-8";
    @Override
    public URL[] getClassPathDirs(Thread var1) {
        List<URL> urls = new ArrayList();
        URL[] var2 = urlsFromClassLoader(var1.getContextClassLoader());
        int var3 = var2.length;
        for(int var4 = 0; var4 < var3; ++var4) {
            if(this.isDirectoryUrl(var2[var4].toString())){
                try{
                    URL var5 = new URL(URLDecoder.decode(var2[var4].toString(), enctype));
                    urls.add(var5);
                }catch (Exception e){

                }
                urls.add(var2[var4]);
            }
        }
        return urls.toArray(new URL[0]);
    }

    private static URL[] urlsFromClassLoader(ClassLoader classLoader) {
        return classLoader instanceof URLClassLoader ? ((URLClassLoader)classLoader).getURLs() : new URL[0];
    }
    private boolean isDirectoryUrl(String urlString) {
        return urlString.startsWith("file:") && urlString.endsWith("/");
    }
}
