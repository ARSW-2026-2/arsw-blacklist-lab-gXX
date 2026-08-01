package edu.eci.arsw.blacklist;

import java.time.Duration;
import java.util.List;

public record SearchResult(
        String ipAddress,
        List<Integer> matchingProviderIds,
        int consultedProviders,
        Duration elapsed) {

    public SearchResult {
        matchingProviderIds = List.copyOf(matchingProviderIds);
    }

    public boolean isTrustworthy(int alarmThreshold) {
        return matchingProviderIds.size() < alarmThreshold;
    }
}
