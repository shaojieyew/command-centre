package c2.controller;

import c2.controller.exception.NotFoundExceptionResponse;
import c2.model.Project;
import c2.properties.C2Properties;
import c2.service.ProjectService;
import c2.services.mvnRegistry.AbstractRegistrySvc;
import c2.services.mvnRegistry.RegistrySvcFactory;
import c2.services.mvnRegistry.model.Package;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
public class ArtifactsController {

  @Autowired private ProjectService projectService;

  @Operation(summary = "Get list of artifact from registry using project mvn setting")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of package, return empty list if none"),
          @ApiResponse(responseCode = "404", description = "Project not found",
                  content = @Content) })
  @GetMapping(path = {"/project/{projectId}/artifacts","/project/{projectId}/{group}/artifacts"})
  public List<Package> findAll(@PathVariable long projectId, @PathVariable Optional<String> group ) throws JsonProcessingException {
    List<Package> packages = new ArrayList<>();
    Project project = projectService.findById(projectId).orElseGet(null);

    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());
        RegistrySvcFactory.create(prop).forEach(svc->{
          if(group.isPresent()){
            packages.addAll(svc.getPackages(group.get()));
          }else{
            packages.addAll(svc.getPackages());
          }
        });
    }
    return packages;
  }


  @Operation(summary = "Get a package from registry using project mvn setting")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Package found"),
          @ApiResponse(responseCode = "404", description = "Package not found",
                  content = @Content) })
  @GetMapping(path = "/project/{projectId}/mvn/registry")
  public Package find(@PathVariable long projectId,
                      @RequestParam(name = "group") String group,
                      @RequestParam(name = "artifact") String artifact,
                      @RequestParam(name = "version") String version) throws JsonProcessingException {

    Package _package = null;
    Project project = projectService.findById(projectId).orElseGet(null);

    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());
        List<AbstractRegistrySvc> services = RegistrySvcFactory.create(prop);
        for (AbstractRegistrySvc svc : services){
          for (Package pkg : svc.getPackages()){
            if(pkg.getArtifact().equalsIgnoreCase(artifact)
                    && pkg.getGroup().equalsIgnoreCase(group)
                    && pkg.getVersion().equalsIgnoreCase(version)){

              _package = pkg;
              break;
            }
          }
        }
    }
    if(_package==null){
      throw new NotFoundExceptionResponse();
    }
    return _package;
  }


  @Operation(summary = "Get list of main class an artifact")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of mainClass, return empty list if none") })
  @GetMapping(path = {"/project/{projectId}/analyze/registry"})
  public List<String> analyzeMainClass(@PathVariable long projectId,  @RequestParam(name = "group") String group
          ,  @RequestParam(name = "artifact") String artifact
          ,  @RequestParam(name = "version") String version
          ,  @RequestParam(name = "filter",required=false ) String filter ) throws Exception {

    Package _package = null;
    AbstractRegistrySvc _svc = null;
    Project project = projectService.findById(projectId).orElseGet(null);
    Map<Class, Method> mainClass = new HashMap<>();
    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());

        List<AbstractRegistrySvc> services = RegistrySvcFactory.create(prop);
        for (AbstractRegistrySvc svc : services){
          for (Package pkg : svc.getPackages()){
            if(pkg.getArtifact().equalsIgnoreCase(artifact)
                    && pkg.getGroup().equalsIgnoreCase(group)
                    && pkg.getVersion().equalsIgnoreCase(version)){
              _package = pkg;
              _svc = svc;
              File file = _svc.download(_package);
              mainClass =  c2.services.util.JarAnalyzer.getMainMethods(file.getAbsolutePath());
              break;
            }
          }
        }
    }
    if(filter==null){
      filter = "";
    }

    String finalFilter = filter;
    return mainClass.keySet().stream().map(str->str.getName())
            .filter(str->str.toUpperCase().contains(finalFilter.toUpperCase()))
            .collect(Collectors.toList());
  }
}
