package edu.eci.arsw.blacklist;

public interface BlackListSearch {
    SearchResult search(String ipAddress, int alarmThreshold);
}
