package aseproject.qrpdfparser;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@SpringBootApplication
public class QrPdfParserApplication {


    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
//        factory.setMaxFileSize("128KB");
//        factory.setMaxRequestSize("128KB");
        return factory.createMultipartConfig();
    }


    public static void main(String[] args) throws IOException {
        SpringApplication.run(QrPdfParserApplication.class, args);

    }
}
