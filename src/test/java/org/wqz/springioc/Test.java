package org.wqz.springioc;

import org.dom4j.DocumentException;
import org.wqz.springioc.factory.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/3 下午9:03
 */
public class Test {

    @org.junit.Test

    public void testIOC() throws DocumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");



    }

}
