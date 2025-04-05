package org.wqz.springioc.aop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/5 下午3:52
 */
public class ProceedingJoinPoint {


    private Method targetMethod;

    public ProceedingJoinPoint(Method method, Object targetObject, Object[] args) {

        this.args=args;
        this.targetObject=targetObject;
        this.targetMethod=method;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    private Object targetObject;

    private Object[] args;


    public Object proceed() throws InvocationTargetException, IllegalAccessException {

        try {
            return targetMethod.invoke(targetObject, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {

        }
        return null;

    }
}
