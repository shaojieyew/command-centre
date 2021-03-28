package c2.services.nifi;

import com.davis.client.ApiClient;
import com.davis.client.ApiException;
import com.davis.client.api.FlowApi;
import com.davis.client.model.*;
import org.apache.hadoop.fs.InvalidRequestException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.HttpService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NifiSvc {
    private String nifiHost;

    public static FlowApi flowApi = null;

    public NifiSvc(String nifiHost) {
        this.nifiHost = nifiHost;
        ApiClient apiClient = new ApiClient().setBasePath(nifiHost+"/nifi-api");
        flowApi = new FlowApi(apiClient);
    }

    public static final String NIFI_RUN_STATUS_STOPPED = "STOPPED";
    public static final String NIFI_RUN_STATUS_DISABLED = "DISABLED";
    public static final String NIFI_RUN_STATUS_RUNNING = "RUNNING";


    /**
     * get list of process groups by regex pattern
     * @param patternString on empty or null return all process groups
     * @return
     */
    public Map<ProcessGroupStatusDTO, String> findProcessGroup(String patternString){
        Map<ProcessGroupStatusDTO, String> result = getAllProcessGroup("root", new ArrayList<String>());
        if(patternString==null || patternString.trim().length()==0){
            return result;
        }

        List<ProcessGroupStatusDTO> groupToDrop =result.keySet().stream()
                .filter(k->{
            Pattern pattern = Pattern.compile(patternString.trim());
            return !(pattern.matcher(result.get(k)+"/"+k.getName()).find());
            //return !(pattern.matcher(k.getName()).matches() || k.getId().equals(patternString.trim()) || pattern.matcher(result.get(k)).matches());
                }).collect(Collectors.toList());

        for(ProcessGroupStatusDTO k : groupToDrop){
            result.remove(k);
        }
        return result;
    }

    /**
     * get list of processors by regex pattern
     * @param patternString on empty or null return all processors
     * @return
     */
    public Map<ProcessorStatusDTO, String>  findProcessor(String patternString){
        Map<ProcessorStatusDTO, String> result= getAllProcess("root", new ArrayList<String>());
        if(patternString==null || patternString.trim().length()==0){
            return result;
        }

        List<ProcessorStatusDTO> processorToDrop =result.keySet().stream()
                .filter(k->{
                    Pattern pattern = Pattern.compile(patternString.trim());
                    return !(pattern.matcher(result.get(k)+"/"+k.getName()).find());
                   // return !(pattern.matcher(k.getName()).matches() || k.getId().equals(patternString.trim()) || pattern.matcher(result.get(k)).matches());
                }).collect(Collectors.toList());


        for(ProcessorStatusDTO k : processorToDrop){
            result.remove(k);
        }
        return result;
    }

    /**
     * Update the processor by the processor Id
     * @param id
     * @param status
     * @throws Exception
     */
    public void updateRunStatus(String id, String status) throws IOException, ParseException {
        String entity = getProcessorJson(id);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(entity);
        JSONObject jsonAppsObject = (JSONObject)json.get("revision");
        String revision = jsonAppsObject.toString();

        String url = nifiHost+":"+8081+"/nifi-api"+"/processors/"+id+"/run-status";

        HashMap<String,String> requestMap = new HashMap<>();
        requestMap.put("content-type","application/json");
        HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.PUT, url, requestMap, "{\"revision\":"+revision+",\"state\":\""+status+"\",\"disconnectedNodeAcknowledged\":false}");
        // HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.PUT, url, requestMap, "{\"revision\":{\"clientId\":\""+"53a1ac4c-0177-1000-cd3a-285758ff3b47"+"\",\"version\":1},\"state\":\""+status+"\",\"disconnectedNodeAcknowledged\":false}");
        int statusCode = con.getResponseCode();

        String strResponse = HttpService.inputStreamToString(con.getInputStream());
        if(statusCode != 200){
            throw new InvalidRequestException(strResponse);
        }
    }

    /**
     * update all processors to the status within a processor group by process groupid
     * @param groupid
     * @param status
     * @param updateOnlyLeadingProcessor
     */
    public void updateAllProcessInProcessGroup(String groupid, String status, boolean updateOnlyLeadingProcessor) {
        try {
            ProcessGroupStatusEntity processGroup = getProcessGroup(groupid);
            Set<String> nonLeadProcessors = new HashSet<>();
            if(updateOnlyLeadingProcessor){
                nonLeadProcessors = processGroup.getProcessGroupStatus().getAggregateSnapshot().getConnectionStatusSnapshots()
                        .stream().map(c->{
                            try {
                                return getConnection( c.getId()).getConnectionStatus().getDestinationId();
                            } catch (ApiException e) {
                                e.printStackTrace();
                            }
                            return "";
                        })
                        .filter(c->c.length()>0)
                        .collect(Collectors.toSet());
            }

            for( ProcessorStatusSnapshotEntity processorStatusSnapshotEntity:processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessorStatusSnapshots() ){
                String processId = processorStatusSnapshotEntity.getId();
                if(updateOnlyLeadingProcessor){
                    if(!nonLeadProcessors.contains(processId)){
                        try {
                            updateRunStatus(processId,status);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    try {
                        updateRunStatus(processId,status);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
            for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
                String groupProcessId = processGroupStatusSnapshotEntity.getId();
                updateAllProcessInProcessGroup(groupProcessId,status, updateOnlyLeadingProcessor);
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private Map<ProcessGroupStatusDTO, String> getAllProcessGroup(String groupId, List<String> path) {
        Map<ProcessGroupStatusDTO, String> list = new HashMap<>();
        try {
            ProcessGroupStatusEntity processGroup = getProcessGroup(groupId);
            if(!groupId.equals("root"))
                list.put(processGroup.getProcessGroupStatus(), String.join("/",path));
            List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
            for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
                String groupProcessId = processGroupStatusSnapshotEntity.getId();
                path.add(processGroup.getProcessGroupStatus().getName());
                list.putAll(getAllProcessGroup(groupProcessId, path));
                path.remove(path.size()-1);
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Map<ProcessorStatusDTO, String> getAllProcess(String groupId, List<String> path) {
        Map<ProcessorStatusDTO, String> list = new HashMap<>();
        try {
            ProcessGroupStatusEntity processGroup = getProcessGroup(groupId);
            path.add(processGroup.getProcessGroupStatus().getName());
            for( ProcessorStatusSnapshotEntity processorStatusSnapshotEntity:processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessorStatusSnapshots() ){
                String processId = processorStatusSnapshotEntity.getId();
                list.put(getProcessor(processId).getProcessorStatus(), String.join("/",path));
            }
            List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
            for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
                String groupProcessId = processGroupStatusSnapshotEntity.getId();
                list.putAll(getAllProcess(groupProcessId, path));
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        path.remove(path.size()-1);
        return list;
    }

    private ProcessorStatusEntity getProcessor(String id) throws ApiException {
        return flowApi.getProcessorStatus(id, false, "");
    }

    private ProcessGroupStatusEntity getProcessGroup(String id) throws ApiException {
        return flowApi.getProcessGroupStatus(id, true, false, "");
    }

    private ConnectionStatusEntity getConnection(String id) throws ApiException {
        return flowApi.getConnectionStatus(id, false, "");
    }

    private String getProcessorJson(String id) throws IOException {
        String url = nifiHost+"/nifi-api"+"/processors/"+id;
        HashMap<String,String> requestMap = new HashMap<>();
        requestMap.put("content-type","application/json");
        HttpURLConnection con = HttpService.getConnection( HttpService.HttpMethod.GET, url, requestMap, null);
        int statusCode = con.getResponseCode();

        String strResponse = HttpService.inputStreamToString(con.getInputStream());

        if(statusCode != 200){
            throw new InvalidRequestException(strResponse);
        }
        return strResponse;
    }

}
