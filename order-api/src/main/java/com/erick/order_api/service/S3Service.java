package com.erick.order_api.service;

import com.erick.order_api.config.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public String uploadFile(MultipartFile file) {
        String fileName = generateNameUnique(file.getOriginalFilename());

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(awsProperties.getBucket())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload da foto " + e.getMessage());
        }

        return generateUrl(fileName);
    }

    private String generateNameUnique(String nameOriginal) {
    String extension = "";
    if (nameOriginal != null && nameOriginal.contains(".")) {
        extension = nameOriginal.substring(nameOriginal.lastIndexOf("."));
    }
    return "orders/" + UUID.randomUUID() + extension;
    }

    private String generateUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                awsProperties.getBucket(),
                awsProperties.getRegion(),
                fileName);
    }
}
