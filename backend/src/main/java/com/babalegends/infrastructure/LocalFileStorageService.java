package com.babalegends.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path root;
    private final String publicBaseUrl;

    public LocalFileStorageService(@Value("${app.upload-dir}") String uploadDir,
                                   @Value("${app.public-base-url}") String publicBaseUrl) {

        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory " + this.root, e);
        }
        log.info("Upload directory: {}", this.root);

    }

    @Override
    public String store(String folder, MultipartFile file, String extension) {
        if (!extension.matches("[a-z0-9]{1,10}")) {
            throw new IllegalArgumentException("Invalid file extension: " + extension);
        }

        String key = folder + "/" + UUID.randomUUID() + '.' + extension;
        Path target = resolveInsideRoot(key);

        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
        } catch (IOException e) {
            deleteQuietly(target);
            throw new UncheckedIOException("Could not store file", e);
        }

        return key;
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolveInsideRoot(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete file", e);
        }
    }

    @Override
    public String urlFor(String key) {
        return key == null ? null : publicBaseUrl + "/uploads/" + key;
    }

    private Path resolveInsideRoot(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        return path;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // melhor esforço: o erro original é o que importa
        }

    }
}
