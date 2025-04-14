package aseproject.qrpdfparser.service;

import aseproject.qrpdfparser.dto.ErrorQrDTO;
import aseproject.qrpdfparser.dto.StatusDTO;
import aseproject.qrpdfparser.model.User;
import aseproject.qrpdfparser.repository.UserRepository;
import aseproject.qrpdfparser.service.utils.FileSystemRepository;
import aseproject.qrpdfparser.service.utils.PDFUtils;
import aseproject.qrpdfparser.service.utils.QRDecoder;
import com.google.zxing.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AppService {

    private UserRepository userRepository;
    private FileSystemRepository fileSystemRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AppService(UserRepository userRepository, FileSystemRepository fileSystemRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fileSystemRepository = fileSystemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public StatusDTO<String> saveFileInFileSystem(String name, MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String path = fileSystemRepository.saveFile(name, bytes);
                StatusDTO<String> statusDTO = new StatusDTO<>(200, null);
                statusDTO.setBody(path);
                return statusDTO;
            } catch (Exception e) {
                return new StatusDTO<>(500, "Ошибка сервера");
            }
        }
        return new StatusDTO<>(500, "Ошибка сервера: " + "файл не сохранился");
    }

    public StatusDTO<ErrorQrDTO> parseDocuments(String path) {
        PDDocument document = null;
        try (InputStream inputStream = new FileInputStream(path)) {
            document = PDDocument.load(inputStream);

            List<Result> qrFromDocList = PDFUtils.getQRResultsFromDocument(document);
            Map<String, PDDocument> filesOnQrTypeMap = createFilesOnQrTypeMap(document, qrFromDocList);
            fileSystemRepository.saveFilesOnQr(document, qrFromDocList, filesOnQrTypeMap);

            byte[] errorQrFile = fileSystemRepository.errorDoc();
            if (errorQrFile != null) {
                List<String> listQrName = filesOnQrTypeMap.keySet().stream()
                        .map(QRDecoder::getNormalStringFromJsonString)
                        .toList();
                return new StatusDTO<>(200, new ErrorQrDTO(listQrName, errorQrFile));
            } else {
                return new StatusDTO<>(200, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new StatusDTO<>(500, "Ошибка сервера");
    }

    public Map<String, PDDocument> createFilesOnQrTypeMap(PDDocument document, List<Result> qrFromDocList) throws IOException {
        Map<String, PDDocument> filesOnQrTypeMap = new HashMap<>();
        for (int i = 1; i < document.getNumberOfPages(); i++) {
            PDDocument document1 = new PDDocument();
            filesOnQrTypeMap.put(qrFromDocList.get(i).getText(), document1);
        }

        return filesOnQrTypeMap;
    }

    public void docsToZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        //TODO люба рекурсия может забить стек, лучше руками создать new SimpleStack и туда класть, что обрабатываем
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                docsToZip(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();

    }

//    public Set<String> getAllDocuments() {
//        try (InputStream inputStream = new FileInputStream("C://IdeaProjects/test.pdf")) {   //исправить, надо передавать путь к папке
//            // парсим док
//            PDDocument document = null;
//            document = PDDocument.load(inputStream);
//
//            //int number_of_pages = document.getNumberOfPages();
//
//            DictCreation myDict = new DictCreation();
//            Map<String, PDDocument> diction = myDict.createDictionary(document);
//
//            Set<String> documentNames = diction.keySet();
//            document.close();
//            return documentNames;
//        }catch (IOException e) {
//            //e.printStackTrace();
//        }
//        return null;
//        }
//
//    public void parseByHand(String docName, String page_id) throws IOException {
//        try {
//
//            InputStream inputStream = new FileInputStream("C://IdeaProjects//testDocs/3.pdf"); //надо исправить, передавать название документа, который надо распарсить
//            PDDocument documentWithErrors = null;
//            documentWithErrors = PDDocument.load(inputStream);
//
//            InputStream inputStream1 = new FileInputStream("C://IdeaProjects/test.pdf"); //надо исправить, передавать название документа, который надо распарсить
//            PDDocument primaryDocument = null;
//            primaryDocument= PDDocument.load(inputStream1);
//
//            DictCreation myDict = new DictCreation();
//            Map<String, PDDocument> QRDict = myDict.createDictionary(primaryDocument);  //нужен словарь со всеми документами
//
//            PDDocument tempdoc = QRDict.get(docName); //docName - содержание qr кода
//            tempdoc.addPage(documentWithErrors.getPage(Integer.parseInt(page_id)));
//
//            tempdoc.save("C://IdeaProjects//testDocs/" + docName + ".pdf");
//            tempdoc.close();
//            documentWithErrors.close();
//            primaryDocument.close();
//
//        } catch (IOException e) {
//            //e.printStackTrace();
//        }
//    }
//    public void addDoc(Document doc){
//        docsRepository.save(doc);
//    }
}

