package app.task;

import app.Cli;
import app.spec.Kind;
import app.spec.nifi.NifiQueryKind;
import app.spec.nifi.NifiQuerySpec;
import app.spec.resource.GroupResourceKind;
import app.spec.resource.GroupResourceSpec;
import app.spec.spark.AppDeploymentKind;
import app.spec.spark.AppDeploymentSpec;
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
        for(Kind kind : cli.getSpecFile()){
            if(kind instanceof GroupResourceKind){
            List<GroupResourceSpec> spec = kind.getSpec();
            for(GroupResourceSpec s : spec){
                    createFileGroup.startTask(cli, s);
            }
        }}
        for(Kind kind : cli.getSpecFile()){
            if(kind instanceof AppDeploymentKind) {
                List<AppDeploymentSpec> spec = kind.getSpec();
                for (AppDeploymentSpec s : spec) {
                    createApp.startTask(cli, s);
                }
            }
        }
        for(Kind kind : cli.getSpecFile()){
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
