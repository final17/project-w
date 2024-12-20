package com.projectw.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SlackAlert {

    String onSuccess() default "";
    String onFailure() default "";
    String hookUrl() default "";

    boolean attachResult() default false;
    String[] requestEL() default "";
}
