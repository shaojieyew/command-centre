package example.hdfs;

import app.c2.properties.C2Properties;
import app.c2.properties.C2PropertiesLoader;
import app.c2.service.hdfs.HdfsSvc;
import app.c2.service.hdfs.HdfsSvcFactory;
import com.google.common.io.Resources;

public class HdfsExample {

    public static void main(String arg[]) throws Exception {
        String path = Resources.getResource("setting.yml").getPath();
        C2Properties c2PlatformProperties =  C2PropertiesLoader.load(path   );
        HdfsSvc client = HdfsSvcFactory.create(c2PlatformProperties.getHadoopYarnProperties());
        client.getFileStatusList("/").stream().forEach(p->System.out.println(p.getPathSuffix()));
        boolean dirCreated =client.createDirectory("/user/abc/aaa");
        System.out.println(dirCreated);
        boolean renamed =client.renameFile("/user/abc/aaa","/user/abc/c");
        System.out.println(renamed);
        boolean deleted =client.deleteFile("/user/abc",true);
        System.out.println(deleted);
    }
}
