package com.github.wmwtr.springbootdevtools.AutoConfigure;

import com.github.wmwtr.springbootdevtools.FileWatch.DefaultPatternSourceDirectoryStrategy;
import com.github.wmwtr.springbootdevtools.FileWatch.FileWatcher;
import com.github.wmwtr.springbootdevtools.FileWatch.PatternSourceDirectoryStrategy;
import com.github.wmwtr.springbootdevtools.Restart.DefaultPatternClassPathStrategy;
import com.github.wmwtr.springbootdevtools.Restart.PatternClassPathStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnInitializedRestarter
public class AutoConfiguration {
    @Configuration
    @ConditionalOnProperty(prefix = "spring.devtools.restart", name = "enabled", matchIfMissing = true)
    static class FileWatch{
        @Bean
        @ConditionalOnMissingBean(PatternClassPathStrategy.class)
        PatternClassPathStrategy patternClassPathStrategy(){
            return new DefaultPatternClassPathStrategy();
        }
        @Bean
        @ConditionalOnMissingBean(PatternSourceDirectoryStrategy.class)
        PatternSourceDirectoryStrategy patternSourceDirectoryStrategy(){
            return new DefaultPatternSourceDirectoryStrategy();
        }
        @Bean
        FileWatcher fileWatcher(PatternSourceDirectoryStrategy dirStrategy, PatternClassPathStrategy clStrategy){
            FileWatcher.initialize(1000, dirStrategy, clStrategy);
            return FileWatcher.getInstance();
        }
    }
}
