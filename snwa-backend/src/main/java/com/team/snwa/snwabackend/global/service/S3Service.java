package com.team.snwa.snwabackend.global.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Template s3Template;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * Presigned URL 생성
     * 
     * @param userId      사용자 ID
     * @param contentType 파일 타입 (예: image/jpeg)
     * @param directory   저장할 디렉토리 (예: profiles)
     * @return [presignedUrl, fileUrl]
     */
    public String[] createPresignedUrl(Long userId, String contentType, String directory) {
        // 파일명 생성: {directory}/{userId}/{uuid}
        String fileName = String.format("%s/%d/%s", directory, userId, UUID.randomUUID());

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 유효시간 10분
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String presignedUrl = presignedRequest.url().toString();
        // 실제 저장될 파일 URL (단순 S3 URL)
        // String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucket,
        // fileName);
        // 또는 s3Template 등을 통해 가져올 수도 있으나, 보통 버킷 URL + key 조합
        // 여기서는 S3 URL 형식을 직접 조합
        String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

        return new String[] { presignedUrl, fileUrl };
    }

    /**
     * S3 객체 삭제
     * 
     * @param fileUrl 삭제할 파일 URL
     */
    public void deleteObject(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                s3Template.deleteObject(bucket, key);
                log.info("S3 Object deleted: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to delete S3 object: {}", fileUrl, e);
        }
    }

    /**
     * URL에서 S3 Key 추출
     */
    public String extractKeyFromUrl(String fileUrl) {
        try {
            // "https://{bucket}.s3.{region}.amazonaws.com/{key}" 형태라고 가정
            // 또는 "https://s3.{region}.amazonaws.com/{bucket}/{key}"
            // 간단하게: 버킷명 이후의 경로를 추출하거나, URL 파싱
            String splitStr = ".com/";
            int index = fileUrl.indexOf(splitStr);
            if (index != -1) {
                return fileUrl.substring(index + splitStr.length());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
