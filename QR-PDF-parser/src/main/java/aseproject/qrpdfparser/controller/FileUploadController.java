package aseproject.qrpdfparser.controller;

import aseproject.qrpdfparser.model.User;
import aseproject.qrpdfparser.service.AppService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

import static aseproject.qrpdfparser.service.AppService.docsToZip;


@RestController
//@RequestMapping("api/v1/QRdocs")
@AllArgsConstructor
public class FileUploadController {

    private AppService appService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody String handleFileUpload(@RequestParam("name") String name,
                                                 @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(name + ".pdf")));
                stream.write(bytes);
                stream.close();
                String path = name + ".pdf";

                appService.ParseDocuments(path);

                return "Файл загружен и разделен успешно!";

            } catch (Exception e) {
                return "Что-то не то";
            }
        } else {
            return "Что-то не то";
        }
    }


    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/download/stream")
    public void downloadZipStream(HttpServletResponse response) throws IOException {
        String sourceFile = "src/main/resources/files";
        File fileToZip = new File(sourceFile);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=files.zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            docsToZip(fileToZip, fileToZip.getName(), zipOutputStream);
        }

        FileUtils.deleteDirectory(new File("src/main/resources/files"));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/download")
    public ResponseEntity<?> downloadZip() throws IOException {
        String sourceFile = "src/main/resources/files";
        String tmpZipFilename = "zipFiles";
        FileOutputStream fos = new FileOutputStream(tmpZipFilename);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        docsToZip(fileToZip, fileToZip.getName(), zipOut);
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

    //@PreAuthorize("hasAuthority('ROLE_USER')")
//    @GetMapping("/all-docs")
//    public Set<String> getAllDocuments(){
//        return appService.getAllDocuments();
//    }

//    @PreAuthorize("hasAuthority('ROLE_USER')")
//    @GetMapping("/page{page_id}")
//    public String parseByHand(@RequestBody String name, @PathVariable String page_id) throws IOException {
//        appService.parseByHand(name, page_id);
//        return "Cтраница добавлена в документ " + name + ".pdf";
//    }

    @PostMapping("/new-user")
    public String addUser(@RequestBody User user) {
        appService.addUser(user);
        return "User is saved!";
    }


    @GetMapping("/openPdf")
    public void openPdf() throws IOException {
        Desktop desktop = Desktop.getDesktop();
        File file = new File("C:/Users/HONOR/Download/169-292-1-SM.pdf");
        desktop.open(file);
    }


}
