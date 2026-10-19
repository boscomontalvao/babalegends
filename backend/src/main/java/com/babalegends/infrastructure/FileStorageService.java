package com.babalegends.infrastructure;


import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Saves the file and returns the relative key (e.g., "profile/3f2a....jpg").
     * The file extension is already validated by the caller; the original filename is never used.
     */
    String store(String folder, MultipartFile file, String extension);

    /**
     * Removes the file. Does not fail if the key is null or the file does not exist.
     */
    void delete(String key);

    /**
     * Constructs the public URL from the key. Returns null if the key is null.
     */
    String urlFor(String key);
}
