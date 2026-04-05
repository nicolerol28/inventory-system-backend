package com.miapp.inventory_system.shared.infrastructure.storage;

import com.miapp.inventory_system.shared.gateway.StorageGateway;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

@Service
public class R2StorageClient implements StorageGateway {

    @Value("${R2_ACCESS_KEY_ID}")
    private String accessKeyId;

    @Value("${R2_SECRET_ACCESS_KEY}")
    private String secretAccessKey;

    @Value("${R2_ENDPOINT}")
    private String endpoint;

    @Value("${R2_BUCKET_NAME}")
    private String bucketName;

    @Value("${R2_PUBLIC_URL}")
    private String publicUrl;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .region(Region.of("auto"))
                .build();
    }

    @Override
    public String uploadFile(String fileName, byte[] fileContent, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(fileContent));

            return publicUrl + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file '" + fileName + "' to R2: " + e.getMessage(), e);
        }
    }
}
