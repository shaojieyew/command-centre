package app.c2.Service;

import app.c2.dao.FileDao;
import app.c2.dao.configuration.FileStorageProperties;
import app.c2.model.App;
import app.c2.model.File;
import app.c2.model.FileFactory;
import app.c2.model.Project;
import app.c2.service.AppService;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import app.c2.services.git.GitSvc;
import app.c2.services.git.GitSvcFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultipartFileStorageService {

    public final static String DEFAULT_NAMESPACE = "default";

    @Autowired
    FileDao fileDao;
    @Autowired
    ProjectService projectService;
    @Autowired
    FileStorageService fileStorageService;

    public File addUploadedFileToApp(MultipartFile multipartFile, long projectId, String appName) throws IOException {
        File f =  saveFile( multipartFile,  projectId);
        return fileStorageService.addFileToApp(f, projectId, appName);
    }

    public File addUploadedFileToProject(MultipartFile multipartFile, long projectId, String namespace) throws IOException {
        if(namespace==null || namespace.length()==0){
            namespace=DEFAULT_NAMESPACE;
        }
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        if(fileName.contains("..")) {
            throw new IOException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        File f = saveFile(multipartFile,projectId);
        f = fileStorageService.save(f);
        Optional<Project> projectOpt = projectService.findById(projectId);
        if(!projectOpt.isPresent()){
            return null;
        }
        Project project = projectOpt.get();
        return fileStorageService.addFileToProject(f, project, namespace);
    }

    public File saveFile(MultipartFile multipartFile, long projectId) throws IOException {
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        if(fileName.contains("..")) {
            throw new IOException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        File f = FileFactory.createFromFile(multipartFile,projectId,"user", FileFactory.SOURCE_TYPE_UPLOAD);
        f = fileStorageService.save(f);
        return f;
    }
}
