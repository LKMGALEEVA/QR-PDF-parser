package aseproject.qrpdfparser.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table
public class Document {
    @Id
    private String KKS;
    private String revision;
    private String title;
    private Integer number_of_Pages;

}
