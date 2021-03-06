package app.c2.controller;

import app.c2.C2PlatformProperties;
import app.c2.controller.exception.NotFoundExceptionResponse;
import app.c2.model.Project;
import app.c2.properties.C2Properties;
import app.c2.service.ProjectService;
import app.c2.service.maven.AbstractRegistrySvc;
import app.c2.service.maven.MavenSvcFactory;
import app.c2.service.maven.model.Package;
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
  @Autowired
  C2PlatformProperties c2PlatformProperties;

  @Operation(summary = "Get list of artifact from registry using project mvn setting")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of package, return empty list if none"),
          @ApiResponse(responseCode = "404", description = "Project not found",
                  content = @Content) })
  @GetMapping(path = {"/project/{projectId}/artifacts","/project/{projectId}/artifacts/{group}"})
  public List<Package> findAll(@PathVariable long projectId, @PathVariable Optional<String> group ) throws JsonProcessingException {
    List<Package> packages = new ArrayList<>();
    Project project = projectService.findById(projectId).orElseGet(null);

    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());
        MavenSvcFactory.create(prop, c2PlatformProperties.getTmp()+"/maven").forEach(svc->{
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
  @GetMapping(path = {"/project/{projectId}/artifacts/{group}/{artifact}/{version}"})
  public Package find(@PathVariable long projectId,
                      @PathVariable(name = "group") String group,
                      @PathVariable(name = "artifact") String artifact,
                      @PathVariable(name = "version") String version) throws JsonProcessingException {

    Package _package = null;
    Project project = projectService.findById(projectId).orElseGet(null);

    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());
        List<AbstractRegistrySvc> services = MavenSvcFactory.create(prop,  c2PlatformProperties.getTmp()+"/maven");
        for (AbstractRegistrySvc svc : services){
          Package pkg = new Package();
          pkg.setArtifact(artifact);
          pkg.setGroup(group);
          pkg.setPackage_type(Package.PackageType.MAVEN);
          pkg.setVersion(version);
          try{
            File file = svc.download(pkg);
            if(file!=null){
              break;
            }
          }catch (Exception e){

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

    @GetMapping(path = {"/project/{projectId}/artifacts/{group}/{artifact}/{version}/analyze"})
  public List<String> analyzeMainClass(@PathVariable long projectId,
    @PathVariable(name = "group") String group,
    @PathVariable(name = "artifact") String artifact,
    @PathVariable(name = "version") String version,
    @RequestParam(name = "filter",required=false ) String filter ) throws Exception {

    Project project = projectService.findById(projectId).orElseGet(null);
    Map<Class, Method> mainClasses = new HashMap<>();
    if(project !=null && project.getEnv()!=null) {
        C2Properties prop = (project.getEnv());
        List<AbstractRegistrySvc> services = MavenSvcFactory.create(prop,  c2PlatformProperties.getTmp()+"/maven");
        for (AbstractRegistrySvc svc : services){
          Package pkg = new Package();
          pkg.setArtifact(artifact);
          pkg.setGroup(group);
          pkg.setPackage_type(Package.PackageType.MAVEN);
          pkg.setVersion(version);
          try{
            File file = svc.download(pkg);
            mainClasses =  app.c2.service.util.JarAnalyzer.getMainMethods(file.getAbsolutePath());
            break;
          }catch (Exception e){

          }
        }
    }
    if(filter==null){
      filter = "";
    }

    String finalFilter = filter;
    return mainClasses.keySet().stream().map(str->str.getName())
            .filter(str->str.toUpperCase().contains(finalFilter.toUpperCase()))
            .collect(Collectors.toList());
  }
}
