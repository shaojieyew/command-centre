package app.c2.controller;

import app.c2.C2PlatformProperties;
import app.c2.model.Project;
import app.c2.properties.C2Properties;
import app.c2.service.ProjectService;
import app.c2.service.git.GitSvcFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
public class GitController {

  @Autowired private ProjectService projectService;

  @Operation(summary = "Get list of repositories remote url using project repository setting")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of repository accessible from project, return empty if none")
           })
  @GetMapping(path = "/project/{projectId}/git/repo")
  public List<String> getRepositories(@PathVariable long projectId) throws IOException {
    Project project = projectService.findById(projectId).orElseGet(null);

    if(project !=null && project.getEnv()!=null ) {
        C2Properties prop = (project.getEnv());
        return GitSvcFactory.create(prop).stream().map(s->s.getRemoteUrl()).collect(Collectors.toList());
    }
    return new ArrayList();
  }

  @Operation(summary = "Get list of branches using project repository setting")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of branches, return empty if none") })
  @GetMapping(path = "/project/{projectId}/git/branch")
  public List<String> getBranches(@PathVariable long projectId, @RequestParam(name = "remoteUrl") String remoteUrl) throws IOException, GitAPIException {
    Project project = projectService.findById(projectId).orElseGet(null);

    if(project !=null && project.getEnv()!=null ) {
        C2Properties prop = (project.getEnv());
        return GitSvcFactory.create(prop, remoteUrl).getBranches();
    }
    return new ArrayList<String>();
  }


  @Operation(summary = "Get list of files in a specific repo branch")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of files, return empty if none"), })
  @GetMapping(path = {"/project/{projectId}/git/files"})
  public List<String> getFiles(@PathVariable long projectId,  @RequestParam(name = "remoteUrl") String remoteUrl,  @RequestParam(name = "branch") String branch,  @RequestParam(name = "filter", required = false) String filter) throws IOException, GitAPIException {
    Project project = projectService.findById(projectId).orElseGet(null);
    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());
        return new GitSvcFactory().create(prop, remoteUrl).getFilesPath(branch, filter);
    }
    return new ArrayList<String>();
  }

  @Operation(summary = "Get string content of a file from git repo")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return string content"), })
  @GetMapping(path = {"/project/{projectId}/git/file"})
  public String getFile(@PathVariable long projectId,  @RequestParam(name = "remoteUrl") String remoteUrl, @RequestParam(name = "branch") String branch,  @RequestParam(name = "path") String path) throws IOException, GitAPIException {
    String res = projectService.getFileAsString(projectId,remoteUrl,branch,path);
    return new ObjectMapper().writeValueAsString(res);
  }
}
