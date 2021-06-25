package app.c2.service.nifi;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerFactory;
import app.c2.common.http.HttpUtil;
import app.c2.service.nifi.model.Breadcrumb;
import app.c2.service.nifi.model.NifiComponent;
import app.c2.service.nifi.model.ProcessGroupFlowBreadcumb;
import app.c2.service.nifi.model.ProcessGroupStatusEntityV2;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.hadoop.fs.InvalidRequestException;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractNifiSvc {
    private String nifiHost;
    private String principle;
    private String keytab;
    private String username;
    private String password;

    private String token;
    private long tokenLastUpdate = 0;

    public AbstractNifiSvc(String nifiHost) {
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

    public abstract Set<NifiComponent> findNifiComponent(String pattern, String processType) throws Exception;

    public void updateRunStatusById(String id, String status, String scope) throws Exception {
        try{
            ProcessorEntity process = getProcessor(id);
            updateRunStatus( id,  status);
        } catch (Exception e) {
            ProcessGroupStatusEntity process = getProcessGroupStatus(id);
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
            ProcessGroupStatusEntity processGroup = getProcessGroupStatus(groupid);
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

    ProcessorEntity getProcessor(String id) throws Exception {
        String s = requestProcessorJson(id);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        ProcessorEntity processorEntity = objectMapper.readValue(s, ProcessorEntity.class);
        return processorEntity;
    }

    ProcessGroupStatusEntity getProcessGroupStatus(String id) throws ApiException, IOException, LoginException {
        String s = requestProcessGroupStatusJson(id);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        ProcessGroupStatusEntity processGroupStatusEntity = objectMapper.readValue(s, ProcessGroupStatusEntity.class);
        return processGroupStatusEntity;
    }

    private ConnectionStatusEntity getProcessConnection(String id) throws ApiException, IOException, LoginException {
        String s = requestConnectionsJson(id);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ConnectionStatusEntity connectionStatusEntity = objectMapper.readValue(s, ConnectionStatusEntity.class);
        return connectionStatusEntity;
    }

    ProcessGroupStatusEntityV2 getSummary() throws ApiException, IOException, LoginException {
        String s = requestSummary("");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        ProcessGroupStatusEntityV2 processGroupStatusEntityV2 = objectMapper.readValue(s, ProcessGroupStatusEntityV2.class);
        return processGroupStatusEntityV2;
    }
    SearchResultsEntity getSearchResult(String keyword) throws IOException, LoginException {
        String s = requestSearchResultJson(keyword);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SearchResultsEntity searchResultsEntity = objectMapper.readValue(s, SearchResultsEntity.class);
        return searchResultsEntity;
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

    private String requestProcessGroupStatusJson(String id) throws IOException, LoginException {
        //String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id;
        String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id+"/status";

        return requestJson(url);
    }
    String requestProcessGroupJson(String id) throws IOException, LoginException {
        //String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id;
        String url = nifiHost+"/nifi-api"+"/flow/process-groups/"+id;

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

    private String requestSearchResultJson(String keyword) throws IOException, LoginException {
        String url = nifiHost+"/nifi-api"+"/flow/search-results?q="+keyword;
        return requestJson(url);
    }


    private String requestSummary(String keyword) throws IOException, LoginException {
        String url = nifiHost+"/nifi-api"+"/flow/process-groups/root/status?recursive=true";
        return requestJson(url);
    }
    class RequestResponse{
        String response;
        long lastUpdate;

        public RequestResponse(String response, long lastUpdate) {
            this.response = response;
            this.lastUpdate = lastUpdate;
        }
    }

    Map<String, RequestResponse> responseCache = new HashMap<>();

    private String requestJson(String url) throws IOException, LoginException {
        if(responseCache.containsKey(url)){
            if(System.currentTimeMillis()-responseCache.get(url).lastUpdate<1000*60){
                return responseCache.get(url).response;
            }
        }


        url = URIUtil.encodeQuery(url);
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
        strResponse=strResponse.replaceAll("\"statsLastRefreshed\":\"[^\"]*\",","");
        strResponse=strResponse.replaceAll(",\"statsLastRefreshed\":\"[^\"]*\"","");
        strResponse=strResponse.replaceAll("\"timestamp\":\"[^\"]*\",","");
        strResponse=strResponse.replaceAll(",\"timestamp\":\"[^\"]*\"","");

        responseCache.put(url, new RequestResponse(strResponse, System.currentTimeMillis()));
        return strResponse;
    }
    public enum ProcessType {
        ProcessGroup
    }
    public enum Scope {
        Root
    }
}
