package app.task.nifi;

import app.c2.service.nifi.NifiSvc;
import app.cli.NifiCli;

public class StopNifiProcessors extends UpdateNifiProcessorsStatus {

    public StopNifiProcessors(NifiCli cli) {
        super(cli);
    }

    @Override
    public String getStatus() {
        return NifiSvc.NIFI_RUN_STATUS_STOPPED;
    }
}
