package example.nifi;

import app.c2.services.nifi.NifiSvc;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.davis.client.model.ProcessorStatusDTO;

import java.util.Map;

public class NifiApi {


    public static void main(String args[]) {
        NifiSvc svc = new NifiSvc("http://localhost:8081");

        // search for process group
        Map<ProcessGroupStatusDTO, String> a= svc.findProcessGroup("Ni.*/aa.*");
        // search for processor
        Map<ProcessorStatusDTO, String> b= svc.findProcessor("Ni.*/aa.*/Gen.*");
        // start processor/stop processor
        b.keySet().stream().forEach(k->{
            try {
                svc.updateRunStatus(k.getId(),NifiSvc.NIFI_RUN_STATUS_STOPPED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // start processor/stop processor in group
        svc.updateAllProcessInProcessGroup("27281353-0179-1000-a523-953b88dae040",NifiSvc.NIFI_RUN_STATUS_STOPPED,true);
    }

}
