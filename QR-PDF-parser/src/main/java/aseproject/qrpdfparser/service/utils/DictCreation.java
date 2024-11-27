package aseproject.qrpdfparser.service.utils;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.zxing.Result;
import netscape.javascript.JSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DictCreation {

    public Map<String, PDDocument> createDictionary(PDDocument document) throws IOException {
        Map<String, PDDocument> QrDict = new HashMap<>();

        List<Result> results = PDFUtils.getQRResultsFromDocument(document);

        for (int i = 1; i < document.getNumberOfPages(); i++){
            PDDocument document1 =  new PDDocument();
            QrDict.put(results.get(i).getText(), document1);
        }

        return QrDict;
    }
}
