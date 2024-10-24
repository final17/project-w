package com.projectw.common.utils;

import com.projectw.common.exceptions.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class FileUtil {
    // 허용된 파일 확장자 목록
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif"
    ));

    // 파일명에서 허용되지 않는 문자들
    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileUploadException("파일명이 유효하지 않습니다.");
        }

        // 파일 확장자 검증
        String ext = getExtension(originalFilename);
        if (!isValidExtension(ext)) {
            throw new FileUploadException("허용되지 않는 파일 형식입니다: " + ext);
        }

        // 파일 크기 검증 (예: 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileUploadException("파일 크기가 너무 큽니다. 최대 10MB까지 허용됩니다.");
        }
    }

    public static String getExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) return "";
        return filename.substring(lastDotIndex).toLowerCase();
    }

    public static boolean isValidExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null) return null;
        // 특수문자 제거
        return INVALID_FILENAME_PATTERN.matcher(filename).replaceAll("_");
    }

    public static File createSecureFile(String baseDir, String email, String originalFilename) throws IOException {
        // 이메일 디렉토리 검증
        String sanitizedEmail = sanitizeFilename(email);
        if (sanitizedEmail == null || sanitizedEmail.isBlank()) {
            throw new FileUploadException("유효하지 않은 이메일입니다.");
        }

        // 디렉토리 생성
        File emailDirectory = new File(baseDir, sanitizedEmail);
        if (!emailDirectory.exists()) {
            if (!emailDirectory.mkdirs()) {
                throw new FileUploadException("디렉토리 생성에 실패했습니다: " + emailDirectory.getPath());
            }
        }

        // 안전한 파일명 생성
        String ext = getExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString() + ext;

        // 경로 순회 공격 방지
        File dest = new File(emailDirectory, newFileName).getCanonicalFile();
        if (!dest.getParentFile().equals(emailDirectory.getCanonicalFile())) {
            throw new FileUploadException("잘못된 파일 경로입니다.");
        }

        return dest;
    }
}