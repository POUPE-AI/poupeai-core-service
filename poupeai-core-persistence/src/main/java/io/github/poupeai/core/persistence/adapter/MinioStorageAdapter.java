package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.port.output.StoragePort;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MinioStorageAdapter implements StoragePort {

    @Value("${minio.bucket-name:poupeai-receipts}")
    private String bucketName;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    private MinioClient minioClient;

    private MinioClient getClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        return minioClient;
    }

    @Override
    public void upload(String key, InputStream content, String contentType, long size, Map<String, String> tags) {
        try {
            MinioClient client = getClient();
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' criado.", bucketName);
            }

            var builder = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .stream(content, size, -1)
                    .contentType(contentType);

            if (tags != null && !tags.isEmpty()) {
                builder.tags(tags);
            }

            client.putObject(builder.build());

            log.info("Arquivo enviado com sucesso: bucket='{}', key='{}', tags={}", bucketName, key, tags);

        } catch (Exception e) {
            log.error("Erro ao enviar arquivo para o MinIO", e);
            throw new RuntimeException("Falha ao enviar arquivo para o storage", e);
        }
    }
}
