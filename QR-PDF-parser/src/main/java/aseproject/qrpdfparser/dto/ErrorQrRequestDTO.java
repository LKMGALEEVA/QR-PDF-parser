package aseproject.qrpdfparser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorQrRequestDTO {
    @JsonProperty("isDefiniteQrType")
    Boolean isDefiniteQrType;
    @JsonProperty("definiteQrType")
    String definiteQrType;

    @JsonProperty("indexErrorPage")
    Integer indexErrorPage;

    @JsonProperty("kksCode")
    private String kksCode;
    @JsonProperty("workType")
    private String workType;
    @JsonProperty("docType")
    private String docType;
    //TODO тут должно быть ещё поле versionPrefix;
    @JsonProperty("version")
    private Integer version;
}
