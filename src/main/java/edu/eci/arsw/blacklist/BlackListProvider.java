package edu.eci.arsw.blacklist;

/**
 * Represents an external blacklist provider.
 */
public interface BlackListProvider {
    int id();

    boolean isBlacklisted(String ipAddress);
}
