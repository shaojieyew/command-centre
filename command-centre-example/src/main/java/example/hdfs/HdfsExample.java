package example.hdfs;

import app.c2.properties.C2Properties;
import app.c2.properties.C2PropertiesLoader;
import app.c2.services.hdfs.HdfsSvc;
import app.c2.services.hdfs.HdfsSvcFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import sun.misc.ClassLoaderUtil;

import java.net.URL;
import java.nio.charset.Charset;

public class HdfsExample {

    public static void main(String arg[]) throws Exception {
        String path = Resources.getResource("setting.yml").getPath();
        C2Properties c2PlatformProperties =  C2PropertiesLoader.load(path   );
        HdfsSvc client = HdfsSvcFactory.create(c2PlatformProperties);
        client.getFileStatusList("/").stream().forEach(p->System.out.println(p.getPathSuffix()));
        boolean dirCreated =client.createDirectory("/user/abc/aaa");
        System.out.println(dirCreated);
        boolean renamed =client.renameFile("/user/abc/aaa","/user/abc/c");
        System.out.println(renamed);
        boolean deleted =client.deleteFile("/user/abc",true);
        System.out.println(deleted);
    }
}
