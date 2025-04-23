package aseproject.qrpdfparser.service;

import aseproject.qrpdfparser.dto.ErrorQrRequestDTO;
import aseproject.qrpdfparser.dto.ErrorQrResponseDTO;
import aseproject.qrpdfparser.dto.StatusDTO;
import aseproject.qrpdfparser.model.User;
import aseproject.qrpdfparser.repository.UserRepository;
import aseproject.qrpdfparser.service.utils.FileSystemRepository;
import aseproject.qrpdfparser.service.utils.PDFUtils;
import aseproject.qrpdfparser.service.utils.QRDecoder;
import com.google.zxing.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
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

    public StatusDTO<ErrorQrResponseDTO> parseDocuments(String path) {
        PDDocument document = null;
        try (InputStream inputStream = new FileInputStream(path)) {
            document = PDDocument.load(inputStream);

            List<Result> qrFromDocList = PDFUtils.getQRResultsFromDocument(document);
            Map<String, PDDocument> filesOnQrTypeMap = createFilesOnQrTypeMap(document, qrFromDocList);
            fileSystemRepository.saveFilesOnQr(document, qrFromDocList, filesOnQrTypeMap);

            byte[] errorQrFile = fileSystemRepository.getErrorDocToByte();
            if (errorQrFile != null) {
                List<String> listQrName = filesOnQrTypeMap.keySet().stream()
                        .map(QRDecoder::getNormalStringFromJsonString)
                        .toList();
                return new StatusDTO<>(200, new ErrorQrResponseDTO(listQrName, errorQrFile));
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

    public StatusDTO<Void> definiteQrType(ErrorQrRequestDTO errorQrRequestDTO) {
        String nameFile = generateFileName(errorQrRequestDTO);
        PDDocument document = getOrCreateMainDocument(nameFile, errorQrRequestDTO.getIsDefiniteQrType());
        if (document == null) {
            return new StatusDTO<>(500, "Ошибка получения основного документа");
        }

        PDDocument errorDoc;
        try {
            errorDoc = fileSystemRepository.getErrorDocToObj();
        } catch (IOException e) {
            return new StatusDTO<>(500, "Ошибка получения файла с неопределёнными QR");
        }
        PDDocument newErrorDoc = transferErrorPage(errorDoc, document, errorQrRequestDTO.getIndexErrorPage());

        try {
            fileSystemRepository.saveDocumentByName(nameFile, document);
            fileSystemRepository.saveErrorDoc(newErrorDoc);
            document.close();
            newErrorDoc.close();
            errorDoc.close();
        } catch (IOException e) {
            return new StatusDTO<>(500, "Ошибка сохранения файла");
        }

        return new StatusDTO<>(200, "Успешное переопределение страницы");
    }

    private String generateFileName(ErrorQrRequestDTO dto) {
        if (dto.getIsDefiniteQrType()) {
            return dto.getDefiniteQrType();
        }
        return String.join("_",
                dto.getKksCode(),
                dto.getWorkType(),
                dto.getDocType(),
                String.valueOf(dto.getVersion()));
    }

    private PDDocument getOrCreateMainDocument(String nameFile, boolean isDefinite) {
        PDDocument document = null;
        if (isDefinite) {
            try {
                document = fileSystemRepository.getDocumentByName(nameFile);
            } catch (IOException e) {
                return null;
            }
        } else {
            try {
                PDDocument doc = fileSystemRepository.getDocumentByName(nameFile);
                if (doc != null) {
                    document = doc;
                } else {
                    document = new PDDocument();
                }
            } catch (Exception e) {
                document = new PDDocument();
            }
        }
        return document;
    }

    private PDDocument transferErrorPage(PDDocument errorDoc, PDDocument mainDoc, int indexToTransfer) {
        PDDocument newErrorDoc = new PDDocument();
        PDPage errorPage = errorDoc.getPage(indexToTransfer);

        for (int i = 0; i < errorDoc.getNumberOfPages(); i++) {
            if (i != indexToTransfer) {
                newErrorDoc.addPage(errorDoc.getPage(i));
            } else {
                mainDoc.addPage(errorPage);
            }
        }
        return newErrorDoc;
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
}

