package edu.eci.arsw.blacklist;

import java.util.List;
import java.util.Objects;

/**
 * Laboratory implementation: students must complete this class using Java 21 virtual threads.
 */
public final class VirtualThreadBlackListSearch implements BlackListSearch {
    private final List<BlackListProvider> providers;

    public VirtualThreadBlackListSearch(List<BlackListProvider> providers) {
        this.providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public SearchResult search(String ipAddress, int alarmThreshold) {
        throw new UnsupportedOperationException(
                "TODO: implement with Java 21 virtual threads");
    }
}
