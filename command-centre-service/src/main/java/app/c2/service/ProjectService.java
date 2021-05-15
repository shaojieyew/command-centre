package app.c2.service;

import app.c2.dao.ProjectDao;
import app.c2.model.File;
import app.c2.model.Project;
import app.c2.properties.C2Properties;
import app.c2.service.git.GitSvcFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

  @Autowired private ProjectDao projectDao;
  @Autowired private FileStorageService fileStorageService;

  public List<Project> findAll() {
    return (List<Project>) projectDao.findAll();
  }

  public Project save(Project project) {
    return projectDao.save(project);
  }

  public void delete(Project project) {
    projectDao.delete(project);
  }

  public List<Project> findByOwner(String owner) {
    return projectDao.findByOwner(owner);
  }

  public Optional<Project> findById(Long id) {
    return projectDao.findById(id);
  }

//  public File getFile(long projectId, String remoteUrl, String branch, String filePath) throws IOException, GitAPIException {
//    Optional<Project> project = findById(projectId);
//    if(project.isPresent() && project.get().getEnv()!=null) {
//      C2Properties prop = project.get().getEnv();
//      return GitSvcFactory.create(prop, remoteUrl).getFile(branch, filePath);
//    }
//    return null;
//  }

  public String getFileAsString(long projectId, String remoteUrl, String branch, String filePath) throws IOException, GitAPIException {
    Optional<Project> project = findById(projectId);
    if(project.isPresent() && project.get().getEnv()!=null) {
      C2Properties prop = project.get().getEnv();
      return GitSvcFactory.create(prop, remoteUrl).getFileAsString(branch, filePath);
    }
    return null;
  }

  public Set<File> getFiles(long projectId, String namespace) throws JsonProcessingException {
    if(namespace==null){
      namespace = FileStorageService.DEFAULT_NAMESPACE;
    }

    Optional<Project> project = projectDao.findById(projectId);
    if(project!=null){
      Iterable<File> filesIter=   fileStorageService.getFiles(project.get().getFileIds().get(namespace));
      ArrayList<File> actualList = Lists.newArrayList(filesIter);
      actualList.forEach(f->f.setFileBlob(null));
      return actualList.stream().collect(Collectors.toSet());
    }
    return new HashSet<>();

  }

  public void deleteFile(long projectId, String filename, String namespace) throws JsonProcessingException {
    if(namespace==null){
      namespace = FileStorageService.DEFAULT_NAMESPACE;
    }

    Optional<Project> projectOpt = projectDao.findById(projectId);
    if(projectOpt.isPresent()){
      Project project = projectOpt.get();
      Map<String, Set<Long>> fileIdsMap = project.getFileIds();
      if(fileIdsMap.keySet().contains(namespace)){
        Set<File> files = getFiles(projectId, namespace);
        fileIdsMap.put(namespace, files.stream().filter(f->!f.getName().equals(filename))
                .map(f->f.getId()).collect(Collectors.toSet()));
        project.setFileIds(fileIdsMap);
        save(project);
      }
    }
  }


  public void deleteFile(long projectId, long fileId, String namespace) throws JsonProcessingException {
    if(namespace==null){
      namespace = FileStorageService.DEFAULT_NAMESPACE;
    }

    Optional<Project> projectOpt = projectDao.findById(projectId);
    if(projectOpt.isPresent()){
      Set<File> files = getFiles(projectId, namespace);
      Project project = projectOpt.get();
      Map<String, Set<Long>> fileIdsMap = project.getFileIds();
      fileIdsMap.put(namespace, files.stream().filter(f->f.getId()!=(fileId)).map(f->f.getId()).collect(Collectors.toSet()));
      project.setFileIds(fileIdsMap);
      save(project);
    }
  }

  public void deleteFiles(long projectId, String namespace) throws JsonProcessingException {
    if(namespace==null){
      namespace = FileStorageService.DEFAULT_NAMESPACE;
    }
    Optional<Project> projectOpt = projectDao.findById(projectId);
    if(projectOpt.isPresent()){
      Project project = projectOpt.get();
      Map<String, Set<Long>> fileIdsMap = project.getFileIds();
      fileIdsMap.remove(namespace);
      project.setFileIds(fileIdsMap);
      save(project);
    }
  }
}
