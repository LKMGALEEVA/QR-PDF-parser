package aseproject.qrpdfparser.controller;

import aseproject.qrpdfparser.dto.ErrorQrRequestDTO;
import aseproject.qrpdfparser.dto.ErrorQrResponseDTO;
import aseproject.qrpdfparser.dto.StatusDTO;
import aseproject.qrpdfparser.model.User;
import aseproject.qrpdfparser.service.AppService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;


@RestController
public class FileUploadController {

    private AppService appService;
    private String sourceFile;

    @Autowired
    public FileUploadController(AppService appService) {
        this.appService = appService;
        this.sourceFile = "src/main/resources/files";
    }

    //TODO тут может быть ошибка с name полем (проеврить на занчи запрешённые и по-хорошему бы запретить пробелы, но с ними работать будет так-то)
//    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<StatusDTO<ErrorQrResponseDTO>> handleFileUpload(@RequestParam("name") String name,
                                                                          @RequestParam("file") MultipartFile file) {
        StatusDTO<String> statusSaveFile = appService.saveFileInFileSystem(name, file);

        if (statusSaveFile.getCode() == 200) {
            StatusDTO<ErrorQrResponseDTO> errorQrDTOStatusDTO = appService.parseDocuments(statusSaveFile.getBody());

            return ResponseEntity.status(errorQrDTOStatusDTO.getCode())
                    .body(errorQrDTOStatusDTO);
        }
        return ResponseEntity.status(500)
                .body(new StatusDTO<>(statusSaveFile.getCode(), statusSaveFile.getMessage()));
    }


//    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(value = "/definitePage", method = RequestMethod.POST)
    public ResponseEntity<String> handleErrorQrPage(
            @RequestBody ErrorQrRequestDTO errorQrRequestDTO) {
        StatusDTO<Void> statusDTO = appService.definiteQrType(errorQrRequestDTO);
        return ResponseEntity.status(statusDTO.getCode()).body(statusDTO.getMessage());
    }


//    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/download/stream")
    public void downloadZipStream(HttpServletResponse response) throws IOException {
        //TODO вынести логику из контроллера в сервис и репозитории
        File fileToZip = new File(sourceFile);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=files.zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            appService.docsToZip(fileToZip, fileToZip.getName(), zipOutputStream);
        }

        FileUtils.deleteDirectory(new File("src/main/resources/files"));
    }

    @Deprecated
//    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/download")
    public ResponseEntity<?> downloadZip() throws IOException {
        File fileToZip = new File(sourceFile);

        //TODO удалить из корня zip файл?
        String tmpZipFilename = "zipFiles";
        FileOutputStream fos = new FileOutputStream(tmpZipFilename);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        appService.docsToZip(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();

        byte[] fileByte = Files.readAllBytes(Path.of(tmpZipFilename));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/zip");
        responseHeaders.add("Content-Disposition", "attachment; filename=\"files.zip\"");

        FileUtils.deleteDirectory(new File("src/main/resources/files"));

        return ResponseEntity
                .ok()
                .headers(responseHeaders)
                .body(fileByte);
    }

    @PostMapping("/new-user")
    public String addUser(@RequestBody User user) {
        appService.addUser(user);
        return "User is saved!";
    }

}
