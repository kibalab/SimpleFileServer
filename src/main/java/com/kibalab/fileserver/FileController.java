package com.kibalab.fileserver;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";

    static {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is Empty");
            }

            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            File destFile = new File(UPLOAD_DIR + filename);

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                IOUtils.copy(file.getInputStream(), fos);
            }

            return ResponseEntity.ok("OK: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("filename") String fileUrl, HttpServletResponse response) throws IOException {
        File file = new File(UPLOAD_DIR+fileUrl);

        Tika tika = new Tika();
        String mediaType = tika.detect(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, mediaType)
                .header(HttpHeaders.CONTENT_LENGTH, file.length() + "")
                .body(FileCopyUtils.copyToByteArray(file));
    }
}
