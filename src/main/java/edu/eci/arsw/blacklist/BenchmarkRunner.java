package edu.eci.arsw.blacklist;

import java.util.List;

public final class BenchmarkRunner {

    private BenchmarkRunner() {
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Error: Arguments are missing.");
            System.err.println("Correct use: <strategy> <ipAddress> <simulateIo> <warmups> <measuredRuns> [poolSize]");
            System.exit(1);
        }

        String strategy = args[0].toUpperCase();
        String ipAddress = args[1];
        boolean simulateIo = Boolean.parseBoolean(args[2]);
        int warmups = Integer.parseInt(args[3]);
        int measuredRuns = Integer.parseInt(args[4]);
        int poolSize = 0;

        if ("FIXED".equals(strategy)) {
            if (args.length < 6) {
                System.err.println("Error: The strategy FIXED requires [poolSize].");
                System.exit(1);
            }
            poolSize = Integer.parseInt(args[5]);
        }

        int providerCount = 100;
        int alarmThreshold = 5;

        List<BlackListProvider> providers = ProviderFactory.create(providerCount, simulateIo);
        BlackListSearch search = null;

        switch (strategy) {
            case "SEQUENTIAL":
                search = new SequentialBlackListSearch(providers);
                break;
            case "FIXED":
                search = new FixedPoolBlackListSearch(providers, poolSize);
                break;
            case "VIRTUAL":
                search = new VirtualThreadBlackListSearch(providers);
                break;
            default:
                System.err.println("Error: Strategy unrecognizable. Use SEQUENTIAL, FIXED o VIRTUAL.");
                System.exit(1);
        }

        System.out.println("=== Benchmark configuration ===");
        System.out.println("Strategy   : " + strategy);
        System.out.println("Target IP  : " + ipAddress);
        System.out.println("Simulate I/O  : " + simulateIo);
        System.out.println("Warm-ups     : " + warmups);
        System.out.println("Executions  : " + measuredRuns);
        if ("FIXED".equals(strategy)) {
            System.out.println("Pool Size    : " + poolSize);
        }
        System.out.println("===================================\n");

        if (warmups > 0) System.out.println("Executing " + warmups + " warmups...");
        for (int i = 0; i < warmups; i++) {
            search.search(ipAddress, alarmThreshold);
        }

        double minTimeMs = Double.MAX_VALUE;
        double maxTimeMs = Double.MIN_VALUE;
        double totalTimeMs = 0;

        List<Integer> expectedMatches = null;
        int expectedConsulted = -1;

        String scenario = simulateIo ? "IO" : "NO-IO";
        String poolSizeStr = "FIXED".equals(strategy) ? String.valueOf(poolSize) : "N/A";

        System.out.println("\n=== CSV Results ===");
        System.out.println("scenario,strategy,pool_size,run,elapsed_ms,matches,consulted_providers");

        for (int i = 1; i <= measuredRuns; i++) {
            SearchResult result = search.search(ipAddress, alarmThreshold);

            if (expectedMatches == null) {
                expectedMatches = result.matchingProviderIds();
                expectedConsulted = result.consultedProviders();
            } else {
                if (!expectedMatches.equals(result.matchingProviderIds()) || expectedConsulted != result.consultedProviders()) {
                    throw new IllegalStateException("Functional error: Results vary between executions. Check the concurrency conditions..");
                }
            }

            double elapsedMs = result.elapsed().toNanos() / 1_000_000.0;

            minTimeMs = Math.min(minTimeMs, elapsedMs);
            maxTimeMs = Math.max(maxTimeMs, elapsedMs);
            totalTimeMs += elapsedMs;

            System.out.printf("%s,%s,%s,%d,%.3f,%d,%d%n",
                    scenario, strategy, poolSizeStr, i, elapsedMs,
                    result.matchingProviderIds().size(), result.consultedProviders());
        }

        if (measuredRuns > 0) {
            double avgTimeMs = totalTimeMs / measuredRuns;
            System.out.println("\n=== Time summary ===");
            System.out.printf("Minimum   : %.3f ms%n", minTimeMs);
            System.out.printf("Maximum   : %.3f ms%n", maxTimeMs);
            System.out.printf("Average : %.3f ms%n", avgTimeMs);
        }
    }
}