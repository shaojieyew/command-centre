package app.task;

import app.cli.Cli;
import app.spec.Kind;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import app.spec.resource.GroupResourceKind;
import app.spec.resource.GroupResourceSpec;
import app.spec.spark.SparkDeploymentKind;
import app.spec.spark.SparkDeploymentSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteSpec extends Task{
    @Autowired
    DeleteFile deleteFile;
    @Autowired
    DeleteApp deleteApp;
    @Autowired
    DeleteNifiQuery deleteNifiQuery;

    public void startTask(Cli cli) throws Exception {
        startTask(cli, cli.getSpecFile());
    }

    public void startTask(Cli cli, List<Kind> k) throws Exception {
        for(Kind kind :k){
            if(kind instanceof SparkDeploymentKind) {
                List<SparkDeploymentSpec> spec = kind.getSpec();
                for (SparkDeploymentSpec s : spec) {
                    deleteApp.startTask(cli, s);
                }
            }
        }
        for(Kind kind : k){
            if(kind instanceof GroupResourceKind){
                List<GroupResourceSpec> spec = kind.getSpec();
                for(GroupResourceSpec s : spec){
                    deleteFile.startTask(cli, s);
                }
            }}
        for(Kind kind : k){
            if(kind instanceof NifiQueryKind) {
                List<NifiQuerySpec> spec = kind.getSpec();
                for (NifiQuerySpec s : spec) {
                    deleteNifiQuery.startTask(cli, s);
                }
            }
        }
    }

    @Override
    protected String getTaskName() {
        return null;
    }

    @Override
    protected void task() throws Exception {

    }

}
