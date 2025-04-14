package aseproject.qrpdfparser.dto;

import lombok.Data;

@Data
public class StatusDTO<T> {
    int code;
    String message;
    T body;

    public StatusDTO(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public StatusDTO(int code, T body) {
        this.code = code;
        this.body = body;
    }

}
