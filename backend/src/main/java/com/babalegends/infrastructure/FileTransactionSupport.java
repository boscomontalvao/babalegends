package com.babalegends.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper component providing transactional lifecycle hooks for physical file operations.
 * <p>
 * Coordinates file deletion with database transactions by registering {@link TransactionSynchronization}
 * callbacks. This ensures files are only permanently removed once a transaction commits, or orphaned
 * uploads are cleaned up if a transaction rolls back.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FileTransactionSupport {

    private final FileStorageService fileStorageService;

    /**
     * Schedules a file for deletion only after the current transaction successfully commits.
     * <p>
     * Useful when replacing existing files (such as avatar or card pictures), preventing
     * data loss if the updating transaction fails and rolls back.
     *
     * @param key the storage key of the file to delete, or null (no-op)
     */
    public void deleteAfterCommit(String key) {
        if (key == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    fileStorageService.delete(key);
                } catch (RuntimeException e) {
                    log.warn("Could not delete old file {}", key, e);
                }
            }
        });
    }

    /**
     * Schedules a newly uploaded file for cleanup if the current transaction rolls back.
     * <p>
     * Prevents orphaned files from lingering in storage when an operation (e.g. database save)
     * fails after the physical file was already stored.
     *
     * @param key the storage key of the newly uploaded file to clean up on failure
     */
    public void deleteOnRollback(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        fileStorageService.delete(key);
                    } catch (RuntimeException e) {
                        log.warn("Could not clean up file {}", key, e);
                    }
                }
            }
        });
    }
}
