package c2.controller;

import c2.controller.exception.MissingFieldExceptionResponse;
import c2.controller.exception.NotFoundExceptionResponse;
import c2.model.File;
import c2.model.Project;
import c2.service.FileStorageService;
import c2.service.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
public class ProjectController {

  private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
  @Autowired private ProjectService projectService;
  @Autowired private FileStorageService fileStorageService;

  @Operation(summary = "Get project by projectId")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Project found"),
          @ApiResponse(responseCode = "404", description = "Project not found",
                  content = @Content) })
  @GetMapping(value = {"/project/{projectId}"})
  public Project findAll(@PathVariable Long projectId) {
    Optional<Project> result = projectService.findById(projectId);
    return result.orElseThrow(NotFoundExceptionResponse::new);
  }

  @Operation(summary = "Save or create new project")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Project saved"),
          @ApiResponse(responseCode = "400", description = "Invalid project name or owner name",
                  content = @Content) })
  @PutMapping("/project/save")
  public Project save(@RequestBody Project project) throws MissingFieldExceptionResponse {
    if(project.getName()==null || project.getName().length()==0){
      throw new MissingFieldExceptionResponse("name", "project name is required");
    }
    if(project.getOwner()==null || project.getOwner().length()==0){
      throw new MissingFieldExceptionResponse("owner", "userId of project owner is required");
    }
    if(project.getId()!=0){
      if(!projectService.findById(project.getId()).isPresent()){
        project.setId(0);
      }
    }
    return projectService.save(project);
  }

  @Operation(summary = "Upload multiple files to project")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Files uploaded")})
  @PutMapping("/project/{projectId}/upload/files")
  public List<File> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("projectId") long projectId)  {
    if(projectId <=0 ){
      return null;
    }
    return Arrays.asList(files)
            .stream()
            .map(file -> uploadFile(file, projectId))
            .collect(Collectors.toList());
  }


  @Operation(summary = "Add file to a project")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File added to project") })
  @PutMapping(path = {"/git/project/{projectId}/file"})
  public File saveFile(@PathVariable long projectId, @RequestParam(name = "remoteUrl") String remoteUrl, @RequestParam(name = "branch") String branch, @RequestParam(name = "path") String path) throws IOException, GitAPIException {
    c2.model.File file = fileStorageService.addGitFileToProject(remoteUrl,branch,path,projectId);
    file.setFileBlob(null);
    return file;
  }

  @Operation(summary = "Add multiple git files to an App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File added to App") })
  @PutMapping(path = {"/project/{projectId}/git/files"})
  public List<File> saveFiles(@PathVariable long projectId, @RequestParam(name = "remoteUrl") String remoteUrl, @RequestParam(name = "branch") String branch, @RequestParam(name = "paths") String paths) throws IOException, GitAPIException {
    List<File> list = new ArrayList<>();
    for(String path: paths.split(",")){
      File file = null;
      file = fileStorageService.addGitFileToProject(remoteUrl,branch,path,projectId);
      file.setFileBlob(null);
      list.add(file);
    }
    return list;
  }

  @Operation(summary = "Upload single file to Project")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File uploaded") })
  @PutMapping("/project/{projectId}/upload/file")
  public File uploadFile(@RequestParam("file") MultipartFile multipartFile, @PathVariable("projectId") long projectId)  {
    if(projectId <=0 ){
      return null;
    }
    File file;
    try {
      file = fileStorageService.addUploadedFileToProject(multipartFile, projectId);
      file.setFileBlob(null);
      return file;
    } catch (IOException e) {
      logger.info(e.getMessage());
      return null;
    }
  }

  @Operation(summary = "Get files in a Project")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of file, empty if none is file") })
  @GetMapping(value = {"/project/{projectId}/files"})
  public List<File> getFilesInProject(@PathVariable long projectId){
    return projectService.getFiles(projectId);
  }

  @Operation(summary = "Remove file from a project")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File removed from project")})
  @DeleteMapping("/project/{projectId}/file/{fileName}")
  public void deleteFileFromProject(@PathVariable Long projectId, @PathVariable String fileName) throws JsonProcessingException {
    projectService.deleteFile(projectId, fileName);
  }

}
