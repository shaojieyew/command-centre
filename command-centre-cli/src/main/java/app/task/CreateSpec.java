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
public class CreateSpec extends Task{
    @Autowired
    CreateFileGroup createFileGroup;
    @Autowired
    CreateApp createApp;
    @Autowired
    CreateNifiQuery createNifiQuery;

    public void startTask(Cli cli) throws Exception {
        startTask(cli, cli.getSpecFile());
    }

    public void startTask(Cli cli, List<Kind> k) throws Exception {
        for(Kind kind : k){
            if(kind instanceof GroupResourceKind){
                List<GroupResourceSpec> spec = kind.getSpec();
                for(GroupResourceSpec s : spec){
                    createFileGroup.startTask(cli, s);
                }
            }}
        for(Kind kind :k){
            if(kind instanceof SparkDeploymentKind) {
                List<SparkDeploymentSpec> spec = kind.getSpec();
                for (SparkDeploymentSpec s : spec) {
                    createApp.startTask(cli, s);
                }
            }
        }
        for(Kind kind : k){
            if(kind instanceof NifiQueryKind) {
                List<NifiQuerySpec> spec = kind.getSpec();
                for (NifiQuerySpec s : spec) {
                    createNifiQuery.startTask(cli, s);
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
