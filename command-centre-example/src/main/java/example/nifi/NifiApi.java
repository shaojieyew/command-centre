package example.nifi;

import app.c2.service.nifi.NifiSvc;
import app.c2.service.nifi.model.NifiComponent;
import com.davis.client.ApiException;

import java.util.Set;

public class NifiApi {


    public static void main(String args[]) throws Exception {
        NifiSvc svc = new NifiSvc("http://localhost:8081");

        // search for process group
        Set<NifiComponent> a= svc.findNifiComponent("Ni.*/aa.*", NifiSvc.ProcessType.ProcessGroup.toString(), null);
        // search for processor
        Set<NifiComponent> b= svc.findNifiComponent("Ni.*/.*", null, null);
        // start processor/stop processor
        b.stream().forEach(k->{
            try {
                svc.updateRunStatusById(k.getId(),NifiSvc.NIFI_RUN_STATUS_STOPPED, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // start processor/stop processor in group
//        svc.updateAllProcessInProcessGroup("27281353-0179-1000-a523-953b88dae040",NifiSvc.NIFI_RUN_STATUS_STOPPED,true);
    }

}
