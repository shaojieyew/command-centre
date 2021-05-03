package app.c2.service;

import app.c2.dao.NifiQueryDao;
import app.c2.model.NifiQuery;
import app.c2.model.Project;
import app.c2.model.compositKey.NifiQueryId;
import app.c2.properties.C2Properties;
import app.c2.services.nifi.NifiSvc;
import app.c2.services.nifi.NifiSvcFactory;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.davis.client.model.ProcessorStatusDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.checkerframework.checker.units.qual.A;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NifiQueryService {
    @Autowired
    ProjectService projectService;

    @Autowired
    NifiQueryDao nifiQueryDao;

    public void save(NifiQuery nifiQuery){
        nifiQueryDao.save(nifiQuery);
    }

    public void delete(long projectId, String name){
        nifiQueryDao.deleteById(new NifiQueryId(projectId,name));
    }


    public List<ProcessorStatusDTO> findProcessorByQueryName(long projectId, String queryName) throws JsonProcessingException {
        Optional<NifiQuery> savedQuery = nifiQueryDao.findByProjectIdAndName(projectId, queryName).stream().findFirst();
        if(!savedQuery.isPresent()){
            return new ArrayList<>();
        }
        String query = savedQuery.get().getQuery();
        String processType = savedQuery.get().getType();

        return findProcessor( projectId,  query,  processType);
    }

    public List<ProcessorStatusDTO> findProcessor(long projectId, String query, String processType) throws JsonProcessingException {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();

            return NifiSvcFactory.create(prop).findProcessor(query).entrySet().stream().map(s->{
                String name = s.getKey().getName();
                String group = s.getValue();
                ProcessorStatusDTO processorStatusDTO = s.getKey();
                processorStatusDTO.setName(group+"/"+name);
                return processorStatusDTO;
            }).filter(s->processType==null
                    || s.getAggregateSnapshot().getType().equalsIgnoreCase(processType))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<ProcessGroupStatusDTO> findProcessGroup(long projectId, String query) throws JsonProcessingException {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();

            return NifiSvcFactory.create(prop).findProcessGroup(query).entrySet().stream().map(s->{
                String name = s.getKey().getName();
                String group = s.getValue();
                ProcessGroupStatusDTO processGroupStatusDTO = s.getKey();
                processGroupStatusDTO.setName(group+"/"+name);
                return processGroupStatusDTO;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void stopProcessorByQueryName(long projectId, String queryName) throws JsonProcessingException {
        updateProcessorByQueryName( projectId,  queryName,  NifiSvc.NIFI_RUN_STATUS_STOPPED);
    }
    public void startProcessorByQueryName(long projectId, String queryName) throws JsonProcessingException {
        updateProcessorByQueryName( projectId,  queryName,  NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }

    public void updateProcessorByQueryName(long projectId, String queryName, String status) throws JsonProcessingException {
        Optional<NifiQuery> savedQuery = nifiQueryDao.findByProjectIdAndName(projectId, queryName).stream().findFirst();
        if(!savedQuery.isPresent()){
            return;
        }

        String query = savedQuery.get().getQuery();
        String processType = savedQuery.get().getType();
        String scope = savedQuery.get().getScope();
        boolean onlyLeadingProcessor = scope!=null && scope.equalsIgnoreCase("first");
        if(processType!=null && processType.equalsIgnoreCase("group")){
            stopProcessorGroup(projectId,  query, onlyLeadingProcessor);
        }else{
            stopProcessor( projectId,  query,  processType);
        }
    }


    public void stopProcessor(long projectId, String query, String processType) throws JsonProcessingException {
        updateProcessor( projectId,  query,  processType, NifiSvc.NIFI_RUN_STATUS_STOPPED);
    }
    public void startProcessor(long projectId, String query, String processType) throws JsonProcessingException {
        updateProcessor( projectId,  query, processType,  NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }
    public void stopProcessorGroup(long projectId, String query, boolean onlyLeadingProcessor) throws JsonProcessingException {
        updateProcessGroup( projectId,  query, onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }
    public void startProcessorGroup(long projectId, String query, boolean onlyLeadingProcessor) throws JsonProcessingException {
        updateProcessGroup( projectId,  query, onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }

    public void updateProcessor(long projectId, String query, String processType, String status) throws JsonProcessingException {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();
            NifiSvc svc = NifiSvcFactory.create(prop);
            svc.findProcessor(query).entrySet().stream().forEach(s->{
                try {
                    if(processType==null || (processType!=null && processType.equalsIgnoreCase(s.getKey().getAggregateSnapshot().getType())))
                    svc.updateRunStatus(s.getKey().getId(),status);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void updateProcessGroup(long projectId, String query, boolean onlyLeadingProcessor,String status) throws JsonProcessingException {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();
            NifiSvc svc = NifiSvcFactory.create(prop);
            svc.findProcessGroup(query).entrySet().stream().forEach(s->{
                svc.updateAllProcessInProcessGroup(s.getKey().getId(),status, onlyLeadingProcessor);
            });
        }
    }
}
