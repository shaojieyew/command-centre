package app.c2.services.nifi;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerFactory;
import app.c2.common.http.HttpUtil;
import com.davis.client.ApiClient;
import com.davis.client.ApiException;
import com.davis.client.api.FlowApi;
import com.davis.client.model.*;
import org.apache.hadoop.fs.InvalidRequestException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NifiSvc {
    private String nifiHost;

    public static FlowApi flowApi = null;
    private String principle;
    private String keytab;
    private String username;
    private String password;

    public NifiSvc(String nifiHost) {
        this.nifiHost = nifiHost;
        ApiClient apiClient = new ApiClient().setBasePath(nifiHost+"/nifi-api");
        flowApi = new FlowApi(apiClient);
    }

    public void setPrinciple(String principle) {
        this.principle = principle;
    }

    public void setKeytab(String keytab) {
        this.keytab = keytab;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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
    public void updateRunStatus(String id, String status) throws Exception {
        String entity = requestProcessorJson(id);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(entity);
        JSONObject jsonAppsObject = (JSONObject)json.get("revision");
        String revision = jsonAppsObject.toString();

        String url = nifiHost+"/nifi-api"+"/processors/"+id+"/run-status";
        HttpCaller httpCaller = HttpCallerFactory.create();
        HttpPut httpPut = new HttpPut(url);
        try {
            String token = requestToken();
            httpPut.addHeader("Authorization","Bearer "+token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        httpPut.addHeader("content-type",MediaType.APPLICATION_JSON);
        String requestJson = "{\"revision\":"+revision+",\"state\":\""+status+"\",\"disconnectedNodeAcknowledged\":false}";
        httpPut.setEntity(HttpUtil.stringToHttpEntity(requestJson));
        HttpResponse response = httpCaller.execute(httpPut);
        try{
            int statusCode = response.getStatusLine().getStatusCode();
            String strResponse = HttpUtil.httpEntityToString(response.getEntity());
            if(statusCode != 200){
                throw new InvalidRequestException(strResponse);
            }
        }catch (Exception ex){
            System.out.println("Failed on "+ id);
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


    private String requestToken() throws Exception {
        String token = null;
        if(username!=null && password!=null){
            token = requestToken(username,password);
        }
        if(principle!=null && keytab!=null){
            token = requestTokenKerberos(principle,keytab);
        }
        return token;
    }

    private String requestToken(String username, String password) throws Exception {
        String url = nifiHost+"/nifi-api"+"/access/token";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type",MediaType.APPLICATION_FORM_URLENCODED);
        httpPost.setEntity(new StringEntity(String.format("username=%s&password=%s",username, password)));
        HttpResponse response = HttpCallerFactory.create().execute(httpPost);
        String token = HttpUtil.httpEntityToString(response.getEntity());
        if(response.getStatusLine().getStatusCode()!=200){
            throw new Exception(token);
        }
        return token;
    }

    private String token;
    private long tokenLastUpdate = 0;
    private String requestTokenKerberos(String principle, String keytab) throws Exception {
        if((System.currentTimeMillis()-tokenLastUpdate)<(1000*60*60) && token!=null && token.length()>0){
            return token;
        }
        String url = nifiHost+"/nifi-api"+"/access/kerberos";
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = HttpCallerFactory.create(principle,keytab).execute(httpPost);
        token = HttpUtil.httpEntityToString(response.getEntity());
        if(response.getStatusLine().getStatusCode()!=200){
            throw new Exception(token);
        }
        tokenLastUpdate = System.currentTimeMillis();
        return token;
    }

    private String requestProcessorJson(String id)  {
        String url = nifiHost+"/nifi-api"+"/processors/"+id;
        HttpCaller httpCaller = HttpCallerFactory.create();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("content-type",MediaType.APPLICATION_JSON);
        try {
            String token = requestToken();
            httpGet.addHeader("Authorization","Bearer "+token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpResponse response = httpCaller.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();
        String strResponse="";
        try{
            strResponse = HttpUtil.httpEntityToString(response.getEntity());
            if(statusCode != 200){
                throw new InvalidRequestException(strResponse);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return strResponse;
    }

}
