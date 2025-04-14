package aseproject.qrpdfparser.service.utils;

import com.google.zxing.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            PDDocument tempdoc = filesOnQrTypeMap.get(qrFromDocList.get(i).getText());
            tempdoc.addPage(document.getPage(i));
        }

        new File("src/main/resources/files").mkdirs();

        Set<String> resultDocuments = filesOnQrTypeMap.keySet();
        for (String result : resultDocuments) {
            PDDocument tempdoc = filesOnQrTypeMap.get(result);

            String name = QRDecoder.getNormalStringFromJsonString(result);
            //TODO исправить наименование
            tempdoc.save("src/main/resources/files/" + name + ".pdf");
            tempdoc.close();
        }
    }

    public byte[] errorDoc() throws IOException {
        File pdfFile = new File("src/main/resources/files/QR-code Error.pdf");
        if (pdfFile.exists()) {
            return Files.readAllBytes(pdfFile.toPath());
        }
        return null;
    }
}
