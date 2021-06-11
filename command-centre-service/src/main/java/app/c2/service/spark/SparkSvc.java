package app.c2.service.spark;

import app.c2.common.SystemUtil;
import app.c2.service.spark.model.SparkArgKeyValuePair;
import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class SparkSvc {
    String sparkHome;
    String hadoopConfDir;
    public SparkSvc(String sparkHome, String hadoopConfDir) {
        this.sparkHome = sparkHome;
        this.hadoopConfDir = hadoopConfDir;
    }

    public String submitSpark(String appName, String main, File jar, List<String> args, Set<SparkArgKeyValuePair> sparkArgs, List<File> files) throws Exception {
        if (jar == null) {
            throw new Exception("Jar not found.");
        }

        Map<String, String> env = new HashMap<>();
        env.put("HADOOP_CONF_DIR", hadoopConfDir);
        SystemUtil.setEnv(env);

        SparkLauncher launcher= new SparkLauncher()
                .setSparkHome(sparkHome)
                .setMaster("yarn")
                .setDeployMode("cluster")
                .setAppResource(jar.getAbsolutePath())
                .setMainClass(main)
                .setAppName(appName);

        if(args!=null && args.size()>0){
            for(String arg: args){
                launcher = launcher.addAppArgs(arg);
            }
        }
        if(sparkArgs!=null && sparkArgs.size()>0) {
            for (SparkArgKeyValuePair kv : sparkArgs) {
                launcher = launcher.addSparkArg(kv.getName(), kv.getValue());
            }
        }

        if(files!=null && files.size()>0){
            for(File file: files){
                launcher = launcher.addFile(file.getAbsolutePath());
            }
        }
        return startSparkLauncher(launcher);
    }

//    private String startSparkLauncher(SparkLauncher launcher) throws IOException {
//        SparkAppHandle handler = null;
//        String applicationId = null;
//        handler = launcher.startApplication();
//        while(!handler.getState().isFinal()){
//            applicationId = handler.getAppId();
//            if(applicationId!=null){
//                break;
//            }
//        }
//        handler.disconnect();
//
//        return applicationId;
//    }

    private String startSparkLauncher(SparkLauncher launcher) throws InterruptedException, IOException {
        final String[]applicationId = {null};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SparkAppHandle handle = launcher.startApplication(new SparkAppHandle.Listener() {
            @Override
            public void stateChanged(SparkAppHandle sparkAppHandle) {
                if (countDownLatch.getCount() == 0)
                    return;
                if (sparkAppHandle.getState().toString().equalsIgnoreCase("RUNNING")) {
                    applicationId[0] = sparkAppHandle.getAppId();
                    countDownLatch.countDown();
                } else if (sparkAppHandle.getState().isFinal()) {
                    countDownLatch.countDown();
                }
            }

            @Override
            public void infoChanged(SparkAppHandle sparkAppHandle) {

            }
        });
        countDownLatch.await();
        handle.disconnect();
        return applicationId[0];
    }

}
