package app.task.spark;

import app.c2.service.yarn.YarnSvc;
import app.c2.service.yarn.YarnSvcFactory;
import app.cli.SparkCli;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;

public class ApplySparkApp extends RunSparkApp {

    public ApplySparkApp(SparkCli cli, SparkDeploymentKind kind, SparkDeploymentSpec spec) {
        super(cli, kind, spec);
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
        YarnSvc yarnSvc = YarnSvcFactory.create(cli.getC2CliProperties());
        yarnSvc.setStates("NEW,NEW_SAVING,SUBMITTED,ACCEPTED,RUNNING")
                .get().stream()
                .filter(f->f.getName().equals(appName))
                .forEach(s->{
                    YarnSvcFactory.create(cli.getC2CliProperties()).setApplicationId(s.getId())
                            .kill();
                });
    }
}
