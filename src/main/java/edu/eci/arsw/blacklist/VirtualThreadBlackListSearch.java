package edu.eci.arsw.blacklist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class VirtualThreadBlackListSearch implements BlackListSearch {
    private final List<BlackListProvider> providers;

    public VirtualThreadBlackListSearch(List<BlackListProvider> providers) {
        this.providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public SearchResult search(String ipAddress, int alarmThreshold) {
        long start = System.nanoTime();
        
        List<Integer> matchingProviders = new ArrayList<>();
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Integer>> futures = new ArrayList<>();
            
            for (BlackListProvider provider : providers) {
                Future<Integer> future = executor.submit(() -> {
                    if (provider.isBlacklisted(ipAddress)) {
                        return provider.id();
                    }
                    return null;
                });
                futures.add(future);
            }

            for (Future<Integer> future : futures) {
                Integer providerId = future.get();

                if (providerId != null) {
                    matchingProviders.add(providerId);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        throw new RuntimeException(
            "Search interrupted while consulting providers", e);
        } catch (ExecutionException e) {
            throw new RuntimeException(
                "Error while executing provider search", e);
        }

        long end = System.nanoTime();

        return new SearchResult(ipAddress, matchingProviders, providers.size(), Duration.ofNanos(end - start));
    }
}
