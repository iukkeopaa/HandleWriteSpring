package org.wqz.springioc.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.wqz.springioc.tx.TransactionalManager;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/5 下午4:42
 */
public class CglibProxy {

    private Class<?> targetClass;

    private List<String> methodsName;


    public CglibProxy(Class<?> targetClass, List<String> methods) {
        this.targetClass = targetClass;
        this.methodsName  = methods;
    }

    public Object getProxyInstance(){

        Enhancer  enhancer = new Enhancer();

        enhancer.setSuperclass(targetClass);

        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

                Object result=null;

                if (methodsName.contains(method.getName())){

                    System.out.println("关闭事务的自动提交");

                    Connection connection = TransactionalManager.threadLocal.get();

                    connection.setAutoCommit(false);

                    try {
                        methodProxy.invokeSuper(o,objects);
                        connection.commit();

                    }catch (Throwable throwable){

                        connection.rollback();
                    }


                }else {

                    result =methodProxy.invokeSuper(o,objects);

                }
                return result;



            }
        });

        return enhancer.create();

    }
}
