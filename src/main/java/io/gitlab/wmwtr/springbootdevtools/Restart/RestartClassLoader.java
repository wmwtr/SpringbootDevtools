package io.gitlab.wmwtr.springbootdevtools.Restart;

import io.gitlab.wmwtr.springbootdevtools.Restart.ClassLoaderExcludeFiles;
import org.springframework.core.SmartClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

/**
 * @author wmwtr on 2020/8/13
 */
public class RestartClassLoader extends URLClassLoader implements SmartClassLoader {
    public RestartClassLoader(ClassLoader parent, URL[] urls){
        super(urls, parent);
    }

    @Override
	public boolean isClassReloadable(Class<?> classType) {
		return (classType.getClassLoader() instanceof RestartClassLoader);
	}

	@Override
	public URL getResource(String name) {
		URL resource = findResource(name);
		if (resource != null) {
			return resource;
		}
		return getParent().getResource(name);
	}
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = this.findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException var4) {
                    c = Class.forName(name, false, getParent());
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
    /**
     * 重写findClass方法
     *
     * @param name 是我们这个类的全路径
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (ClassLoaderExcludeFiles.getInstance().getPath(name) == null){
            return super.findClass(name);
        }
        throw new ClassNotFoundException();
    }


    /**
     * 将class文件转化为字节码数组
     *
     * @return
     */
    private byte[] getData(String name) {
        String classpath = this.getURLs()[0].getPath();
        if (classpath.endsWith("/")){
            classpath = classpath.substring(0, classpath.length()-1);
        }
        try {
            classpath = URLDecoder.decode(classpath, "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
        }

        String path = classpath + "\\" + name.replace(".", "\\") + ".class";
        //System.out.println(path);
        File file = new File(path);
        if (file.exists()) {
            FileInputStream in = null;
            ByteArrayOutputStream out = null;
            try {
                in = new FileInputStream(file);
                out = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = in.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
            return out.toByteArray();
        } else {
            return null;
        }
    }

}
