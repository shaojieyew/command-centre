package app.c2.service.nifi;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerFactory;
import app.c2.common.http.HttpUtil;
import app.c2.service.nifi.model.NifiComponent;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.hadoop.fs.InvalidRequestException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NifiSvc {
    private String nifiHost;
    private String principle;
    private String keytab;
    private String username;
    private String password;


    private String token;
    private long tokenLastUpdate = 0;

    public static void main(String arg[]) throws Exception {
        NifiSvc nifiSvc = new NifiSvc("http://localhost:8081");
        //Set<NifiComponent> s = nifiSvc.getAllProcess("root", new ArrayList<>(), new ArrayList<>());
        nifiSvc.updateAllProcessInProcessGroup("ccc8f601-0179-1000-8752-432197b03963","STOPPED", false);
    }

    public NifiSvc(String nifiHost) {
        this.nifiHost = nifiHost;
    }

    private void updateCredential() throws Exception {
        updateToken();
    }

    public void setPrinciple(String principle) throws Exception {
        this.principle = principle;
        updateCredential();
    }

    public void setKeytab(String keytab) throws Exception {
        this.keytab = keytab;
        updateCredential();
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

    public Set<NifiComponent> processCache;
    public long lastUpdatedProcessCache;

    public Set<NifiComponent> processGroupCache;
    public long lastUpdatedProcessGroupCache;



    public Set<NifiComponent> findNifiComponent(String pattern, String processType, String id) throws Exception {
        Set<NifiComponent> result = new HashSet<>();
        Exception error =null;
        if(processType==null){
           try{
               result.addAll(findProcessor( pattern, processType, id));
           }catch (Exception ex){
               error = ex;
           }
           try{
                result.addAll(findProcessGroup( pattern,  id));
           }catch (Exception ex){
               error = ex;
           }
           if(result.size()==0 && error!=null){
               throw error;
           }
        }else{
            if(!processType.trim().equalsIgnoreCase(ProcessType.ProcessGroup.toString())){
                return  findProcessor( pattern, processType, id);
            }else{
                return findProcessGroup( pattern, id);
            }
        }
        return result;
    }

    /**
     * get list of process groups by regex pattern
     * @param patternString on empty or null return all process groups
     * @return
     */

    public Set<NifiComponent> findProcessGroup(String patternString, String id) throws ApiException, IOException, LoginException {
        Set<NifiComponent> result = findProcessGroup();
        return result
                .stream()
                .filter(entry->{
                    if(patternString ==null || patternString.trim().length()==0 ||
                            (Pattern.compile(patternString.trim()).matcher(entry.getFlowPath()+"/"+entry.getName()).find())){
                        if(id ==null || id.trim().length()==0 ||
                               entry.getId().equalsIgnoreCase(id)){
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toSet());
    }

    /**
     * get list of processors by regex pattern
     * @param patternString on empty or null return all processors
     * @return
     */
    public Set<NifiComponent>  findProcessor(String patternString, String processType, String id) throws Exception {
        Set<NifiComponent> result= findProcess();
        return result
                .stream()
                .filter(entry->{
                    if(patternString ==null || patternString.trim().length()==0 ||
                            ( Pattern.compile(patternString.trim()).matcher(entry.getFlowPath()+"/"+entry.getName()).find())){
                        if(processType==null ||  processType.length()==0 ||
                                (processType!=null && processType.equalsIgnoreCase(entry.getType()))){
                            if(id==null || id.length()==0 ||
                                    id.equalsIgnoreCase(entry.getId()) ||
                                    entry.getFlowPathId().contains(id)){
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toSet());
    }

    public void updateRunStatusById(String id, String status, String scope) throws Exception {
        try{
            ProcessorEntity process = getProcessor(id);
            updateRunStatus( id,  status);
        } catch (ApiException e) {
            ProcessGroupStatusEntity process = getProcessGroup(id);
            boolean updateRootProcessorOnly = false;
            if(scope!=null){
                 updateRootProcessorOnly = scope.equalsIgnoreCase(Scope.Root.toString());
            }
            updateAllProcessInProcessGroup(id, status, updateRootProcessorOnly);
        }
    }

    /**
     * Update the processor by the processor Id
     * @param id
     * @param status
     * @throws Exception
     */
    private void updateRunStatus(String id, String status) throws Exception {
        String entity = requestProcessorJson(id);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject)parser.parse(entity);
        JSONObject jsonAppsObject = (JSONObject)json.get("revision");
        String revision = jsonAppsObject.toString();

        String url = nifiHost+"/nifi-api"+"/processors/"+id+"/run-status";
        HttpCaller httpCaller = HttpCallerFactory.create();
        HttpPut httpPut = new HttpPut(url);
        try {
            String token = updateToken();
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
            ex.printStackTrace();
        }
    }

    /**
     * update all processors to the status within a processor group by process groupid
     * @param groupid
     * @param status
     * @param updateOnlyRootProcessor
     */
    private void updateAllProcessInProcessGroup(String groupid, String status, boolean updateOnlyRootProcessor) {
        try {
            ProcessGroupStatusEntity processGroup = getProcessGroup(groupid);
            Set<String> nonLeadProcessors = new HashSet<>();
            if(updateOnlyRootProcessor){
                nonLeadProcessors = processGroup.getProcessGroupStatus().getAggregateSnapshot().getConnectionStatusSnapshots()
                        .stream().map(c->{
                            try {
                                return getProcessConnection( c.getId()).getConnectionStatus().getDestinationId();
                            } catch (ApiException e) {
                                e.printStackTrace();
                            } catch (LoginException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return "";
                        })
                        .filter(c->c.length()>0)
                        .collect(Collectors.toSet());
            }

            for( ProcessorStatusSnapshotEntity processorStatusSnapshotEntity:
                    processGroup.getProcessGroupStatus()
                    .getAggregateSnapshot().getProcessorStatusSnapshots() ){
                String processId = processorStatusSnapshotEntity.getId();
                if(updateOnlyRootProcessor){
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
                updateAllProcessInProcessGroup(groupProcessId,status, updateOnlyRootProcessor);
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Set<NifiComponent> findProcess() throws Exception {
        if(processCache==null || (System.currentTimeMillis()-lastUpdatedProcessCache)>15000L){
            processCache = getAllProcess("root", new ArrayList<String>(), new ArrayList<String>());
            lastUpdatedProcessCache = System.currentTimeMillis();
        }
        return new HashSet<>(processCache);
    }

    private Set<NifiComponent> findProcessGroup() throws ApiException, IOException, LoginException {
        if(processGroupCache==null || (System.currentTimeMillis()-lastUpdatedProcessGroupCache)>15000L){
            processGroupCache = getAllProcessGroup("root", new ArrayList<String>(), new ArrayList<String>());
            lastUpdatedProcessGroupCache = System.currentTimeMillis();
        }
        return new HashSet<>(processGroupCache);
    }

    private Set<NifiComponent> getAllProcessGroup(String groupId, List<String> path, List<String> idLineage) throws ApiException, IOException, LoginException {
        Set<NifiComponent> list = new HashSet<>();
            ProcessGroupStatusEntity processGroup = getProcessGroup(groupId);
            if(!groupId.equals("root")){
                NifiComponent nifiComponent = new NifiComponent();
                nifiComponent.setFlowPath( String.join("/",path));
                nifiComponent.setFlowPathId( String.join("/",idLineage));
                nifiComponent.setId( processGroup.getProcessGroupStatus().getId());
                nifiComponent.setName( processGroup.getProcessGroupStatus().getName());
                nifiComponent.setType( "ProcessGroup");
                list.add(nifiComponent);
            }
            List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
            for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
                String groupProcessId = processGroupStatusSnapshotEntity.getId();
                path.add(processGroup.getProcessGroupStatus().getName());
                idLineage.add(processGroup.getProcessGroupStatus().getId());
                list.addAll(getAllProcessGroup(groupProcessId, path, idLineage));
                path.remove(path.size()-1);
            }
        return list;
    }

    private Set<NifiComponent> getAllProcess(String groupId, List<String> path, List<String> idLineage) throws Exception {
        Set<NifiComponent> list = new HashSet<>();
        ProcessGroupStatusEntity processGroup = getProcessGroup(groupId);
        path.add(processGroup.getProcessGroupStatus().getName());
        idLineage.add(processGroup.getProcessGroupStatus().getId());
        for( ProcessorStatusSnapshotEntity processorStatusSnapshotEntity:processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessorStatusSnapshots() ){
            String processId = processorStatusSnapshotEntity.getId();
            ProcessorEntity nifiProcessor = getProcessor(processId);
            NifiComponent nifiComponent = new NifiComponent();
            nifiComponent.setFlowPath( String.join("/",path));
            nifiComponent.setFlowPathId( String.join("/",idLineage));
            nifiComponent.setId(nifiProcessor.getComponent().getId());
            nifiComponent.setName( nifiProcessor.getComponent().getName());
            String[]typePath = nifiProcessor.getComponent().getType().split("\\.");
            nifiComponent.setType( typePath[typePath.length-1]);
            nifiComponent.setStatus(nifiProcessor.getComponent().getState().toString());
            list.add(nifiComponent);
        }
        List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
        for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
            String groupProcessId = processGroupStatusSnapshotEntity.getId();
            list.addAll(getAllProcess(groupProcessId, path, idLineage));
        }

        path.remove(path.size()-1);
        return list;
    }

    private ProcessorEntity getProcessor(String id) throws Exception {
        String s = requestProcessorJson(id);
        s=s.replaceAll("\"statsLastRefreshed\":\"[^\"]*\",?","");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        ProcessorEntity processor = objectMapper.readValue(s, ProcessorEntity.class);
        return processor;
//        return flowApi.getProcessorStatus(id, false, "");
    }

    private ProcessGroupStatusEntity getProcessGroup(String id) throws ApiException, IOException, LoginException {

        String s = requestProcessGroupJson(id);
        s=s.replaceAll("\"statsLastRefreshed\":\"[^\"]*\",?","");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        ProcessGroupStatusEntity processor = objectMapper.readValue(s, ProcessGroupStatusEntity.class);
        return processor;
       // return flowApi.getProcessGroupStatus(id, true, false, "");
    }

    private ConnectionStatusEntity getProcessConnection(String id) throws ApiException, IOException, LoginException {

        String s = requestConnectionsJson(id);
        s=s.replaceAll("\"statsLastRefreshed\":\"[^\"]*\",?","");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ConnectionStatusEntity processor = objectMapper.readValue(s, ConnectionStatusEntity.class);
        return processor;
    }

    private String updateToken() throws Exception {
        String token = null;
        if(username!=null && password!=null){
            token = updateToken(username,password);
        }
        if(principle!=null && keytab!=null){
            token = updateTokenKerberos(principle,keytab);
        }
        return token;
    }

    private String updateToken(String username, String password) throws Exception {
        if((System.currentTimeMillis()-tokenLastUpdate)<(1000*60*15) && token!=null && token.length()>0){
            return token;
        }
        String url = nifiHost+"/nifi-api"+"/access/token";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type",MediaType.APPLICATION_FORM_URLENCODED);
        httpPost.setEntity(new StringEntity(String.format("username=%s&password=%s",username, password)));
        HttpResponse response = HttpCallerFactory.create().execute(httpPost);
        token = HttpUtil.httpEntityToString(response.getEntity());
        tokenLastUpdate = System.currentTimeMillis();
        return token;
    }

    private String updateTokenKerberos(String principle, String keytab) throws Exception {
        if((System.currentTimeMillis()-tokenLastUpdate)<(1000*60*15) && token!=null && token.length()>0){
            return token;
        }
        String url = nifiHost+"/nifi-api"+"/access/kerberos";
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = HttpCallerFactory.create(principle,keytab).execute(httpPost);
        token = HttpUtil.httpEntityToString(response.getEntity());
        tokenLastUpdate = System.currentTimeMillis();
        return token;
    }

    private String requestProcessGroupJson(String id) throws IOException, LoginException {
        //String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id;
        String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id+"/status";

        return requestJson(url);
    }
    private String requestConnectionsJson(String id) throws IOException, LoginException {
        //String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id;
        String url = nifiHost+"/nifi-api"+"/flow/connections/"+id+"/status";

        return requestJson(url);
    }

    private String requestProcessorJson(String id) throws IOException, LoginException {
        String url = nifiHost+"/nifi-api"+"/processors/"+id;
        return requestJson(url);
    }

    private String requestJson(String url) throws IOException, LoginException {
        HttpCaller httpCaller = HttpCallerFactory.create();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("content-type",MediaType.APPLICATION_JSON);
        try {
            String token = updateToken();
            httpGet.addHeader("Authorization","Bearer "+token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpResponse response = httpCaller.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();
        String strResponse="";
        strResponse = HttpUtil.httpEntityToString(response.getEntity());
        if(statusCode != 200){
            throw new InvalidRequestException(strResponse);
        }
        return strResponse;
    }
    public enum ProcessType {
        ProcessGroup
    }
    public enum Scope {
        Root
    }
}
