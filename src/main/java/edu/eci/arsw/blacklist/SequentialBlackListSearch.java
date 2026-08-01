package edu.eci.arsw.blacklist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Complete baseline supplied to students.
 */
public final class SequentialBlackListSearch implements BlackListSearch {
    private final List<BlackListProvider> providers;

    public SequentialBlackListSearch(List<BlackListProvider> providers) {
        this.providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public SearchResult search(String ipAddress, int alarmThreshold) {
        Objects.requireNonNull(ipAddress, "ipAddress");
        if (alarmThreshold <= 0) {
            throw new IllegalArgumentException("alarmThreshold must be greater than zero");
        }

        long startedAt = System.nanoTime();
        List<Integer> matches = new ArrayList<>();
        int consulted = 0;

        for (BlackListProvider provider : providers) {
            consulted++;
            if (provider.isBlacklisted(ipAddress)) {
                matches.add(provider.id());
            }
        }

        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
        return new SearchResult(ipAddress, matches, consulted, elapsed);
    }
}
