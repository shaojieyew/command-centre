package app.c2.service;

import app.c2.dao.FileDao;
import app.c2.dao.configuration.FileStorageProperties;
import app.c2.model.App;
import app.c2.model.File;
import app.c2.model.FileFactory;
import app.c2.model.Project;
import app.c2.service.git.GitSvc;
import app.c2.service.git.GitSvcFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

    public final static String DEFAULT_NAMESPACE = "default";

    @Autowired
    FileDao fileDao;
    @Autowired
    ProjectService projectService;
    @Autowired
    AppService appService;

    public List<File> findByProjectId(long projectId) {
        return fileDao.findByProjectId(projectId);
    }

    public Optional<File> findByFileHash(String hash) {
        return fileDao.findByFileHash(hash).stream().findFirst();
    }

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) throws IOException {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new IOException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public File save(File file) {
        Optional<File> res = fileDao.findByProjectId(file.getProjectId()).stream()
                .filter(f->f.getName().equalsIgnoreCase(file.getName()))
                .filter(f->f.getFileHash().equals(file.getFileHash())).findFirst();
        if(res.isPresent()){
            return res.get();
        }else{
            return fileDao.save(file);
        }
    }

    public Optional<File> getFile(long fileId) {
        return fileDao.findById(fileId);
    }

    public Iterable<File> getFiles(Set<Long> ids) {
        Iterable<Long> iterable = ids;
        return fileDao.findAllById(iterable);
    }

    public Optional<File> getFile(long projectId, String fileName, String fileHash) {
        return fileDao.findByProjectId(projectId).stream()
                .filter(f->f.getName().equalsIgnoreCase(fileName))
                .filter(f->f.getFileHash().equals(fileHash)).findFirst();
    }

    public Optional<File> getFile(long projectId, String fileName) {
        return fileDao.findByProjectId(projectId).stream()
                .filter(f->f.getName().equalsIgnoreCase(fileName)).max(new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getUpdateTimestamp().compareTo(o2.getUpdateTimestamp());
                    }
                });
    }

    public File addStringContentToProject(String content, String filename, long projectId, String namespace) throws IOException {
        if(namespace==null || namespace.length()==0){
            namespace=DEFAULT_NAMESPACE;
        }
        Optional<Project> projectOptional = projectService.findById(projectId);
        if(!projectOptional.isPresent()){
            return null;
        }
        Project project = projectOptional.get();
        File f = saveFile(content, filename , projectId) ;
        f = save(f);

        return addFileToProject(f, project, namespace);
    }
    public File addUploadedFileToProject(java.io.File ioFile, long projectId, String namespace, String filename) throws IOException {
        if(namespace==null || namespace.length()==0){
            namespace=DEFAULT_NAMESPACE;
        }
        File f = saveFile(ioFile, filename, projectId);
        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            return null;
        }
        Project project = projectOpt.get();

        return addFileToProject(f, project, namespace);
    }
    public File addUploadedFileToProject(java.io.File ioFile, long projectId, String namespace) throws IOException {
        return addUploadedFileToProject( ioFile,  projectId,  namespace,  ioFile.getName());
    }
    public File addGitFileToProject(String remoteUrl, String branch, String filePath, long projectId, String namespace) throws IOException, GitAPIException {
        return  addGitFileToProject( remoteUrl,  branch,  filePath,  projectId,  namespace, filePath.split("/")[filePath.split("/").length-1]);
    }
    public File addGitFileToProject(String remoteUrl, String branch, String filePath, long projectId, String namespace, String filename) throws IOException, GitAPIException {
        if(namespace==null || namespace.length()==0){
            namespace=DEFAULT_NAMESPACE;
        }
        Optional<Project> projectOptional = projectService.findById(projectId);
        if(!projectOptional.isPresent()){
            return null;
        }
        Project project = projectOptional.get();
        File f = saveFile(remoteUrl, branch, filePath, projectId, filename);
        return addFileToProject(f, project, namespace);
    }
