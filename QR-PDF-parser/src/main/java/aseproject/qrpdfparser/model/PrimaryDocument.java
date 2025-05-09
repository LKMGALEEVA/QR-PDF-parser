package aseproject.qrpdfparser.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table
public class PrimaryDocument {
    @Id
    @GeneratedValue
    private Integer id;
    private String title;
    private Integer number_of_Pages;
}
