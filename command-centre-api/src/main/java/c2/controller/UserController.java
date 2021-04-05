package c2.controller;

import c2.controller.exception.NotFoundExceptionResponse;
import c2.model.Project;
import c2.model.User;
import c2.service.ProjectService;
import c2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/")
public class UserController {

  @Autowired private UserService userService;
  @Autowired private ProjectService projectService;

  @Operation(summary = "Get user by userId")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Found user"),
          @ApiResponse(responseCode = "404", description = "User not found",
                  content = @Content) })
  @GetMapping(path = "/user/{userId}")
  public User find(@PathVariable String userId)  {
    Optional<User> user = userService.findById(userId);
    return user.orElseThrow(NotFoundExceptionResponse::new);
  }

  @Operation(summary = "Get all users")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of user, empty list if none found") })
  @GetMapping(path = "/users")
  public List<User> findAll()  {
    List<User> users = userService.findAll();
    return users;
  }

  @Operation(summary = "Save or create new user")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User saved") })
  @PutMapping(path = "/user")
  public User save(@RequestBody User user)  {
    return userService.save(user);
  }

  @Operation(summary = "Get projects accessible by user as project owner or member")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of projects")})
  @GetMapping(value = {"/user/{userId}/project"})
  public List<Project> findAll(@PathVariable String userId) {
    return projectService.findByOwner(userId);
  }
}
