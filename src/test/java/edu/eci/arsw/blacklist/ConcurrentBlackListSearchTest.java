package edu.eci.arsw.blacklist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcurrentBlackListSearchTest {

    private List<BlackListProvider> providers;
    private SearchResult baselineResult;
    private static final String TEST_IP = "202.24.34.55";
    private static final int THRESHOLD = 5;

    @BeforeEach
    void setUp() {

        providers = ProviderFactory.create(100, false);
        BlackListSearch sequentialSearch = new SequentialBlackListSearch(providers);
        baselineResult = sequentialSearch.search(TEST_IP, THRESHOLD);
    }

    @Test
    void sequentialImplementationIsDeterministic() {
        BlackListSearch search = new SequentialBlackListSearch(providers);
        SearchResult secondResult = search.search(TEST_IP, THRESHOLD);

        assertEquals(100, baselineResult.consultedProviders(), "Must consult exactly 100 providers");
        assertEquals(baselineResult.matchingProviderIds(), secondResult.matchingProviderIds(), "Results must be deterministic");
        assertFalse(baselineResult.matchingProviderIds().isEmpty(), "Should find matches for the test IP");
    }

    @Test
    void fixedPoolOf2ThreadsMatchesSequentialBaseline() {
        BlackListSearch search = new FixedPoolBlackListSearch(providers, 2);
        SearchResult result = search.search(TEST_IP, THRESHOLD);

        assertMatchesBaselineAndValidatesConstraints(result);
    }

    @Test
    void fixedPoolOf4ThreadsMatchesSequentialBaseline() {
        BlackListSearch search = new FixedPoolBlackListSearch(providers, 4);
        SearchResult result = search.search(TEST_IP, THRESHOLD);

        assertMatchesBaselineAndValidatesConstraints(result);
    }

    @Test
    void fixedPoolOf8ThreadsMatchesSequentialBaseline() {
        BlackListSearch search = new FixedPoolBlackListSearch(providers, 8);
        SearchResult result = search.search(TEST_IP, THRESHOLD);

        assertMatchesBaselineAndValidatesConstraints(result);
    }

    @Test
    void virtualThreadStrategyMatchesSequentialBaseline() {
        BlackListSearch search = new VirtualThreadBlackListSearch(providers);
        SearchResult result = search.search(TEST_IP, THRESHOLD);

        assertMatchesBaselineAndValidatesConstraints(result);
    }

    @Test
    void creatingFixedPoolWithNonPositiveSizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FixedPoolBlackListSearch(providers, 0);
        }, "Pool size of 0 should throw exception");

        assertThrows(IllegalArgumentException.class, () -> {
            new FixedPoolBlackListSearch(providers, -1);
        }, "Negative pool size should throw exception");
    }

    /**
     * Helper method to validate common constraints across all concurrent implementations.
     */
    private void assertMatchesBaselineAndValidatesConstraints(SearchResult result) {

        assertEquals(100, result.consultedProviders(), "Must report all 100 providers as consulted");


        assertEquals(baselineResult.matchingProviderIds(), result.matchingProviderIds(), "Must match sequential baseline");

        List<Integer> matches = result.matchingProviderIds();


        assertEquals(matches.size(), new HashSet<>(matches).size(), "Matches contain duplicates");


        List<Integer> sortedMatches = new ArrayList<>(matches);
        Collections.sort(sortedMatches);
        assertEquals(sortedMatches, matches, "Matches are not in ascending order");
    }
}