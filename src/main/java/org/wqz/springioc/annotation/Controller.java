package org.wqz.springioc.annotation;

import java.lang.annotation.*;

@Documented // 生成帮助手册
@Target({ElementType.TYPE}) // service只能在类上面使用
@Retention(RetentionPolicy.RUNTIME) //service只在运行时候生效
public @interface Controller {

    String value() default "";
}
