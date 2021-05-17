package app.c2.service.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarAnalyzer {
    /**
     * returns all the main methods in a jar
     * @param jarPath
     * @return
     * @throws Exception
     */
    public static Map<Class, Method> getMainMethods(String jarPath) throws Exception {
        URL jarUrl;
         jarUrl = new URL("file:///"+jarPath);
         URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl});
        JarFile jar = new JarFile(jarPath);
        Map<Class, Method> methods = new HashMap<>();
        for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); )
        {
            JarEntry entry = entries.nextElement();
            String file = entry.getName();
            if (file.endsWith(".class"))
            {
                String classname =
                        file.replace('/', '.').substring(0, file.length() - 6).split("\\$")[0];
                try {
                    Class<?> classType = loader.loadClass(classname);
                    Method mainMethod = Arrays.asList( classType.getMethods()).stream()
                            .filter(x->x.getModifiers() == (Modifier.STATIC|Modifier.PUBLIC))
                            .filter(x->x.getName().equals("main"))
                            .filter(x->x.getReturnType().getName().equals("void"))
                            .filter(x->
                                x.getParameterTypes().length==1
                                        && x.getParameterTypes()[0].isArray()
                            ).findFirst().orElseGet(null);
                    if(mainMethod!=null){
                        methods.put(classType, mainMethod);
                    }
                } catch (Throwable e) {;
                }
            }
        }
        return methods;
    }
} 