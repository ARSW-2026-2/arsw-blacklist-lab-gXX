package edu.eci.arsw.blacklist;

import java.util.Objects;

/**
 * Deterministic provider used only for learning and benchmarking.
 * It can simulate blocking I/O by sleeping for a controlled amount of time.
 */
public final class MockBlackListProvider implements BlackListProvider {
    private final int id;
    private final boolean simulateIo;
    private final int latencyMillis;

    public MockBlackListProvider(int id, boolean simulateIo, int latencyMillis) {
        if (id < 0) {
            throw new IllegalArgumentException("id must be non-negative");
        }
        if (latencyMillis < 0) {
            throw new IllegalArgumentException("latencyMillis must be non-negative");
        }
        this.id = id;
        this.simulateIo = simulateIo;
        this.latencyMillis = latencyMillis;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public boolean isBlacklisted(String ipAddress) {
        Objects.requireNonNull(ipAddress, "ipAddress");
        if (simulateIo && latencyMillis > 0) {
            try {
                Thread.sleep(latencyMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Provider consultation was interrupted", ex);
            }
        }

        // Deterministic rule: the same IP always produces the same matches.
        // Divisor 13 yields enough matches among 100 providers to exercise a threshold of five.
        int mixedHash = 31 * ipAddress.hashCode() + (id * 17);
        return Math.floorMod(mixedHash, 13) == 0;
    }
}
