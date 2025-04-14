package aseproject.qrpdfparser.repository;

import aseproject.qrpdfparser.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InDBSaveFiles extends JpaRepository<Document, String> {


}
