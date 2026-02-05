package io.github.poupeai.core.domain.port.output;

import java.io.InputStream;
import java.util.Map;

public interface StoragePort {
    void upload(String key, InputStream content, String contentType, long size, Map<String, String> tags);

    void delete(String key);

    String generatePresignedUrl(String key);
}
