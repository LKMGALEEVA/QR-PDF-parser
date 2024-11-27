package aseproject.qrpdfparser.service;

import aseproject.qrpdfparser.model.Document;
import aseproject.qrpdfparser.model.User;
import aseproject.qrpdfparser.repository.InDBSaveFiles;
import aseproject.qrpdfparser.repository.UserRepository;
import aseproject.qrpdfparser.service.utils.DictCreation;
import aseproject.qrpdfparser.service.utils.InMemorySavesFiles;
import aseproject.qrpdfparser.service.utils.PDFUtils;
import com.google.zxing.Result;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.json.JSONObject;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
@AllArgsConstructor
public class AppService {

    private UserRepository repository;
    private InDBSaveFiles docsRepository;
    private PasswordEncoder passwordEncoder;

    public void addUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
    }

    public void addDoc(Document doc){
        docsRepository.save(doc);
    }

    public void ParseDocuments(String path) {

        PDDocument document = null;
//        try (InputStream is = AppService.class.getResourceAsStream(path)) {
        try (InputStream inputStream = new FileInputStream(path)) {
            // парсим док
            document = PDDocument.load(inputStream);

            List<Result> results = PDFUtils.getQRResultsFromDocument(document);
            //int number_of_pages = document.getNumberOfPages();

            DictCreation myDict = new DictCreation();
            Map<String, PDDocument> diction = myDict.createDictionary(document);
            //Map<String, PDDocument> diction = myDict.createDictionary(document);
            //System.out.println(diction);


            InMemorySavesFiles imsf = new InMemorySavesFiles();
            imsf.saveFiles(document, results, diction);
            //document.close();

            // считываем все qr коды
            //
            //return "Документ разделен успешно";
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                }
            }
        }
        //return null;
    }

    public static void docsToZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
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

    public void parseByHand(String docName, String page_id) throws IOException {
        try {

            InputStream inputStream = new FileInputStream("C://IdeaProjects//testDocs/3.pdf"); //надо исправить, передавать название документа, который надо распарсить
            PDDocument documentWithErrors = null;
            documentWithErrors = PDDocument.load(inputStream);

            InputStream inputStream1 = new FileInputStream("C://IdeaProjects/test.pdf"); //надо исправить, передавать название документа, который надо распарсить
            PDDocument primaryDocument = null;
            primaryDocument= PDDocument.load(inputStream1);

            DictCreation myDict = new DictCreation();
            Map<String, PDDocument> QRDict = myDict.createDictionary(primaryDocument);  //нужен словарь со всеми документами

            PDDocument tempdoc = QRDict.get(docName); //docName - содержание qr кода
            tempdoc.addPage(documentWithErrors.getPage(Integer.parseInt(page_id)));

            tempdoc.save("C://IdeaProjects//testDocs/" + docName + ".pdf");
            tempdoc.close();
            documentWithErrors.close();
            primaryDocument.close();

        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}

