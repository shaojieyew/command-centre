package example.hdfs;

import c2.services.hdfs.HdfsSvc;

public class HdfsExample {

    public static void main(String arg[]) throws Exception {
        HdfsSvc client = new HdfsSvc("http://localhost:9001","username");
        client.getFileStatusList("/").stream().forEach(p->System.out.println(p.getPathSuffix()));
        boolean dirCreated =client.createDirectory("/user/abc/aaa");
        System.out.println(dirCreated);
        boolean renamed =client.renameFile("/user/abc/aaa","/user/abc/c");
        System.out.println(renamed);
        boolean deleted =client.deleteFile("/user/abc",true);
        System.out.println(deleted);
    }
}
