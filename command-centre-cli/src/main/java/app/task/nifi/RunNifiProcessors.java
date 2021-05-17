package app.task.nifi;

import app.c2.service.nifi.NifiSvc;
import app.c2.service.nifi.NifiSvcFactory;
import app.c2.service.nifi.model.NifiComponent;
import app.cli.NifiCli;
import app.spec.Kind;
import app.spec.nifi.NifiQuerySpec;
import app.task.Task;
import com.davis.client.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RunNifiProcessors extends UpdateNifiProcessorsStatus {

    public RunNifiProcessors(NifiCli cli) {
        super(cli);
    }

    @Override
    public String getStatus() {
        return NifiSvc.NIFI_RUN_STATUS_RUNNING;
    }
}
