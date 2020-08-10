package com.keven.mvcframework.v1.servlet;
import com.keven.mvcframework.annotation.KevenAutowired;
import com.keven.mvcframework.annotation.KevenController;
import com.keven.mvcframework.annotation.KevenRequestMapping;
import com.keven.mvcframework.annotation.KevenService;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 简单基础版
 *
 * 满足CRUD基本功能(没有AOP、臃肿、无注解、无分层...）
 */
public class KevenDispatcherServlet extends HttpServlet {
    private Map<String,Object> mapping = new HashMap<String, Object>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {this.doPost(req,resp);}
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.mapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!");
            return;
        }
        Method method = (Method) this.mapping.get(url);
        Map<String,String[]> params = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{req, resp, params.get("name")[0]});
    }

    //当我晕车的时候，我就不去看源码了

    //init方法肯定干得的初始化的工作
    //inti首先我得初始化所有的相关的类，IOC容器、servletBean
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try{
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);
            String scanPackage = configContext.getProperty("scanPackage");
            // 扫描包初始化IOC封装key
            doScanner(scanPackage);
            // IOC容器key赋值
            for (String className : mapping.keySet()) {
                if(!className.contains(".")){
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(KevenController.class)){
                    mapping.put(className, clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(KevenRequestMapping.class)) {
                        KevenRequestMapping requestMapping = clazz.getAnnotation(KevenRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(KevenRequestMapping.class)) {
                            continue;
                        }
                        KevenRequestMapping requestMapping = method.getAnnotation(KevenRequestMapping.class);
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        mapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                }else if (clazz.isAnnotationPresent(KevenService.class)){
                        KevenService service = clazz.getAnnotation(KevenService.class);
                        String beanName = service.value();
                        if ("".equals(beanName)){
                            beanName = clazz.getName();
                        }
                        Object instance = clazz.newInstance();
                        mapping.put(beanName, instance);
                        for (Class<?> i : clazz.getInterfaces()) {
                            mapping.put(i.getName(), instance);
                        }
                }else {
                    continue;
                }
            }
            for (Object object : mapping.values()) {
                if (object == null){
                    continue;
                }
                Class clazz = object.getClass();
                if (clazz.isAnnotationPresent(KevenController.class)){
                    Field [] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (!field.isAnnotationPresent(KevenAutowired.class)){
                            continue;
                        }
                        KevenAutowired autowired = field.getAnnotation(KevenAutowired.class);
                        String beanName = autowired.value();
                        if ("".equals(beanName)){
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        try {
                            field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("Keven MVC Framework is init");
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()){
                doScanner(scanPackage + "." +  file.getName());
            }else {
                if (!file.getName().endsWith(".class")){
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class",""));
                mapping.put(clazzName, null);
            }
        }
    }
}