package online.banking.system.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Gives ONE lock object per account number.
 *
 * Two withdrawals on the SAME account must wait for each other, but work on
 * DIFFERENT accounts can run at the same time. A single global lock would make
 * every customer wait for every other customer, so each account gets its own.
 *
 * ConcurrentHashMap is a thread-safe Map: many threads can call lockFor() at
 * once, and computeIfAbsent() guarantees only one lock object per key.
 */
public final class AccountLockManager {

    private static final ConcurrentMap<String, Object> LOCKS = new ConcurrentHashMap<>();

    private AccountLockManager() {
    }

    public static Object lockFor(String accountNo) {
        return LOCKS.computeIfAbsent(accountNo, key -> new Object());
    }
}
