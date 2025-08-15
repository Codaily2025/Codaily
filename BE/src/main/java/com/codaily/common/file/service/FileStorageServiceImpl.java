package com.codaily.common.file.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.base-url:http://localhost:8081}")
    private String baseUrl;

    @Override
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir, directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일명 생성 (UUID + 원본 확장자)
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 파일 URL 반환
            String fileUrl = baseUrl + "/files/" + directory + "/" + fileName;
            log.info("파일 업로드 완료: {}", fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(baseUrl + "/files/", "");
            Path filePath = Paths.get(uploadDir, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", fileUrl);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }
}
