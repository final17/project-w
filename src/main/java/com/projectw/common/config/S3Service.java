package com.projectw.common.config;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    public final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Value("${cloud.aws.s3.max-file-size:10485760}") // 10MB 기본값 설정
    private long maxFileSize;

    // S3에 파일 업로드
    public String uploadFile(MultipartFile file) throws IOException {
        // 파일 크기 제한 체크
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("파일 크기가 최대 업로드 크기를 초과했습니다. 최대 크기: %dMB, 현재 파일 크기: %.2fMB",
                            maxFileSize / (1024 * 1024), (double) file.getSize() / (1024 * 1024)));
        }

        // 고유한 파일 이름 생성
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // 파일을 S3에 업로드
        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
        } catch (AmazonServiceException e) {
            throw new RuntimeException("S3에 파일 업로드 실패: " + e.getMessage(), e);
        }

        // 업로드된 파일의 URL 반환
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    // 파일 삭제
    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(bucketName, fileName);
        } catch (AmazonServiceException e) {
            throw new RuntimeException("S3에 파일 삭제 실패: " + e.getMessage(), e);
        }
    }
}
