package aseproject.qrpdfparser.service.utils;

import aseproject.qrpdfparser.QrPdfParserApplication;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFUtils {

    //TODO небезопсаная инициализация
    static {
        InputStream is = QrPdfParserApplication.class.getResourceAsStream("/qr-code Error.gif");
        try {
            errorImage = ImageIO.read(is);
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage errorImage;

    public static List<Result> getQRResultsFromDocument(PDDocument document) throws IOException {

        // Parsed QR results will be stored here
        List<Result> results = new ArrayList<>();

        List<BufferedImage> images = getImagesFromDocument(document);
        for (BufferedImage image : images) {
            try {
                results.add(QRDecoder.decodeBufferedImage(image));
            } catch (ReaderException e) {
                // TODO log this happened but do nothing!
                e.printStackTrace();
            }
        }
        return results;
    }

    public static List<Result> getQRResultsFromPage(PDPage page) throws IOException {

        // Parsed QR results will be stored here
        List<Result> results = new ArrayList<>();

        List<BufferedImage> images = getImagesFromPage(page);
        for (BufferedImage image : images) {
            try {
                results.add(QRDecoder.decodeBufferedImage(image));
            } catch (ReaderException e) {
                // TODO тут нет логики не найденности QR в BufferedImage image
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    public static List<BufferedImage> getImagesFromDocument(PDDocument document) throws IOException {

        List<BufferedImage> images = new ArrayList<>();

        for (PDPage page : document.getPages()) {
            if (getImagesFromPage(page).isEmpty()) {
                images.add(errorImage);
            }
            images.addAll(getImagesFromPage(page));
        }

        return images;
    }

    public static List<BufferedImage> getImagesFromPage(PDPage page) throws IOException {

        List<BufferedImage> images = new ArrayList<>();
        images.addAll(getImagesFromResources(page.getResources()));

        return images;
    }

    private static List<BufferedImage> getImagesFromResources(PDResources resources) throws IOException {

        List<BufferedImage> images = new ArrayList<>();

        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);

            if (xObject instanceof PDFormXObject) {
                // Recursive
                images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
            } else if (xObject instanceof PDImageXObject) {
                images.add(((PDImageXObject) xObject).getImage());
            }
        }

        return images;
    }
}