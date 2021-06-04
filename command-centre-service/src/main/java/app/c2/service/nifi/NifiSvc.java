package app.c2.service.nifi;

import app.c2.common.http.HttpCaller;
import app.c2.common.http.HttpCallerFactory;
import app.c2.common.http.HttpUtil;
import app.c2.service.nifi.model.Breadcrumb;
import app.c2.service.nifi.model.NifiComponent;
import app.c2.service.nifi.model.ProcessGroupFlowBreadcumb;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.httpclient.util.URIUtil;
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
import java.util.*;
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

        Set<NifiComponent> res = nifiSvc.findNifiComponent("d200ee81-0179-1000-334a-72e0c82b53e2/d200fb9c-0179-1000-bfe9-67b2b78d0f87","processgroup");

        //Set<NifiComponent> s = nifiSvc.getAllProcess("root", new ArrayList<>(), new ArrayList<>());
        //nifiSvc.updateAllProcessInProcessGroup("ccc8f601-0179-1000-8752-432197b03963","STOPPED", false);
    }

    private NifiComponent updateFlowPath(NifiComponent result) throws Exception {

        Breadcrumb breadcrumb = null;
        if(ProcessType.ProcessGroup.toString().equals(result.getType())){
            String response = requestProcessGroupJson(result.getId());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .registerModule(new JodaModule());
            ProcessGroupFlowBreadcumb processGroupStatusEntity = objectMapper.readValue(response, ProcessGroupFlowBreadcumb.class);
            result.setId(processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getBreadcrumb().getId());
            result.setName(processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getBreadcrumb().getName());
            result.setType(ProcessType.ProcessGroup.toString());
            result.setFlowPathId("");
            result.setFlowPath("");
            breadcrumb = processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getParentBreadcrumb();
        }else{
            ProcessorEntity processorEntity = getProcessor(result.getId());
            result.setId(processorEntity.getId());
            result.setName(processorEntity.getComponent().getName());
            String typeP[] = processorEntity.getComponent().getType().split("\\.");
            result.setType(typeP[typeP.length-1]);
            result.setStatus(processorEntity.getStatus().getRunStatus());

            String response = requestProcessGroupJson(processorEntity.getComponent().getParentGroupId());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .registerModule(new JodaModule());
            ProcessGroupFlowBreadcumb processGroupStatusEntity = objectMapper.readValue(response, ProcessGroupFlowBreadcumb.class);
            result.setFlowPathId(processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getBreadcrumb().getId());
            result.setFlowPath(processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getBreadcrumb().getName());
            breadcrumb = processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getParentBreadcrumb();
        }
        while(breadcrumb!=null){
            Breadcrumb b =breadcrumb.getBreadcrumb();
            if(result.getFlowPathId().length()==0){
                result.setFlowPathId(b.getId());
                result.setFlowPath(b.getName());
            }else{
                result.setFlowPathId(b.getId()+"/"+result.getFlowPathId());
                result.setFlowPath(b.getName()+"/"+result.getFlowPath());
            }
            breadcrumb = breadcrumb.getParentBreadcrumb();
        }
        return result;
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

//    public Set<NifiComponent> processCache;
//    public long lastUpdatedProcessCache;
//
//    public Set<NifiComponent> processGroupCache;
//    public long lastUpdatedProcessGroupCache;
//

    public Set<NifiComponent> findNifiComponent(String pattern, String processType) throws Exception {
        String[] keyword = pattern.split("/");
        if(keyword.length==0){
            keyword = new String[]{"",""};
        }
        if(keyword[0].equals("*")){
            throw new Exception("NiFi query cannot starts with *");
        }
        Set<NifiComponent> results = new HashSet<>();

        String rootId = getProcessGroupStatus("root").getProcessGroupStatus().getId();

        List<String> lastGroupIds = null;
        for(int i =0;i< keyword.length; i++){
            String s = keyword[i];
            List<String> finalLastGroupIds = lastGroupIds;
            if(i==0 && s.length()==0){
                lastGroupIds = new ArrayList<>();
                lastGroupIds.add(rootId);
                continue;
            }

            if(i == keyword.length-1){
                if(s.length()>0 && !s.equals("*")){
                    SearchResultsEntity searchResult = getSearchResult(s);
                    List<NifiComponent> processResults = searchResult.getSearchResultsDTO().getProcessorResults().stream()
                            .filter(p-> s.equalsIgnoreCase(p.getName()) || s.equalsIgnoreCase(p.getId()))
                            .filter(p-> finalLastGroupIds == null || finalLastGroupIds.contains(p.getGroupId()))
                            .map(p->{
                                NifiComponent nifiComponent = new NifiComponent();
                                nifiComponent.setId(p.getId());
                                nifiComponent.setName(p.getName());
                                return nifiComponent;
                            })
                            .collect(Collectors.toList());
                    results.addAll(processResults);

                    List<NifiComponent> groupResults = searchResult
                            .getSearchResultsDTO()
                            .getProcessGroupResults().stream()
                            .filter(p-> s.equalsIgnoreCase(p.getName()) || s.equalsIgnoreCase(p.getId()))
                            .filter(p-> finalLastGroupIds == null || finalLastGroupIds.contains(p.getGroupId()))
                            .map(p->{
                                NifiComponent nifiComponent = new NifiComponent();
                                nifiComponent.setId(p.getId());
                                nifiComponent.setName(p.getName());
                                nifiComponent.setType(ProcessType.ProcessGroup.toString());
                                return nifiComponent;
                            })
                            .collect(Collectors.toList());
                    results.addAll(groupResults);

                    if(searchResult.getSearchResultsDTO().getProcessGroupResults().size()==0 &&
                    searchResult.getSearchResultsDTO().getProcessorResults().size()==0){
                        for (String lastGroupId : lastGroupIds) {
                            processResults = getProcessGroupStatus(lastGroupId)
                                    .getProcessGroupStatus().getAggregateSnapshot()
                                    .getProcessorStatusSnapshots()
                                    .stream()
                                    .filter(p->(Pattern.compile(s.trim()).matcher(p.getProcessorStatusSnapshot().getName()).find()))
                                    .map(p -> {
                                        NifiComponent nifiComponent = new NifiComponent();
                                        nifiComponent.setId(p.getId());
                                        nifiComponent.setName(p.getProcessorStatusSnapshot().getName());
                                        return nifiComponent;
                                    }).collect(Collectors.toList());
                            groupResults = getProcessGroupStatus(lastGroupId)
                                    .getProcessGroupStatus().getAggregateSnapshot()
                                    .getProcessGroupStatusSnapshots()
                                    .stream()
                                    .filter(p-> {
                                        try {
                                            return (Pattern.compile(s.trim()).matcher(getProcessGroupStatus(p.getId()).getProcessGroupStatus().getName()).find());
                                        } catch (ApiException | IOException | LoginException e) {
                                        }
                                        return false;
                                    })
                                    .map(p -> {
                                        NifiComponent nifiComponent = new NifiComponent();
                                        nifiComponent.setId(p.getId());
                                        nifiComponent.setType(ProcessType.ProcessGroup.toString());
                                        try {
                                            nifiComponent.setName(getProcessGroupStatus(p.getId()).getProcessGroupStatus().getName());
                                        } catch (ApiException | IOException | LoginException e) {
                                        }
                                        return nifiComponent;
                                    }).collect(Collectors.toList());
                            results.addAll(processResults);
                            results.addAll(groupResults);
                        }
                    }
                }else{
                    for (String lastGroupId : lastGroupIds) {
                        List<NifiComponent> processResults = getProcessGroupStatus(lastGroupId)
                                .getProcessGroupStatus().getAggregateSnapshot()
                                .getProcessorStatusSnapshots()
                                .stream().map(p -> {
                                    NifiComponent nifiComponent = new NifiComponent();
                                    nifiComponent.setId(p.getId());
                                    nifiComponent.setName(p.getProcessorStatusSnapshot().getName());
                                    return nifiComponent;
                                }).collect(Collectors.toList());
                        List<NifiComponent> groupResults = getProcessGroupStatus(lastGroupId)
                                .getProcessGroupStatus().getAggregateSnapshot()
                                .getProcessGroupStatusSnapshots()
                                .stream().map(p -> {
                                    NifiComponent nifiComponent = new NifiComponent();
                                    nifiComponent.setId(p.getId());
                                    nifiComponent.setType(ProcessType.ProcessGroup.toString());
                                    try {
                                        nifiComponent.setName(getProcessGroupStatus(p.getId()).getProcessGroupStatus().getName());
                                    } catch (ApiException | IOException | LoginException e) {
                                    }
                                    return nifiComponent;
                                }).collect(Collectors.toList());
                        results.addAll(processResults);
                        results.addAll(groupResults);
                    }
                }
            }else{
                if(s.length()>0 && !s.equals("*")){
                    SearchResultsEntity searchResult = getSearchResult(s);
                    if(searchResult.getSearchResultsDTO().getProcessGroupResults().size()>0){
                        List<NifiComponent> groupResults = searchResult
                                .getSearchResultsDTO()
                                .getProcessGroupResults().stream()
                                .filter(p-> s.equalsIgnoreCase(p.getName()) || s.equalsIgnoreCase(p.getId()))
                                .filter(p-> finalLastGroupIds == null || finalLastGroupIds.contains(p.getGroupId()))
                                .map(p->{
                                    NifiComponent nifiComponent = new NifiComponent();
                                    nifiComponent.setId(p.getId());
                                    nifiComponent.setName(p.getName());
                                    return nifiComponent;
                                })
                                .collect(Collectors.toList());

                        lastGroupIds = groupResults.stream().map(NifiComponent::getId).collect(Collectors.toList());
                    }else {
                        List<String> newLastGroupIds = new ArrayList<>();
                        for (String lastGroupId : lastGroupIds) {
                            List<String> groupIds = getProcessGroupStatus(lastGroupId)
                                    .getProcessGroupStatus().getAggregateSnapshot()
                                    .getProcessGroupStatusSnapshots()
                                    .stream()
                                    .filter(p->{
                                        try {
                                            return (Pattern.compile(s.trim()).matcher(getProcessGroupStatus(p.getId()).getProcessGroupStatus().getName()).find());
                                        }catch (Exception e){

                                        }
                                        return false;
                                    })
                                    .map(ProcessGroupStatusSnapshotEntity::getId)
                                    .collect(Collectors.toList());
                            newLastGroupIds.addAll(groupIds);
                        }
                        lastGroupIds = newLastGroupIds;
                    }
                }else{
                    for (String lastGroupId : lastGroupIds) {
                        List<ProcessGroupStatusSnapshotEntity> g = getProcessGroupStatus(lastGroupId)
                                .getProcessGroupStatus().getAggregateSnapshot()
                                .getProcessGroupStatusSnapshots();
                        lastGroupIds = g.stream().map(ProcessGroupStatusSnapshotEntity::getId).collect(Collectors.toList());
                    }
                }
            }
        }

        for (NifiComponent result : results) {
            result = updateFlowPath(result);
        }

        if(processType!=null && processType.length()>0){
            results = results.stream().filter(p->{
                String type = p.getType().split("\\.")[p.getType().split("\\.").length-1];
                return p.getType().equalsIgnoreCase(type);
            }).collect(Collectors.toSet());
        }

        return results;
    }

//    public Set<NifiComponent> findNifiComponent(String pattern, String processType, String id) throws Exception {
//        Set<NifiComponent> result = new HashSet<>();
//        Exception error =null;
//        if(processType==null){
//           try{
//               result.addAll(findProcessor( pattern, processType, id));
//           }catch (Exception ex){
//               error = ex;
//           }
//           try{
//                result.addAll(findProcessGroup( pattern,  id));
//           }catch (Exception ex){
//               error = ex;
//           }
//           if(result.size()==0 && error!=null){
//               throw error;
//           }
//        }else{
//            if(!processType.trim().equalsIgnoreCase(ProcessType.ProcessGroup.toString())){
//                return  findProcessor( pattern, processType, id);
//            }else{
//                return findProcessGroup( pattern, id);
//            }
//        }
//        return result;
//    }

//    /**
//     * get list of process groups by regex pattern
//     * @param patternString on empty or null return all process groups
//     * @return
//     */
//
//    public Set<NifiComponent> findProcessGroup(String patternString, String id) throws ApiException, IOException, LoginException {
//        Set<NifiComponent> result = findProcessGroup();
//        return result
//                .stream()
//                .filter(entry->{
//                    if(patternString ==null || patternString.trim().length()==0 ||
//                            (Pattern.compile(patternString.trim()).matcher(entry.getFlowPath()+"/"+entry.getName()).find())){
//                        if(id ==null || id.trim().length()==0 ||
//                               entry.getId().equalsIgnoreCase(id)){
//                            return true;
//                        }
//                    }
//                    return false;
//                })
//                .collect(Collectors.toSet());
//    }
//
//    /**
//     * get list of processors by regex pattern
//     * @param patternString on empty or null return all processors
//     * @return
//     */
//    public Set<NifiComponent>  findProcessor(String patternString, String processType, String id) throws Exception {
//        Set<NifiComponent> result= findProcess();
//        return result
//                .stream()
//                .filter(entry->{
//                    if(patternString ==null || patternString.trim().length()==0 ||
//                            ( Pattern.compile(patternString.trim()).matcher(entry.getFlowPath()+"/"+entry.getName()).find())){
//                        if(processType==null ||  processType.length()==0 ||
//                                (processType!=null && processType.equalsIgnoreCase(entry.getType()))){
//                            if(id==null || id.length()==0 ||
//                                    id.equalsIgnoreCase(entry.getId()) ||
//                                    entry.getFlowPathId().contains(id)){
//                                return true;
//                            }
//                        }
//                    }
//                    return false;
//                })
//                .collect(Collectors.toSet());
//    }

    public void updateRunStatusById(String id, String status, String scope) throws Exception {
        try{
            ProcessorEntity process = getProcessor(id);
            updateRunStatus( id,  status);
        } catch (ApiException e) {
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


//    private Set<NifiComponent> findProcess() throws Exception {
//        if(processCache==null || (System.currentTimeMillis()-lastUpdatedProcessCache)>15000L){
//            processCache = getAllProcess("root", new ArrayList<String>(), new ArrayList<String>());
//            lastUpdatedProcessCache = System.currentTimeMillis();
//        }
//        return new HashSet<>(processCache);
//    }
//
//    private Set<NifiComponent> findProcessGroup() throws ApiException, IOException, LoginException {
//        if(processGroupCache==null || (System.currentTimeMillis()-lastUpdatedProcessGroupCache)>15000L){
//            processGroupCache = getAllProcessGroup("root", new ArrayList<String>(), new ArrayList<String>());
//            lastUpdatedProcessGroupCache = System.currentTimeMillis();
//        }
//        return new HashSet<>(processGroupCache);
//    }

//    private Set<NifiComponent> getAllProcessGroup(String groupId, List<String> path, List<String> idLineage) throws ApiException, IOException, LoginException {
//        Set<NifiComponent> list = new HashSet<>();
//            ProcessGroupStatusEntity processGroup = getProcessGroupStatus(groupId);
//            if(!groupId.equals("root")){
//                NifiComponent nifiComponent = new NifiComponent();
//                nifiComponent.setFlowPath( String.join("/",path));
//                nifiComponent.setFlowPathId( String.join("/",idLineage));
//                nifiComponent.setId( processGroup.getProcessGroupStatus().getId());
//                nifiComponent.setName( processGroup.getProcessGroupStatus().getName());
//                nifiComponent.setType( "ProcessGroup");
//                list.add(nifiComponent);
//            }
//            List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
//            for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
//                String groupProcessId = processGroupStatusSnapshotEntity.getId();
//                path.add(processGroup.getProcessGroupStatus().getName());
//                idLineage.add(processGroup.getProcessGroupStatus().getId());
//                list.addAll(getAllProcessGroup(groupProcessId, path, idLineage));
//                path.remove(path.size()-1);
//            }
//        return list;
//    }
//
//    private Set<NifiComponent> getAllProcess(String groupId, List<String> path, List<String> idLineage) throws Exception {
//        Set<NifiComponent> list = new HashSet<>();
//        ProcessGroupStatusEntity processGroup = getProcessGroupStatus(groupId);
//        path.add(processGroup.getProcessGroupStatus().getName());
//        idLineage.add(processGroup.getProcessGroupStatus().getId());
//        for( ProcessorStatusSnapshotEntity processorStatusSnapshotEntity:processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessorStatusSnapshots() ){
//            String processId = processorStatusSnapshotEntity.getId();
//            ProcessorEntity nifiProcessor = getProcessor(processId);
//            NifiComponent nifiComponent = new NifiComponent();
//            nifiComponent.setFlowPath( String.join("/",path));
//            nifiComponent.setFlowPathId( String.join("/",idLineage));
//            nifiComponent.setId(nifiProcessor.getComponent().getId());
//            nifiComponent.setName( nifiProcessor.getComponent().getName());
//            String[]typePath = nifiProcessor.getComponent().getType().split("\\.");
//            nifiComponent.setType( typePath[typePath.length-1]);
//            nifiComponent.setStatus(nifiProcessor.getComponent().getState().toString());
//            list.add(nifiComponent);
//        }
//        List<ProcessGroupStatusSnapshotEntity> groups = processGroup.getProcessGroupStatus().getAggregateSnapshot().getProcessGroupStatusSnapshots();
//        for( ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotEntity:groups ){
//            String groupProcessId = processGroupStatusSnapshotEntity.getId();
//            list.addAll(getAllProcess(groupProcessId, path, idLineage));
//        }
//
//        path.remove(path.size()-1);
//        return list;
//    }

    private ProcessorEntity getProcessor(String id) throws Exception {
        String s = requestProcessorJson(id);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        ProcessorEntity processorEntity = objectMapper.readValue(s, ProcessorEntity.class);
        return processorEntity;
    }

    private ProcessGroupStatusEntity getProcessGroupStatus(String id) throws ApiException, IOException, LoginException {
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

    private SearchResultsEntity getSearchResult(String keyword) throws IOException, LoginException {
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
    private String requestProcessGroupJson(String id) throws IOException, LoginException {
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

    private String requestJson(String url) throws IOException, LoginException {
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
        return strResponse;
    }
    public enum ProcessType {
        ProcessGroup
    }
    public enum Scope {
        Root
    }
}
