package app.task.spark;

import app.c2.service.yarn.YarnSvcFactory;
import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentSpec;
import app.util.ConsoleHelper;

import static app.task.spark.StopSparkApp.YARN_RUNNING_STATE;

public class ApplySparkApp extends RunSparkApp {

    public ApplySparkApp(SparkCli cli,  SparkDeploymentSpec spec) {
        super(cli,  spec);
    }

    @Override
    protected String getTaskName() {
        return "Apply Spark Application - "+spec.getName();
    }

    @Override
    protected void preTask() throws Exception {
        if(spec==null){
            throw new Exception("Invalid application spec");
        }
        String appName = spec.getName();

        YarnSvcFactory.create(cli.getC2CliProperties()).setStates(YARN_RUNNING_STATE)
                .get().stream()
                .filter(f->f.getName().equals(appName))
                .forEach(s->{
                    try {
                        YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(s.getId())
                                .kill();
                    } catch (Exception e) {
                        ConsoleHelper.console.display(e);
                    }
                });

        int count = 0;
        long runningCount = 0;
        do{
            runningCount = YarnSvcFactory.create(cli.getC2CliProperties()).setStates(YARN_RUNNING_STATE)
                    .get().stream()
                    .filter(f->f.getName().equals(appName))
                    .count();
            count++;
        } while(count<3 && runningCount >0);
    }
}
