package com.codaily.common.file.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileServeController {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/{directory}/{filename}")
    @Operation(summary = "파일 서빙", description = "업로드된 파일을 제공합니다.")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String directory,
            @PathVariable String filename) {

        log.info("파일 요청: {}/{}", directory, filename);

        try {
            // 파일 경로 생성
            Path filePath = Paths.get(uploadDir, directory, filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일을 찾을 수 없음: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 콘텐츠 타입 감지
            String contentType;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (Exception e) {
                contentType = "application/octet-stream";
            }

            if (contentType == null) {
                // 확장자 기반 추정
                String fileName = filename.toLowerCase();
                if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else {
                    contentType = "application/octet-stream";
                }
            }

            log.info("파일 서빙 성공: {} ({})", filePath, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // 1년 캐시
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 서빙 중 오류: {}/{}", directory, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
