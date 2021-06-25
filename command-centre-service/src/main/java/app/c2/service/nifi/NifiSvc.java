package app.c2.service.nifi;

import app.c2.service.nifi.model.Breadcrumb;
import app.c2.service.nifi.model.NifiComponent;
import app.c2.service.nifi.model.ProcessGroupFlowBreadcumb;
import com.davis.client.ApiException;
import com.davis.client.model.ProcessGroupStatusSnapshotEntity;
import com.davis.client.model.ProcessorEntity;
import com.davis.client.model.SearchResultsEntity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NifiSvc extends AbstractNifiSvc {

    public NifiSvc(String nifiHost) {
        super(nifiHost);
    }

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
                return processType.equalsIgnoreCase(type);
            }).collect(Collectors.toSet());
        }

        return results;
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
            result.setFlowPath(processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getBreadcrumb().getName());
            breadcrumb = processGroupStatusEntity.getProcessGroupFlow().getBreadcrumb().getParentBreadcrumb();
        }
        while(breadcrumb!=null){
            Breadcrumb b =breadcrumb.getBreadcrumb();
            if(result.getFlowPath().length()==0){
                result.setFlowPath(b.getName());
            }else{
                result.setFlowPath(b.getName()+"/"+result.getFlowPath());
            }
            breadcrumb = breadcrumb.getParentBreadcrumb();
        }
        return result;
    }

}
