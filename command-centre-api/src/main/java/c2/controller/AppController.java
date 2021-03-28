package c2.controller;

import c2.controller.exception.NotFoundExceptionResponse;
import c2.model.App;
import c2.model.AppInstance;
import c2.model.File;
import c2.service.*;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
public class AppController {

  private static final Logger logger = LoggerFactory.getLogger(AppController.class);
  @Autowired private AppService appService;
  @Autowired private AppInstanceService appInstanceService;
  @Autowired private FileStorageService fileStorageService;

  @Operation(summary = "Save or create new App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "App saved") })
  @PutMapping("/project/{projectId}/app")
  public App saveApp(
          @PathVariable long projectId, @RequestBody App app) {
    app.setProjectId(projectId);
    return appService.save(app);
  }

  @Operation(summary = "Delete App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "App deleted") })
  @DeleteMapping("/project/{projectId}/app/{appName}")
  public void deleteApp(
          @PathVariable long projectId, @PathVariable String appName) {
     appService.delete(appName,projectId);
  }

  @Operation(summary = "Launch App using Spark launcher, creating an AppInstance")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "App submitted")})
  @PostMapping("/project/{projectId}/app/submit")
  public AppInstance submitApp(
          @PathVariable long projectId, @RequestBody App app) throws Exception {
    app.setProjectId(projectId);
    return appService.submitApp(app);
  }

  @Operation(summary = "Get App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "App found"),
          @ApiResponse(responseCode = "404", description = "App not found",
                  content = @Content) })
  @GetMapping("/project/{projectId}/app/{appName}")
  public App getApp(
          @PathVariable long projectId, @PathVariable String appName) {
    return appService.findApp(projectId, appName).orElseThrow(NotFoundExceptionResponse::new);
  }


  @Operation(summary = "Kill running instance by appName")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Submitted for killing")})
  @PostMapping("/project/{projectId}/app/{appName}/kill")
  public void killAppInstanceByAppName(
          @PathVariable long projectId,
          @PathVariable String appName) throws Exception {
    appInstanceService.findByProjectAppId(projectId,appName)
            .forEach(i-> {
              try {
                appInstanceService
                        .kill(projectId,i.getAppId());
              } catch (Exception e) {
                e.printStackTrace();
              }
            });
  }

  @Operation(summary = "Add git file to an App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File added to App") })
  @PutMapping(path = {"/project/{projectId}/app/{appName}/git/file"})
  public File saveFile(@PathVariable long projectId, @PathVariable String appName, @RequestParam(name = "remoteUrl") String remoteUrl, @RequestParam(name = "branch") String branch, @RequestParam(name = "path") String path) throws IOException, GitAPIException {
    c2.model.File file = fileStorageService.addGitFileToApp(remoteUrl,branch,path,projectId, appName);
    file.setFileBlob(null);
    return file;
  }

  @Operation(summary = "Add multiple git files to an App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File added to App") })
  @PutMapping(path = {"/project/{projectId}/app/{appName}/git/files"})
  public List<File> saveFiles(@PathVariable long projectId, @PathVariable String appName, @RequestParam(name = "remoteUrl") String remoteUrl, @RequestParam(name = "branch") String branch, @RequestParam(name = "paths") String paths) throws IOException, GitAPIException {
    List<File> list = new ArrayList<>();

    for(String path: paths.split(",")){
      File file = null;
      file = fileStorageService.addGitFileToApp(remoteUrl,branch,path,projectId, appName);
      file.setFileBlob(null);
      list.add(file);
    }
    return list;
  }

  @Operation(summary = "Upload single file to App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File uploaded") })
  @PutMapping("/project/{projectId}/app/{appName}/upload/file")
  public File uploadFile(@RequestParam("file") MultipartFile multipartFile, @PathVariable("projectId") long projectId, @PathVariable("appName") String appName)  {
    if(projectId <=0 ){
      return null;
    }
    if(appName == null ){
      return null;
    }
    File file;
    try {
      file = fileStorageService.addUploadedFileToApp(multipartFile, projectId, appName);
      file.setFileBlob(null);
      return file;
    } catch (IOException e) {
      logger.info(e.getMessage());
      return null;
    }
  }

  @Operation(summary = "Upload multiple files to App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Files uploaded")})
  @PutMapping("/project/{projectId}/app/{appName}/upload/files")
  public List<File> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files, @PathVariable("projectId") long projectId, @PathVariable("appName") String appName)  {
    if(projectId <=0 ){
      return null;
    }
    return Arrays.asList(files)
            .stream()
            .map(file -> uploadFile(file, projectId, appName))
            .collect(Collectors.toList());
  }

  @Operation(summary = "Get files in a App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of file, empty if none is file") })
  @GetMapping(value = {"/project/{projectId}/app/{appName}/files"})
  public List<File> getFilesInProjectApp(@PathVariable long projectId, @PathVariable String appName){
    return appService.getFiles(projectId,appName);
  }

  @Operation(summary = "Remove file from a App")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "File removed from App")})
  @DeleteMapping("/project/{projectId}/app/{appName}/file/{fileName}")
  public void deleteFileFromProjectApp(@PathVariable Long projectId, @PathVariable String appName , @PathVariable String fileName) throws JsonProcessingException {
    appService.deleteFile(projectId, appName, fileName);
  }
}
