package org.wqz.springioc.annotation;


import java.lang.annotation.*;


@Target({ElementType.FIELD}) // service只能在类上面使用
@Retention(RetentionPolicy.RUNTIME) //service只在运行时候生效
@Inherited
@Documented
public @interface Autowired {

    String value() default "";
}
