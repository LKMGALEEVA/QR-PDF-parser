package aseproject.qrpdfparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorQrDTO {
    List<String> qrTypes;
    byte[] errorFile;
}
