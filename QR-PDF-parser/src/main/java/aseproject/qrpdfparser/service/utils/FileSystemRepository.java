package aseproject.qrpdfparser.service.utils;

import com.google.zxing.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class FileSystemRepository {
    //TODO много харкода, типо путей к файлам

    public String saveFile(String name, byte[] bytes) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new FileOutputStream(new File(name + ".pdf")));
        stream.write(bytes);
        stream.close();
        return name + ".pdf";
    }

    public void saveFilesOnQr(PDDocument document, List<Result> qrFromDocList, Map<String, PDDocument> filesOnQrTypeMap) throws IOException {
        int number_of_pages = document.getNumberOfPages();

        for (int i = 0; i < number_of_pages; i++) {
            //filesOnQrTypeMap have empty PDDocument*
            PDDocument tempdoc = filesOnQrTypeMap.get(qrFromDocList.get(i).getText());
            tempdoc.addPage(document.getPage(i));
        }

        new File("src/main/resources/files").mkdirs();

        Set<String> resultDocuments = filesOnQrTypeMap.keySet();
        for (String result : resultDocuments) {
            PDDocument tempdoc = filesOnQrTypeMap.get(result);
            String name = QRDecoder.getNormalStringFromJsonString(result);
            saveDocumentByName(name, tempdoc);
            tempdoc.close();
        }
    }

    public byte[] getErrorDocToByte() throws IOException {
        File pdfFile = new File("src/main/resources/files/QR-code Error.pdf");
        if (pdfFile.exists()) {
            return Files.readAllBytes(pdfFile.toPath());
        }
        return null;
    }

    public PDDocument getErrorDocToObj() throws IOException {
        String path = "src/main/resources/files/QR-code Error.pdf";

        try (InputStream inputStream = new FileInputStream(path)) {
            return PDDocument.load(inputStream);
        }
    }

    public void saveErrorDoc(PDDocument document) throws IOException {
        String path = "src/main/resources/files/QR-code Error.pdf";
        if (document.getPages().getCount() == 0) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } else {
            document.save(path);
        }
    }

    public PDDocument getDocumentByName(String name) throws IOException {
        String path = "src/main/resources/files/" + name + ".pdf";

        try (InputStream inputStream = new FileInputStream(path)) {
            return PDDocument.load(inputStream);
        }
    }

    public void saveDocumentByName(String name, PDDocument document) throws IOException {
        document.save("src/main/resources/files/" + name + ".pdf");
    }

}