//
//    public File addUploadedFileToProject(MultipartFile multipartFile, long projectId, String namespace) throws IOException {
//        if(namespace==null || namespace.length()==0){
//            namespace=DEFAULT_NAMESPACE;
//        }
//        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//        if(fileName.contains("..")) {
//            throw new IOException("Sorry! Filename contains invalid path sequence " + fileName);
//        }
//        File f = saveFile(multipartFile,projectId);
//        f = save(f);
//        Optional<Project> projectOpt = projectService.findById(projectId);
//        if(!projectOpt.isPresent()){
//            return null;
//        }
//        Project project = projectOpt.get();
//        return addFileToProject(f, project, namespace);
//    }

    public File addFileToProject(File f, Project project, String namespace) throws JsonProcessingException {
        Set<Long> fileIds = project.getFileIds(namespace);
        Map<String, Set<Long>> fileIdsMap = project.getFileIds();
        File finalF = f;
        fileIds = fileIds.stream().map(fId->fileDao.findById(fId))
                .filter(file->!file.get().getName().equalsIgnoreCase(finalF.getName()))
                .map(file->file.get().getId()).collect(Collectors.toSet());
        fileIds.add(f.getId());
        fileIdsMap.put(namespace,fileIds);
        project.setFileIds(fileIdsMap);
        projectService.save(project);
        return f;
    }

    public File addStringContentToApp(String content, String filename, long projectId, String appName) throws IOException {
        File f = saveFile(content, filename, projectId );
        return addFileToApp(f, projectId, appName);
    }

    public File addUploadedFileToApp(java.io.File ioFile, long projectId, String appName) throws IOException {
        return addUploadedFileToApp( ioFile,  projectId,  appName,  ioFile.getName());
    }
    public File addUploadedFileToApp(java.io.File ioFile, long projectId, String appName, String filename) throws IOException {
        File f = saveFile(ioFile, filename, projectId);
        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            return null;
        }
        return addFileToApp(f, projectId, appName);
    }

    public File addGitFileToApp(String remoteUrl, String branch, String filePath, long projectId, String appName) throws IOException, GitAPIException {
        File f = saveFile(remoteUrl,  branch,  filePath,  projectId);
        return addFileToApp(f, projectId, appName);
    }
    public File addGitFileToApp(String remoteUrl, String branch, String filePath, long projectId, String appName, String filename) throws IOException, GitAPIException {
        File f = saveFile(remoteUrl,  branch,  filePath,  projectId, filename);
        return addFileToApp(f, projectId, appName);
    }


//    public File addUploadedFileToApp(MultipartFile multipartFile, long projectId, String appName) throws IOException {
//        File f =  saveFile( multipartFile,  projectId);
//        return addFileToApp(f, projectId, appName);
//    }

    public File addFileToApp(File f, long projectId, String appName) throws JsonProcessingException {
        Optional<App> appOptional = appService.findById(projectId,appName);
        if(!appOptional.isPresent()){
            return null;
        }
        App app = appOptional.get();
        Set<Long> fileIds = app.getFileIds();
        File finalF = f;
        fileIds = fileIds.stream().map(fId->fileDao.findById(fId))
                .filter(file->!file.get().getName().equalsIgnoreCase(finalF.getName()))
                .map(file->file.get().getId()).collect(Collectors.toSet());
        fileIds.add(f.getId());
        app.setFileIds(fileIds);
        appService.save(app);
        return f;
    }

    public File saveFile(String remoteUrl, String branch, String filePath, long projectId) throws IOException, GitAPIException {
        return saveFile( remoteUrl,  branch,  filePath,  projectId,   filePath.split("/")[filePath.split("/").length-1]);
    }

    public File saveFile(String remoteUrl, String branch, String filePath, long projectId, String filename) throws IOException, GitAPIException {
        Optional<Project> projectOptional = projectService.findById(projectId);
        if(!projectOptional.isPresent()){
            return null;
        }
        Project project = projectOptional.get();
        GitSvc gitSvc = GitSvcFactory.create(project.getEnv(),remoteUrl);
        String content = gitSvc.getFileAsString(branch, filePath);
        //String filename = filePath.split("/")[filePath.split("/").length-1];
        File f = FileFactory.createFromString(content,"text/plain",filename ,projectId,remoteUrl+"/-/"+branch+"/-/"+filePath, FileFactory.SOURCE_TYPE_GIT);

        f = save(f);
        return f;
    }
    public File saveFile(String content,  String filename, long projectId) throws IOException {
        File f = FileFactory.createFromString(content,"text/plain",filename ,projectId,null, FileFactory.SOURCE_TYPE_STRING);

        f = save(f);
        return f;
    }
    public File saveFile(java.io.File ioFile, String filename, long projectId) throws IOException {
        File f = FileFactory.createNioFile(ioFile,projectId,"user", FileFactory.SOURCE_TYPE_UPLOAD, filename);

        f = save(f);
        return f;
    }
    public File saveFile(java.io.File ioFile, long projectId) throws IOException {
        return saveFile( ioFile,  ioFile.getName(),  projectId);
    }

}
