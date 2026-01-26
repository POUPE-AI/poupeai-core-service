package io.github.poupeai.core.persistence.adapter;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinioStorageAdapter Tests")
class MinioStorageAdapterTest {

    @Mock
    private MinioClient minioClient;

    private MinioStorageAdapter minioStorageAdapter;

    @BeforeEach
    void setUp() {
        minioStorageAdapter = new MinioStorageAdapter();
        minioStorageAdapter.setMinioClient(minioClient);
        minioStorageAdapter.setBucketName("test-bucket");
    }

    @Nested
    @DisplayName("Upload Tests")
    class UploadTests {

        @Test
        @DisplayName("Should upload file successfully when bucket exists")
        void shouldUploadFileSuccessfullyWhenBucketExists() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));

            InputStream content = new ByteArrayInputStream("test content".getBytes());
            Map<String, String> tags = Map.of("key1", "value1");

            assertDoesNotThrow(() -> minioStorageAdapter.upload("test-key", content, "image/jpeg", 12L, tags));

            verify(minioClient).bucketExists(any(BucketExistsArgs.class));
            verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
            verify(minioClient).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("Should create bucket when it does not exist")
        void shouldCreateBucketWhenNotExists() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));

            InputStream content = new ByteArrayInputStream("test content".getBytes());

            minioStorageAdapter.upload("test-key", content, "image/png", 12L, null);

            verify(minioClient).makeBucket(any(MakeBucketArgs.class));
            verify(minioClient).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("Should upload without tags when tags are null")
        void shouldUploadWithoutTagsWhenNull() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));

            InputStream content = new ByteArrayInputStream("test".getBytes());

            assertDoesNotThrow(() -> minioStorageAdapter.upload("key", content, "application/pdf", 4L, null));

            verify(minioClient).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("Should upload without tags when tags are empty")
        void shouldUploadWithoutTagsWhenEmpty() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));

            InputStream content = new ByteArrayInputStream("test".getBytes());

            assertDoesNotThrow(() -> minioStorageAdapter.upload("key", content, "application/pdf", 4L, Map.of()));

            verify(minioClient).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when upload fails")
        void shouldThrowRuntimeExceptionWhenUploadFails() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
            when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("Connection failed"));

            InputStream content = new ByteArrayInputStream("test".getBytes());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageAdapter.upload("key", content, "image/jpeg", 4L, null));

            assertEquals("Falha ao enviar arquivo para o storage", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw RuntimeException when bucket check fails")
        void shouldThrowRuntimeExceptionWhenBucketCheckFails() throws Exception {
            when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                    .thenThrow(new RuntimeException("Network error"));

            InputStream content = new ByteArrayInputStream("test".getBytes());

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageAdapter.upload("key", content, "image/jpeg", 4L, null));

            assertEquals("Falha ao enviar arquivo para o storage", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete file successfully")
        void shouldDeleteFileSuccessfully() throws Exception {
            doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

            assertDoesNotThrow(() -> minioStorageAdapter.delete("test-key"));

            ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
            verify(minioClient).removeObject(captor.capture());

            RemoveObjectArgs args = captor.getValue();
            assertEquals("test-bucket", args.bucket());
            assertEquals("test-key", args.object());
        }

        @Test
        @DisplayName("Should throw RuntimeException when delete fails")
        void shouldThrowRuntimeExceptionWhenDeleteFails() throws Exception {
            doThrow(new RuntimeException("Delete failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> minioStorageAdapter.delete("test-key"));

            assertEquals("Falha ao remover arquivo do storage", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Client Initialization Tests")
    class ClientInitializationTests {

        @Test
        @DisplayName("Should return injected client")
        void shouldReturnInjectedClient() {
            MinioClient client = minioStorageAdapter.getClient();
            assertSame(minioClient, client);
        }

        @Test
        @DisplayName("Should return bucket name")
        void shouldReturnBucketName() {
            assertEquals("test-bucket", minioStorageAdapter.getBucketName());
        }
    }
}
