package app.c2.controller;

import app.c2.controller.exception.NotFoundExceptionResponse;
import app.c2.model.AppInstance;
import app.c2.service.AppInstanceService;
import app.c2.service.SparkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/")
public class AppInstanceController {

  @Autowired private AppInstanceService appInstanceService;
  @Autowired private SparkService sparkService;

  @Operation(summary = "Launch app instance using Spark launcher")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Submitted app instance for launching") })
  @PostMapping("/project/{projectId}/appInstance/{appId}/submit")
  public AppInstance submitApp(
          @PathVariable long projectId,
          @PathVariable String appId) throws Exception {
    return sparkService.submitApp(appId, true);
  }

  @Operation(summary = "Get AppInstance by projectId and appId")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "AppInstance found"),
          @ApiResponse(responseCode = "404", description = "AppInstance not found",
                  content = @Content) })
  @GetMapping("/project/{projectId}/appInstance/{appId}")
  public AppInstance getAppInstanceByAppId(
          @PathVariable long projectId,
          @PathVariable String appId)  {

    Optional<AppInstance> appInstanceOptional = appInstanceService.findById(appId);
    return appInstanceOptional.orElseThrow(NotFoundExceptionResponse::new);
  }

  @Operation(summary = "Kill running instance by appId")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Submitted for killing")})
  @PostMapping("/project/{projectId}/appInstance/{appId}/kill")
  public void killAppInstance(
          @PathVariable long projectId,
          @PathVariable String appId) throws Exception {
      appInstanceService
              .kill(projectId,appId);
  }


  @Operation(summary = "Get list of past app instances by appName")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of appInstance"),})
  @GetMapping("/project/{projectId}/app/{appName}/appInstances")
  public List<AppInstance> getAppInstancesHistory(
          @PathVariable long projectId,
          @PathVariable String appName)  {
    List<AppInstance> instances = appInstanceService
            .findByProjectAppId(projectId,appName);
    instances.sort(new Comparator<AppInstance>() {
              @Override
              public int compare(AppInstance o1, AppInstance o2) {
                return o1.getUpdatedTimestamp().compareTo(o2.getUpdatedTimestamp());
              }
            });
    return instances;
  }

  @Operation(summary = "Get the last launched AppInstance by appName")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "AppInstance found"),
          @ApiResponse(responseCode = "404", description = "AppInstance not found",
                  content = @Content) })
  @GetMapping("/project/{projectId}/app/{appName}/appInstance")
  public AppInstance getAppInstance(
          @PathVariable long projectId,
          @PathVariable String appName)  {
    return appInstanceService.findByProjectAppIdLastLaunched(projectId,appName)
            .orElseThrow(NotFoundExceptionResponse::new);
  }
}
