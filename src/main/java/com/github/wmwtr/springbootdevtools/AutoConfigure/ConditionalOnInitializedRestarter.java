package com.github.wmwtr.springbootdevtools.AutoConfigure;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnInitializedRestarterCondition.class)
public @interface ConditionalOnInitializedRestarter {

}
