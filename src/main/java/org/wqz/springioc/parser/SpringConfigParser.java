package org.wqz.springioc.parser;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @Description:
 * @Author: wjh
 * @Date: 2025/4/3 下午8:51
 */
public class SpringConfigParser {

    public static String getComponentScanPackage(String springConfig) throws IOException, DocumentException {


        InputStream inputStream=null;
        try {
            SAXReader saxReader = new SAXReader();
            inputStream= SpringConfigParser.class.getClassLoader().getResource(springConfig).openStream();
            Document document = saxReader.read(inputStream);
            Element rootElement = document.getRootElement();
            rootElement.element("component-scan ");
            Attribute attribute = rootElement.attribute("base-package");
            attribute.getValue();
        }catch (DocumentException e){
            e.printStackTrace();
        }finally {

            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }finally {

            }

        }

        return "";

    }
}
