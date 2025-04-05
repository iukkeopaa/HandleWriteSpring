package org.wqz.springioc.factory;

import org.dom4j.DocumentException;
import org.wqz.springioc.annotation.*;
import org.wqz.springioc.parser.SpringConfigParser;
import org.wqz.springioc.proxy.CglibProxy;
import org.wqz.springioc.proxy.JdkProxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/3 下午5:27
 */
public class ClassPathXmlApplicationContext {


    //applicationContent.xml
    private String springConfig;

    //存储
    private List<String> classPath =new ArrayList<String>();

    //存储IOC容器  key:对象实现的接口key 实现接口的对象作为value 
    private Map<Class<?>,List<Object>> iocInterfaces = new ConcurrentHashMap<Class<?>,List<Object>>();
    //存储IOC扫描对象（IOC容器） key 被扫描的class value 这个类对应的对象
    private Map<Class<?>,Object> iocContainer = new ConcurrentHashMap<Class<?>,Object>();

    private Map<String,Object> iocNameContainer = new ConcurrentHashMap<String,Object>();

    private Set<Class<?>> proxyClasses = new HashSet<Class<?>>();

    private Set<Class<?>> targetClasses = new HashSet<Class<?>>();

    public ClassPathXmlApplicationContext(String springConfig) throws DocumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.springConfig=springConfig;

