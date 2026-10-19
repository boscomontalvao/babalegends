package com.babalegends.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {

    @TempDir
    Path tmp;

    @Test
    void storesAndDeletes() {
        var storage = new LocalFileStorageService(tmp.toString(), "http://localhost:8080");
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3});

        String key = storage.store("profile", file, "png");

        assertTrue(Files.exists(tmp.resolve(key)));
        assertEquals("http://localhost:8080/uploads/" + key, storage.urlFor(key));

        storage.delete(key);
        assertFalse(Files.exists(tmp.resolve(key)));
    }

    @Test
    void rejectsPathOutsideRoot() {
        var storage = new LocalFileStorageService(tmp.toString(), "http://localhost:8080");
        var file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});

        assertThrows(IllegalArgumentException.class, () -> storage.store("../fora", file, "png"));
    }
}
