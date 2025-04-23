package aseproject.qrpdfparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ErrorQrResponseDTO {
    List<String> qrTypes;
    byte[] errorFile;
}
