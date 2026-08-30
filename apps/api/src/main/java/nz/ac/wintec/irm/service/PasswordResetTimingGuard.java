package nz.ac.wintec.irm.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * Keeps password-reset responses behind a common deadline so small persistence
 * and delivery-cost differences do not expose whether an account exists.
 */
@Component
public class PasswordResetTimingGuard {

    private static final Duration MINIMUM_RESPONSE_TIME = Duration.ofMillis(500);

    public <T> T protect(Supplier<T> operation) {
        long deadline = System.nanoTime() + MINIMUM_RESPONSE_TIME.toNanos();
        try {
            return operation.get();
        } finally {
            await(deadline);
        }
    }

    private static void await(long deadline) {
        long remaining;
        while ((remaining = deadline - System.nanoTime()) > 0) {
            LockSupport.parkNanos(remaining);
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
