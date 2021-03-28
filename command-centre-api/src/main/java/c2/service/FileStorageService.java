package c2.service;

import c2.dao.FileDao;
import c2.exception.FileStorageException;
import c2.dao.configuration.FileStorageProperties;
import c2.model.App;
import c2.model.File;
import c2.model.FileFactory;
import c2.model.Project;
import c2.services.git.GitSvc;
import c2.services.git.GitSvcFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;

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
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
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

    public Iterable<File> getFiles(List<Long> ids) {
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


    public File addGitFileToProject(String remoteUrl, String branch, String filePath, long projectId) throws IOException, GitAPIException {
        Optional<Project> projectOptional = projectService.findById(projectId);
        if(!projectOptional.isPresent()){
            return null;
        }
        Project project = projectOptional.get();
        GitSvc gitSvc = GitSvcFactory.create(project.getEnv(),remoteUrl);
        String content = gitSvc.getFileAsString(branch, filePath);
        String filename = filePath.split("/")[filePath.split("/").length-1];
        File f = FileFactory.createFromString(content,"text/plain",filename ,projectId,remoteUrl+"/-/"+branch, FileFactory.SOURCE_TYPE_GIT, filePath);
        f = save(f);
        List<Long> fileIds = project.getFileIds();
        fileIds.add(f.getId());
        project.setFileIds(fileIds);
        projectService.save(project);
        return f;
    }

    public File addGitFileToApp(String remoteUrl, String branch, String filePath, long projectId, String appName) throws IOException, GitAPIException {
        Optional<App> appOptional = appService.findById(projectId,appName);
        if(!appOptional.isPresent()){
            return null;
        }
        Optional<Project> projectOptional = projectService.findById(projectId);
        if(!projectOptional.isPresent()){
            return null;
        }
        App app = appOptional.get();
        Project project = projectOptional.get();
        GitSvc gitSvc = GitSvcFactory.create(project.getEnv(),remoteUrl);
        String content = gitSvc.getFileAsString(branch, filePath);
        String filename = filePath.split("/")[filePath.split("/").length-1];
        File f = FileFactory.createFromString(content,"text/plain",filename ,projectId,remoteUrl+"/-/"+branch, FileFactory.SOURCE_TYPE_GIT, filePath);
        f = save(f);
        List<Long> fileIds = app.getFileIds();
        fileIds.add(f.getId());
        app.setFileIds(fileIds);
        appService.save(app);
        return f;
    }

    public File addUploadedFileToProject(MultipartFile file, long projectId) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(fileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        File f = FileFactory.createFromMultipartFile(file,projectId,"user", FileFactory.SOURCE_TYPE_UPLOAD);
        f = save(f);
        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            return null;
        }
        Project project = projectOpt.get();
        List<Long> fileIds = project.getFileIds();
        fileIds.add(f.getId());
        project.setFileIds(fileIds);
        projectService.save(project);

        return f;
    }

    public File addUploadedFileToApp(MultipartFile file, long projectId, String appName) throws IOException {
        Optional<App> appOptional = appService.findById(projectId,appName);
        if(!appOptional.isPresent()){
            return null;
        }
        App app = appOptional.get();

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(fileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        File f = FileFactory.createFromMultipartFile(file,projectId,"user", FileFactory.SOURCE_TYPE_UPLOAD);
        f = save(f);
        List<Long> fileIds = app.getFileIds();
        fileIds.add(f.getId());
        app.setFileIds(fileIds);
        appService.save(app);
        return f;
    }
}
