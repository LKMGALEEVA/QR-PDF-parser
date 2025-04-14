package aseproject.qrpdfparser.service.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.json.JSONObject;

public class QRDecoder {

    public static Result decode(InputStream imageInputStream) throws IOException, NotFoundException, ChecksumException, FormatException {
        BufferedImage bufferedImage = ImageIO.read(imageInputStream);
        return decodeBufferedImage(bufferedImage);
    }


    public static Result decodeBufferedImage(BufferedImage bufferedImage) throws NotFoundException, ChecksumException, FormatException {
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        QRCodeReader qrCodeReader = new QRCodeReader();
        return qrCodeReader.decode(bitmap);
    }

   /*
   TODO:
   Метод будет работать с изображением как с единым целым и либо вернёт результат для первого
    QR-кода (если он есть), либо выбросит исключение, если QR-код не найден.
    Чтобы извлечь несколько QR-кодов с одного изображения,
    нужно использовать другой подход (например, MultiFormatReader для поиска нескольких QR-кодов).
    */

    public static String getNormalStringFromJsonString(String inputName){
        String name;
        if (Objects.equals(inputName, "QR-code Error")) {
            name = "QR-code Error";
        } else {
            JSONObject obj = new JSONObject(inputName);
            name = obj.get("kks_code").toString() + "_"
                    + obj.get("type_doc").toString() + "_"
                    + obj.get("type_work").toString() + "_"
                    + obj.get("version_doc").toString();
        }
        return name;
    }

}
