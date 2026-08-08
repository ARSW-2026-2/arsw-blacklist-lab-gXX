package edu.eci.arsw.blacklist;

import java.util.List;
import java.util.Objects;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Laboratory implementation: students must complete this class.
 */
public final class FixedPoolBlackListSearch implements BlackListSearch {
    private final List<BlackListProvider> providers;
    private final int poolSize;

    public FixedPoolBlackListSearch(List<BlackListProvider> providers, int poolSize) {
        this.providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be greater than zero");
        }
        this.poolSize = poolSize;
    }

    @Override
    public SearchResult search(String ipAddress, int alarmThreshold) {
        Objects.requireNonNull(ipAddress, "ipAddress");
        if (alarmThreshold <= 0) {
            throw new IllegalArgumentException("alarmThreshold must be greater than zero");
        }

        long startedAt = System.nanoTime();


        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        record PartialResult(List<Integer> matches, int consulted) {}
        List<Callable<PartialResult>> tasks = new ArrayList<>();

        int baseSize = providers.size() / poolSize;
        int remainder = providers.size() % poolSize;
        int start = 0;

        for (int i = 0; i < poolSize; i++) {
            int chunkSize = baseSize + (i < remainder ? 1 : 0);
            if (chunkSize > 0) {
                List<BlackListProvider> chunk = providers.subList(start, start + chunkSize);

                tasks.add(() -> {
                    List<Integer> matches = new ArrayList<>();
                    int consulted = 0;
                    for (BlackListProvider provider : chunk) {
                        consulted++;
                        if (provider.isBlacklisted(ipAddress)) {
                            matches.add(provider.id());
                        }
                    }
                    return new PartialResult(matches, consulted);
                });
                start += chunkSize;
            }
        }

        List<Integer> totalMatches = new ArrayList<>();
        int totalConsulted = 0;

        try {
            List<Future<PartialResult>> futures = executor.invokeAll(tasks);

            for (Future<PartialResult> future : futures) {
                PartialResult result = future.get();
                totalMatches.addAll(result.matches());
                totalConsulted += result.consulted();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("The thread was interrupted during the execution", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Fail in the execution of the concurrent task", e.getCause());
        } finally {
            executor.shutdown();
        }

        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

        return new SearchResult(ipAddress, totalMatches, totalConsulted, elapsed);
    }
}
