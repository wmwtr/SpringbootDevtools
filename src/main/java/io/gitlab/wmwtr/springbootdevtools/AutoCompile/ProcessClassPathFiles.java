package io.gitlab.wmwtr.springbootdevtools.AutoCompile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessClassPathFiles {
    private static final Log logger = LogFactory.getLog(ProcessClassPathFiles.class);
    private final File classPath;
    private final Compile compile;
    public ProcessClassPathFiles(File classPath){
        Assert.notNull(classPath, "classPath must not be null");
        this.classPath = classPath;
        this.compile = new Compile();
    }
    private String convertToSuffixClass(String name){
        if(name.contains(".")){
            name = name.substring(0, name.indexOf(".")) + ".class";
        }
        return name;
    }
    private File getTargetFile(String relativeName){
        if (relativeName.endsWith(".java")){
            return new File(classPath + "//" + convertToSuffixClass(relativeName));
        }
        return new File(classPath + "//" + relativeName);
    }

    public void delDir(String relativeName) throws IOException {
        File file = getTargetFile(relativeName);
        if (file.exists()){
            FileUtils.deleteDirectory(file);
        }
    }
    public void createDir(String relativeName) throws IOException{
        File file = getTargetFile(relativeName);
		if(!file.exists()){
			file.mkdir();
		}
    }
    public void createClassFile(File... javaFile){
        compile.compile(javaFile);
    }
    public void createOtherFile(String relativeName, File src) throws IOException{
        String var1 = classPath + "//" + relativeName;
        FileUtils.copyFileToDirectory(src, new File(var1.substring(0, var1.length() - src.getName().length())));
    }
    public void delClassFile(String relativeName) throws IOException {
        File file = getTargetFile(relativeName);
        if (file.exists()){
            FileUtils.forceDelete(file);
        }
    }
    public void delOtherFile(String relativeName) throws IOException {
        File file = getTargetFile(relativeName);
        if (file.exists()){
            FileUtils.forceDelete(file);
        }
    }


    private class Compile {
        List<String> options = new ArrayList<>();
        Compile(){
            options.add("-d");
            options.add(classPath.getPath());
        }
        private void compile(File... javaFile) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
            StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> javaFileObjects = manager.getJavaFileObjects(javaFile);
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, collector, options, null, javaFileObjects);
            collector.getDiagnostics().forEach(item -> logger.info(item.toString()));
            task.call();
        }
    }
}
