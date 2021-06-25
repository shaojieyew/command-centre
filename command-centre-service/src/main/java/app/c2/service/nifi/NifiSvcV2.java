package app.c2.service.nifi;

import app.c2.service.nifi.model.NifiComponent;
import app.c2.service.nifi.model.ProcessGroupStatusEntityV2;
import com.davis.client.ApiException;
import org.apache.commons.lang.StringUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NifiSvcV2 extends AbstractNifiSvc {

    public static void main(String arg[]) throws Exception {
        NifiSvcV2 nifiSvc = new NifiSvcV2("http://localhost:8080");
        nifiSvc.findNifiComponent("41659bc4-017a-1000-f7c7-5a40a3605662","ProcessGroup")
                .stream().forEach(System.out::println);
    }

    public NifiSvcV2(String nifiHost) {
        super(nifiHost);
    }

    @Override
    public Set<NifiComponent> findNifiComponent(String pattern, String processType) throws Exception {
        if(processType==null){
            processType = "";
        }
        if(pattern==null){
            pattern="";
        }
        String finalPattern = pattern;
        String finalProcessType = processType;
        return listSummary()
                .filter(nifiComponent ->{
                    boolean matchingProcessType = true;
                    if(!StringUtils.isEmpty(finalProcessType)){
                        matchingProcessType = finalProcessType.equalsIgnoreCase(nifiComponent.getType());
                    }
                    if(finalPattern.equals(nifiComponent.getId())
                            || finalPattern.equals(nifiComponent.getGroupId())){
                        return matchingProcessType;
                    }

                   // boolean isProcessGroup = false;
                    //isProcessGroup = ProcessType.ProcessGroup.name().equalsIgnoreCase(nifiComponent.getType());
                    if(finalProcessType.equalsIgnoreCase(ProcessType.ProcessGroup.name())){
                        return Pattern.compile(finalPattern.trim()).matcher(nifiComponent.getFlowPath()).find() && matchingProcessType;
                    }else{
                        return Pattern.compile(finalPattern.trim()).matcher(nifiComponent.getFlowPath()).find() && matchingProcessType ;
                    }
                })
                .collect(Collectors.toSet());
    }

    public Stream<NifiComponent>  listSummary() throws LoginException, ApiException, IOException {
        ProcessGroupStatusEntityV2 summary = getSummary();
        return getAllNifiComponent(null, null, summary.getProcessGroupStatus().getAggregateSnapshot());
    }

    public Stream<NifiComponent> getAllNifiComponent(String flowpath,String groupName, app.c2.service.nifi.model.ProcessGroupStatusSnapshotDTO processGroupStatusSnapshotDTO) throws LoginException, ApiException, IOException {
        if(groupName==null){
            groupName="";
        }
        if(flowpath==null){
            flowpath="";
        }
        String finalGroupName = groupName;
        String finalFlowpath = flowpath;

        Stream<NifiComponent> nifiComponentStream = processGroupStatusSnapshotDTO.getProcessorStatusSnapshots()
                .stream().map(processor->{
                            String name = processor.getProcessorStatusSnapshot().getName();
                            String flowPath = String.format("%s/%s", finalFlowpath, name);
                            NifiComponent nifiComponent = new NifiComponent();
                            nifiComponent.setFlowPath(flowPath);
                            nifiComponent.setName(name);
                            nifiComponent.setGroupId(processor.getProcessorStatusSnapshot().getGroupId());
                            nifiComponent.setGroup(finalGroupName);
                            nifiComponent.setId(processor.getId());
                            nifiComponent.setType(processor.getProcessorStatusSnapshot().getType());
                            nifiComponent.setStatus(String.valueOf(processor.getProcessorStatusSnapshot().getRunStatus()));
                            return nifiComponent;
                        }
                );

        Stream<NifiComponent> nestedNifiComponentStream = processGroupStatusSnapshotDTO.getProcessGroupStatusSnapshots().stream()
                .flatMap(pg->{
                    try {
                        NifiComponent nifiComponent = new NifiComponent();
                        nifiComponent.setName(pg.getProcessGroupStatusSnapshot().getName());
                        nifiComponent.setId(pg.getId());
                        nifiComponent.setType(ProcessType.ProcessGroup.name());
                        nifiComponent.setGroup(finalGroupName);
                        nifiComponent.setFlowPath(finalFlowpath +"/"+pg.getProcessGroupStatusSnapshot().getName());
                        nifiComponent.setGroupId(processGroupStatusSnapshotDTO.getId());
                        NifiComponent[] group = {nifiComponent};
                        return Stream.concat(Arrays.stream(group), getAllNifiComponent(finalFlowpath +"/"+pg.getProcessGroupStatusSnapshot().getName(),pg.getProcessGroupStatusSnapshot().getName(),pg.getProcessGroupStatusSnapshot()));
                    } catch (LoginException | ApiException | IOException ignored) {
                    }
                    return Stream.empty();
                });
        return Stream.concat(nifiComponentStream, nestedNifiComponentStream);
    }


}
