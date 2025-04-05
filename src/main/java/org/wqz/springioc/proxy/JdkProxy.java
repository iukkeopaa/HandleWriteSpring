package org.wqz.springioc.proxy;

import org.wqz.springioc.aop.ProceedingJoinPoint;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/5 下午3:46
 */
public class JdkProxy<O> {

    Class<?> targetClass;


    Class<?> aopClass;


    Object targetObject;



    String methodName;

    Method aopMethod;

    public JdkProxy(O targetClass, O c, O targetObject, O targetMethodName, O method) {

    }

    public Object getProxyInnstance(){

      return   Proxy.newProxyInstance(targetClass.getClassLoader(), targetClass.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals(methodName)){

                    ProceedingJoinPoint proceedingJoinPoint = new ProceedingJoinPoint(method,targetObject,args );

                    aopMethod.invoke(aopClass.newInstance(),proceedingJoinPoint);


                }

                else {

                    return method.invoke(targetObject,args);
                }

                return null;
            }
        });


    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public Class<?> getAopClass() {
        return aopClass;
    }

    public void setAopClass(Class<?> aopClass) {
        this.aopClass = aopClass;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getAopMethod() {
        return aopMethod;
    }

    public void setAopMethod(Method aopMethod) {
        this.aopMethod = aopMethod;
    }

}
