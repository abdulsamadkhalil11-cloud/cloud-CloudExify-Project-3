package com.ems.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates unique, role-prefixed employee IDs such as MGR-0001,
 * DEV-0001, HR-0001. A static counter per prefix means IDs are stable
 * and readable instead of random UUIDs.
 */
public final class IdGenerator {

    private static final Map<String, Integer> COUNTERS = new HashMap<>();

    private IdGenerator() {
    }

    public static synchronized String nextId(String prefix) {
        int next = COUNTERS.getOrDefault(prefix, 0) + 1;
        COUNTERS.put(prefix, next);
        return String.format("%s-%04d", prefix, next);
    }

    /**
     * Makes sure future-generated IDs never collide with IDs loaded
     * from a saved file (call once after loading existing data).
     */
    public static synchronized void reserve(String prefix, int usedNumber) {
        int current = COUNTERS.getOrDefault(prefix, 0);
        if (usedNumber > current) {
            COUNTERS.put(prefix, usedNumber);
        }
    }
}