       this.init();
    }

    private void init() throws DocumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {


        //1. 调用xml解析方法解析applicationContext.xml获取到要扫描的包
        String componentScanPackage = SpringConfigParser.getComponentScanPackage(springConfig);
        System.out.println("解析applicationContext.xml获取到要扫描的包:"+componentScanPackage);
        //2.扫描包下的所有的类

        this.laodClassess(componentScanPackage);
        System.out.println("classPath"+classPath);

        this.doInitInstance();

        System.out.println("切面类的集合"+proxyClasses);

        //this.doAOP();
        //        System.out.println("目标类的集合"+targetClasses);
        //
        //
        //
        //
        //        this.doDI();

        this.doAOP();
        System.out.println("目标类的集合"+targetClasses);

        this.doDI();

        System.out.println("实现了接口的类的集合"+iocInterfaces);
        System.out.println("IOC对象的集合"+iocContainer);
        System.out.println("IOC对象名字为key的集合");

    }

    private void doAOP() throws ClassNotFoundException, IllegalAccessException {

        if (proxyClasses.size()>0){

            for (Class<?> c :proxyClasses){

                Method[] declaredMethods = c.getDeclaredMethods();


                if(declaredMethods!=null){

                    for(Method method:declaredMethods){

                        boolean annotationPresent = method.isAnnotationPresent(Around.class);

                        if(annotationPresent){

                            Around aroundAnnotation = method.getAnnotation(Around.class);
                            String value=aroundAnnotation.value();


                            String fulltargetClassName =value.substring(0,value.lastIndexOf("."));

                            String targetMethodName =value.substring(value.lastIndexOf(".")+1,value.length()-2);


                            Class<?> targetClass = Class.forName(fulltargetClassName);

                            targetClasses.add(targetClass);


                            Object targetObject = this.iocContainer.get(targetClass);

                            //先把dao层注入都service

                            this.doDIByClass(targetClass);

                            JdkProxy<Object> jdkProxy = new JdkProxy<Object>(targetClass,c,targetObject,targetMethodName,method);

                            Object proxyInnstance = jdkProxy.getProxyInnstance();

                            //



                            iocContainer.put(targetClass,proxyInnstance);


                            String simpleName = targetClass.getSimpleName();
                           String targetClassName= simpleName.substring(0,1).toLowerCase()+simpleName.substring(1);

                            Service serviceAnnotation = targetClass.getAnnotation(Service.class);

                            if (serviceAnnotation!=null){

                                String value1 = serviceAnnotation.value();
                                if(!value1.equals("")){

                                    targetClassName=value1;
                                }
                            } else {

                                Controller controllerAnnotation = targetClass.getAnnotation(Controller.class);

                                String value1 = controllerAnnotation.value();
                                if(!value1.equals("")){

                                    targetClassName=value1;
                                }
                            }

                            iocNameContainer.put(targetClassName,proxyClasses);

                            Class<?>[] interfaces = targetClass.getInterfaces();

                            if(interfaces==null) continue;

                            for (Class<?> anInterface:interfaces){

                                List<Object> objects = iocInterfaces.get(anInterface);

                                for(int i=0;i<objects.size();i++){

                                    Object eachobj = objects.get(i);
                                    if (eachobj.getClass()==targetClass){

                                        objects.set(i,proxyInnstance);
                                        break;
                                    }

                                }
                            }


                        }
                    }
                }
            }
        }
    }

    private void doDI() throws IllegalAccessException {

        Set<Class<?>> classes = iocContainer.keySet();

        for(Class<?> aclass:classes){

            if (targetClasses .contains(aclass)){
                this.doDIByClass(aclass);
            }


        }
    }

    private void doDIByClass(Class<?> aclass) throws IllegalAccessException {
        Field[] declaredFields = aclass.getDeclaredFields();

        if(declaredFields!=null){
            for (Field declaredField:declaredFields ){
                boolean annotationPresent = declaredField.isAnnotationPresent(Autowired.class);
                if(annotationPresent){

                    Autowired autowiredAnnotation = declaredField.getAnnotation(Autowired.class);
                    String objectName = autowiredAnnotation.value();
                    Object bean=null;

                    if (!"".equals(objectName)){



                        bean = this.getBean(objectName);


                        if(bean==null){

                            throw new RuntimeException("异常：No named bean:"+objectName);

                        }


                    }else {

                        Class<?> type = declaredField.getType();

                       bean= this.getBean(type);

                       if (bean==null){


                           bean=this.getBeanByInterface(type);


                           if (bean==null){
                               throw new RuntimeException("yichang");
                           }

                       }


                    }

                    //依赖注入

                    declaredField.setAccessible(true);

                    Object o = iocContainer.get(aclass);
                    declaredField.set(o,bean);







                }
            }
        }
    }

    public Object getBean(String objectName){

        if (iocNameContainer.containsKey(objectName)){
            return iocNameContainer.get(objectName);
        }

        return null;
    }

    public Object getBean(Class<?> c){

        if (iocContainer.containsKey(c)){

             return iocContainer.get(c);
        }

        return null;
    }


    public Object getBeanByInterface(Class<?> c){

        List<Object> objects = iocInterfaces.get(c);
        if (objects==null){
            return null;
        }

        if(objects.size()>1){

            throw new RuntimeException("异常");
        }

        return objects.get(0);


    }

    private void doInitInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for(String classPath:classPath){

            Class<?> c = Class.forName(classPath);

            if (c.isAnnotationPresent(Aspect.class)){
                if(!proxyClasses.contains(c)){

                    proxyClasses.add(c);

                    continue;
                }

            }


            if(c.isAnnotationPresent(Service.class)||c.isAnnotationPresent(Controller.class)||c.isAnnotationPresent(Repository.class)){
                Object o = c.newInstance();

                Method[] declaredMethods = c.getDeclaredMethods();

                List<String> transactionalMethods =new ArrayList<String>();

                for (Method declaredMethod : declaredMethods) {

                    boolean annotationPresent = declaredMethod.isAnnotationPresent(Transactional.class);
                    if(annotationPresent){
                        transactionalMethods.add(declaredMethod.getName());
                    }
                }
                if (transactionalMethods.size()>0){

                    CglibProxy cglibProxy =new CglibProxy(c,transactionalMethods);
                    o = cglibProxy.getProxyInstance();


                }
                Class<?>[] interfaces = c.getInterfaces();
                if(interfaces!=null){
                    for(Class<?> anInterface:interfaces){
                        //TODO
                        List<Object> objects = iocInterfaces.get(interfaces);
                        if(objects==null){
                            ArrayList<Object> objs = new ArrayList<>();
                            objs.add(o);
                            iocInterfaces.put(anInterface,objects);
                        } else {
                            objects.add(o);
                        }
                    }
                }
                //把扫描的对象进行存储到IOD容器中
                iocContainer.put(c,o);

                Controller controllerAnnotation = c.getAnnotation(Controller.class);
                Service serviceAnnotation = c.getAnnotation(Service.class);

                if(controllerAnnotation!=null||serviceAnnotation!=null){

                    String value="";
                    if (controllerAnnotation!=null){
                        value=controllerAnnotation.value();

                    } else if (serviceAnnotation!=null) {

                        value=serviceAnnotation.value();
                        
                    }

                    String objectName="";

                    if ("".equals(value)){

                        //默认的对象的名字就是这个的类名小写

                        String className = c.getSimpleName();//usercontroller

                      objectName=  String.valueOf(className.charAt(0)).toLowerCase()+ className.substring(1);

                    }else {

                        //
                        objectName=value;
                    }


                    if (iocNameContainer.containsKey(objectName)){

                        throw new RuntimeException("Spring IOC container has already exits the bean name"+objectName);
                    }
                    iocNameContainer.put(objectName,o);



                }
            }
        }
    }

    /**
     *
     * @param componentScanPackage
     */
    private void laodClassess(String componentScanPackage) {

        URL url = Thread.currentThread().getContextClassLoader().getResource("");

        System.out.println(url.toString());

        componentScanPackage = componentScanPackage.replace(".", "/");

        String classPath = url.toString().replace("file:/", "");
        if (classPath.contains("test-classes")) {
            classPath = classPath.replace("test-classes", "classes");
        }

        classPath = classPath + componentScanPackage;
        System.out.println(classPath);
        //递归扫描指定路径下的所有的classes
        this.findAllClasses(new File(classPath));



    }

    private void findAllClasses(File file) {
        File[] files = file.listFiles();
        for (File f:files){
            if(!f.isDirectory()){
                //不是目录

                String fileName = f.getName();
                if(fileName.endsWith(".class")){
                    String path = f.getPath();
//                    System.out.println(path);
                    String s = this.handleClassPath(path);
                    System.out.println(s);
                    classPath.add(s);
                }
            }else {

                this.findAllClasses(f);


            }
        }
    }

    private String handleClassPath(String path){

        int index = path.indexOf("classes\\");
        path=path.substring(index+8,path.length()-6);
        path = path.replace("\\", ".");
        return path;

    }
}
