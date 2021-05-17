package app.c2.service.spark;

import app.c2.properties.C2Properties;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
public class SparkSvcFactory {
    public static SparkSvc create(String sparkHome, C2Properties properties) throws IOException {
        String hadoopConfDir = "tmp/hadoopConf";
        File hadoopConfDirFile = new File(hadoopConfDir);
        FileUtils.deleteQuietly(hadoopConfDirFile);
        hadoopConfDirFile.mkdirs();
        FileUtils.copyFile(new File(properties.getHadoopYarnProperties().getCoreSite()),new File(hadoopConfDir+"/core-site.xml"));
        FileUtils.copyFile(new File(properties.getHadoopYarnProperties().getHdfsSite()),new File(hadoopConfDir+"/hdfs-site.xml"));
        FileUtils.copyFile(new File(properties.getHadoopYarnProperties().getYarnSite()),new File(hadoopConfDir+"/yarn-site.xml"));
        SparkSvc sparkSvc = new SparkSvc(sparkHome, hadoopConfDir);
        return sparkSvc;
    }
}
