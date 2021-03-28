package c2.service;

import c2.dao.ProjectDao;
import c2.model.Project;
import c2.properties.C2Properties;
import c2.services.git.GitSvcFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  public File getFile(long projectId, String remoteUrl, String branch, String filePath) throws IOException, GitAPIException {
    Optional<Project> project = findById(projectId);
    if(project.isPresent() && project.get().getEnv()!=null) {
      C2Properties prop = project.get().getEnv();
      return GitSvcFactory.create(prop, remoteUrl).getFile(branch, filePath);
    }
    return null;
  }
  public String getFileAsString(long projectId, String remoteUrl, String branch, String filePath) throws IOException, GitAPIException {
    Optional<Project> project = findById(projectId);
    if(project.isPresent() && project.get().getEnv()!=null) {
      C2Properties prop = project.get().getEnv();
      return GitSvcFactory.create(prop, remoteUrl).getFileAsString(branch, filePath);
    }
    return null;
  }

  public ArrayList<c2.model.File> getFiles(long projectId) {

    Optional<Project> project = projectDao.findById(projectId);
    if(project!=null){
      Iterable<c2.model.File> filesIter=   fileStorageService.getFiles(project.get().getFileIds());
      ArrayList<c2.model.File> actualList = Lists.newArrayList(filesIter);
      actualList.forEach(f->f.setFileBlob(null));
      return actualList;
    }
    return new ArrayList<c2.model.File>();

  }

  public void deleteFile(long projectId, String filename) throws JsonProcessingException {

    Optional<Project> projectOpt = projectDao.findById(projectId);
    if(projectOpt.isPresent()){

      List<c2.model.File> files = getFiles(projectId);
      Project project = projectOpt.get();
      project.setFileIds(files.stream().filter(f->!f.getName().equals(filename)).map(f->f.getId()).collect(Collectors.toList()));
      save(project);
    }
  }
}
