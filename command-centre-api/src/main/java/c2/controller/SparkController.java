package c2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/")
public class SparkController {


  @Operation(summary = "Get list spark arguments")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Return list of spark arg") })
  @GetMapping(path = {"/spark/arg"})
  public List<String> find(@RequestParam(name = "filter",required = false) String filter)  {

    String[] sparkArgs = {
            "spark.executor.memory",
            "spark.driver.cores",
            "spark.driver.maxResultSize"};

    if(filter!=null){
      return Arrays.stream(sparkArgs).filter(str->str.toUpperCase().contains(filter.toUpperCase())).collect(Collectors.toList());
    }else{
      return Arrays.stream(sparkArgs).collect(Collectors.toList());
    }
  }
}
