package edu.eci.arsw.blacklist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class SequentialBlackListSearchTest {
    @Test
    void shouldConsultAllProvidersAndReturnDeterministicMatches() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch search = new SequentialBlackListSearch(providers);

        SearchResult first = search.search("202.24.34.55", 5);
        SearchResult second = search.search("202.24.34.55", 5);

        assertEquals(100, first.consultedProviders());
        assertEquals(first.matchingProviderIds(), second.matchingProviderIds());
        assertFalse(first.matchingProviderIds().isEmpty());
    }
}
