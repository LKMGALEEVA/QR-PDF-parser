package aseproject.qrpdfparser.service.utils;

import aseproject.qrpdfparser.service.AppService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.zxing.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Repository
public class InMemorySavesFiles {

    public void saveFiles(PDDocument document, List<Result> results, Map<String, PDDocument> QrDict) throws IOException{

        int number_of_pages = document.getNumberOfPages();
        int j = 0;
        for (int i = 0; i < number_of_pages; i++) {
            PDDocument tempdoc = QrDict.get(results.get(i).getText());
            tempdoc.addPage(document.getPage(i));
        }

        new File("src/main/resources/files").mkdirs();

        Set<String> resultDocuments = QrDict.keySet();
        for (String result: resultDocuments) {
            PDDocument tempdoc = QrDict.get(result);

            String name;
            if (Objects.equals(result, "QR-code Error")){
                name = "QR-code Error";
            }
            else
            {
                JSONObject obj = new JSONObject(result);
                name = obj.get("kks_code").toString() + "_"
                    + obj.get("type_doc").toString() + "_"
                    + obj.get("type_work").toString() + "_"
                    + obj.get("version_doc").toString();
            }
            tempdoc.save("src/main/resources/files/" + name + ".pdf");  //исправить, надо присваивать норм названия
            tempdoc.close();
        }
    }

}
