package app.c2.service;

import app.c2.dao.NifiQueryDao;
import app.c2.model.NifiQuery;
import app.c2.model.Project;
import app.c2.model.compositeField.NifiQueryId;
import app.c2.properties.C2Properties;
import app.c2.service.nifi.NifiSvc;
import app.c2.service.nifi.NifiSvcFactory;
import app.c2.service.nifi.model.NifiComponent;
import com.davis.client.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NifiQueryService {

    @Autowired
    ProjectService projectService;

    @Autowired
    NifiQueryDao nifiQueryDao;

    public List<NifiQuery> findAllNifiQuery(){
        return Lists.newArrayList(nifiQueryDao.findAll());
    }
    public Optional<NifiQuery> findByProjectIdAndName(long projectId, String queryName){
        return nifiQueryDao.findById(new NifiQueryId(projectId, queryName));
    }

    public void save(NifiQuery nifiQuery){
        nifiQueryDao.save(nifiQuery);
    }

    public void delete(long projectId, String name){
        nifiQueryDao.deleteById(new NifiQueryId(projectId,name));
    }


    public List<NifiComponent> findProcessorByQueryName(long projectId, String queryName) throws Exception {
        Optional<NifiQuery> savedQuery = nifiQueryDao.findByProjectIdAndName(projectId, queryName).stream().findFirst();
        if(!savedQuery.isPresent()){
            return new ArrayList<>();
        }
        String query = savedQuery.get().getQuery();
        String processType = savedQuery.get().getType();

        return findProcessor( projectId,  query,  processType);
    }

    public List<NifiComponent> findProcessor(long projectId, String query, String processType) throws Exception {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();

            return NifiSvcFactory.create(prop).findNifiComponent(query, processType, null).stream().map(s->{
                String name = s.getName();
                String group = s.getFlowPath();
                NifiComponent processorStatusDTO = s;
                processorStatusDTO.setName(group+"/"+name);
                return processorStatusDTO;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<NifiComponent> findProcessGroup(long projectId, String query) throws Exception {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();

            return NifiSvcFactory.create(prop).findNifiComponent(query,NifiSvc.ProcessType.ProcessGroup.toString(),null).stream().map(s->{
                String name = s.getName();
                String group = s.getFlowPath();
                NifiComponent processGroupStatusDTO = s;
                processGroupStatusDTO.setName(group+"/"+name);
                return processGroupStatusDTO;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void stopProcessorByQueryName(long projectId, String queryName) throws Exception {
        updateProcessorByQueryName( projectId,  queryName,  NifiSvc.NIFI_RUN_STATUS_STOPPED);
    }
    public void startProcessorByQueryName(long projectId, String queryName) throws Exception {
        updateProcessorByQueryName( projectId,  queryName,  NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }

    public void updateProcessorByQueryName(long projectId, String queryName, String status) throws Exception {
        Optional<NifiQuery> savedQuery = nifiQueryDao.findByProjectIdAndName(projectId, queryName).stream().findFirst();
        if(!savedQuery.isPresent()){
            return;
        }

        String query = savedQuery.get().getQuery();
        String processType = savedQuery.get().getType();
        String scope = savedQuery.get().getScope();
        boolean onlyLeadingProcessor = scope!=null && scope.equalsIgnoreCase(NifiSvc.Scope.Root.toString());
        if(processType!=null && processType.equalsIgnoreCase(NifiSvc.ProcessType.ProcessGroup.toString())){
            stopProcessorGroup(projectId,  query, onlyLeadingProcessor);
        }else{
            stopProcessor( projectId,  query,  processType);
        }
    }


    public void stopProcessor(long projectId, String query, String processType) throws Exception {
        updateProcessor( projectId,  query,  processType, NifiSvc.NIFI_RUN_STATUS_STOPPED);
    }
    public void startProcessor(long projectId, String query, String processType) throws Exception {
        updateProcessor( projectId,  query, processType,  NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }
    public void stopProcessorGroup(long projectId, String query, boolean onlyLeadingProcessor) throws Exception {
        updateProcessGroup( projectId,  query, onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }
    public void startProcessorGroup(long projectId, String query, boolean onlyLeadingProcessor) throws Exception {
        updateProcessGroup( projectId,  query, onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }

    public void updateProcessor(long projectId, String query, String processType, String status) throws Exception {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();
            NifiSvc svc = NifiSvcFactory.create(prop);
            svc.findNifiComponent(query,processType, null).forEach(s->{
                try {
                    if (processType == null || (!processType.equalsIgnoreCase(s.getType()))) {
                        svc.updateRunStatusById(s.getId(),status,null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void updateProcessGroup(long projectId, String query, boolean onlyLeadingProcessor,String status) throws Exception {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();
            NifiSvc svc = NifiSvcFactory.create(prop);
            for (NifiComponent nifiComponent : svc.findNifiComponent(query, null, null)) {
                if(onlyLeadingProcessor){
                    svc.updateRunStatusById(nifiComponent.getId(),status,NifiSvc.Scope.Root.toString());
                }else{
                    svc.updateRunStatusById(nifiComponent.getId(),status,null);
                }
            }
        }
    }


    public void stopProcessGroupById(long projectId, String groupId, boolean onlyLeadingProcessor) throws Exception {
        updateProcessGroupById( projectId,  groupId,  onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_STOPPED);
    }
    public void startProcessGroupById(long projectId, String groupId, boolean onlyLeadingProcessor) throws Exception {
        updateProcessGroupById( projectId,  groupId,  onlyLeadingProcessor, NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }
    public void updateProcessGroupById(long projectId, String groupId, boolean onlyLeadingProcessor,String status) throws Exception {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();
            NifiSvc svc = NifiSvcFactory.create(prop);
            if(onlyLeadingProcessor){
                svc.updateRunStatusById(groupId,status,NifiSvc.Scope.Root.toString());
            }else{
                svc.updateRunStatusById(groupId,status,null);
            }
        }
    }

    public void stopProcessById(long projectId, String id) throws Exception {
        updateProcessById( projectId,  id, NifiSvc.NIFI_RUN_STATUS_STOPPED);
    }
    public void startProcessById(long projectId, String id) throws Exception {
        updateProcessById( projectId,  id, NifiSvc.NIFI_RUN_STATUS_RUNNING);
    }

    public void updateProcessById(long projectId, String id ,String status) throws Exception {
        Optional<Project> project = projectService.findById(projectId);
        if(project.isPresent() && project.get().getEnv()!=null) {
            C2Properties prop = project.get().getEnv();
            NifiSvc svc = NifiSvcFactory.create(prop);
            svc.updateRunStatusById(id, status,null);
        }
    }
}
