package edu.eci.arsw.blacklist;

import java.util.ArrayList;
import java.util.List;

public final class ProviderFactory {
    private ProviderFactory() {
    }

    public static List<BlackListProvider> create(int count, boolean simulateIo) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than zero");
        }

        List<BlackListProvider> providers = new ArrayList<>(count);
        for (int id = 0; id < count; id++) {
            int latencyMillis = simulateIo ? 20 + Math.floorMod(id * 29, 181) : 0;
            providers.add(new MockBlackListProvider(id, simulateIo, latencyMillis));
        }
        return List.copyOf(providers);
    }
}
