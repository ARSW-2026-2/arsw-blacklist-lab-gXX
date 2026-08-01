package edu.eci.arsw.blacklist;

import java.util.List;

public final class BenchmarkRunner {
    private BenchmarkRunner() {
    }

    public static void main(String[] args) {
        String ipAddress = args.length > 0 ? args[0] : "202.24.34.55";
        boolean simulateIo = args.length <= 1 || Boolean.parseBoolean(args[1]);
        int providerCount = 100;
        int alarmThreshold = 5;

        List<BlackListProvider> providers = ProviderFactory.create(providerCount, simulateIo);
        BlackListSearch search = new SequentialBlackListSearch(providers);
        SearchResult result = search.search(ipAddress, alarmThreshold);

        System.out.printf("Mode: SEQUENTIAL%n");
        System.out.printf("IP: %s%n", result.ipAddress());
        System.out.printf("Providers consulted: %d%n", result.consultedProviders());
        System.out.printf("Matches: %s%n", result.matchingProviderIds());
        System.out.printf("Trustworthy: %s%n", result.isTrustworthy(alarmThreshold));
        System.out.printf("Elapsed: %.3f ms%n", result.elapsed().toNanos() / 1_000_000.0);
    }
}
