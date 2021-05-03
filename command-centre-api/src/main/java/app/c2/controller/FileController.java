package app.c2.controller;

import app.c2.controller.exception.NotFoundExceptionResponse;
import app.c2.model.File;
import app.c2.service.FileStorageService;
import app.c2.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ProjectService projectService;


    @Operation(summary = "Download file, if fileHash is not defined the latest file will be returned")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File found") })
    @GetMapping("/file/download/{projectId}/{fileName:.+}/{fileHash}")
    public ResponseEntity downloadFile(@PathVariable String fileName, @PathVariable long projectId, @PathVariable String fileHash, HttpServletRequest request) {
        File file;
        if(fileHash.equalsIgnoreCase("latest")){
            file = fileStorageService.getFile(projectId, fileName).get();
        }else{
            file = fileStorageService.getFile(projectId, fileName, fileHash).get();
        }
        byte[] byteContent = file.getFileBlob();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(byteContent);
    }

    @Operation(summary = "Get file by fileId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File found"),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content) })
    @GetMapping("/file/{fileId}")
    public File viewFile(@PathVariable Long fileId) {
        return fileStorageService.getFile(fileId).orElseThrow(NotFoundExceptionResponse::new);
    }
}
