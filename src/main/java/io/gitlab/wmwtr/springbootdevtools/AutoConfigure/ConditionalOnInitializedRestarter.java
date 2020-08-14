package io.gitlab.wmwtr.springbootdevtools.AutoConfigure;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @author wmwtr on 2020/8/13
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnInitializedRestarterCondition.class)
public @interface ConditionalOnInitializedRestarter {

}
