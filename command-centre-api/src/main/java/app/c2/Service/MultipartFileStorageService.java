package app.c2.Service;

import app.c2.dao.FileDao;
import app.c2.model.File;
import app.c2.model.FileFactory;
import app.c2.model.Project;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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
