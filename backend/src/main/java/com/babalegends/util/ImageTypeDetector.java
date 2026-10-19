package com.babalegends.util;

import com.babalegends.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Utility class for inspecting file header magic bytes to detect image formats.
 */
public final class ImageTypeDetector {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ImageTypeDetector() {
    }

    /**
     * Detects the image file extension based on the magic bytes of the provided file.
     *
     * @param file the multipart file whose image type is to be detected
     * @return the detected file extension without a leading dot (e.g., "jpg", "png", "webp")
     * @throws BadRequestException  if the file is null, empty, or has an unsupported image format
     * @throws UncheckedIOException if an I/O error occurs while reading the file stream
     */
    public static String detectImageExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required.");
        }
        byte[] header;

        try (InputStream in = file.getInputStream()) {
            int HEADER_LENGTH = 12;
            header = in.readNBytes(HEADER_LENGTH);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file", e);
        }

        return detect(header)
                .orElseThrow(() -> new BadRequestException("Unsupported image type."));
    }

    /**
     * Inspects the file header byte array against known magic byte signatures.
     *
     * @param header the byte array containing the header of the file
     * @return an {@link Optional} containing the detected image extension, or empty if unknown
     */
    private static Optional<String> detect(byte[] header) {
        if (matchesAt(header, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of("jpg");
        }
        if (matchesAt(header, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of("png");
        }
        if (matchesAt(header, 0, 0x52, 0x49, 0x46, 0x46)
                && matchesAt(header, 8, 0x57, 0x45, 0x42, 0x50)) {
            return Optional.of("webp");
        }
        return Optional.empty();
    }

    /**
     * Verifies if the byte array matches the expected sequence of bytes starting at a given offset.
     *
     * @param data     the byte array to inspect
     * @param offset   the starting index in {@code data} from which to compare
     * @param expected the sequence of expected byte values (treated as unsigned 8-bit integers)
     * @return {@code true} if the sequence matches; {@code false} otherwise
     */
    private static boolean matchesAt(byte[] data, int offset, int... expected) {
        if (data.length < offset + expected.length) {
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if ((data[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }

        return true;
    }
}
